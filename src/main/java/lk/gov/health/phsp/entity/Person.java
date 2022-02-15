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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Entity;
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
import lk.gov.health.phsp.bean.ItemApplicationController;
import lk.gov.health.phsp.pojcs.SlNic;
import lk.gov.health.phsp.pojcs.YearMonthDay;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 *
 * @author Dr. M. H. B. Ariyaratne, MBBS, PGIM Trainee for MSc(Biomedical
 * Informatics)
 */
@Entity
@XmlRootElement
@Table
public class Person implements Serializable {

// <editor-fold defaultstate="collapsed" desc="Persistant Attributes">
    static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item title;
    private String name;
    
    private String code;
    private String tname;
    private String sname;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item sex;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item citizenship;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item ethinicGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item religion;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item mariatalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private Item educationStatus;

    private String occupation;
    private String workPlace;
    private String workplaceContact;
    private String workplaceContactDetails;
    
    

    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dateOfBirth;

    private boolean dobIsAnApproximation;

    @Lob
    private String address;

    private String phone1;
    private String phone2;
    private String email;
    private String fax;
    

    private String nic;

    private String passportNumber;

    private String website;
    private String drivingLicenseNumber;

    private String localReferanceNo;
    private String ssNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private Area gnArea;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area dsArea;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area phmArea;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area phiArea;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area mohArea;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area district;
    @ManyToOne(fetch = FetchType.LAZY)
    private Area province;

    //Created Properties
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser createdBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser editer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date editedAt;

    //Retairing properties
    private boolean retired;
    @ManyToOne(fetch = FetchType.LAZY)
    private WebUser retiredBy;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Transient Attributes">
    @Transient
    private boolean ageCalculated = false;
    @Transient
    private int ageMonths;
    @Transient
    private int ageDays;
    @Transient
    private int ageYears;
    @Transient
    private String age;
    @Transient
    private long ageInDays;
    @Transient
    private int serealNumber;
    @Transient
    private String transPhoneNumbers;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Functions">

    public void calAgeFromDob() {
        ageCalculated = true;
        age = "";
        ageInDays = 0l;
        ageMonths = 0;
        ageDays = 0;
        ageYears = 0;
        if (dateOfBirth == null) {
            return;
        }
        LocalDate dob = new LocalDate(dateOfBirth);
        LocalDate date = new LocalDate(new Date());
        Period period = new Period(dob, date, PeriodType.yearMonthDay());
        ageYears = period.getYears();
        ageMonths = period.getMonths();
        ageDays = period.getDays();
        if (ageYears > 12) {
            age = (period.getYears() + "");
        } else if (ageYears > 0) {
            age = (period.getYears() + " years and " + period.getMonths() + " months.");
        } else {
            age = (period.getMonths() + " months and " + period.getDays() + " days.");
        }
        period = new Period(dob, date, PeriodType.days());
        ageInDays = ((long) period.getDays());
    }

    
    
    public String getAge() {
        if (dateOfBirth != null) {
            calAgeFromDob();
        }
        return age;
    }

    public Long getAgeInDays() {
        if (dateOfBirth != null) {
            calAgeFromDob();
        }
        return ageInDays;
    }

    public int getAgeMonths() {
        if (dateOfBirth != null) {
            calAgeFromDob();
        }
        return ageMonths;
    }

    public int getAgeDays() {
        if (dateOfBirth != null) {
            calAgeFromDob();
        }
        return ageDays;
    }

    public int getAgeYears() {
        if (dateOfBirth != null) {
            calAgeFromDob();
        }
        return ageYears;
    }

