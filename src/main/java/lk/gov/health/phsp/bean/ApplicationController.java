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
package lk.gov.health.phsp.bean;

// <editor-fold defaultstate="collapsed" desc="Import">
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
// </editor-fold>

/**
 *
 * @author Dr M H B Ariyaratne<buddhika.ari@gmail.com>
 */
@Named
@ApplicationScoped
public class ApplicationController {

// <editor-fold defaultstate="collapsed" desc="EJBs">
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private DocumentFacade encounterFacade;
    // </editor-fold>    
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    InstitutionApplicationController institutionApplicationController;

// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private boolean demoSetup = false;
    private boolean production = true;
    private String versionNo = "1.1.4";
    private List<String> userTransactionTypes;
    private Long nationalTestCount;
    private Long nationalCaseCount;
    //Encounter Count
    //Nont Retired
    //cASE = cASE_eNROLLMENT
    //Test = TestEncollment

    private final boolean logActivity = true;

    // </editor-fold>
    public ApplicationController() {
    }
    
    public Long getNationalTestCount() {
        if (nationalTestCount == null){
            nationalTestCount = getNationalCounts(DocumentType.Letter);
        }
        return nationalTestCount;
    }

    public void setNationalTestCount(Long nationalTestCount) {
        this.nationalTestCount = nationalTestCount;
    }
    
    public Long getNationalCaseCount() {
        if(nationalCaseCount == null){            
            nationalCaseCount = getNationalCounts(DocumentType.File);
        }
        return nationalCaseCount;
    }
    
    public void setNationalCaseCount(Long nationalCaseCount) {
        this.nationalCaseCount = nationalCaseCount;
    }

    
    
    // <editor-fold defaultstate="collapsed" desc="Functions">
    public String createNewPersonalHealthNumber(Institution pins) {
        if (pins == null) {
            return null;
        }
        Institution ins = getInstitutionFacade().find(pins.getId());
        if (ins == null) {
            return null;
        }
        Long lastHinIssued = ins.getLastHin();
        if (lastHinIssued == null) {
            lastHinIssued = 0l;
        }
        Long thisHin = lastHinIssued + 1;
        String poi = ins.getPoiNumber();
        String num = String.format("%06d", thisHin);
        String checkDigit = calculateCheckDigit(poi + num);
        String phn = poi + num + checkDigit;
        ins.setLastHin(thisHin);
        getInstitutionFacade().edit(ins);

        return phn;
    }

    public String createNewPersonalHealthNumberformat(Institution pins) {
        if (pins == null) {
            pins=institutionApplicationController.findMinistryOfHealth();
        }else{
            pins = institutionApplicationController.findMinistryOfHealth();
        }
        Institution ins = getInstitutionFacade().find(pins.getId());
        if (ins == null) {
            ins=institutionApplicationController.findMinistryOfHealth();
        }else{
            ins = institutionApplicationController.findMinistryOfHealth();
        }
        String alpha = "BCDFGHJKMPQRTVWXY";
        String numeric = "23456789";
        String alphanum = alpha + numeric;

        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        int length = 6;
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(alphanum.length());
            char randomChar = alphanum.charAt(index);
            sb.append(randomChar);
        }

        String randomString = sb.toString();

        String poi = ins.getPoiNumber();
        String checkDigit = calculateCheckDigit(poi + randomString);
        String phn = poi + randomString + checkDigit;

        return phn;
    }

 

    public boolean validateHin(String validatingHin) {
        if (validatingHin == null) {
            return false;
        }
        char checkDigit = validatingHin.charAt(validatingHin.length() - 1);
        String digit = calculateCheckDigit(validatingHin.substring(0, validatingHin.length() - 1));
        return checkDigit == digit.charAt(0);
    }

    public String calculateCheckDigit(String card) {
        if (card == null) {
            return null;
        }
        String digit;
        /* convert to array of int for simplicity */
        int[] digits = new int[card.length()];
        for (int i = 0; i < card.length(); i++) {
            digits[i] = Character.getNumericValue(card.charAt(i));
        }

        /* double every other starting from right - jumping from 2 in 2 */
        for (int i = digits.length - 1; i >= 0; i -= 2) {
            digits[i] += digits[i];

            /* taking the sum of digits grater than 10 - simple trick by substract 9 */
            if (digits[i] >= 10) {
                digits[i] = digits[i] - 9;
            }
        }
        int sum = 0;
        for (int i = 0; i < digits.length; i++) {
            sum += digits[i];
        }
        /* multiply by 9 step */
        sum = sum * 9;

        /* convert to string to be easier to take the last digit */
        digit = sum + "";
        return digit.substring(digit.length() - 1);
    }

 

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Enums">
    public InstitutionType[] getInstitutionTypes() {
        return InstitutionType.values();
    }

    public WebUserRole[] getWebUserRoles() {
        return WebUserRole.values();
    }

    // <editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    private InstitutionFacade getInstitutionFacade() {
        return institutionFacade;
    }

// </editor-fold>
    public boolean isDemoSetup() {
        return demoSetup;
    }

    public void setDemoSetup(boolean demoSetup) {
        this.demoSetup = demoSetup;
    }

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

  

    public List<String> getUserTransactionTypes() {
        return userTransactionTypes;
    }

    public void setUserTransactionTypes(List<String> userTransactionTypes) {
        this.userTransactionTypes = userTransactionTypes;
    }

    public boolean isProduction() {
        return production;
    }

    public void setProduction(boolean production) {
        this.production = production;
    }

     public DocumentFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(DocumentFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

   
    private Long getNationalCounts(DocumentType countType) {        
        String jpql = "SELECT count(e) FROM Encounter e "
                + " WHERE e.retired=:ret "
                + " AND e.encounterType=:encounterType ";
        
        Map m = new HashMap();
        m.put("ret", false);
        m.put("encounterType", countType);
        return encounterFacade.countByJpql(jpql, m);        
    }
}
