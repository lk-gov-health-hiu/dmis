/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "api_key")
public class ApiKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String keyValue;
    private String name;
    private String description;
    private boolean retired;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date retiredAt;

    private String retireComments;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRetired() { return retired; }
    public void setRetired(boolean retired) { this.retired = retired; }

    public WebUser getCreatedBy() { return createdBy; }
    public void setCreatedBy(WebUser createdBy) { this.createdBy = createdBy; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public WebUser getRetiredBy() { return retiredBy; }
    public void setRetiredBy(WebUser retiredBy) { this.retiredBy = retiredBy; }

    public Date getRetiredAt() { return retiredAt; }
    public void setRetiredAt(Date retiredAt) { this.retiredAt = retiredAt; }

    public String getRetireComments() { return retireComments; }
    public void setRetireComments(String retireComments) { this.retireComments = retireComments; }

}
