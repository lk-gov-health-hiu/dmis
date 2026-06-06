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

import lk.gov.health.phsp.enums.InstitutionType;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import lk.gov.health.phsp.pojcs.Nameable;

/**
 *
 * @author Dr M H B Ariyaratne, buddhika.ari@gmail.com
 */
@Entity
@XmlRootElement
@Table
public class Institution implements Serializable, Nameable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InstitutionType institutionType;

    private String name;
    private String tname;
    private String sname;
    private String code;
    private String address;
    private String fax;
    private String email;
    private String phone;
    private String mobile;
    private String web;
    private String poiNumber;
    @ManyToOne
    private Institution poiInstitution;
    private Long lastHin;

    @ManyToOne
    private Institution parent;
    @ManyToOne
    private Area gnArea;
    @ManyToOne
    private Area phmArea;
    @ManyToOne
    private Area phiArea;
    @ManyToOne
    private Area dsDivision;
    @ManyToOne
    private Area mohArea;
    @ManyToOne
    private Area district;
    @ManyToOne
    private Area rdhsArea;
    @ManyToOne
    private Area province;
    @ManyToOne
    private Area pdhsArea;

    @ManyToOne
    private WebUser creater;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdAt;
    @ManyToOne
    private WebUser editer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date editedAt;
    //Retairing properties
    private boolean retired;
    @ManyToOne
    private WebUser retirer;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date retiredAt;
    private String retireComments;

    private boolean pmci;

    @Transient
    InstitutionType institutionTypeRootTrans;

    @Transient
    private String displayName;

    @Transient
    private String insName;

    public Long getId() {
        return id;
    }

    public InstitutionType getInstitutionTypeRootTrans() {
        if (getInstitutionType() == null) {
            institutionTypeRootTrans = InstitutionType.Other;
        }
        switch (getInstitutionType()) {
            case MOH_Office:
            case Regional_Department_of_Health_Department:
            case Provincial_Department_of_Health_Services:
                institutionTypeRootTrans = InstitutionType.MOH_Office;
                break;
            case Base_Hospital:
            case Cardiology_Clinic:
            case Clinic:
            case District_General_Hospital:
            case Divisional_Hospital:
            case Hospital:
            case Lab:
            case Medical_Clinic:
            case Ministry_of_Health:
            case National_Hospital:
            case Other:
            case Other_Clinic:
            case Partner:
                institutionTypeRootTrans = InstitutionType.Hospital;
            default:
                institutionTypeRootTrans = InstitutionType.Hospital;
        }
        return institutionTypeRootTrans;
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

        if (!(object instanceof Institution)) {
            return false;
        }
        Institution other = (Institution) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (id == null) {
            return null;
        } else {
            return id.toString();
        }
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getCode() {
        if (code == null || code.trim().equals("")) {
            String tmpName = name + "    ";
            code = tmpName.substring(0, 3);
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getLastPartOfName() {
        if (name == null) {
            return "";
        }
        return name.substring(name.lastIndexOf(" ") + 1);
    }

    public Long getLastHin() {
        return lastHin;
    }

    public void setLastHin(Long lastHin) {
        this.lastHin = lastHin;
    }

    public String getPoiNumber() {
        return poiNumber;
    }

    public void setPoiNumber(String poiNumber) {
        this.poiNumber = poiNumber;
    }

    public Institution getParent() {
        return parent;
    }

    public void setParent(Institution parent) {
        this.parent = parent;
    }

    public Area getGnArea() {
        return gnArea;
    }

    public void setGnArea(Area gnArea) {
        this.gnArea = gnArea;
    }

    public Area getPhmArea() {
        return phmArea;
    }

    public void setPhmArea(Area phmArea) {
        this.phmArea = phmArea;
    }

    public Area getPhiArea() {
        return phiArea;
    }

    public void setPhiArea(Area phiArea) {
        this.phiArea = phiArea;
    }

    public Area getDsDivision() {
        return dsDivision;
    }

    public void setDsDivision(Area dsDivision) {
        this.dsDivision = dsDivision;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getRdhsArea() {
        return rdhsArea;
    }

    public void setRdhsArea(Area rdhsArea) {
        this.rdhsArea = rdhsArea;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getPdhsArea() {
        return pdhsArea;
    }

    public void setPdhsArea(Area pdhsArea) {
        this.pdhsArea = pdhsArea;
    }

    public Institution getPoiInstitution() {
        if (this.getId() != null) {
            if (poiInstitution == null) {
                poiInstitution = this;
            }
        }
        return poiInstitution;
    }

    public void setPoiInstitution(Institution poiInstitution) {
        this.poiInstitution = poiInstitution;
    }

    public boolean isPmci() {
        return pmci;
    }

    public void setPmci(boolean pmci) {
        this.pmci = pmci;
    }

    public Area getMohArea() {
        return mohArea;
    }

    public void setMohArea(Area mohArea) {
        this.mohArea = mohArea;
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

    public String getDisplayName() {
        if (this.name == null) {
            displayName = "";
        } else {
            displayName = this.name;
        }
        return displayName;
    }

    @Override
    public String getInsName() {
        insName = name;
        return insName;
    }

    @Override
    public void setInsName(String name) {
        this.insName = name;
        this.name = this.insName;
    }

    @Override
    public Boolean getInstitute() {
        return true;
    }

    @Override
    public Boolean getWebUser() {
        return false;
    }

}
