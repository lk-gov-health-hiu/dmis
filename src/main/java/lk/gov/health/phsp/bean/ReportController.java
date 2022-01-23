/*
 * The MIT License
 *
 * Copyright 2019 buddhika.ari@gmail.com
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Client;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientEncounterComponentFormSetFacade;
import lk.gov.health.phsp.pojcs.NcdReportTem;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import lk.gov.health.phsp.entity.ClientEncounterComponentFormSet;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.ConsolidatedQueryResult;
import lk.gov.health.phsp.entity.DesignComponentForm;
import lk.gov.health.phsp.entity.DesignComponentFormItem;
import lk.gov.health.phsp.entity.DesignComponentFormSet;
import lk.gov.health.phsp.entity.IndividualQueryResult;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.QueryComponent;
import lk.gov.health.phsp.entity.StoredQueryResult;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.QueryCriteriaMatchType;
import lk.gov.health.phsp.enums.QueryType;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.ClientFacade;
import lk.gov.health.phsp.facade.ConsolidatedQueryResultFacade;
import lk.gov.health.phsp.facade.DesignComponentFormItemFacade;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.IndividualQueryResultFacade;
import lk.gov.health.phsp.facade.QueryComponentFacade;
import lk.gov.health.phsp.facade.StoredQueryResultFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.pojcs.AreaCount;
import lk.gov.health.phsp.pojcs.ClientBasicData;
import lk.gov.health.phsp.pojcs.DateInstitutionCount;
import lk.gov.health.phsp.pojcs.EncounterBasicData;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.Replaceable;
import lk.gov.health.phsp.pojcs.ReportColumn;
import lk.gov.health.phsp.pojcs.ReportRow;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
// </editor-fold>   

/**
 *
 * @author hiu_pdhs_sp
 */
@Named(value = "reportController")
@SessionScoped
public class ReportController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">

    @EJB
    private DesignComponentFormItemFacade designComponentFormItemFacade;
    @EJB
    private ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
    private ClientEncounterComponentFormSetFacade clientEncounterComponentFormSetFacade;
    @EJB
    private ClientFacade clientFacade;
    @EJB
    private DocumentFacade encounterFacade;
    @EJB
    private QueryComponentFacade queryComponentFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private StoredQueryResultFacade storedQueryResultFacade;
    @EJB
    private ConsolidatedQueryResultFacade consolidatedQueryResultFacade;
    @EJB
    private IndividualQueryResultFacade individualQueryResultFacade;

// </editor-fold>     
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private DocumentController encounterController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    
    @Inject
    private ExcelReportController excelReportController;
   
    @Inject
    private UserTransactionController userTransactionController;
   
// </editor-fold>  
// <editor-fold defaultstate="collapsed" desc="Class Variables">
    private List<Document> encounters;
    private List<Client> clients;
    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private DesignComponentFormSet fromSet;
    private Area area;
    private NcdReportTem ncdReportTem;
    private StreamedContent file;
    private String mergingMessage;
    private QueryComponent queryComponent;
// </editor-fold> 

// <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new instance of ReportController
     */
    public ReportController() {
    }

