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
package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.pojcs.EncounterWithComponents;


/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class ExcelReportController implements Serializable {

    private final boolean logActivity = true;
    private List<EncounterWithComponents> encountersWithComponents;
  
    String checkingString = "dsfsdfsdfds";
    boolean needCheckLogin = false;

    
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private DocumentFacade encounterFacade;
   

    @Inject
    ApplicationController applicationController;

    /**
     * Creates a new instance of ExcelReportController
     */
    public ExcelReportController() {
    }

   
    public List<EncounterWithComponents> findEncountersWithComponents(List<Long> ids) {
        if (logActivity) {

        }
        if (ids == null) {
            if (logActivity) {

            }
            return null;
        }
        List<EncounterWithComponents> cs = new ArrayList<>();
       
        return cs;
    }

  
    private String evaluateScript(String script) {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            return engine.eval(script) + "";
        } catch (ScriptException ex) {

            return null;
        }
    }

      private List<Long> findEncounterIds(Date fromDate, Date toDate, Institution institution) {
        if (logActivity) {

        }
        String j = "select e.id "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr ";
        j += " and f.completed=:fc ";
        j += " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.id";
        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", DocumentType.Letter);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Long> encs = encounterFacade.findLongList(j, m);
        return encs;
    }

    private Long findCountOfEncounterDates(Date fromDate, Date toDate, Institution institution) {
        if (logActivity) {

        }
        String j = "select count(e.encounterDate) "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr ";
        j += " and f.completed=:fc ";
        j += " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td "
                + " group by e.encounterDate";
        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", DocumentType.Letter);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);

        Long encs = encounterFacade.findLongByJpql(j, m);
        return encs;
    }

   
   

    private String currentTimeAsString() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
        String strDate = dateFormat.format(date);
        return strDate;
    }

   
    private String findQueryComponentCodeByCellString(String text) {

        String str = null;
        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);
        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(text);
        while (m.find()) {
            String block = m.group(1);
            str = block;

        }

        return str;
    }

    
    public boolean isLogActivity() {
        return logActivity;
    }

    public List<EncounterWithComponents> getEncountersWithComponents() {
        return encountersWithComponents;
    }

  

   
    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

    public DocumentFacade getEncounterFacade() {
        return encounterFacade;
    }

    

}