    public String getNameWithTitle() {
        String temT;
        if (getTitle() != null) {
            temT = getTitle().name + " " + getName();
        } else {
            temT = getName();
        }
        return temT;
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Getters & Setters">

    /**
     * @return the gnArea
     */
    public Area getGnArea() {
        return gnArea;
    }

    /**
     * @param gnArea the gnArea to set
     */
    public void setGnArea(Area gnArea) {
        this.gnArea = gnArea;
    }

    /**
     * @return the dsArea
     */
    public Area getDsArea() {
        return dsArea;
    }

    /**
     * @param dsArea the dsArea to set
     */
    public void setDsArea(Area dsArea) {
        this.dsArea = dsArea;
    }

    /**
     * @return the phmArea
     */
    public Area getPhmArea() {
        return phmArea;
    }

    /**
     * @param phmArea the phmArea to set
     */
    public void setPhmArea(Area phmArea) {
        this.phmArea = phmArea;
    }

    /**
     * @return the mohArea
     */
    public Area getMohArea() {
        return mohArea;
    }

    /**
     * @param mohArea the mohArea to set
     */
    public void setMohArea(Area mohArea) {
        this.mohArea = mohArea;
    }

    /**
     * @return the district
     */
    public Area getDistrict() {
        return district;
    }

    /**
     * @param district the district to set
     */
    public void setDistrict(Area district) {
        this.district = district;
    }

    /**
     * @return the province
     */
    public Area getProvince() {
        return province;
    }

    /**
     * @param province the province to set
     */
    public void setProvince(Area province) {
        this.province = province;
    }

    /**
     * @param ageMonths the ageMonths to set
     */
    public void setAgeMonths(int ageMonths) {
        this.ageMonths = ageMonths;
        guessDob();
    }

    /**
     * @param ageDays the ageDays to set
     */
    public void setAgeDays(int ageDays) {
        this.ageDays = ageDays;
        guessDob();
    }

    /**
     * @param ageYears the ageYears to set
     */
    public void setAgeYears(int ageYears) {
        this.ageYears = ageYears;
        guessDob();
    }

    private void guessDob() {
        int years = ageYears;
        int month = ageMonths;
        int day = ageDays;
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        now.add(Calendar.YEAR, -years);
        now.add(Calendar.MONTH, -month);
        now.add(Calendar.DATE, -day);
        dateOfBirth = now.getTime();
    }

    private void ageAndSexFromNic() {
        if (nic == null) {
            return;
        }
        SlNic n = new SlNic();
        n.setNic(nic);
        if (n.getDateOfBirth() != null) {
            dateOfBirth = n.getDateOfBirth();
        }
        ItemApplicationController itemApplicationController = CDI.current().select(ItemApplicationController.class).get();
        if (n.getSex() != null) {
            if (n.getSex().equalsIgnoreCase("male")) {
                sex = itemApplicationController.getMale();
            } else {
                sex = itemApplicationController.getFemale();
            }
        }
    }

    /**
     * @param age the age to set
     */
    public void setAge(String age) {
        this.age = age;
    }

    /**
     * @param ageInDays the ageInDays to set
     */
    public void setAgeInDays(long ageInDays) {
        this.ageInDays = ageInDays;
    }

    public Item getSex() {
        return sex;
    }

    public void setSex(Item sex) {
        this.sex = sex;
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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public WebUser getEditer() {
        return editer;
    }

    public void setEditer(WebUser editer) {
        this.editer = editer;
    }

    public Date getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Date editedAt) {
        this.editedAt = editedAt;
    }

    public WebUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(WebUser createdBy) {
        this.createdBy = createdBy;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public boolean isRetired() {
        return retired;
    }

    public void setRetired(boolean retired) {
        this.retired = retired;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    public WebUser getRetiredBy() {
        return retiredBy;
    }

    public void setRetiredBy(WebUser retiredBy) {
        this.retiredBy = retiredBy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address.toUpperCase();
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    public Item getTitle() {
        return title;
    }

    public void setTitle(Item title) {
        this.title = title;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        ageCalculated = false;
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;

        ageAndSexFromNic();

    }

    public int getSerealNumber() {
        return serealNumber;
    }

    public void setSerealNumber(int serealNumber) {
        this.serealNumber = serealNumber;
    }

    public Item getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(Item citizenship) {
        this.citizenship = citizenship;
    }

    public Item getEthinicGroup() {
        return ethinicGroup;
    }

    public void setEthinicGroup(Item ethinicGroup) {
        this.ethinicGroup = ethinicGroup;
    }

    public Item getReligion() {
        return religion;
    }

    public void setReligion(Item religion) {
        this.religion = religion;
    }

    public Item getMariatalStatus() {
        return mariatalStatus;
    }

    public void setMariatalStatus(Item mariatalStatus) {
        this.mariatalStatus = mariatalStatus;
    }

    public Item getEducationStatus() {
        return educationStatus;
    }

    public void setEducationStatus(Item educationStatus) {
        this.educationStatus = educationStatus;
    }

// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Over-rides">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Person)) {
            return false;
        }
        Person other = (Person) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
// </editor-fold>

    public boolean isDobIsAnApproximation() {
        return dobIsAnApproximation;
    }

    public void setDobIsAnApproximation(boolean dobIsAnApproximation) {
        this.dobIsAnApproximation = dobIsAnApproximation;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getTransPhoneNumbers() {
        boolean phoneOneNotBlank = false;
        boolean phoneTwoNotBlank = false;
        if (phone1 != null && !phone1.trim().equals("")) {
            phoneOneNotBlank = true;
        }
        if (phone2 != null && !phone2.trim().equals("")) {
            phoneTwoNotBlank = true;
        }
        if (phoneOneNotBlank && phoneTwoNotBlank) {
            transPhoneNumbers = phone1 + ", " + phone2;
        } else if (phoneOneNotBlank) {
            transPhoneNumbers = phone1;
        } else if (phoneTwoNotBlank) {
            transPhoneNumbers = phone2;
        } else {
            transPhoneNumbers = "";
        }
        return transPhoneNumbers;
    }

    public boolean isAgeCalculated() {
        return ageCalculated;
    }

    public void setAgeCalculated(boolean ageCalculated) {
        this.ageCalculated = ageCalculated;
    }

    public String getLocalReferanceNo() {
        return localReferanceNo;
    }

    public void setLocalReferanceNo(String localReferanceNo) {
        this.localReferanceNo = localReferanceNo;
    }

    public String getSsNumber() {
        return ssNumber;
    }

    public void setSsNumber(String ssNumber) {
        this.ssNumber = ssNumber;
    }

    public Area getPhiArea() {
        return phiArea;
    }

    public void setPhiArea(Area phiArea) {
        this.phiArea = phiArea;
    }

    public String getWorkPlace() {
        return workPlace;
    }

    public void setWorkPlace(String workPlace) {
        this.workPlace = workPlace;
    }

    public String getWorkplaceContact() {
        return workplaceContact;
    }

    public void setWorkplaceContact(String workplaceContact) {
        this.workplaceContact = workplaceContact;
    }
    
    

    public String getWorkplaceContactDetails() {
        return workplaceContactDetails;
    }

    public void setWorkplaceContactDetails(String workplaceContactDetails) {
        this.workplaceContactDetails = workplaceContactDetails;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTname() {
        return tname;
    }

    public void setTname(String tname) {
        this.tname = tname;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    
    
}
