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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A user's personal Claude (Anthropic) API key, used by the letter-import
 * feature so each user is billed on their own key rather than an
 * application-wide one.
 *
 * <p>The raw key is <strong>never</strong> stored in clear text: only the
 * encrypted form ({@link #encryptedKey}) and a short, non-sensitive hint
 * ({@link #last4}) for display are persisted. At most one non-retired record
 * per {@link #owner} should be {@link #active}.</p>
 */
@Entity
@Table(name = "user_claude_api_key")
public class UserClaudeApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser owner;

    /** Encrypted Claude API key (see CryptoService). Never the raw value. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String encryptedKey;

    /** Last 4 characters of the raw key, kept only for masked display. */
    @Column(length = 8)
    private String last4;

    /** Optional per-user Claude model override (else the configured default). */
    private String modelOverride;

    private boolean active;

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

    public String getEncryptedKey() { return encryptedKey; }
    public void setEncryptedKey(String encryptedKey) { this.encryptedKey = encryptedKey; }

    public String getLast4() { return last4; }
    public void setLast4(String last4) { this.last4 = last4; }

    public String getModelOverride() { return modelOverride; }
    public void setModelOverride(String modelOverride) { this.modelOverride = modelOverride; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

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
