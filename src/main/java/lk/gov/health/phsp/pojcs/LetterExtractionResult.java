/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;

/**
 * Structured metadata extracted by Claude from a single letter (one PDF
 * segment) for the letter-import review step. Identifier fields are populated
 * only when Claude could resolve the printed name to a real DMIS entity via the
 * grounding tools; otherwise the free-text name is kept for the reviewer.
 */
public class LetterExtractionResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subject;
    /** Letter date as printed, ISO yyyy-MM-dd when parseable, else null. */
    private String letterDate;
    private String senderInstitutionName;
    private Long resolvedInstitutionId;
    private String senderName;
    private Long resolvedStaffId;
    private String registrationNo;
    private String referenceNo;
    /** Claude's self-reported confidence, 0..1. */
    private double confidence;

    /** The raw JSON returned by Claude, kept for auditing/debugging. */
    private String rawJson;
    private long inputTokens;
    private long outputTokens;
    /** Non-null if extraction failed; the reviewer then fills fields manually. */
    private String errorMessage;

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getLetterDate() { return letterDate; }
    public void setLetterDate(String letterDate) { this.letterDate = letterDate; }

    public String getSenderInstitutionName() { return senderInstitutionName; }
    public void setSenderInstitutionName(String senderInstitutionName) { this.senderInstitutionName = senderInstitutionName; }

    public Long getResolvedInstitutionId() { return resolvedInstitutionId; }
    public void setResolvedInstitutionId(Long resolvedInstitutionId) { this.resolvedInstitutionId = resolvedInstitutionId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Long getResolvedStaffId() { return resolvedStaffId; }
    public void setResolvedStaffId(Long resolvedStaffId) { this.resolvedStaffId = resolvedStaffId; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    public long getInputTokens() { return inputTokens; }
    public void setInputTokens(long inputTokens) { this.inputTokens = inputTokens; }

    public long getOutputTokens() { return outputTokens; }
    public void setOutputTokens(long outputTokens) { this.outputTokens = outputTokens; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public boolean isFailed() { return errorMessage != null; }
}
