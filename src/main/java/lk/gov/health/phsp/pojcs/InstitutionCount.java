/*
 * The MIT License
 *
 * Copyright 2020 buddhika.
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
package lk.gov.health.phsp.pojcs;

import java.util.Date;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.DocumentType;

/**
 *
 * @author buddhika
 */
public class InstitutionCount {

    private Institution institution;
    private Institution referralInstitution;
    private Area area;
    private Long count;
    private Date date;
    private DocumentType encounerType;
    private Item item;
    private Item itemValue;
    private Long id;
    private String positiveRate;

    public InstitutionCount(Long count, Institution institution, Date date, DocumentType encounerType) {
        this.institution = institution;
        this.count = count;
        this.date = date;
        this.encounerType = encounerType;
    }
    
    public InstitutionCount(Date date, Long count) {
        this.count = count;
        this.date = date;
    }

    public InstitutionCount(Item itemValue, Long count) {
        this.itemValue = itemValue;
        this.count = count;
    }




     public InstitutionCount(Area area, Item itemValue, Long count) {
        this.itemValue = itemValue;
        this.count = count;
        this.area = area;
    }

    public InstitutionCount(Long count, Institution institution, Date date, DocumentType encounerType, Item item, Item itemValue) {
        this.institution = institution;
        this.count = count;
        this.date = date;
        this.encounerType = encounerType;
        this.item = item;
        this.itemValue = itemValue;
    }

    public InstitutionCount(Long count, Area area, Date date, DocumentType encounerType) {
        this.area = area;
        this.count = count;
        this.date = date;
        this.encounerType = encounerType;
    }

    public InstitutionCount(Institution institution, Long count) {
        this.institution = institution;
        this.count = count;
    }

    public InstitutionCount(Institution institution, Institution referralInstitution, Long count) {
        this.institution = institution;
        this.referralInstitution = referralInstitution;
        this.count = count;
    }

    public InstitutionCount(Area area, Long count) {
        this.area = area;
        this.count = count;
    }

    public InstitutionCount(Area area, Institution institution, Long count) {
        this.area = area;
        this.count = count;
        this.institution = institution;
    }

    public InstitutionCount() {
    }

    // getters and setters
    public String getPositiveRate() {
        return this.positiveRate;
    }

    public void setPositiveRate(String positiveRate) {
        this.positiveRate = positiveRate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DocumentType getEncounerType() {
        return encounerType;
    }

    public void setEncounerType(DocumentType encounerType) {
        this.encounerType = encounerType;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getItemValue() {
        return itemValue;
    }

    public void setItemValue(Item itemValue) {
        this.itemValue = itemValue;
    }

    public Institution getReferralInstitution() {
        return referralInstitution;
    }

    public void setReferralInstitution(Institution referralInstitution) {
        this.referralInstitution = referralInstitution;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
