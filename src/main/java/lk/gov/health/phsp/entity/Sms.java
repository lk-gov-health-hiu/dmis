/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author buddhika
 */
@Entity
public class Sms implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Document encounter;
    @Lob
    String sendingMessage;
    @Lob
    String receivedMessage;
    String userId;
    String password;
    String sendingUrl;
    private String receipientNumber;
    private String smsType;
    //Sending Properties
    private boolean awaitingSending;
    private boolean sendingFailed;
    private boolean sentSuccessfully;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date sentAt;
    //Created Properties
    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;
    //DataEntry
    @ManyToOne
    private Institution institution;

    public Document getEncounter() {
        return encounter;
    }

    public void setEncounter(Document encounter) {
        this.encounter = encounter;
    }

    public String getSendingMessage() {
        return sendingMessage;
    }

    public void setSendingMessage(String sendingMessage) {
        this.sendingMessage = sendingMessage;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSendingUrl() {
        return sendingUrl;
    }

    public void setSendingUrl(String sendingUrl) {
        this.sendingUrl = sendingUrl;
    }

    public WebUser getCreater() {
        return creater;
    }

    public void setCreater(WebUser creater) {
        this.creater = creater;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public WebUser getRetirer() {
        return retirer;
    }

    public void setRetirer(WebUser retirer) {
        this.retirer = retirer;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Sms)) {
            return false;
        }
        Sms other = (Sms) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.divudi.entity.Sms[ id=" + id + " ]";
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public Boolean getSentSuccessfully() {
        return sentSuccessfully;
    }

    public void setSentSuccessfully(Boolean sentSuccessfully) {
        this.sentSuccessfully = sentSuccessfully;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getReceipientNumber() {
        return receipientNumber;
    }

    public void setReceipientNumber(String receipientNumber) {
        this.receipientNumber = receipientNumber;
    }

    public boolean isSendingFailed() {
        return sendingFailed;
    }

    public void setSendingFailed(boolean sendingFailed) {
        this.sendingFailed = sendingFailed;
    }

    public boolean isAwaitingSending() {
        return awaitingSending;
    }

    public void setAwaitingSending(boolean awaitingSending) {
        this.awaitingSending = awaitingSending;
    }

    public boolean isSentSuccessfully() {
        return sentSuccessfully;
    }

    public void setSentSuccessfully(boolean sentSuccessfully) {
        this.sentSuccessfully = sentSuccessfully;
    }

    
    
    
}
