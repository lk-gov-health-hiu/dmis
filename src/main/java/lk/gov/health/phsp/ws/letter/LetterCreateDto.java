/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

/**
 * Request body for POST /api/letters.
 *
 * <p>Dates are accepted as ISO-8601 strings (yyyy-MM-dd). Identifier fields
 * are Long primary keys of the referenced entity. All fields are optional
 * except {@link #documentName}.</p>
 */
public class LetterCreateDto {

    private String documentName;
    private String documentNumber;
    private String documentCode;
    private String comments;

    private String documentDate;
    private String receivedDate;

    private String documentGenerationType;

    private Long documentLanguageId;
    private Long letterStatusId;
    private Long receivedAsId;

    private Long referenceDocumentId;
    private Long parentDocumentId;

    private Long institutionId;
    private Long institutionUnitId;
    private Long ownerId;

    private Long fromInstitutionId;
    private Long fromWebUserId;
    private String senderName;
    private String registrationNo;

    private Long toInstitutionId;
    private Long toWebUserId;

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getDocumentCode() { return documentCode; }
    public void setDocumentCode(String documentCode) { this.documentCode = documentCode; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getDocumentDate() { return documentDate; }
    public void setDocumentDate(String documentDate) { this.documentDate = documentDate; }

    public String getReceivedDate() { return receivedDate; }
    public void setReceivedDate(String receivedDate) { this.receivedDate = receivedDate; }

    public String getDocumentGenerationType() { return documentGenerationType; }
    public void setDocumentGenerationType(String documentGenerationType) { this.documentGenerationType = documentGenerationType; }

    public Long getDocumentLanguageId() { return documentLanguageId; }
    public void setDocumentLanguageId(Long documentLanguageId) { this.documentLanguageId = documentLanguageId; }

    public Long getLetterStatusId() { return letterStatusId; }
    public void setLetterStatusId(Long letterStatusId) { this.letterStatusId = letterStatusId; }

    public Long getReceivedAsId() { return receivedAsId; }
    public void setReceivedAsId(Long receivedAsId) { this.receivedAsId = receivedAsId; }

    public Long getReferenceDocumentId() { return referenceDocumentId; }
    public void setReferenceDocumentId(Long referenceDocumentId) { this.referenceDocumentId = referenceDocumentId; }

    public Long getParentDocumentId() { return parentDocumentId; }
    public void setParentDocumentId(Long parentDocumentId) { this.parentDocumentId = parentDocumentId; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

    public Long getInstitutionUnitId() { return institutionUnitId; }
    public void setInstitutionUnitId(Long institutionUnitId) { this.institutionUnitId = institutionUnitId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getFromInstitutionId() { return fromInstitutionId; }
    public void setFromInstitutionId(Long fromInstitutionId) { this.fromInstitutionId = fromInstitutionId; }

    public Long getFromWebUserId() { return fromWebUserId; }
    public void setFromWebUserId(Long fromWebUserId) { this.fromWebUserId = fromWebUserId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public Long getToInstitutionId() { return toInstitutionId; }
    public void setToInstitutionId(Long toInstitutionId) { this.toInstitutionId = toInstitutionId; }

    public Long getToWebUserId() { return toWebUserId; }
    public void setToWebUserId(Long toWebUserId) { this.toWebUserId = toWebUserId; }

}
