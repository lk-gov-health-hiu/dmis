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
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lk.gov.health.phsp.enums.EncounterType;

/**
 *
 * @author buddhika
 */
@Entity
@Table
@XmlRootElement
public class Document implements Serializable {

    @OneToMany(mappedBy = "itemEncounter")
    private List<ClientEncounterComponentItem> clientEncounterComponentItems;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private Client client;
    @ManyToOne
    private Area area;

    private String unitWard;
    private String bht;
    private String comments;

    private boolean firstEncounter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Document referenceTest;
    @ManyToOne(fetch = FetchType.LAZY)
    private Document referenceCase;

    private Boolean sampled;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampledAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser sampledBy;

    private Boolean sentToLab;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sentToLabAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser sentToLabBy;
    
    private Boolean sampleMissing;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampleMissingAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser sampleMissingBy;

    private Boolean receivedAtLab;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date receivedAtLabAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser receivedAtLabBy;

    private Boolean sampleRejectedAtLab;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date sampleRejectedAtLabAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser sampleRejectedAtLabBy;

    private Boolean resultEntered;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultEnteredAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultEnteredBy;

    private Boolean pendingAtLab;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date pendingAtLabAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser pendingAtLabBy;

    private Boolean resultReviewed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultReviewedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultReviewedBy;

    private Boolean resultConfirmed;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultConfirmedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultConfirmedBy;

    private Boolean resultPrinted;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultPrintedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultPrintedBy;

    @Lob
    private String resultPrintHtml;

    private Boolean resultNoted;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultNotedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser resultNotedBy;

    private Double ctValue;
    private Double ctValue2;
    private String ctValueStr;
    @ManyToOne
    private Item pcrResult;
    private String pcrResultStr;
    private String resultComments;

    @Deprecated
    private Boolean resultPositive;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date resultDateTime;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date resultMarkedUser;

    @Temporal(javax.persistence.TemporalType.DATE)
    private Date encounterDate;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date encounterFrom;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date encounterTo;

    private Integer encounterYear;
    private Integer encounterMonth;
    private Integer encounterQuarter;

    @Enumerated(EnumType.STRING)
    private EncounterType encounterType;

    private String encounterNumber;

    @ManyToOne
    private Item pcrTestType;
    @ManyToOne
    private Item pcrOrderingCategory;
    @ManyToOne
   
    private Item vaccinationStatus;
    
    @ManyToOne
    private Item symptomaticStatus;
    private String pcrOrderingCategoryOther;
     @Deprecated
    private Boolean symptomatic;

    private String labNumber;
    private String plateNumber;

    @ManyToOne
    private Document parentEncounter;

    @ManyToOne
    Institution institution;
    @ManyToOne
    private Institution institutionUnit;

    @ManyToOne
    Institution referalInstitution;

    @ManyToOne
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    private Institution createdInstitution;

    private boolean retired;
    @ManyToOne
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    @Lob
    private String retireComments;

    @ManyToOne
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

    @Transient
    @Deprecated
    private Item testType;

    @Transient
    private String resultCssClass;

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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public Date getEncounterFrom() {
        return encounterFrom;
    }

    public void setEncounterFrom(Date encounterFrom) {
        this.encounterFrom = encounterFrom;
    }

    public Date getEncounterTo() {
        return encounterTo;
    }

