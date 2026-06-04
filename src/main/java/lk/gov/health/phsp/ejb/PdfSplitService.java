/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.imageio.ImageIO;
import lk.gov.health.phsp.pojcs.PageRange;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Local (no external service) PDF processing for the letter-import feature.
 *
 * <p>Users upload a single PDF in which individual letters are separated by
 * blank pages. This service detects those blank separator pages, groups the
 * remaining pages into per-letter {@link PageRange} segments, extracts a
 * per-letter sub-PDF (to hand to Claude as a {@code document} block), and
 * rasterizes pages to PNG (to store as letter attachments).</p>
 *
 * <p>Blank detection is done by rasterizing each page at a low DPI and
 * measuring the fraction of "ink" (dark) pixels. This works for scanned,
 * image-only PDFs that have no text layer, which a text-based check would
 * miss.</p>
 */
@Stateless
public class PdfSplitService implements Serializable {

    private static final long serialVersionUID = 1L;

    /** DPI used for the cheap blank-detection render. */
    private static final int DETECTION_DPI = 50;

    /** Luminance (0-255) below which a pixel is counted as ink. */
    private static final int INK_LUMINANCE_THRESHOLD = 128;

    /**
     * Default maximum fraction of ink pixels for a page to count as blank.
     * A speckle-free blank page is ~0; scanned blanks carry a little noise,
     * so a small non-zero default is used.
     */
    public static final double DEFAULT_BLANK_THRESHOLD = 0.005;

    /** Default DPI for the stored letter image render. */
    public static final int DEFAULT_RENDER_DPI = 150;

    /**
     * Detects per-letter page ranges using the default blank threshold.
     */
    public List<PageRange> detectSegments(byte[] pdfBytes) throws IOException {
        return detectSegments(pdfBytes, DEFAULT_BLANK_THRESHOLD);
    }

    /**
     * Detects per-letter page ranges by treating blank pages as separators.
     *
     * <p>Leading, trailing, and consecutive blank pages are ignored; each run
     * of consecutive non-blank pages becomes one {@link PageRange}. If no blank
     * pages are found, the whole document is returned as a single range.</p>
     *
     * @param pdfBytes       the uploaded PDF
     * @param blankThreshold maximum ink fraction (0..1) for a page to be blank
     * @return ordered, non-overlapping letter ranges (possibly empty)
     */
    public List<PageRange> detectSegments(byte[] pdfBytes, double blankThreshold) throws IOException {
        List<PageRange> ranges = new ArrayList<>();
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ranges;
        }
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            int pageCount = document.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(document);

            int segmentStart = -1;
            for (int i = 0; i < pageCount; i++) {
                boolean blank = isPageBlank(renderer, i, blankThreshold);
                if (blank) {
                    if (segmentStart >= 0) {
                        ranges.add(new PageRange(segmentStart, i - 1));
                        segmentStart = -1;
                    }
                } else if (segmentStart < 0) {
                    segmentStart = i;
                }
            }
            if (segmentStart >= 0) {
                ranges.add(new PageRange(segmentStart, pageCount - 1));
            }
        }
        return ranges;
    }

    /**
     * Renders a single page to grayscale at low DPI and returns the fraction of
     * dark (ink) pixels, comparing it against {@code blankThreshold}.
     */
    private boolean isPageBlank(PDFRenderer renderer, int pageIndex, double blankThreshold) throws IOException {
        BufferedImage image = renderer.renderImageWithDPI(pageIndex, DETECTION_DPI, ImageType.GRAY);
        int width = image.getWidth();
        int height = image.getHeight();
        long totalPixels = (long) width * height;
        if (totalPixels == 0) {
            return true;
        }
        long inkPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // GRAY image: the low byte of the RGB int is the luminance.
                int luminance = image.getRGB(x, y) & 0xFF;
                if (luminance < INK_LUMINANCE_THRESHOLD) {
                    inkPixels++;
                }
            }
        }
        double inkFraction = (double) inkPixels / (double) totalPixels;
        return inkFraction <= blankThreshold;
    }

    /**
     * Extracts the given inclusive, zero-based page range into a standalone PDF.
     *
     * @return the bytes of a new PDF containing only those pages
     */
    public byte[] extractSubPdf(byte[] pdfBytes, int startPage, int endPage) throws IOException {
        try (PDDocument source = PDDocument.load(new ByteArrayInputStream(pdfBytes));
             PDDocument target = new PDDocument()) {
            int pageCount = source.getNumberOfPages();
            int from = Math.max(0, startPage);
            int to = Math.min(pageCount - 1, endPage);
            for (int i = from; i <= to; i++) {
                PDPage imported = target.importPage(source.getPage(i));
                // importPage clones structure; nothing else required.
                if (imported == null) {
                    throw new IOException("Failed to import page " + i);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            target.save(out);
            return out.toByteArray();
        }
    }

    /**
     * Rasterizes a single page to PNG at the default render DPI.
     */
    public byte[] renderPageToPng(byte[] pdfBytes, int pageIndex) throws IOException {
        return renderPageToPng(pdfBytes, pageIndex, DEFAULT_RENDER_DPI);
    }

    /**
     * Rasterizes a single zero-based page to PNG at the given DPI.
     */
    public byte[] renderPageToPng(byte[] pdfBytes, int pageIndex, int dpi) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        }
    }
}
