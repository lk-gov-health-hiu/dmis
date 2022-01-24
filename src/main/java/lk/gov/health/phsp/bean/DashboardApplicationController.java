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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import org.joda.time.DateTimeComparator;
import org.json.JSONObject;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class DashboardApplicationController {

   
    @EJB
    DocumentFacade encounterFacade;

    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    AreaApplicationController areaApplicationController;
   
//    AreaController
    @Inject
    private AreaController areaController;

    private Long todayPcr;
    private Long todayRat;
    private Long todayPositivePcr;
    private Long todayPositiveRat;
    private Long yesterdayPcr;
    private Long yesterdayRat;
    private Long yesterdayPositivePcr;
    private Long yesterdayPositiveRat;
    private Long yesterdayTests;
    private Long todaysTests;
    private Long firstContactCount;
    private Long communityRandomCount;
    private Long foreignCount;
    private Long hospitalCount;
    private Long otherCount;
    private String todayPcrPositiveRate;
    private String todayRatPositiveRate;
    private String yesterdayPcrPositiveRate;
    private String yesterdayRatPositiveRate;

//  Json data of PCR and RAT positive cases to be used to generate chart
    private JSONObject pcrPositiveCasesJSON;
    private JSONObject ratPositiveCasesJSON;

//  Round double values to two decimal format
    private static DecimalFormat df = new DecimalFormat("0.0");

    private List<InstitutionCount> orderingCounts;
   

    Item testType;
    Item orderingCat;
    Item pcr;
    Item rat;

    Boolean dashboardPrepared;

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardApplicationController() {
    }

    @PostConstruct
    public void updateDashboard() {
        
    }

  
    public List<InstitutionCount> listOrderingCategoryCounts(
            Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.pcrOrderingCategory, count(c))  "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        j += " group by c.pcrOrderingCategory";
        List<Object> objs = encounterFacade.findObjectByJpql(j, m, TemporalType.TIMESTAMP);
        List<InstitutionCount> tics = new ArrayList<>();
        for (Object o : objs) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                tics.add(ic);
            }
        }
        return tics;
    }

    // This will generate the list of investigations done by insitutions in a given RDHS area

    public Map<String, List<String>> generateRdhsInvestigationHashmap(
            List<Institution> myInstitutions
    ) {

        Map<String, List<String>> hashMap = new HashMap<>();

        Date todayStart = CommonController.startOfTheDate(CommonController.getYesterday());
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        for (Institution ins:myInstitutions) {
            System.out.println("mohArea = " + ins.getName());
            List<String> tempList = new ArrayList<>();
            Long tempTodayPcr = this.getOrderCount(
                    ins,
                    todayStart,
                    now,
                    itemApplicationController.getPcr(),
                    null,
                    null,
                    null
            );
            System.out.println("tempTodayPcr = " + tempTodayPcr);
            Long tempTodayRat = this.getOrderCount(
                    ins,
                    todayStart,
                    now,
                    itemApplicationController.getRat(),
                    null,
                    null,
                    null
            );
            System.out.println("tempTodayRat = " + tempTodayRat);
            tempList.add(tempTodayPcr.toString());
            tempList.add(tempTodayRat.toString());
            hashMap.put(ins.getName(), tempList);
        }

        return hashMap;
    }

    public Long getOrderCount(Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

//  This will return the count of investigations pending dispatch
public Long samplesAwaitingDispatch(
        Area area,
        Date fromDate,
        Date toDate,
        Institution institution,
        Item testType
) {
    Map hashMap = new HashMap();
    String jpql = "select count(c) "
            + " from Encounter c "
            + " where c.retired=:ret "
            + " and c.encounterType=:type "
            + " and c.encounterDate between :fd and :td "
            + " and c.pcrTestType=:testType ";

    if (institution != null){
        jpql += " and c.institution=:ins ";
         hashMap.put("ins", institution);
    }

    if (testType != null) {
        jpql += " and c.pcrTestType=:tt";
        hashMap.put("tt", testType);
    }

    if( area != null) {
        if (null != area.getType()) switch (area.getType()) {
            case District:
                jpql += "and c.institution.district=:district ";
                hashMap.put("district", area.getDistrict());
                break;
            case RdhsAra:
                jpql += " and (c.institution.rdhsArea=:rdArea or c.institution.district=:district) ";
                hashMap.put("rdArea", area);
                hashMap.put("district", area.getDistrict());
                break;
            case PdhsArea:
                jpql += " and (c.institution.pdhsArea=:pdArea or c.institution.province=:province) ";
                hashMap.put("phArea", area);
                hashMap.put("province", area.getProvince());
                break;
            case Province:
                jpql += " and c.institution.province=:province ";
                hashMap.put("province", area.getProvince());
                break;
            case MOH:
                jpql += " and (c.institution.mohArea=:mohArea) ";
                hashMap.put("mohArea", area);
                break;
            default:
                break;
        }
    }



    jpql += " and (c.sentToLab is null or c.sentToLab = :sl) ";

    hashMap.put("ret", false);
    hashMap.put("type", DocumentType.Letter);
    hashMap.put("fd", fromDate);
    hashMap.put("sl", false);
    hashMap.put("td", toDate);
    hashMap.put("testType", testType);


    return encounterFacade.findLongByJpql(jpql, hashMap, TemporalType.DATE);

}

//This function will return a series of cases depending on a provided time period
public Map<String, String> getSeriesOfCases(
        Date fromDate,
        int duration,
        Item testType,
        Item result
) {
    int MILLIS_IN_A_DAY = 1000 * 60 * 60 * 24;

    Map<String, String> hashMap = new HashMap<String, String>();
    Date startOfTheDate = CommonController.startOfTheDate(fromDate);

    if (duration < 1) {
        hashMap.put("", "");
    } else {
        for (int i = 0; i <= duration; i++) {
            Date currentDate = new Date(startOfTheDate.getTime() - (long) MILLIS_IN_A_DAY * i);
            Date endDate = CommonController.endOfTheDate(currentDate);
            Long positive_cases = this.getConfirmedCount(
                    null,
                    currentDate,
                    endDate,
                    testType,
                    null,
                    result,
                    null);
            hashMap.put(currentDate.toString(), Long.toString(positive_cases));
        }
    }
    return hashMap;
}

//  This will return count of Investigations where MOH area is not given
    public Long getOrderCountWithoutMoh(Area area,
                              Date fromDate,
                              Date toDate,
                              Item testType,
                              Item orderingCategory,
                              Item result,
                              Institution lab) {

        Map hashMap = new HashMap();

        String jpql = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";

        hashMap.put("ret", false);

        jpql += " and c.encounterType=:etype ";

        hashMap.put("etype", DocumentType.Letter);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                jpql += " and (c.institution.rdhsArea=:area or c.institution.district=:dis) ";
                hashMap.put("area", area);
                hashMap.put("dis", area.getDistrict());
            } else if (area.getType() == AreaType.Province) {
                jpql += " and (c.institution.pdhsArea=:area or c.institution.province=:pro) ";
                hashMap.put("area", area);
                hashMap.put("pro", area.getProvince());
            }
            jpql += " and (c.client.person.mohArea=null) ";
        }

        jpql += " and c.createdAt between :fd and :td ";
        hashMap.put("fd", fromDate);
        hashMap.put("td", toDate);

        if (testType != null) {
            jpql += " and c.pcrTestType=:tt ";
            hashMap.put("tt", testType);
        }
        if (orderingCategory != null) {
            jpql += " and c.pcrOrderingCategory=:oc ";
            hashMap.put("oc", orderingCategory);
        }
        if (result != null) {
            jpql += " and c.pcrResult=:result ";
            hashMap.put("result", result);
        }
        if (lab != null) {
            jpql += " and c.referalInstitution=:ri ";
            hashMap.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(jpql, hashMap, TemporalType.TIMESTAMP);
    }

    public Long getOrderCountArea(Area area,
            Date fromDate,
            Date toDate,
            Item pcrOrRat,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                j += " and (c.institution.rdhsArea=:area or c.institution.district=:dis) ";
                m.put("area", area);
                m.put("dis", area.getDistrict());
            } else if (area.getType() == AreaType.Province) {
                j += " and (c.institution.pdhsArea=:area or c.institution.province=:pro) ";
                m.put("area", area);
                m.put("pro", area.getProvince());
            } else if (area.getType() == AreaType.MOH) {
                j += " and (c.institution.mohArea=:area) ";
                m.put("area", area);
            }
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (pcrOrRat != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", pcrOrRat);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getProvincialOrderCountArea(Area pdArea,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (pdArea != null) {
            if (pdArea.getType() == AreaType.RdhsAra) {
                j += " and c.institution.rdhsArea=:area ";
                m.put("area", pdArea);
            } else if (pdArea.getType() == AreaType.Province) {
                j += " and c.institution.pdhsArea=:area ";
                m.put("area", pdArea);
            }
        }

        j += " and c.createdAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCountByInstitution(Institution ins,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (ins != null) {
            j += " and c.institution=:ins ";
            m.put("ins", ins);
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCount(Area area,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (area != null && area.getType() != null) {
            if (null != area.getType()) {
                switch (area.getType()) {
                    case District:
                        j += " and c.client.person.district=:dis ";
                        m.put("dis", area);
                        break;
                    case Province:
                        j += " and c.client.person.district.province=:pro ";
                        m.put("pro", area);
                        break;
                    case MOH:
                        j += " and c.client.person.mohArea=:moh ";
                        m.put("moh", area);
                        break;
                    default:
                        break;
                }
            }
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getConfirmedCountArea(Area area,
            Date fromDate,
            Date toDate,
            Item testType,
            Item orderingCategory,
            Item result,
            Institution lab) {
        Map m = new HashMap();
        String j = "select count(c) "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        if (area != null) {
            if (area.getType() == AreaType.RdhsAra) {
                j += " and c.institution.rdhsArea=:area ";
                m.put("area", area);
            } else if (area.getType() == AreaType.Province) {
                j += " and c.institution.pdhsArea=:area ";
                m.put("area", area);
            }
        }

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", fromDate);
        m.put("td", toDate);

        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        if (lab != null) {
            j += " and c.referalInstitution=:ri ";
            m.put("ri", lab);
        }
        return encounterFacade.findLongByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long getPositivePcr(Date fd, Date td) {
        String j = "select count(e "
                + " from Encounter e "
                + " where (e.retired is null or e.retired=false) "
                + " and e.pcrTestType=:pcr "
                + " and e.resultConfirmedAt between :fd and :td "
                + " and e.pcrResult=:pos ";
        Map m;
        m = new HashMap();
        m.put("fd", fd);
        m.put("td", td);
        m.put("pos", itemApplicationController.getPcrPositive());
        m.put("pcr", itemApplicationController.getPcr());
        return encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Boolean getDashboardPrepared() {
        if (dashboardPrepared == null) {
            updateDashboard();
            dashboardPrepared = true;
        }
        return dashboardPrepared;
    }

    public Long getTodayPcr() {
        if (todayPcr == null) {
            updateDashboard();
        }
        return todayPcr;
    }

    public Long getTodayRat() {
        if (todayRat == null) {
            updateDashboard();
        }
        return todayRat;
    }

    public Long getTodayPositivePcr() {
        if (todayPositivePcr == null) {
            updateDashboard();
        }
        return todayPositivePcr;
    }

    public Long getTodayPositiveRat() {
        if (todayPositiveRat == null) {
            updateDashboard();
        }
        return todayPositiveRat;
    }

//    Getter for cases JSON data
    public JSONObject getPcrPositiveCasesJSON() {
        return pcrPositiveCasesJSON;
    }

    public JSONObject getRatPositiveCasesJSON() {
        return ratPositiveCasesJSON;
    }

    public Long getYesterdayPcr() {
        if (yesterdayPcr == null) {
            updateDashboard();
        }
        return yesterdayPcr;
    }




    /**
	 * @return the todayPcrPositiveRate
	 */
	public String getTodayPcrPositiveRate() {
		if (this.todayPcr == null) {
			this.updateDashboard();
		}
		return todayPcrPositiveRate;
	}

	/**
	 * @param todayPcrPositiveRate the todayPcrPositiveRate to set
	 */
	public void setTodayPcrPositiveRate(String todayPcrPositiveRate) {
		this.todayPcrPositiveRate = todayPcrPositiveRate;
	}

	/**
	 * @return the todayRatPositiveRate
	 */
	public String getTodayRatPositiveRate() {
		if (this.todayRat == null) {
			this.updateDashboard();
		}
		return todayRatPositiveRate;
	}

	/**
	 * @param todayRatPositiveRate the todayRatPositiveRate to set
	 */
	public void setTodayRatPositiveRate(String todayRatPositiveRate) {
		this.todayRatPositiveRate = todayRatPositiveRate;
	}

	/**
	 * @return the yesterdayPcrPositiveRate
	 */
	public String getYesterdayPcrPositiveRate() {
		if (this.yesterdayPcr == null) {
			this.updateDashboard();
		}
		return yesterdayPcrPositiveRate;
	}

	/**
	 * @param yesterdayPcrPositiveRate the yesterdayPcrPositiveRate to set
	 */
	public void setYesterdayPcrPositiveRate(String yesterdayPcrPositiveRate) {
		this.yesterdayPcrPositiveRate = yesterdayPcrPositiveRate;
	}

	/**
	 * @return the yesterdayRatPositiveRate
	 */
	public String getYesterdayRatPositiveRate() {
		if (this.yesterdayRat == null) {
			this.updateDashboard();
		}
		return yesterdayRatPositiveRate;
	}

	/**
	 * @param yesterdayRatPositiveRate the yesterdayRatPositiveRate to set
	 */
	public void setYesterdayRatPositiveRate(String yesterdayRatPositiveRate) {
		this.yesterdayRatPositiveRate = yesterdayRatPositiveRate;
	}

	public void setYesterdayPcr(Long yesterdayPcr) {
        this.yesterdayPcr = yesterdayPcr;
    }

    public Long getYesterdayRat() {
        if (yesterdayRat == null) {
            updateDashboard();
        }
        return yesterdayRat;
    }

    public void setYesterdayRat(Long yesterdayRat) {
        this.yesterdayRat = yesterdayRat;
    }

    public Long getYesterdayPositivePcr() {
        if (yesterdayPositivePcr == null) {
            updateDashboard();
        }
        return yesterdayPositivePcr;
    }

    public void setYesterdayPositivePcr(Long yesterdayPositivePcr) {
        this.yesterdayPositivePcr = yesterdayPositivePcr;
    }

    public Long getYesterdayPositiveRat() {
        if (yesterdayPositiveRat == null) {
            updateDashboard();
        }
        return yesterdayPositiveRat;
    }

    public void setYesterdayPositiveRat(Long yesterdayPositiveRat) {
        this.yesterdayPositiveRat = yesterdayPositiveRat;
    }

    public Long getYesterdayTests() {
        if (getYesterdayPcr() != null && getYesterdayRat() != null) {
            yesterdayTests = getYesterdayPcr() + getYesterdayRat();
        } else if (getYesterdayPcr() != null) {
            yesterdayTests = getYesterdayPcr();
        } else if (getYesterdayRat() != null) {
            yesterdayTests = getYesterdayRat();
        } else {
            yesterdayTests = 0l;
        }
        return yesterdayTests;
    }

    public Long getTodaysTests() {
        if (getTodayPcr() != null && getTodayRat() != null) {
            todaysTests = getTodayPcr() + getTodayRat();
        } else if (getTodayPcr() != null) {
            todaysTests = getTodayPcr();
        } else if (getTodayRat() != null) {
            todaysTests = getTodayRat();
        } else {
            todaysTests = 0l;
        }
        return todaysTests;
    }

    public void setTodaysTests(Long todaysTests) {
        this.todaysTests = todaysTests;
    }

    public List<InstitutionCount> getOrderingCounts() {
        if (orderingCounts == null) {
            updateDashboard();
        }
        return orderingCounts;
    }

    public void setOrderingCounts(List<InstitutionCount> orderingCounts) {
        this.orderingCounts = orderingCounts;
    }

    public Long getFirstContactCount() {
        return firstContactCount;
    }

    public Long getCommunityRandomCount() {
        return communityRandomCount;
    }

    public Long getForeignCount() {
        return foreignCount;
    }

    public Long getHospitalCount() {
        return hospitalCount;
    }

    public Long getOtherCount() {
        return otherCount;
    }

   
    public List<InstitutionCount> countOfResultsByGnArea(Area moh,
            Date from,
            Date to,
            Item orderingCategory,
            Item result,
            Integer numberOfResults
            ) {
        List<InstitutionCount> ics ;
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.gnArea, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);

        j += " and (c.client.person.mohArea=:moh) ";
        m.put("moh",moh);

        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", from);
        m.put("td", to);
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        j += " group by c.client.person.gnArea"
                + " order by count(c) desc ";

        ics = new ArrayList<>();

        List<Object> objCounts;
        if(numberOfResults!=null){
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP,numberOfResults);
        }else{
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        }

        if (objCounts == null || objCounts.isEmpty()) {
            return ics;
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ics.add(ic);
            }
        }
        return ics;
    }

    public List<InstitutionCount> countOfResultsByProvince(
            Date from,
            Date to,
            Item orderingCategory,
            Item result,
            Integer numberOfResults
            ) {
        List<InstitutionCount> ics ;
        Map m = new HashMap();
        String j = "select new lk.gov.health.phsp.pojcs.InstitutionCount(c.client.person.district.province, c.institution, count(c))   "
                + " from Encounter c "
                + " where (c.retired is null or c.retired=:ret) ";
        m.put("ret", false);
        j += " and c.encounterType=:etype ";
        m.put("etype", DocumentType.Letter);
        j += " and c.resultConfirmedAt between :fd and :td ";
        m.put("fd", from);
        m.put("td", to);
        if (testType != null) {
            j += " and c.pcrTestType=:tt ";
            m.put("tt", testType);
        }
        if (orderingCategory != null) {
            j += " and c.pcrOrderingCategory=:oc ";
            m.put("oc", orderingCategory);
        }
        if (result != null) {
            j += " and c.pcrResult=:result ";
            m.put("result", result);
        }
        j += " group by c.client.person.district.province "
                + " order by count(c) desc ";

        ics = new ArrayList<>();

        List<Object> objCounts;
        if(numberOfResults!=null){
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP,numberOfResults);
        }else{
            objCounts = encounterFacade.findAggregates(j, m, TemporalType.TIMESTAMP);
        }

        if (objCounts == null || objCounts.isEmpty()) {
            return ics;
        }
        for (Object o : objCounts) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                ics.add(ic);
            }
        }
        return ics;
    }

}
