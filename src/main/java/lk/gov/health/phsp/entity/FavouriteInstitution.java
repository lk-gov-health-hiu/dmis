/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An institution that {@link #institution} has starred as a favourite, so its
 * users can narrow the Our Letter To/Copy autocompletes to the institutions
 * they write to most. Each institution keeps its own list.
 */
@Entity
public class FavouriteInstitution implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The institution that owns this favourites list. */
    @ManyToOne
    private Institution institution;

    /** The institution starred as a favourite. */
    @ManyToOne
    private Institution favourite;

    @ManyToOne
    private WebUser createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    private boolean retired;

    @ManyToOne
    private WebUser retiredBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date retiredAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Institution getInstitution() { return institution; }
    public void setInstitution(Institution institution) { this.institution = institution; }

    public Institution getFavourite() { return favourite; }
    public void setFavourite(Institution favourite) { this.favourite = favourite; }

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

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FavouriteInstitution)) {
            return false;
        }
        FavouriteInstitution other = (FavouriteInstitution) object;
        return id != null && id.equals(other.id);
    }

}
