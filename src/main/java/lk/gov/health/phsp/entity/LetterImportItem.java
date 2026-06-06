/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.enums.LetterImportItemStatus;

/**
 * One detected letter within a {@code LetterImportBatch}: its page range, the
 * metadata Claude proposed (editable by the reviewer), a rendered preview
 * image, and — once accepted — a link to the created {@code Document} letter.
 */
@Entity
@Table(name = "letter_import_item")
public class LetterImportItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private LetterImportBatch batch;

    /** Order of this letter within the batch (0-based). */
    private Integer segmentIndex;
    /** Zero-based inclusive page range within the batch PDF. */
    private Integer startPage;
    private Integer endPage;

    @Enumerated(EnumType.STRING)
    private LetterImportItemStatus status;

    // --- Proposed / edited metadata ---------------------------------------
    private String subject;
    @Temporal(TemporalType.DATE)
    private Date letterDate;
    /** Stamp/received date; defaults to today in the review UI. */
    @Temporal(TemporalType.DATE)
    private Date receivedDate;
    private String senderName;
    private String registrationNo;
    private String referenceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution resolvedInstitution;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resolvedStaff;

    private Double confidence;

    private Long inputTokens;
    private Long outputTokens;

    /** Rendered preview image of the first page of this letter. */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] previewImage;
    private String previewContentType;

    /** Raw JSON Claude returned, for auditing. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawJson;

    /** Set once the reviewer accepts and a letter is created. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Document createdDocument;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date decidedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LetterImportBatch getBatch() { return batch; }
    public void setBatch(LetterImportBatch batch) { this.batch = batch; }

    public Integer getSegmentIndex() { return segmentIndex; }
    public void setSegmentIndex(Integer segmentIndex) { this.segmentIndex = segmentIndex; }

    public Integer getStartPage() { return startPage; }
    public void setStartPage(Integer startPage) { this.startPage = startPage; }

    public Integer getEndPage() { return endPage; }
    public void setEndPage(Integer endPage) { this.endPage = endPage; }

    public LetterImportItemStatus getStatus() { return status; }
    public void setStatus(LetterImportItemStatus status) { this.status = status; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Date getLetterDate() { return letterDate; }
    public void setLetterDate(Date letterDate) { this.letterDate = letterDate; }

    public Date getReceivedDate() { return receivedDate; }
    public void setReceivedDate(Date receivedDate) { this.receivedDate = receivedDate; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRegistrationNo() { return registrationNo; }
    public void setRegistrationNo(String registrationNo) { this.registrationNo = registrationNo; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    public Institution getResolvedInstitution() { return resolvedInstitution; }
    public void setResolvedInstitution(Institution resolvedInstitution) { this.resolvedInstitution = resolvedInstitution; }

    public WebUser getResolvedStaff() { return resolvedStaff; }
    public void setResolvedStaff(WebUser resolvedStaff) { this.resolvedStaff = resolvedStaff; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Long getInputTokens() { return inputTokens; }
    public void setInputTokens(Long inputTokens) { this.inputTokens = inputTokens; }

    public Long getOutputTokens() { return outputTokens; }
    public void setOutputTokens(Long outputTokens) { this.outputTokens = outputTokens; }

    public byte[] getPreviewImage() { return previewImage; }
    public void setPreviewImage(byte[] previewImage) { this.previewImage = previewImage; }

    public String getPreviewContentType() { return previewContentType; }
    public void setPreviewContentType(String previewContentType) { this.previewContentType = previewContentType; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    public Document getCreatedDocument() { return createdDocument; }
    public void setCreatedDocument(Document createdDocument) { this.createdDocument = createdDocument; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Date decidedAt) { this.decidedAt = decidedAt; }
}
