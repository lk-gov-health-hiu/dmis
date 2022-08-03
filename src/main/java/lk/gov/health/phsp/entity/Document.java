/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.enums.DocumentGenerationType;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.pojcs.Nameable;

/**
 *
 * @author buddhika
 */
@Entity
@Table
@XmlRootElement
public class Document implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String documentName;
    private String documentNumber;
    private String documentCode;
    @Lob
    private String comments;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date documentDate;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date receivedDate;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    
    @Enumerated(EnumType.STRING)
    private DocumentGenerationType documentGenerationType;

    @ManyToOne(fetch = FetchType.LAZY)
    private Document referenceDocument;
    @ManyToOne
    private Document parentDocument;
    @ManyToOne
    private Item documentLanguage;
    @ManyToOne
    private Item letterStatus;

    @ManyToOne
    private Institution institution;
    @ManyToOne
    private Institution institutionUnit;
    @ManyToOne
    private WebUser owner;

    @ManyToOne
    private Item receivedAs;

    @ManyToOne
    private Institution fromInstitution;
    @ManyToOne
    private WebUser fromWebUser;
    @ManyToOne
    private Institution toInstitution;
    @ManyToOne
    private WebUser toWebUser;
    

    @Transient
    private Nameable fromInsOrUser;
     @Transient
    private Nameable toInsOrUser;
    
    private String registrationNo;
    private String senderName;


    @ManyToOne
    private Institution currentInstitution;
    @ManyToOne
    private WebUser currentOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private Institution createdInstitution;

    private boolean retired;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    @Lob
    private String retireComments;

    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredReversedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredReversedAt;

    /*
    Last Edit Properties
     */
    @ManyToOne
    private WebUser lastEditBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastEditeAt;

    private boolean completed;
    @ManyToOne
    private WebUser completedBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date completedAt;
    
    
    

    public String getIdString() {
        if (id == null) {
            return "";
        }
        return id + "";
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

        if (!(object instanceof Document)) {
            return false;
        }
        Document other = (Document) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id + "";
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getDocumentDate() {
        return documentDate;
    }

    public void setDocumentDate(Date documentDate) {
        this.documentDate = documentDate;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Document getReferenceDocument() {
        return referenceDocument;
    }

    public void setReferenceDocument(Document referenceDocument) {
        this.referenceDocument = referenceDocument;
    }

    public Document getParentDocument() {
        return parentDocument;
    }

    public void setParentDocument(Document parentDocument) {
        this.parentDocument = parentDocument;
    }

    public Institution getInstitutionUnit() {
        return institutionUnit;
    }

    public void setInstitutionUnit(Institution institutionUnit) {
        this.institutionUnit = institutionUnit;
    }

    public WebUser getOwner() {
        return owner;
    }

    public void setOwner(WebUser owner) {
        this.owner = owner;
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

    public Institution getCreatedInstitution() {
        return createdInstitution;
    }

    public void setCreatedInstitution(Institution createdInstitution) {
        this.createdInstitution = createdInstitution;
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

    public WebUser getRetiredReversedBy() {
        return retiredReversedBy;
    }

    public void setRetiredReversedBy(WebUser retiredReversedBy) {
        this.retiredReversedBy = retiredReversedBy;
    }

    public Date getRetiredReversedAt() {
        return retiredReversedAt;
    }

    public void setRetiredReversedAt(Date retiredReversedAt) {
        this.retiredReversedAt = retiredReversedAt;
    }

    public WebUser getLastEditBy() {
        return lastEditBy;
    }

    public void setLastEditBy(WebUser lastEditBy) {
        this.lastEditBy = lastEditBy;
    }

    public Date getLastEditeAt() {
        return lastEditeAt;
    }

    public void setLastEditeAt(Date lastEditeAt) {
        this.lastEditeAt = lastEditeAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public WebUser getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(WebUser completedBy) {
        this.completedBy = completedBy;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Institution getCurrentInstitution() {
        return currentInstitution;
    }

    public void setCurrentInstitution(Institution currentInstitution) {
        this.currentInstitution = currentInstitution;
    }

    public WebUser getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(WebUser currentOwner) {
        this.currentOwner = currentOwner;
    }

    public Institution getFromInstitution() {
        return fromInstitution;
    }

    public void setFromInstitution(Institution fromInstitution) {
        this.fromInstitution = fromInstitution;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public Item getReceivedAs() {
        return receivedAs;
    }

    public void setReceivedAs(Item receivedAs) {
        this.receivedAs = receivedAs;
    }

    public Item getDocumentLanguage() {
        return documentLanguage;
    }

    public void setDocumentLanguage(Item documentLanguage) {
        this.documentLanguage = documentLanguage;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public WebUser getFromWebUser() {
        return fromWebUser;
    }

    public void setFromWebUser(WebUser fromWebUser) {
        this.fromWebUser = fromWebUser;
    }
    
    
    
    
    

    public Nameable getFromInsOrUser() {
        if(this.fromInstitution!=null && this.fromWebUser!=null){
            fromInsOrUser = fromWebUser;
        }if(this.fromInstitution!=null && this.fromWebUser==null){
            fromInsOrUser = fromInstitution;
        }if(this.fromInstitution==null && this.fromWebUser!=null){
            fromInsOrUser = fromWebUser;
        }if(this.fromInstitution==null && this.fromWebUser==null){
            fromInsOrUser = null;
        }
        return fromInsOrUser;
    }

    public void setFromInsOrUser(Nameable fromInsOrUser) {
        this.fromInsOrUser = fromInsOrUser;
        if(fromInsOrUser==null){
            this.fromInstitution=null;
            this.fromWebUser=null;
        }else if(fromInsOrUser instanceof Institution){
            this.fromInstitution= (Institution) fromInsOrUser;
            this.fromWebUser=null;
        }else if(fromInsOrUser instanceof WebUser){
            this.fromWebUser = (WebUser) fromInsOrUser;
            this.fromInstitution=null;
        }else{
            this.fromInstitution=null;
            this.fromWebUser=null;
        }
        
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public Nameable getToInsOrUser() {
        if(this.toInstitution!=null && this.toWebUser!=null){
            toInsOrUser = toWebUser;
        }if(this.toInstitution!=null && this.toWebUser==null){
            toInsOrUser = toInstitution;
        }if(this.toInstitution==null && this.toWebUser!=null){
            toInsOrUser = toWebUser;
        }if(this.toInstitution==null && this.toWebUser==null){
            toInsOrUser = null;
        }
        return toInsOrUser;
    }

    public void setToInsOrUser(Nameable toInsOrUser) {
        this.toInsOrUser = toInsOrUser;
        if(toInsOrUser==null){
            this.toInstitution=null;
            this.toWebUser=null;
        }else if(toInsOrUser instanceof Institution){
            this.toInstitution= (Institution) toInsOrUser;
            this.toWebUser=null;
        }else if(toInsOrUser instanceof WebUser){
            this.toWebUser = (WebUser) toInsOrUser;
            this.toInstitution=null;
        }else{
            this.toInstitution=null;
            this.toWebUser=null;
        }
        
    }

    public Institution getToInstitution() {
        return toInstitution;
    }

    public void setToInstitution(Institution toInstitution) {
        this.toInstitution = toInstitution;
    }

    public WebUser getToWebUser() {
        return toWebUser;
    }

    public void setToWebUser(WebUser toWebUser) {
        this.toWebUser = toWebUser;
    }

    public Item getLetterStatus() {
        return letterStatus;
    }

    public void setLetterStatus(Item letterStatus) {
        this.letterStatus = letterStatus;
    }

    public DocumentGenerationType getDocumentGenerationType() {
        return documentGenerationType;
    }

    public void setDocumentGenerationType(DocumentGenerationType documentGenerationType) {
        this.documentGenerationType = documentGenerationType;
    }
    
    
    
    
}
