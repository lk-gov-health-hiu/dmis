/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.LetterImportBatch;
import lk.gov.health.phsp.entity.LetterImportItem;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.LetterImportItemStatus;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.LetterImportBatchFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.pojcs.LetterExtractionResult;
import lk.gov.health.phsp.pojcs.PageRange;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Background orchestration of a letter-import batch: split the uploaded PDF into
 * per-letter segments, render a preview, ask Claude to OCR + extract metadata,
 * and persist one {@code LetterImportItem} per letter for review.
 *
 * <p>The orchestration method runs {@link Asynchronous} and
 * {@link TransactionAttributeType#NOT_SUPPORTED} (no long-lived transaction);
 * all writes are delegated to {@link LetterImportTxBean} so each commits on its
 * own and the review UI can poll progress.</p>
 */
@Stateless
public class LetterImportService implements Serializable {

    private static final Logger LOG = Logger.getLogger(LetterImportService.class.getName());
    private static final long serialVersionUID = 1L;

    @EJB
    private LetterImportBatchFacade batchFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private WebUserFacade webUserFacade;
    @EJB
    private PdfSplitService pdfSplitService;
    @EJB
    private LetterExtractionService letterExtractionService;
    @EJB
    private LetterImportTxBean txBean;
    @EJB
    private LetterImportConfig config;

    /**
     * Processes a batch end to end. Never propagates exceptions: failures mark
     * the batch FAILED so the UI can surface them.
     *
     * @param batchId the batch to process
     * @param apiKey  the owner's decrypted Claude key (resolved by the caller)
     * @param model   Claude model id, or null for the service default
     */
    @Asynchronous
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void processBatch(Long batchId, String apiKey, String model) {
        LetterImportBatch batch = batchFacade.find(batchId);
        if (batch == null) {
            LOG.log(Level.WARNING, "Letter import batch {0} not found", batchId);
            return;
        }
        byte[] pdf = batch.getPdfBytes();
        String effectiveModel = (model != null && !model.trim().isEmpty())
                ? model : config.getDefaultModel();
        try {
            int pageCount = countPages(pdf);
            int maxPages = config.getMaxPages();
            if (pageCount > maxPages) {
                txBean.markFailed(batchId, "PDF has " + pageCount + " pages, exceeding the "
                        + maxPages + "-page limit. Split it into smaller files.");
                return;
            }
            List<PageRange> segments = pdfSplitService.detectSegments(pdf, config.getBlankThreshold());
            txBean.markProcessing(batchId, pageCount, segments.size());

            int index = 0;
            for (PageRange range : segments) {
                LetterImportItem item = processSegment(batch, range, index, apiKey, effectiveModel);
                txBean.saveItem(item);
                txBean.recordProgress(batchId,
                        item.getInputTokens() != null ? item.getInputTokens() : 0L,
                        item.getOutputTokens() != null ? item.getOutputTokens() : 0L);
                index++;
            }
            txBean.markReady(batchId);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed processing letter import batch " + batchId, e);
            txBean.markFailed(batchId, e.getMessage());
        }
    }

    private LetterImportItem processSegment(LetterImportBatch batch, PageRange range,
            int index, String apiKey, String model) {
        LetterImportItem item = new LetterImportItem();
        item.setBatch(batch);
        item.setSegmentIndex(index);
        item.setStartPage(range.getStart());
        item.setEndPage(range.getEnd());
        item.setStatus(LetterImportItemStatus.PENDING);
        item.setReceivedDate(new Date()); // stamp date defaults to today

        byte[] pdf = batch.getPdfBytes();
        try {
            item.setPreviewImage(pdfSplitService.renderPageToPng(pdf, range.getStart(), config.getRenderDpi()));
            item.setPreviewContentType("image/png");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Preview render failed for segment " + index, e);
        }

        try {
            byte[] subPdf = pdfSplitService.extractSubPdf(pdf, range.getStart(), range.getEnd());
            LetterExtractionResult extraction =
                    letterExtractionService.extractFromSegment(subPdf, apiKey, model);
            applyExtraction(item, extraction);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Extraction failed for segment " + index, e);
            item.setErrorMessage("Extraction failed: " + e.getMessage());
        }
        return item;
    }

    private void applyExtraction(LetterImportItem item, LetterExtractionResult extraction) {
        if (extraction == null) {
            return;
        }
        item.setRawJson(extraction.getRawJson());
        item.setConfidence(extraction.getConfidence());
        item.setInputTokens(extraction.getInputTokens());
        item.setOutputTokens(extraction.getOutputTokens());
        if (extraction.isFailed()) {
            item.setErrorMessage(extraction.getErrorMessage());
        } else {
            item.setSubject(extraction.getSubject());
            item.setLetterDate(parseDate(extraction.getLetterDate()));
            item.setSenderName(extraction.getSenderName());
            item.setRegistrationNo(extraction.getRegistrationNo());
            item.setReferenceNo(extraction.getReferenceNo());
            if (extraction.getResolvedInstitutionId() != null) {
                Institution ins = institutionFacade.find(extraction.getResolvedInstitutionId());
                item.setResolvedInstitution(ins);
            }
            if (extraction.getResolvedStaffId() != null) {
                WebUser staff = webUserFacade.find(extraction.getResolvedStaffId());
                item.setResolvedStaff(staff);
            }
        }
    }

    private Date parseDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(iso.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private int countPages(byte[] pdf) {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdf))) {
            return document.getNumberOfPages();
        } catch (Exception e) {
            return 0;
        }
    }
}