// </editor-fold> 
    private List<StoredQueryResult> myResults;
    private List<StoredQueryResult> reportResults;
    private List<InstitutionCount> institutionCounts;
    private Long reportCount;
    private List<AreaCount> areaCounts;
    private Long areaRepCount;
    private DesignComponentFormSet designingComponentFormSet;
    private List<DesignComponentFormItem> designComponentFormItems;
    private DesignComponentFormItem designComponentFormItem;

    private StoredQueryResult removingResult;
    private StoredQueryResult downloadingResult;

    private Upload currentUpload;
    private StreamedContent downloadingFile;
    private StreamedContent resultExcelFile;

    private ReportTimePeriod reportTimePeriod;
    private TimePeriodType timePeriodType;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private Integer dateOfMonth;
    private Quarter quarterEnum;
    private boolean recalculate;

    public void listMyReports() {
        String j;
        Map m = new HashMap();
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.retired=false "
                + " and s.creater=:me "
                + " order by s.id desc";

        m.put("me", webUserController.getLoggedUser());
        myResults = getStoredQueryResultFacade().findByJpql(j, m, true);
    }

    public void listMyReportsLast() {
        String j;
        Map m = new HashMap();
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.retired=false "
                + " and s.creater=:me "
                + " order by s.id desc";

        m.put("me", webUserController.getLoggedUser());
        myResults = getStoredQueryResultFacade().findByJpql(j, m, 100);
    }

    public void listExistingMonthlyReports() {
        setTimePeriodType(TimePeriodType.Monthly);
        listExistingReports();
        userTransactionController.recordTransaction("List Existing Monthly Reports");
    }

    public void listExistingReports() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }

        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Please select a report");
            return;
        }

        switch (getTimePeriodType()) {
            case Yearley:
                setFromDate(CommonController.startOfTheYear(getYear()));
                setToDate(CommonController.endOfYear(getYear()));
                break;
            case Quarterly:
                setFromDate(CommonController.startOfQuarter(getYear(), getQuarter()));
                setToDate(CommonController.endOfQuarter(getYear(), getQuarter()));
                break;
            case Monthly:
                setFromDate(CommonController.startOfTheMonth(getYear(), getMonth()));
                setToDate(CommonController.endOfTheMonth(getYear(), getMonth()));
                break;
            case Dates:
            //TODO: Add what happens when selected dates

        }

        String j;
        Map m = new HashMap();
        j = "select s "
                + " from StoredQueryResult s "
                + " where s.retired=false "
                + " and s.institution=:ins "
                + " and s.queryComponent=:qc "
                + " and s.resultFrom=:f "
                + " and s.resultTo=:t "
                + " order by s.id desc";

        m.put("ins", institution);
        m.put("qc", queryComponent);
        m.put("f", getFromDate());
        m.put("t", getToDate());

        reportResults = getStoredQueryResultFacade().findByJpql(j, m);

    }

    public void removeReport() {
        if (removingResult == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        if (removingResult.isProcessCompleted()
                && !removingResult.getCreater().equals(webUserController.getLoggedUser())) {
            JsfUtil.addErrorMessage("You can not remove others successful reports.");
            return;
        }
        removingResult.setRetired(true);
        removingResult.setRetirer(webUserController.getLoggedUser());
        removingResult.setRetiredAt(new Date());
        getStoredQueryResultFacade().edit(removingResult);
        JsfUtil.addSuccessMessage("Removed");
        listExistingReports();
        listMyReports();
        userTransactionController.recordTransaction("Remove Report");
    }

  

  
    public void clearReportData() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }

        StoredQueryResult sqr = new StoredQueryResult();
        switch (getTimePeriodType()) {
            case Yearley:
                sqr.setResultFrom(CommonController.startOfTheYear(getYear()));
                sqr.setResultTo(CommonController.endOfYear(getYear()));
                sqr.setResultYear(getYear());
                break;
            case Quarterly:
                sqr.setResultFrom(CommonController.startOfQuarter(getYear(), getQuarter()));
                sqr.setResultTo(CommonController.endOfQuarter(getYear(), getQuarter()));
                sqr.setResultYear(getYear());
                sqr.setResultQuarter(getQuarter());
                break;
            case Monthly:
                sqr.setResultFrom(CommonController.startOfTheMonth(getYear(), getMonth()));
                sqr.setResultTo(CommonController.endOfTheMonth(getYear(), getMonth()));
                sqr.setResultYear(getYear());
                sqr.setResultMonth(getMonth());
                break;
            case Dates:
                sqr.setResultFrom(fromDate);
                sqr.setResultTo(toDate);
                break;
        }
        setFromDate(sqr.getResultFrom());
        setToDate(sqr.getResultTo());

        String j;
        Map m = new HashMap();
        j = "select r "
                + " from ConsolidatedQueryResult r "
                + " where r.resultFrom=:fd "
                + " and r.resultTo=:td ";
        m.put("fd", sqr.getResultFrom());
        m.put("td", sqr.getResultTo());
        j += " and r.institution=:ins ";
        m.put("ins", institution);
        List<ConsolidatedQueryResult> crs = getConsolidatedQueryResultFacade().findByJpql(j, m);
        for (ConsolidatedQueryResult cr : crs) {
            cr.setLongValue(null);
            getConsolidatedQueryResultFacade().edit(cr);
        }

        List<Long> encIds = findEncounterIds(sqr.getResultFrom(), sqr.getResultTo(), institution);

        for (Long encId : encIds) {
            m = new HashMap();
            j = "select r "
                    + " from IndividualQueryResult r "
                    + " where r.encounterId=:enid";
            m.put("enid", encId);
            List<IndividualQueryResult> iqrs = getIndividualQueryResultFacade().findByJpql(j, m);

            for (IndividualQueryResult iqr : iqrs) {
                iqr.setIncluded(null);
                getIndividualQueryResultFacade().edit(iqr);
            }

        }

        JsfUtil.addSuccessMessage("All Previous Calculated Data Discarded.");

    }

    public void createNewMonthlyReport() {
        setTimePeriodType(TimePeriodType.Monthly);
        createNewReport();
        System.gc();
        userTransactionController.recordTransaction("Create New Monthly Report");
    }

    public void createNewReport() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Please select an institutions");
            return;
        }

        if (queryComponent == null) {
            JsfUtil.addErrorMessage("Please select a report");
            return;
        }

        StoredQueryResult sqr = new StoredQueryResult();
        sqr.setCreatedAt(new Date());
        sqr.setCreater(webUserController.getLoggedUser());
        sqr.setRecalculate(recalculate);
        sqr.setInstitution(institution);
        sqr.setRequestCreatedAt(new Date());
        sqr.setTimePeriodType(getTimePeriodType());
        sqr.setQueryComponent(queryComponent);

        switch (getTimePeriodType()) {
            case Yearley:
                sqr.setResultFrom(CommonController.startOfTheYear(getYear()));
                sqr.setResultTo(CommonController.endOfYear(getYear()));
                sqr.setResultYear(getYear());

                break;
            case Quarterly:
                sqr.setResultFrom(CommonController.startOfQuarter(getYear(), getQuarter()));
                sqr.setResultTo(CommonController.endOfQuarter(getYear(), getQuarter()));
                sqr.setResultYear(getYear());
                sqr.setResultQuarter(getQuarter());
                break;
            case Monthly:

                sqr.setResultFrom(CommonController.startOfTheMonth(getYear(), getMonth()));
                sqr.setResultTo(CommonController.endOfTheMonth(getYear(), getMonth()));
                sqr.setResultYear(getYear());
                sqr.setResultMonth(getMonth());
                break;
            case Dates:
            //TODO: Add what happens when selected dates

        }

        getStoredQueryResultFacade().create(sqr);

        setFromDate(sqr.getResultFrom());
        setToDate(sqr.getResultTo());
        JsfUtil.addSuccessMessage("Added to the Queue to Process");
        boolean reportDone = getExcelReportController().processReport(sqr);
        if (reportDone) {
            JsfUtil.addSuccessMessage("Report Created. Please click the list button to list it.");
        } else {
            JsfUtil.addErrorMessage("Error");
        }

    }

    public TimePeriodType getTimePeriodType() {
        if (timePeriodType == null) {
            timePeriodType = TimePeriodType.Monthly;
        }
        return timePeriodType;
    }

    public void setTimePeriodType(TimePeriodType timePeriodType) {
        this.timePeriodType = timePeriodType;
    }

    public Integer getYear() {
        if (year == null || year == 0) {
            year = CommonController.getYear(CommonController.startOfTheLastQuarter());
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getQuarter() {
        if (quarter == null) {
            quarter = CommonController.getQuarter(CommonController.startOfTheLastQuarter());
        }
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public Integer getMonth() {
        if (month == null) {
            month = CommonController.getMonth(CommonController.startOfTheLastMonth());
        }
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDateOfMonth() {
        return dateOfMonth;
    }

    public void setDateOfMonth(Integer dateOfMonth) {
        this.dateOfMonth = dateOfMonth;
    }

    public Quarter getQuarterEnum() {
        if (quarterEnum == null) {
            switch (getQuarter()) {
                case 1:
                    quarterEnum = Quarter.First;
                    break;
                case 2:
                    quarterEnum = Quarter.Second;
                    break;
                case 3:
                    quarterEnum = Quarter.Third;
                    break;
                case 4:
                    quarterEnum = Quarter.Fourth;
                    break;
                default:
                    quarterEnum = Quarter.First;
            }
        }
        return quarterEnum;
    }

    public void setQuarterEnum(Quarter quarterEnum) {
        switch (quarterEnum) {
            case First:
                quarter = 1;
                break;
            case Second:
                quarter = 2;
                break;
            case Third:
                quarter = 3;
                break;
            case Fourth:
                quarter = 4;
                break;
            default:
                quarter = 1;
        }
        this.quarterEnum = quarterEnum;
    }

    public StoredQueryResultFacade getStoredQueryResultFacade() {
        return storedQueryResultFacade;
    }

    public ReportTimePeriod getReportTimePeriod() {
        return reportTimePeriod;
    }

    public void setReportTimePeriod(ReportTimePeriod reportTimePeriod) {
        this.reportTimePeriod = reportTimePeriod;
    }

// <editor-fold defaultstate="collapsed" desc="Navigation">
    public String toViewReports() {
        userTransactionController.recordTransaction("To View Reports");
        return "/reports/index";
    }

    public String toInstitutionMonthlySummeries() {
        String forSys = "/reports/summaries/institution_monthly_summaries_sa";
        String forIns = "/reports/summaries/institution_monthly_summaries_ia";
        String forMeu = "/reports/summaries/institution_excel_reports_meu";
        String forMea = "/reports/summaries/institution_excel_reports_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
                action = forMea;
                break;
            case Phi:
                action = forMeu;
                break;
            case Moh:
            case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Institution Monthly Summeries");
        return action;
    }

    public String toConsolidateSummeries() {
        String forSys = "/reports/summaries/consolidate_summaries_sa";
        String forIns = "/reports/summaries/consolidate_summaries_ia";
        String forMeu = "/reports/summaries/consolidate_meu";
        String forMea = "/reports/summaries/consolidate_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
                action = forMea;
                break;
            case Phi:
                action = forMeu;
                break;
            case Moh:
                case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Consolidate Summeries");
        return action;
    }

    public String toSingleVariableClinicalData() {
        String forSys = "/reports/clinical_data/single_variable_sa";
        String forIns = "/reports/clinical_data/single_variable_ia";
        String forMeu = "/reports/clinical_data/single_variable_meu";
        String forMea = "/reports/clinical_data/single_variable_mea";
        String forClient = "";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
                action = forMea;
                break;
            case Phi:
                action = forMeu;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To Single Variable Clinical Data");
        return action;
    }

    public String toViewMySummeries() {

        listMyReports();
        userTransactionController.recordTransaction("To View My Summeries");
        return "/reports/summaries/my_summaries";
    }

    private List<Long> findEncounterIds(Date fromDate, Date toDate, Institution institution) {
        String j = "select e.id "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed=:fc "
                + " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " group by e";

        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", EncounterType.Test_Enrollment);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Long> encounterIds = clientEncounterComponentFormSetFacade.findLongList(j, m);

        return encounterIds;

    }

    private List<Document> findEncounters(Date fromDate, Date toDate, Institution institution) {
        String j = "select e "
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
        m.put("t", EncounterType.Test_Enrollment);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);

        List<Document> encs = encounterFacade.findByJpql(j, m);

        return encs;

    }

    private List<Client> findClients(Date fromDate, Date toDate, Institution institution) {
        String j = "select e.client "
                + " from  ClientEncounterComponentFormSet f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed=:fc "
                + " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td"
                + " order by e.client.id";

        Map m = new HashMap();
        m.put("i", institution);
        m.put("t", EncounterType.Test_Enrollment);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Client> encounterIds = clientFacade.findLongList(j, m);

        return encounterIds;

    }

    private List<ClientEncounterComponentItem> findClientEncounterComponentItems(Date fromDate, Date toDate, Institution institution, String sex) {
        Map m = new HashMap();
        String j = "select i "
                + " from  ClientEncounterComponentItem i join i.parentComponent.parentComponent f join f.encounter e"
                + " where e.retired<>:er"
                + " and f.retired<>:fr "
                + " and f.completed=:fc "
                + " and e.institution=:i "
                + " and e.encounterType=:t "
                + " and e.encounterDate between :fd and :td";

        if (sex != null) {
            j += " and e.client.person.sex.code=:sex";
            m.put("sex", sex);
        }
        j += " order by e.client.id";

        m.put("i", institution);
        m.put("t", EncounterType.Test_Enrollment);
        m.put("er", true);
        m.put("fr", true);
        m.put("fc", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<ClientEncounterComponentItem> encounterIds = clientEncounterComponentItemFacade.findLongList(j, m);

        return encounterIds;

    }

    private List<ClientEncounterComponentItem> ClientEncounterComponentFormItems(Document e) {
        Map m = new HashMap();
        String j = "select i "
                + " from  ClientEncounterComponentItem i "
                + " join i.parentComponent.parentComponent f "
                + " join f.encounter e"
                + " where e=:e";
        m.put("e", e);

        List<ClientEncounterComponentItem> encounterIds = clientEncounterComponentItemFacade.findLongList(j, m);

        return encounterIds;

    }


    public Long findReplaceblesInCalculationString(String text, List<Document> ens) {

        Long l = 0l;

        if (ens == null) {

            return l;
        }
        if (ens.isEmpty()) {

            l = 0l;
            return l;
        }

        List<Replaceable> ss = new ArrayList<>();

        String patternStart = "#{";
        String patternEnd = "}";
        String regexString = Pattern.quote(patternStart) + "(.*?)" + Pattern.quote(patternEnd);

        Pattern p = Pattern.compile(regexString);
        Matcher m = p.matcher(text);

        

        return l;

    }

    public boolean matchQuery(QueryComponent q, ClientEncounterComponentItem qi) {
        if (q.getItem() == null) {
            return false;
        }
        if (qi.getItem() == null) {
            return false;
        }
        if (!qi.getItem().getCode().equalsIgnoreCase(q.getItem().getCode())) {
            return false;
        }

        boolean m = false;
        Integer int1 = null;
        Integer int2 = null;
        Double real1 = null;
        Double real2 = null;
        Long lng1 = null;
        Long lng2 = null;
        Item itemVariable = null;
        Item itemValue = null;

        if (q.getMatchType() == QueryCriteriaMatchType.Variable_Value_Check) {

            switch (q.getQueryDataType()) {
                case integer:
                    int1 = q.getIntegerNumberValue();
                    int2 = q.getIntegerNumberValue2();
                    break;
                case item:
                    itemValue = q.getItemValue();
                    itemVariable = q.getItem();
                    break;
                case real:
                    real1 = q.getRealNumberValue();
                    real2 = q.getRealNumberValue2();
                    break;
                case longNumber:
                    lng1 = q.getLongNumberValue();
                    lng2 = q.getLongNumberValue2();
                    break;

            }
            switch (q.getEvaluationType()) {
                case Equal:
                    if (int1 != null) {
                        m = int1.equals(qi.getIntegerNumberValue());
                    }
                    if (lng1 != null) {
                        m = lng1.equals(qi.getLongNumberValue());
                    }
                    if (real1 != null) {
                        m = real1.equals(qi.getRealNumberValue());
                    }

                    if (itemValue != null && itemVariable != null) {

                        if (itemValue.getCode().equals(qi.getItemValue().getCode())) {
                            m = true;
                        }
                    }
                    break;
                case Less_than:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() < int1;
                    }
                    if (lng1 != null && qi.getLongNumberValue() != null) {
                        m = qi.getLongNumberValue() < lng1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() < real1;
                    }

                case Between:
                    if (int1 != null && int2 != null && qi.getIntegerNumberValue() != null) {
                        if (int1 > int2) {
                            Integer intTem = int1;
                            intTem = int1;
                            int1 = int2;
                            int2 = intTem;
                        }
                        if (qi.getIntegerNumberValue() > int1 && qi.getIntegerNumberValue() < int2) {
                            m = true;
                        }
                    }
                    if (lng1 != null && lng2 != null && qi.getLongNumberValue() != null) {
                        if (lng1 > lng2) {
                            Long intTem = lng1;
                            intTem = lng1;
                            lng1 = lng2;
                            lng2 = intTem;
                        }
                        if (qi.getLongNumberValue() > lng1 && qi.getLongNumberValue() < lng2) {
                            m = true;
                        }
                    }
                    if (real1 != null && real2 != null && qi.getRealNumberValue() != null) {
                        if (real1 > real2) {
                            Double realTem = real1;
                            realTem = real1;
                            real1 = real2;
                            real2 = realTem;
                        }
                        if (qi.getRealNumberValue() > real1 && qi.getRealNumberValue() < real2) {
                            m = true;
                        }
                    }

                case Grater_than:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() > int1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() > real1;
                    }

                case Grater_than_or_equal:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() < int1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() < real1;
                    }
                case Less_than_or_equal:
                    if (int1 != null && qi.getIntegerNumberValue() != null) {
                        m = qi.getIntegerNumberValue() >= int1;
                    }
                    if (real1 != null && qi.getRealNumberValue() != null) {
                        m = qi.getRealNumberValue() >= real1;
                    }
            }
        }

        return m;
    }

    public Long findMatchingCount(List<Document> encs, List<QueryComponent> qrys) {

        Long c = 0l;
        for (Document e : encs) {
            boolean suitableForInclusion = true;
            for (QueryComponent q : qrys) {
                boolean thisMatchOk = false;
               
                if (!thisMatchOk) {
                    suitableForInclusion = false;
                }
            }
            if (suitableForInclusion) {
                c++;
            }
        }
        return c;
    }

    
    public String toExcelReports() {
        return "/reports/excel/index";
    }

    public String toViewClientRegistrationsByInstitution() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_system_by_ins";
        String forIns = "/reports/client_registrations/for_ins_by_ins";
        String forMe = "/reports/client_registrations/for_me_by_ins";
        String forClient = "/reports/client_registrations/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Client Registrations By Institution");
        return action;
    }

    public String toViewClinicVisitsByInstitution() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_ins_by_ins";
        String forIns = "/reports/clinic_visits/for_ins_by_ins";
        String forMe = "/reports/clinic_visits/for_ins_by_ins";
        String forClient = "/reports/clinic_visits/for_ins_by_ins";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Visits By Institution");
        return action;
    }

    public String toViewDailyClinicsVisitCounts() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_sa_daily";
        String forIns = "/reports/clinic_visits/for_ia_daily";
        String forMe = "/reports/clinic_visits/for_me_daily";
        String forClient = "/reports/index";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Daily Clinic Visits");
        return action;
    }

    public String toViewDailyClinicsRegistrationCounts() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_sa_daily";
        String forIns = "/reports/client_registrations/for_ia_daily";
        String forMe = "/reports/client_registrations/for_me_daily";
        String forClient = "/reports/index";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Daily Clinic Visits");
        return action;
    }

    public String toViewClientRegistrationsByDistrict() {
        areaCounts = null;
        areaRepCount = null;
        return "/reports/client_registrations/for_system_by_dis";
    }

    public String toViewClientRegistrationsByProvince() {
        areaCounts = null;
        areaRepCount = null;
        return "/reports/client_registrations/for_system_by_pro";
    }

    public String toViewClientRegistrations() {
        encounters = new ArrayList<>();
        String forSys = "/reports/client_registrations/for_system";
        String forIns = "/reports/client_registrations/for_ins";
        String forMe = "/reports/client_registrations/for_me";
        String forClient = "/reports/client_registrations/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Client Registrations");
        return action;
    }

    public String toViewClinicEnrollments() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_enrollments/for_system";
        String forIns = "/reports/clinic_enrollments/for_ins";
        String forMe = "/reports/clinic_enrollments/for_me";
        String forClient = "/reports/clinic_enrollments/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Enrollments");
        return action;
    }

    public String toViewClinicVisits() {
        encounters = new ArrayList<>();
        String forSys = "/reports/clinic_visits/for_system";
        String forIns = "/reports/clinic_visits/for_ins";
        String forMe = "/reports/clinic_visits/for_me";
        String forClient = "/reports/clinic_visits/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Clinic Visits");
        return action;
    }

    public String toViewDataForms() {
        String forSys = "/reports/data_forms/for_system";
        String forIns = "/reports/data_forms/for_ins";
        String forMe = "/reports/data_forms/for_me";
        String forClient = "/reports/data_forms/for_clients";
        String noAction = "";
        String action = "";
        switch (webUserController.getLoggedUser().getWebUserRole()) {
            case Client:
                action = forClient;
                break;
            case Epidemiologist:
            case Re:
            case Rdhs:
            case Pdhs:
            case Nurse:
            case ChiefEpidemiologist:
                action = forIns;
                break;
            case Phm:
            case Phi:
                action = forMe;
                break;
            case Moh:case Amoh:
            case User:
                action = noAction;
                break;
            case Super_User:
            case System_Administrator:
                action = forSys;
                break;
        }
        userTransactionController.recordTransaction("To View Data Forms");
        
        
        
        
        return action;
    }

// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Functions">
    public void fillClientRegistrationForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Client c "
                + " where c.retired=:ret "
                + " and c.reservedClient<>:res "
                + " and c.createdAt between :fd and :td ";
        m.put("ret", false);
        m.put("res", true);
        m.put("fd", fromDate);
        m.put("td", toDate);
        if (institution != null) {
            j += " and c.createInstitution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
     
    }


    public void fillRegistrationsOfClientsByInstitution() {

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.createInstitution, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
            j = j + " and c.createInstitution in :ins ";
            m.put("ins", webUserController.getLoggableInstitutions());
        }

        j = j + " group by c.createInstitution ";
        j = j + " order by c.createInstitution.name ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        institutionCounts = new ArrayList<>();
        reportCount = 0l;
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
                reportCount += ic.getCount();
            }
        }
        userTransactionController.recordTransaction("Fill Registrations Of Clients By Institution");
    }

    public void fillClinicVisitsByInstitution() {

        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(e.institution, count(e)) "
                + " from Encounter e "
                + " where e.retired<>:ret "
                + " and e.encounterType=:et ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("et", EncounterType.Test_Enrollment);
        j = j + " and e.encounterDate between :fd and :td ";

        j = j + " group by e.institution ";
        j = j + " order by e.institution.name ";
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        institutionCounts = new ArrayList<>();
        reportCount = 0l;
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                institutionCounts.add(ic);
                reportCount += ic.getCount();
            }
        }
        userTransactionController.recordTransaction("Fill Clinic Visits By Institution");
    }

    public void fillRegistrationsOfClientsByDistrict() {

        String j = "select new lk.gov.health.phsp.pojcs.AreaCount(c.createInstitution.district, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        j = j + " group by c.createInstitution.district ";
        j = j + " order by c.createInstitution.district.name ";

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        areaCounts = new ArrayList<>();
        areaRepCount = 0l;
        for (Object o : objs) {
            if (o instanceof AreaCount) {
                AreaCount ic = (AreaCount) o;
                areaCounts.add(ic);
                areaRepCount += ic.getCount();
            }
        }
    }

    public void fillRegistrationsOfClientsByProvince() {

        String j = "select new lk.gov.health.phsp.pojcs.AreaCount(c.createInstitution.province, count(c)) "
                + " from Client c "
                + " where c.retired<>:ret "
                + " and c.reservedClient<>:res ";
        Map m = new HashMap();
        m.put("ret", true);
        m.put("res", true);
        j = j + " and c.createdAt between :fd and :td ";

        j = j + " group by c.createInstitution.province ";
        j = j + " order by c.createInstitution.province.name ";

        m.put("fd", getFromDate());
        m.put("td", getToDate());
        List<Object> objs = getClientFacade().findAggregates(j, m);
        areaCounts = new ArrayList<>();
        areaRepCount = 0l;
        for (Object o : objs) {
            if (o instanceof AreaCount) {
                AreaCount ic = (AreaCount) o;
                areaCounts.add(ic);
                areaRepCount += ic.getCount();
            }
        }
    }

    public void fillClinicEnrollments() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Death);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            if (webUserController.getLoggedUser().isRestrictedToInstitution()) {
                j += " and c.institution in :ins ";
                List<Institution> ins = webUserController.getLoggableInstitutions();
                ins.add(institution);
                m.put("ins", ins);
            }
        }
        encounters = encounterController.getItems(j, m);
        userTransactionController.recordTransaction("Fill Clinic Enrollments");
    }



    public void fillClinicVisitsForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Test_Enrollment);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j, m);
    }

    public void fillClinicEnrollmentsForInstitution() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Death);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            m.put("ins", webUserController.getLoggableInstitutions());
        }
        encounters = encounterController.getItems(j, m);
    }

    public void fillClinicVisitsForInstitution() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Test_Enrollment);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        } else {
            m.put("ins", webUserController.getLoggableInstitutions());
        }
        encounters = encounterController.getItems(j, m);
    }

    public void fillEncountersForSysAdmin() {
        String j;
        Map m = new HashMap();
        j = "select c from Encounter c "
                + " where c.retired=:ret "
                + " c.encounterType=:type "
                + " and c.encounterDate between :fd and :td ";
        m.put("ret", false);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("type", EncounterType.Death);
        if (institution != null) {
            j += " and c.institution in :ins ";
            List<Institution> ins = institutionApplicationController.findChildrenInstitutions(institution);
            ins.add(institution);
            m.put("ins", ins);
        }
        encounters = encounterController.getItems(j, m);
    }

