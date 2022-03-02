/*
 * To change this license header, choose License Headers in Client Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.pojcs.Nameable;

/**
 *
 * @author buddhika
 */
@Entity
@XmlRootElement
public class DocumentHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private HistoryType historyType;

    @Lob
    private String descreption;

    private Double orderNo;

    @ManyToOne
    private Item item;

    @ManyToOne
    private Institution fromInstitution;

    @ManyToOne
    private Institution toInstitution;

    @ManyToOne
    private Institution institution;

    @ManyToOne
    private WebUser fromUser;
    @ManyToOne
    private WebUser toUser;

    @Lob
    private String comments;

    /*
    Create Properties
     */
    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser acceptedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date acceptedAt;
    /*
    Retire Properties
     */
    private boolean retired;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    private boolean completed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date completedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser completedBy;

    @ManyToOne(fetch = FetchType.EAGER)
    private Document document;

    @Transient
    private Nameable fromInsOrUser;
    @Transient
    private Nameable toInsOrUser;

    public Nameable getFromInsOrUser() {
        if (this.fromInstitution != null && this.fromUser != null) {
            fromInsOrUser = fromUser;
        }
        if (this.fromInstitution != null && this.fromUser == null) {
            fromInsOrUser = fromInstitution;
        }
        if (this.fromInstitution == null && this.fromUser != null) {
            fromInsOrUser = fromUser;
        }
        if (this.fromInstitution == null && this.fromUser == null) {
            fromInsOrUser = null;
        }
        return fromInsOrUser;
    }

    public void setFromInsOrUser(Nameable fromInsOrUser) {
        this.fromInsOrUser = fromInsOrUser;
        if (fromInsOrUser == null) {
            this.fromInstitution = null;
            this.fromUser = null;
        } else if (fromInsOrUser instanceof Institution) {
            this.fromInstitution = (Institution) fromInsOrUser;
            this.fromUser = null;
        } else if (fromInsOrUser instanceof WebUser) {
            this.fromUser = (WebUser) fromInsOrUser;
            this.fromInstitution = null;
        } else {
            this.fromInstitution = null;
            this.fromUser = null;
        }

    }

    public Nameable getToInsOrUser() {
        if (this.toInstitution != null && this.toUser != null) {
            toInsOrUser = toUser;
        }
        if (this.toInstitution != null && this.toUser == null) {
            toInsOrUser = toInstitution;
        }
        if (this.toInstitution == null && this.toUser != null) {
            toInsOrUser = toUser;
        }
        if (this.toInstitution == null && this.toUser == null) {
            toInsOrUser = null;
        }
        return toInsOrUser;
    }

    public void setToInsOrUser(Nameable toInsOrUser) {
        this.toInsOrUser = toInsOrUser;
        if (toInsOrUser == null) {
            this.toInstitution = null;
            this.toUser = null;
        } else if (toInsOrUser instanceof Institution) {
            this.toInstitution = (Institution) toInsOrUser;
            this.toUser = null;
        } else if (toInsOrUser instanceof WebUser) {
            this.toUser = (WebUser) toInsOrUser;
            this.toInstitution = null;
        } else {
            this.toInstitution = null;
            this.toUser = null;
        }

    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public Double getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Double orderNo) {
        this.orderNo = orderNo;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(WebUser acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Date getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }

    public WebUser getFromUser() {
        return fromUser;
    }

    public void setFromUser(WebUser fromUser) {
        this.fromUser = fromUser;
    }

    public WebUser getToUser() {
        return toUser;
    }

    public void setToUser(WebUser toUser) {
        this.toUser = toUser;
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

}
