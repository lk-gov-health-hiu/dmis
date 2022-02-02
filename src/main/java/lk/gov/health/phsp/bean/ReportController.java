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
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.DocumentType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.enums.Quarter;
import lk.gov.health.phsp.enums.TimePeriodType;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.pojcs.AreaCount;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.ReportTimePeriod;
import org.primefaces.model.StreamedContent;
// </editor-fold>   

/**
 *
 * @author hiu_pdhs_sp
 */
@Named
@SessionScoped
public class ReportController implements Serializable {
// <editor-fold defaultstate="collapsed" desc="EJBs">


    @EJB
    private DocumentFacade encounterFacade;
    @EJB
    private UploadFacade uploadFacade;
// </editor-fold>     
// <editor-fold defaultstate="collapsed" desc="Controllers">
    @Inject
    private FileController encounterController;
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
    private Date fromDate;
    private Date toDate;
    private Institution institution;
    private Area area;
    private StreamedContent file;
    private String mergingMessage;
// </editor-fold> 

// <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates a new instance of ReportController
     */
    public ReportController() {
    }

// </editor-fold> 
    private List<InstitutionCount> institutionCounts;
    private Long reportCount;
    private List<AreaCount> areaCounts;
    private Long areaRepCount;


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
        m.put("type", DocumentType.Register);
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
        m.put("type", DocumentType.Letter);
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
        m.put("type", DocumentType.Register);
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
        m.put("type", DocumentType.Letter);
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
        m.put("type", DocumentType.Register);
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
    public FileController getEncounterController() {
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

   

    public StreamedContent getFile() {
        return file;
    }

   
    public String getMergingMessage() {
        return mergingMessage;
    }

    public void setMergingMessage(String mergingMessage) {
        this.mergingMessage = mergingMessage;
    }

   

    public DocumentFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(DocumentFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

   

    public UploadFacade getUploadFacade() {
        return uploadFacade;
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

   

    public ExcelReportController getExcelReportController() {
        return excelReportController;
    }

    public void setExcelReportController(ExcelReportController excelReportController) {
        this.excelReportController = excelReportController;
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

   
}
