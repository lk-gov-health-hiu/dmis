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
import lk.gov.health.phsp.enums.LetterImportStatus;

/**
 * A temporarily-held upload of a multi-letter PDF (individual letters separated
 * by blank pages) being processed by the letter-import feature. Holds the raw
 * PDF and aggregate processing state; per-letter proposals live in
 * {@code LetterImportItem}. Old batches are purged by a scheduled cleanup.
 */
@Entity
@Table(name = "letter_import_batch")
public class LetterImportBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The user who uploaded the PDF (and whose Claude key is used). */
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser owner;

    /** Logged institution context, used when creating the resulting letters. */
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    private String originalFileName;

    /** The uploaded PDF, held until the batch is reviewed and purged. */
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] pdfBytes;

    @Enumerated(EnumType.STRING)
    private LetterImportStatus status;

    private Integer pageCount;
    /** Number of detected letters (segments). */
    private Integer letterCount;
    /** Number of letters processed so far (for progress display). */
    private Integer processedCount;

    private String model;
    private Long totalInputTokens;
    private Long totalOutputTokens;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    private boolean retired;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WebUser getOwner() { return owner; }
    public void setOwner(WebUser owner) { this.owner = owner; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public byte[] getPdfBytes() { return pdfBytes; }
    public void setPdfBytes(byte[] pdfBytes) { this.pdfBytes = pdfBytes; }

    public LetterImportStatus getStatus() { return status; }
    public void setStatus(LetterImportStatus status) { this.status = status; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public Integer getLetterCount() { return letterCount; }
    public void setLetterCount(Integer letterCount) { this.letterCount = letterCount; }

    public Integer getProcessedCount() { return processedCount; }
    public void setProcessedCount(Integer processedCount) { this.processedCount = processedCount; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Long getTotalInputTokens() { return totalInputTokens; }
    public void setTotalInputTokens(Long totalInputTokens) { this.totalInputTokens = totalInputTokens; }

    public Long getTotalOutputTokens() { return totalOutputTokens; }
    public void setTotalOutputTokens(Long totalOutputTokens) { this.totalOutputTokens = totalOutputTokens; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public WebUser getCreatedBy() { return createdBy; }
    public void setCreatedBy(WebUser createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isRetired() { return retired; }
    public void setRetired(boolean retired) { this.retired = retired; }

    public WebUser getRetiredBy() { return retiredBy; }
    public void setRetiredBy(WebUser retiredBy) { this.retiredBy = retiredBy; }

    public Date getRetiredAt() { return retiredAt; }
    public void setRetiredAt(Date retiredAt) { this.retiredAt = retiredAt; }

    public String getRetireComments() { return retireComments; }
    public void setRetireComments(String retireComments) { this.retireComments = retireComments; }
}
