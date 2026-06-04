/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;

/**
 * A contiguous range of pages within a PDF that represents a single letter,
 * produced by {@code PdfSplitService.detectSegments}.
 *
 * <p>Page indices are <strong>zero-based and inclusive</strong>:
 * {@code start} is the first page of the letter, {@code end} is the last.</p>
 */
public class PageRange implements Serializable {

    private static final long serialVersionUID = 1L;

    private int start;
    private int end;

    public PageRange() {
    }

    public PageRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /** Zero-based index of the first page (inclusive). */
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    /** Zero-based index of the last page (inclusive). */
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /** Number of pages in this letter (always >= 1 for a valid range). */
    public int getPageCount() {
        return (end - start) + 1;
    }

    @Override
    public String toString() {
        return "PageRange{" + start + ".." + end + " (" + getPageCount() + " pages)}";
    }
}