    public void setEncounterTo(Date encounterTo) {
        this.encounterTo = encounterTo;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public Document getParentEncounter() {
        return parentEncounter;
    }

    public void setParentEncounter(Document parentEncounter) {
        this.parentEncounter = parentEncounter;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Institution getReferalInstitution() {
        return referalInstitution;
    }

    public void setReferalInstitution(Institution referalInstitution) {
        this.referalInstitution = referalInstitution;
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

    public String getEncounterNumber() {
        return encounterNumber;
    }

    public void setEncounterNumber(String encounterNumber) {
        this.encounterNumber = encounterNumber;
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

    public Integer getEncounterYear() {
        return encounterYear;
    }

    public void setEncounterYear(Integer encounterYear) {
        this.encounterYear = encounterYear;
    }

    public Integer getEncounterMonth() {
        return encounterMonth;
    }

    public void setEncounterMonth(Integer encounterMonth) {
        this.encounterMonth = encounterMonth;
    }

    public Integer getEncounterQuarter() {
        return encounterQuarter;
    }

    public void setEncounterQuarter(Integer encounterQuarter) {
        this.encounterQuarter = encounterQuarter;
    }

    public boolean isFirstEncounter() {
        return firstEncounter;
    }

    public void setFirstEncounter(boolean firstEncounter) {
        this.firstEncounter = firstEncounter;
    }

    @XmlTransient
    public List<ClientEncounterComponentItem> getClientEncounterComponentItems() {
        return clientEncounterComponentItems;
    }

    public void setClientEncounterComponentItems(List<ClientEncounterComponentItem> clientEncounterComponentItems) {
        this.clientEncounterComponentItems = clientEncounterComponentItems;
    }

    public ClientEncounterComponentItem getClientEncounterComponentItem(Item item) {
        for (ClientEncounterComponentItem i : getClientEncounterComponentItems()) {
            if (i.getItem().equals(item)) {
                return i;
            }
        }
        return null;
    }

    public ClientEncounterComponentItem getClientEncounterComponentItemByCode(String itemCode) {
        // // System.out.println("itemCode = " + itemCode);
        for (ClientEncounterComponentItem i : getClientEncounterComponentItems()) {
            // // System.out.println("i.getItem() = " + i.getItem());
            // // System.out.println("i.getItemValue() = " + i.getItemValue());
            if (i.getItem().getCode().equals(itemCode)) {
                return i;
            }
        }
        return null;
    }

    public String getRetireComments() {
        return retireComments;
    }

    public void setRetireComments(String retireComments) {
        this.retireComments = retireComments;
    }

    public Boolean getResultPositive() {
        return resultPositive;
    }

    public void setResultPositive(Boolean resultPositive) {
        this.resultPositive = resultPositive;
    }

    public Date getResultDate() {
        return resultDate;
    }

    public void setResultDate(Date resultDate) {
        this.resultDate = resultDate;
    }

    public Date getResultDateTime() {
        return resultDateTime;
    }

    public void setResultDateTime(Date resultDateTime) {
        this.resultDateTime = resultDateTime;
    }

    public Date getResultMarkedUser() {
        return resultMarkedUser;
    }

    public void setResultMarkedUser(Date resultMarkedUser) {
        this.resultMarkedUser = resultMarkedUser;
    }

    public Boolean getSampled() {
        return sampled;
    }

    public void setSampled(Boolean sampled) {
        this.sampled = sampled;
    }

    public Date getSampledAt() {
        return sampledAt;
    }

    public void setSampledAt(Date sampledAt) {
        this.sampledAt = sampledAt;
    }

    public Boolean getSentToLab() {
        return sentToLab;
    }

    public void setSentToLab(Boolean sentToLab) {
        this.sentToLab = sentToLab;
    }

    public Date getSentToLabAt() {
        return sentToLabAt;
    }

    public void setSentToLabAt(Date sentToLabAt) {
        this.sentToLabAt = sentToLabAt;
    }

    public Boolean getReceivedAtLab() {
        return receivedAtLab;
    }

    public void setReceivedAtLab(Boolean receivedAtLab) {
        this.receivedAtLab = receivedAtLab;
    }

    public Date getReceivedAtLabAt() {
        return receivedAtLabAt;
    }

    public void setReceivedAtLabAt(Date receivedAtLabAt) {
        this.receivedAtLabAt = receivedAtLabAt;
    }

    public Boolean getResultEntered() {
        return resultEntered;
    }

    public void setResultEntered(Boolean resultEntered) {
        this.resultEntered = resultEntered;
    }

    public Date getResultEnteredAt() {
        return resultEnteredAt;
    }

    public void setResultEnteredAt(Date resultEnteredAt) {
        this.resultEnteredAt = resultEnteredAt;
    }

    public Boolean getResultConfirmed() {
        return resultConfirmed;
    }

    public void setResultConfirmed(Boolean resultConfirmed) {
        this.resultConfirmed = resultConfirmed;
    }

    public Date getResultConfirmedAt() {
        return resultConfirmedAt;
    }

    public void setResultConfirmedAt(Date resultConfirmedAt) {
        this.resultConfirmedAt = resultConfirmedAt;
    }

    public Double getCtValue() {
        return ctValue;
    }

    public void setCtValue(Double ctValue) {
        this.ctValue = ctValue;
    }

    public Item getPcrResult() {
        return pcrResult;
    }

    public void setPcrResult(Item pcrResult) {
        this.pcrResult = pcrResult;
    }

    public WebUser getSampledBy() {
        return sampledBy;
    }

    public void setSampledBy(WebUser sampledBy) {
        this.sampledBy = sampledBy;
    }

    public WebUser getSentToLabBy() {
        return sentToLabBy;
    }

    public void setSentToLabBy(WebUser sentToLabBy) {
        this.sentToLabBy = sentToLabBy;
    }

    public WebUser getReceivedAtLabBy() {
        return receivedAtLabBy;
    }

    public void setReceivedAtLabBy(WebUser receivedAtLabBy) {
        this.receivedAtLabBy = receivedAtLabBy;
    }

    public WebUser getResultEnteredBy() {
        return resultEnteredBy;
    }

    public void setResultEnteredBy(WebUser resultEnteredBy) {
        this.resultEnteredBy = resultEnteredBy;
    }

    public WebUser getResultConfirmedBy() {
        return resultConfirmedBy;
    }

    public void setResultConfirmedBy(WebUser resultConfirmedBy) {
        this.resultConfirmedBy = resultConfirmedBy;
    }

    public Boolean getResultNoted() {
        return resultNoted;
    }

    public void setResultNoted(Boolean resultNoted) {
        this.resultNoted = resultNoted;
    }

    public Date getResultNotedAt() {
        return resultNotedAt;
    }

    public void setResultNotedAt(Date resultNotedAt) {
        this.resultNotedAt = resultNotedAt;
    }

    public WebUser getResultNotedBy() {
        return resultNotedBy;
    }

    public void setResultNotedBy(WebUser resultNotedBy) {
        this.resultNotedBy = resultNotedBy;
    }

    public String getResultComments() {
        return resultComments;
    }

    public void setResultComments(String resultComments) {
        this.resultComments = resultComments;
    }

    public Boolean getResultReviewed() {
        return resultReviewed;
    }

    public void setResultReviewed(Boolean resultReviewed) {
        this.resultReviewed = resultReviewed;
    }

    public Date getResultReviewedAt() {
        return resultReviewedAt;
    }

    public void setResultReviewedAt(Date resultReviewedAt) {
        this.resultReviewedAt = resultReviewedAt;
    }

    public WebUser getResultReviewedBy() {
        return resultReviewedBy;
    }

    public void setResultReviewedBy(WebUser resultReviewedBy) {
        this.resultReviewedBy = resultReviewedBy;
    }

    public Boolean getResultPrinted() {
        return resultPrinted;
    }

    public void setResultPrinted(Boolean resultPrinted) {
        this.resultPrinted = resultPrinted;
    }

    public Date getResultPrintedAt() {
        return resultPrintedAt;
    }

    public void setResultPrintedAt(Date resultPrintedAt) {
        this.resultPrintedAt = resultPrintedAt;
    }

    public WebUser getResultPrintedBy() {
        return resultPrintedBy;
    }

    public void setResultPrintedBy(WebUser resultPrintedBy) {
        this.resultPrintedBy = resultPrintedBy;
    }

    public String getCtValueStr() {
        return ctValueStr;
    }

    public void setCtValueStr(String ctValueStr) {
        this.ctValueStr = ctValueStr;
    }

    public String getLabNumber() {
        return labNumber;
    }

    public void setLabNumber(String labNumber) {
        this.labNumber = labNumber;
    }

    public String getUnitWard() {
        return unitWard;
    }

    public void setUnitWard(String unitWard) {
        this.unitWard = unitWard;
    }

    public String getPcrResultStr() {
        return pcrResultStr;
    }

    public void setPcrResultStr(String pcrResultStr) {
        this.pcrResultStr = pcrResultStr;
    }

    public String getResultPrintHtml() {
        return resultPrintHtml;
    }

    public void setResultPrintHtml(String resultPrintHtml) {
        this.resultPrintHtml = resultPrintHtml;
    }

    @Deprecated
    public Item getTestType() {
        if (testType == null) {
            if (getClientEncounterComponentItemByCode("test_type") != null && getClientEncounterComponentItemByCode("test_type").getItemValue() != null) {
                testType = getClientEncounterComponentItemByCode("test_type").getItemValue();
            }
        }
        return testType;
    }

    public Item getPcrTestType() {
        return pcrTestType;
    }
    
    

    public void setPcrTestType(Item pcrTestType) {
        this.pcrTestType = pcrTestType;
    }

    public Item getPcrOrderingCategory() {
        return pcrOrderingCategory;
    }

    public void setPcrOrderingCategory(Item pcrOrderingCategory) {
        this.pcrOrderingCategory = pcrOrderingCategory;
    }

    public String getPcrOrderingCategoryOther() {
        return pcrOrderingCategoryOther;
    }

    public void setPcrOrderingCategoryOther(String pcrOrderingCategoryOther) {
        this.pcrOrderingCategoryOther = pcrOrderingCategoryOther;
    }

    public Boolean getSymptomatic() {
        return symptomatic;
    }

    public void setSymptomatic(Boolean symptomatic) {
        this.symptomatic = symptomatic;
    }

    public void setTestType(Item testType) {
        this.testType = testType;
    }

    public Double getCtValue2() {
        return ctValue2;
    }
    
    

    public void setCtValue2(Double ctValue2) {
        this.ctValue2 = ctValue2;
    }

    public Boolean getSampleRejectedAtLab() {
        return sampleRejectedAtLab;
    }

    
    
    public void setSampleRejectedAtLab(Boolean sampleRejectedAtLab) {
        this.sampleRejectedAtLab = sampleRejectedAtLab;
    }

    public Date getSampleRejectedAtLabAt() {
        return sampleRejectedAtLabAt;
    }

    public void setSampleRejectedAtLabAt(Date sampleRejectedAtLabAt) {
        this.sampleRejectedAtLabAt = sampleRejectedAtLabAt;
    }

    public WebUser getSampleRejectedAtLabBy() {
        return sampleRejectedAtLabBy;
    }

    public void setSampleRejectedAtLabBy(WebUser sampleRejectedAtLabBy) {
        this.sampleRejectedAtLabBy = sampleRejectedAtLabBy;
    }

    public Boolean getPendingAtLab() {
        return pendingAtLab;
    }

    public void setPendingAtLab(Boolean pendingAtLab) {
        this.pendingAtLab = pendingAtLab;
    }

    public Date getPendingAtLabAt() {
        return pendingAtLabAt;
    }

    public void setPendingAtLabAt(Date pendingAtLabAt) {
        this.pendingAtLabAt = pendingAtLabAt;
    }

    
    
    public WebUser getPendingAtLabBy() {
        return pendingAtLabBy;
    }

    public void setPendingAtLabBy(WebUser pendingAtLabBy) {
        this.pendingAtLabBy = pendingAtLabBy;
    }

    public String getResultCssClass() {
        System.out.println("getResultCssClass");
        resultCssClass = "";
        if (pcrResult == null || pcrTestType == null) {
            return resultCssClass;
        }
        if (pcrTestType.getCode().equalsIgnoreCase("covid19_pcr_test")) {
            switch (pcrResult.getCode()) {
                case "pcr_positive":
                    resultCssClass = "pcr_positive";
                    break;
                case "pcr_negative":
                    resultCssClass = "pcr_negative";
                    break;
                case "pcr_inconclusive":
                    resultCssClass = "pcr_inconclusive";
                    break;
                case "pcr_invalid":
                    resultCssClass = "pcr_invalid";
            }
        } else if (pcrTestType.getCode().equalsIgnoreCase("covid19_rat")) {
            switch (pcrResult.getCode()) {
                case "pcr_positive":
                    resultCssClass = "rat_positive";
                    break;
                case "pcr_negative":
                    resultCssClass = "rat_negative";
                    break;
                case "pcr_inconclusive":
                    resultCssClass = "rat_inconclusive";
                    break;
                case "pcr_invalid":
                    resultCssClass = "rat_invalid";
            }
        }
        return resultCssClass;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public Document getReferenceTest() {
        return referenceTest;
    }

    public void setReferenceTest(Document referenceTest) {
        this.referenceTest = referenceTest;
    }

    public Document getReferenceCase() {
        return referenceCase;
    }

    public void setReferenceCase(Document referenceCase) {
        this.referenceCase = referenceCase;
    }

    public Institution getInstitutionUnit() {
        return institutionUnit;
    }

    public void setInstitutionUnit(Institution institutionUnit) {
        this.institutionUnit = institutionUnit;
    }

    public Institution getCreatedInstitution() {
        if(createdInstitution==null){
            createdInstitution = institution;
        }
        return createdInstitution;
    }

    public void setCreatedInstitution(Institution createdInstitution) {
        this.createdInstitution = createdInstitution;
    }

    public String getBht() {
        return bht;
    }

    public void setBht(String bht) {
        this.bht = bht;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Item getVaccinationStatus() {
        return vaccinationStatus;
    }

    public void setVaccinationStatus(Item vaccinationStatus) {
        this.vaccinationStatus = vaccinationStatus;
    }

    public Item getSymptomaticStatus() {
        return symptomaticStatus;
    }

    public void setSymptomaticStatus(Item symptomaticStatus) {
        this.symptomaticStatus = symptomaticStatus;
    }

    public Boolean getSampleMissing() {
        return sampleMissing;
    }

    public void setSampleMissing(Boolean sampleMissing) {
        this.sampleMissing = sampleMissing;
    }

    public Date getSampleMissingAt() {
        return sampleMissingAt;
    }

    public void setSampleMissingAt(Date sampleMissingAt) {
        this.sampleMissingAt = sampleMissingAt;
    }

    public WebUser getSampleMissingBy() {
        return sampleMissingBy;
    }

    public void setSampleMissingBy(WebUser sampleMissingBy) {
        this.sampleMissingBy = sampleMissingBy;
    }
    
    
    

}