// </editor-fold>   
// <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public DocumentController getEncounterController() {
        return encounterController;
    }

  

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Document> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<Document> encounters) {
        this.encounters = encounters;
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheYear();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = new Date();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

// </editor-fold> 
    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public NcdReportTem getNcdReportTem() {
        return ncdReportTem;
    }

    public void setNcdReportTem(NcdReportTem ncdReportTem) {
        this.ncdReportTem = ncdReportTem;
    }

    public ClientEncounterComponentFormSetFacade getClientEncounterComponentFormSetFacade() {
        return clientEncounterComponentFormSetFacade;
    }

    public StreamedContent getFile() {
        return file;
    }

    public DesignComponentFormItemFacade getDesignComponentFormItemFacade() {
        return designComponentFormItemFacade;
    }

    public void setDesignComponentFormItemFacade(DesignComponentFormItemFacade designComponentFormItemFacade) {
        this.designComponentFormItemFacade = designComponentFormItemFacade;
    }

    public String getMergingMessage() {
        return mergingMessage;
    }

    public void setMergingMessage(String mergingMessage) {
        this.mergingMessage = mergingMessage;
    }

    public ClientFacade getClientFacade() {
        return clientFacade;
    }

    public void setClientFacade(ClientFacade clientFacade) {
        this.clientFacade = clientFacade;
    }

    public DocumentFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(DocumentFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public ClientEncounterComponentItemFacade getClientEncounterComponentItemFacade() {
        return clientEncounterComponentItemFacade;
    }

    public QueryComponent getQueryComponent() {
        return queryComponent;
    }

    public void setQueryComponent(QueryComponent queryComponent) {
        this.queryComponent = queryComponent;
    }

    public QueryComponentFacade getQueryComponentFacade() {
        return queryComponentFacade;
    }

    public UploadFacade getUploadFacade() {
        return uploadFacade;
    }

  
    public List<StoredQueryResult> getMyResults() {
        return myResults;
    }

    public void setMyResults(List<StoredQueryResult> myResults) {
        this.myResults = myResults;
    }

    public List<StoredQueryResult> getReportResults() {
        return reportResults;
    }

    public void setReportResults(List<StoredQueryResult> reportResults) {
        this.reportResults = reportResults;
    }

    public StoredQueryResult getRemovingResult() {
        return removingResult;
    }

    public void setRemovingResult(StoredQueryResult removingResult) {
        this.removingResult = removingResult;
    }

    public StoredQueryResult getDownloadingResult() {
        return downloadingResult;
    }

    public void setDownloadingResult(StoredQueryResult downloadingResult) {
        this.downloadingResult = downloadingResult;
    }

    public Upload getCurrentUpload() {
        return currentUpload;
    }

    public void setCurrentUpload(Upload currentUpload) {
        this.currentUpload = currentUpload;
    }

    public List<InstitutionCount> getInstitutionCounts() {
        return institutionCounts;
    }

    public void setInstitutionCounts(List<InstitutionCount> institutionCounts) {
        this.institutionCounts = institutionCounts;
    }

    public Long getReportCount() {
        return reportCount;
    }

    public void setReportCount(Long reportCount) {
        this.reportCount = reportCount;
    }

    public StreamedContent getResultExcelFile() {
        return resultExcelFile;
    }

    public void setResultExcelFile(StreamedContent resultExcelFile) {
        this.resultExcelFile = resultExcelFile;
    }

    public ConsolidatedQueryResultFacade getConsolidatedQueryResultFacade() {
        return consolidatedQueryResultFacade;
    }

    public IndividualQueryResultFacade getIndividualQueryResultFacade() {
        return individualQueryResultFacade;
    }

    public ExcelReportController getExcelReportController() {
        return excelReportController;
    }

    public void setExcelReportController(ExcelReportController excelReportController) {
        this.excelReportController = excelReportController;
    }

    public DesignComponentFormSet getDesigningComponentFormSet() {
        return designingComponentFormSet;
    }

    public void setDesigningComponentFormSet(DesignComponentFormSet designingComponentFormSet) {
        this.designingComponentFormSet = designingComponentFormSet;
    }

    public List<DesignComponentFormItem> getDesignComponentFormItems() {
        return designComponentFormItems;
    }

    public void setDesignComponentFormItems(List<DesignComponentFormItem> designComponentFormItems) {
        this.designComponentFormItems = designComponentFormItems;
    }

    public DesignComponentFormItem getDesignComponentFormItem() {
        return designComponentFormItem;
    }

    public void setDesignComponentFormItem(DesignComponentFormItem designComponentFormItem) {
        this.designComponentFormItem = designComponentFormItem;
    }

    public List<AreaCount> getAreaCounts() {
        return areaCounts;
    }

    public void setAreaCounts(List<AreaCount> areaCounts) {
        this.areaCounts = areaCounts;
    }

    public Long getAreaRepCount() {
        return areaRepCount;
    }

    public void setAreaRepCount(Long areaRepCount) {
        this.areaRepCount = areaRepCount;
    }

    public boolean isRecalculate() {
        return recalculate;
    }

    public void setRecalculate(boolean recalculate) {
        this.recalculate = recalculate;
    }

    public DesignComponentFormSet getFromSet() {
        return fromSet;
    }

    public void setFromSet(DesignComponentFormSet fromSet) {
        this.fromSet = fromSet;
    }

}
