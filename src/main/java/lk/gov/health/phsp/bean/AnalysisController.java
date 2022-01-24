/*
 * The MIT License
 *
 * Copyright 2020 chims.
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

// <editor-fold defaultstate="collapsed" desc="Imports">
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.facade.DocumentFacade;
// </editor-fold>
@Named
@ApplicationScoped
public class AnalysisController {
// <editor-fold defaultstate="collapsed" desc="EJBs">

   
    @EJB
    private DocumentFacade encounterFacade;
  
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private UserTransactionController userTransactionController;
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private Long clientCount;
    private Long encounterCount;
    private Date from;
    private Date to;

    private String riskVariable = "cvs_risk_factor";
    private String riskVal1 = "30-40%";
    private String riskVal2 = ">40%";
    private List<String> riskVals;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Constructors">
    public AnalysisController() {
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Navigation Methods">
    public String toCountsForSystemAdmin() {
        userTransactionController.recordTransaction("To Counts For SystemAdmin");
        return "/systemAdmin/all_counts";
    }
// </editor-fold>    

// <editor-fold defaultstate="collapsed" desc="Main Methods">
    public void findEncounterCount() {
        Long fs;
        Map m = new HashMap();
        String j = "select count(s) from Encounter s ";
        j += " where s.retired<>:ret ";
        j += " and s.encounterType=:t ";
        j += " and s.encounterDate between :fd and :td ";
        m.put("fd", getFrom());
        m.put("td", getTo());
        m.put("t", DocumentType.Letter);
        m.put("ret", true);

        fs = getEncounterFacade().findLongByJpql(j, m);

        encounterCount = fs;
        userTransactionController.recordTransaction("Find Encounter Count");
    }

    public Long findEncounterCount(Date pFrom, Date pTo, List<Institution> pIns, DocumentType ec, Item sex) {
        if (ec == null) {
            ec = DocumentType.Letter;
        }
        if (pIns == null || pIns.isEmpty()) {
            return null;
        }
        Long fs;
        Map m = new HashMap();
        String j = "select count(s) from Encounter s ";
        j += " where s.retired<>:ret ";
        j += " and s.encounterType=:t ";
        j += " and s.encounterDate between :fd and :td ";
        m.put("fd", pFrom);
        m.put("td", pTo);
        m.put("t", ec);
        m.put("ret", true);

        if (sex != null) {
            j += " and s.client.person.sex.code=:s ";
            m.put("s", sex.getCode());
        }

        j += " and s.institution in :ins ";
        m.put("ins", pIns);
        // // System.out.println("j = " + j);
        // // System.out.println("m = " + m);
        fs = getEncounterFacade().findLongByJpql(j, m);

        return fs;
    }

  
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Getters and Setters">
  

    public DocumentFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(DocumentFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public Long getClientCount() {
        return clientCount;
    }

    public void setClientCount(Long clientCount) {
        this.clientCount = clientCount;
    }

    public Long getEncounterCount() {
        return encounterCount;
    }

    public void setEncounterCount(Long encounterCount) {
        this.encounterCount = encounterCount;
    }

    public Date getFrom() {
        if (from == null) {
            from = CommonController.startOfTheMonth();
        }
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        if (to == null) {
            to = new Date();
        }
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }
// </editor-fold>

}
