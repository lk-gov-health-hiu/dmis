/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.enums;

/**
 * Lifecycle of a {@code LetterImportBatch} (an uploaded multi-letter PDF).
 */
public enum LetterImportStatus {

    /** Uploaded, not yet processed. */
    NEW("New"),
    /** Being split and OCR'd by Claude. */
    PROCESSING("Processing"),
    /** Processing complete; items are ready for review. */
    READY("Ready for review"),
    /** Processing failed; see the batch error message. */
    FAILED("Failed"),
    /** All items have been accepted or discarded. */
    DONE("Done");

    private final String label;

    LetterImportStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
