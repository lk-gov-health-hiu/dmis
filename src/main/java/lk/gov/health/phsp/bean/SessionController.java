/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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
package lk.gov.health.phsp.bean;

import javax.inject.Named;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class SessionController implements Serializable {

    /**
     * Creates a new instance of SessionController
     */
    public SessionController() {
    }

    private Document lastRat;
    private Document lastPcr;
    private Item lastRatOrderingCategory;
    private Item lastPcrOrdringCategory;
    private String lastWardUnit;
    private Institution lastInstitution;
    private Institution lastInstitutionUnit;
    private Institution lastLab;
    private String lastWorkplace;
    private String lastContactOfWorkplace;
    private String lastContactOfWorkplaceDetails;
    private UUID appKey;

    private Map<Long, Document> rats;
    private Map<Long, Document> pcrs;


    public Document getLastRat() {
        return lastRat;
    }

    public void setLastRat(Document lastRat) {
        this.lastRat = lastRat;
    }

    public Document getLastPcr() {
        return lastPcr;
    }

    public UUID getAppKey() {
        return this.appKey;
    }

    public void setAppKey() {
        UUID uuid = UUID.randomUUID();
        this.appKey = uuid;
    }

    public void setLastPcr(Document lastPcr) {
        this.lastPcr = lastPcr;
    }

    public Item getLastRatOrderingCategory() {
        return lastRatOrderingCategory;
    }



    public void setLastRatOrderingCategory(Item lastRatOrderingCategory) {
        this.lastRatOrderingCategory = lastRatOrderingCategory;
    }

    public Item getLastPcrOrdringCategory() {
        return lastPcrOrdringCategory;
    }

    public void setLastPcrOrdringCategory(Item lastPcrOrdringCategory) {
        this.lastPcrOrdringCategory = lastPcrOrdringCategory;
    }

    public Map<Long, Document> getRats() {
        if (rats == null) {
            rats = new HashMap<>();
        }
        return rats;
    }

    public Map<Long, Document> getPcrs() {
        if (pcrs == null) {
            pcrs = new HashMap<>();
        }
        return pcrs;
    }

    public List<Document> getPcrList() {
        return new ArrayList<>(getPcrs().values());
    }

    public List<Document> getRatList() {
        return new ArrayList<>(getRats().values());
    }

    public Institution getLastInstitution() {
        return lastInstitution;
    }

    public void setLastInstitution(Institution lastInstitution) {
        this.lastInstitution = lastInstitution;
    }

    public Institution getLastInstitutionUnit() {
        return lastInstitutionUnit;
    }

    public void setLastInstitutionUnit(Institution lastInstitutionUnit) {
        this.lastInstitutionUnit = lastInstitutionUnit;
    }

    public Institution getLastLab() {
        return lastLab;
    }

    public void setLastLab(Institution lastLab) {
        this.lastLab = lastLab;
    }

    public String getLastWorkplace() {
        return lastWorkplace;
    }

    public void setLastWorkplace(String lastWorkplace) {
        this.lastWorkplace = lastWorkplace;
    }

    public String getLastContactOfWorkplace() {
        return lastContactOfWorkplace;
    }

    public void setLastContactOfWorkplace(String lastContactOfWorkplace) {
        this.lastContactOfWorkplace = lastContactOfWorkplace;
    }

    public String getLastWardUnit() {
        return lastWardUnit;
    }

    public void setLastWardUnit(String lastWardUnit) {
        this.lastWardUnit = lastWardUnit;
    }

    public String getLastContactOfWorkplaceDetails() {
        return lastContactOfWorkplaceDetails;
    }

    public void setLastContactOfWorkplaceDetails(String lastContactOfWorkplaceDetails) {
        this.lastContactOfWorkplaceDetails = lastContactOfWorkplaceDetails;
    }

}
