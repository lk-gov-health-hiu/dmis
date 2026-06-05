/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.enums;

/**
 * Review state of a single detected letter within a batch.
 */
public enum LetterImportItemStatus {

    /** Awaiting the reviewer's decision. */
    PENDING("Pending"),
    /** Accepted; a Document (letter) was created from it. */
    ACCEPTED("Accepted"),
    /** Discarded by the reviewer; no letter created. */
    DISCARDED("Discarded");

    private final String label;

    LetterImportItemStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
