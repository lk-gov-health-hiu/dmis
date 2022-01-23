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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.ejb.CovidDataHolder;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.NumbersFacade;
import lk.gov.health.phsp.pojcs.CovidData;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import org.json.JSONObject;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class DashboardController implements Serializable {

    @EJB
    private NumbersFacade numbersFacade;
    @EJB
    private DocumentFacade encounterFacade;
    @EJB
    private CovidDataHolder covidDataHolder;

    @Inject
    private DocumentController encounterController;
       @Inject
    private ItemController itemController;
    @Inject
    private DashboardApplicationController dashboardApplicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private ItemApplicationController itemApplicationController;

    private Date fromDate;
    private Date toDate;
    private List<InstitutionCount> ics;

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
    private String todayPcrPositiveRate;
    private String todayRatPositiveRate;
    private String yesterdayPcrPositiveRate;
    private String yesterdayRatPositiveRate;
//    PCR positive patients with no MOH are
    private Long pcrPatientsWithNoMohArea;
//    RAT positive patients with no MOH area
    private Long ratPatientsWithNoMohArea;
//    First encounters with no MOH area
    private Long firstContactsWithNoMOHArea;
//    HashMap to generate investigation chart at MOH dashboard
    private JSONObject investigationHashmap;


    private CovidData myCovidData;

    private List<CovidData> covidDatasForMohs;
    private List<CovidData> covidDatasForAreas;
    private List<CovidData> covidDatasForLabs;
    private List<CovidData> covidDatasForHospitals;
    private List<CovidData> covidDatasForRdhs;
    private List<CovidData> covidDatasForPdhs;
    private List<CovidData> covidDatasForCountry;

    private Long samplesToReceive;
    private Long samplesReceived;
    private Long samplesRejected;
    private Long samplesResultEntered;
    private Long samplesResultReviewed;
    private Long samplesResultsConfirmed;
    private Long samplesPositive;
//    Samples awaiting dispatch
    private Long samplesAwaitingDispatch;

//    Uses to convert doubles to rounded string value
    DecimalFormat df = new DecimalFormat("0.00");

    private List<InstitutionCount> orderingCategories;

    public String toContactNational() {
        orderingCategories = new ArrayList<>();
        for (InstitutionCount oc : dashboardApplicationController.getOrderingCounts()) {
            String code = "";
            if (oc.getItemValue() != null && oc.getItemValue().getCode() != null) {
                code = oc.getItemValue().getCode();
            }
            switch (code) {
                case "exit_for_first_contacts":
                case "first_contact_non_exit":
                    orderingCategories.add(oc);
                    break;
                case "community_screening_random":
                    break;
                case "overseas_returnees_and_foreign_travelers_initial_or_arrival":
                case "overseas_returnees_and_foreign_travelers_exit":
                    break;
                case "routine_for_procedures":
                case "opd_symptomatic":
                case "opd_inward_symptomatic":
                    break;
                case "postmortem_screening":
                case "workplace_random":
                case "workplace_routine":
                case "covid_19_test_ordering_category_other":
                case "":
                    break;
            }
        }
        return "/national/ordering_categories";
    }

    public String toCommunityRandomNational() {
        orderingCategories = new ArrayList<>();
        for (InstitutionCount oc : dashboardApplicationController.getOrderingCounts()) {
            String code = "";
            if (oc.getItemValue() != null && oc.getItemValue().getCode() != null) {
                code = oc.getItemValue().getCode();
            }
            switch (code) {
                case "exit_for_first_contacts":
                case "first_contact_non_exit":
                    break;
                case "community_screening_random":
                case "workplace_random":
                    orderingCategories.add(oc);
                    break;
                case "overseas_returnees_and_foreign_travelers_initial_or_arrival":
                case "overseas_returnees_and_foreign_travelers_exit":
                    break;
                case "routine_for_procedures":
                case "opd_symptomatic":
                case "opd_inward_symptomatic":
                    break;
                case "postmortem_screening":
                case "workplace_routine":
                case "covid_19_test_ordering_category_other":
                case "":
                    break;
            }
        }
        return "/national/ordering_categories";
    }

    public String toForeign() {
        orderingCategories = new ArrayList<>();
        for (InstitutionCount oc : dashboardApplicationController.getOrderingCounts()) {
            String code = "";
            if (oc.getItemValue() != null && oc.getItemValue().getCode() != null) {
                code = oc.getItemValue().getCode();
            }
            switch (code) {
                case "exit_for_first_contacts":
                case "first_contact_non_exit":
                    break;
                case "community_screening_random":
                case "workplace_random":
                    break;
                case "overseas_returnees_and_foreign_travelers_initial_or_arrival":
                case "overseas_returnees_and_foreign_travelers_exit":
                    orderingCategories.add(oc);
                    break;
                case "routine_for_procedures":
                case "opd_symptomatic":
                case "opd_inward_symptomatic":
                    break;
                case "postmortem_screening":
                case "workplace_routine":
                case "covid_19_test_ordering_category_other":
                case "":
                    break;
            }
        }
        return "/national/ordering_categories";
    }

    public String toHospital() {
        orderingCategories = new ArrayList<>();
        for (InstitutionCount oc : dashboardApplicationController.getOrderingCounts()) {
            String code = "";
            if (oc.getItemValue() != null && oc.getItemValue().getCode() != null) {
                code = oc.getItemValue().getCode();
            }
            switch (code) {
                case "exit_for_first_contacts":
                case "first_contact_non_exit":
                    break;
                case "community_screening_random":
                case "workplace_random":
                    break;
                case "overseas_returnees_and_foreign_travelers_initial_or_arrival":
                case "overseas_returnees_and_foreign_travelers_exit":

                    break;
                case "routine_for_procedures":
                case "opd_symptomatic":
                case "opd_inward_symptomatic":
                    orderingCategories.add(oc);
                    break;
                case "postmortem_screening":
                case "workplace_routine":
                case "covid_19_test_ordering_category_other":
                case "":
                    break;
            }
        }
        return "/national/ordering_categories";
    }

    public String toOtherOrderingCategory() {
        orderingCategories = new ArrayList<>();
        for (InstitutionCount oc : dashboardApplicationController.getOrderingCounts()) {
            String code = "";
            if (oc.getItemValue() != null && oc.getItemValue().getCode() != null) {
                code = oc.getItemValue().getCode();
            }
            switch (code) {
                case "exit_for_first_contacts":
                case "first_contact_non_exit":
                    break;
                case "community_screening_random":
                case "workplace_random":
                    break;
                case "overseas_returnees_and_foreign_travelers_initial_or_arrival":
                case "overseas_returnees_and_foreign_travelers_exit":
                    break;
                case "routine_for_procedures":
                case "opd_symptomatic":
                case "opd_inward_symptomatic":
                    break;
                case "postmortem_screening":
                case "workplace_routine":
                case "covid_19_test_ordering_category_other":
                case "":
                    orderingCategories.add(oc);
                    break;
            }
        }
        return "/national/ordering_categories";
    }

    public void prepareMohDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        todayPcr = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        todayRat = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        yesterdayPcr = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        todayPositivePcr = dashboardApplicationController.getConfirmedCount(webUserController.getLoggedInstitution().getMohArea(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        todayPositiveRat = dashboardApplicationController.getConfirmedCount(webUserController.getLoggedInstitution().getMohArea(),
                todayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

        yesterdayPositivePcr = dashboardApplicationController.getConfirmedCount(webUserController.getLoggedInstitution().getMohArea(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        yesterdayPositiveRat = dashboardApplicationController.getConfirmedCount(webUserController.getLoggedInstitution().getMohArea(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

//      Calculate today's positive PCR percentage
        if (this.todayPcr != 0 ) {
        	double tempRate = ((double)this.todayPositivePcr/this.todayPcr) * 100;
        	this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayPcrPositiveRate = "0.00%";
        }
//      Calculate today's RAT percentage
        if (this.todayRat != 0 ) {
        	double tempRate = ((double) this.todayPositiveRat/this.todayRat) * 100;
        	this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayRatPositiveRate = "0.00%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.yesterdayPcr != 0 ) {
        	double tempRate = ((double) this.yesterdayPositivePcr/this.yesterdayPcr) * 100;
        	this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayPcrPositiveRate = "0.00%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0 ) {
        	double tempRate = ((double) this.yesterdayPositiveRat/this.yesterdayRat) * 100;
        	this.yesterdayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayRatPositiveRate = "0.00%";
        }

//      Get samples awaiting dispatch at MOH level to be shown on the dashboard
        this.samplesAwaitingDispatch = dashboardApplicationController.samplesAwaitingDispatch(
                this.webUserController.getLoggedUser().getInstitution().getMohArea(),
                yesterdayStart,
                now,
                null,
                itemApplicationController.getPcr()
        );
    }

    public void prepareHospitalDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        todayPcr = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        todayRat = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        yesterdayPcr = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        todayPositivePcr = dashboardApplicationController.getConfirmedCountByInstitution(webUserController.getLoggedInstitution(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        todayPositiveRat = dashboardApplicationController.getConfirmedCountByInstitution(webUserController.getLoggedInstitution(),
                todayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

        yesterdayPositivePcr = dashboardApplicationController.getConfirmedCountByInstitution(webUserController.getLoggedInstitution(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        yesterdayPositiveRat = dashboardApplicationController.getConfirmedCountByInstitution(webUserController.getLoggedInstitution(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

//      Calculate today's positive PCR percentage
        if (this.todayPcr != 0 ) {
        	double tempRate = ((double) this.todayPositivePcr/this.todayPcr) * 100;
        	this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayPcrPositiveRate = "0.0%";
        }
//      Calculate today's RAT percentage
        if (this.todayRat != 0) {
        	double tempRate = ((double) this.todayPositiveRat/this.todayRat) * 100;
        	this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayRatPositiveRate = "0.0%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.yesterdayPcr != 0) {
        	double tempRate = ((double) this.yesterdayPositivePcr/this.yesterdayPcr) * 100;
        	this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayPcrPositiveRate = "0.0%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0) {
        	double tempRate = ((double) this.yesterdayPositiveRat/this.yesterdayRat) * 100;
        	this.yesterdayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayRatPositiveRate = "0.0%";
        }

    }

    public void prepareRegionalDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        if (webUserController.getLoggedInstitution().getRdhsArea() == null) {
            JsfUtil.addErrorMessage("RDHS is not properly set. Please inform the support team. Dashboard will not be prepared.");
            return;
        }

        todayPcr = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        todayRat = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        yesterdayPcr = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        todayPositivePcr = dashboardApplicationController.getConfirmedCount(
                webUserController.getLoggedInstitution().getRdhsArea().getDistrict(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        todayPositiveRat = dashboardApplicationController.getConfirmedCount(
                webUserController.getLoggedInstitution().getRdhsArea().getDistrict(),
                todayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

        yesterdayPositivePcr = dashboardApplicationController.getConfirmedCount(
                webUserController.getLoggedInstitution().getRdhsArea().getDistrict(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        yesterdayPositiveRat = dashboardApplicationController.getConfirmedCount(
                webUserController.getLoggedInstitution().getRdhsArea().getDistrict(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

//      Set patients with no MOH area for last two days
        Long tmepPcrPatientsWithNoMohArea = dashboardApplicationController.getOrderCountWithoutMoh(
            webUserController.getLoggedInstitution().getRdhsArea(),
            yesterdayStart,
            now,
            itemApplicationController.getPcr(),
            null,
            itemApplicationController.getPcrPositive(),
            null
        );

        if (tmepPcrPatientsWithNoMohArea == null) {
            this.pcrPatientsWithNoMohArea = 0L;
        } else {
            this.pcrPatientsWithNoMohArea = tmepPcrPatientsWithNoMohArea;
        }

//      Set RAT positive patients with no MOH area for the last two days
        Long tempRatPatientsWithNoMohArea = dashboardApplicationController.getOrderCountWithoutMoh(
                webUserController.getLoggedInstitution().getRdhsArea(),
                yesterdayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null
        );

        if (tempRatPatientsWithNoMohArea == null) {
            this.ratPatientsWithNoMohArea = 0L;
        } else {
            this.ratPatientsWithNoMohArea = tempRatPatientsWithNoMohArea;
        }

//        Set first encounters for the last two days with no MOH area
//        Set samples awaiting dispatch
        this.samplesAwaitingDispatch = dashboardApplicationController.samplesAwaitingDispatch(
                this.webUserController.getLoggedInstitution().getRdhsArea(),
                yesterdayStart,
                now,
                null,
                itemApplicationController.getPcr()
        );

//      Calculate today's positive PCR percentage
        if (this.todayPcr != 0) {
        	double tempRate = ((double) this.todayPositivePcr/this.todayPcr) * 100;
        	this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayPcrPositiveRate = "0.00%";
        }
//      Calculate today's RAT percentage
        if (this.todayRat != 0) {
        	double tempRate = ((double) this.todayPositiveRat/this.todayRat) * 100;
        	this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayRatPositiveRate = "0.00%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.yesterdayPcr != 0) {
        	double tempRate = ((double) this.yesterdayPositivePcr/this.yesterdayPcr) * 100;
        	this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayPcrPositiveRate = "0.00%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0) {
        	double tempRate = ((double) this.yesterdayPositiveRat/this.yesterdayRat) * 100;
        	this.yesterdayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayRatPositiveRate = "0.00%";
        }

        // The json is used to generate chart for available insitutions in a given RDHS area
        this.investigationHashmap = new JSONObject(dashboardApplicationController.generateRdhsInvestigationHashmap(
                this.webUserController.getLoggableInstitutions()
        ));

    }

    public void prepareProvincialDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        if (webUserController.getLoggedInstitution().getPdhsArea() == null) {
            JsfUtil.addErrorMessage("Province is not set. Please inform the support team. Dashboard will not be prepared.");
            return;
        }

        todayPcr = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        todayRat = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        yesterdayPcr = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        todayPositivePcr = dashboardApplicationController.getConfirmedCountArea(
                webUserController.getLoggedInstitution().getPdhsArea(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        todayPositiveRat = dashboardApplicationController.getConfirmedCountArea(
                webUserController.getLoggedInstitution().getPdhsArea(),
                todayStart,
                now,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

        yesterdayPositivePcr = dashboardApplicationController.getConfirmedCountArea(
                webUserController.getLoggedInstitution().getPdhsArea(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        yesterdayPositiveRat = dashboardApplicationController.getConfirmedCountArea(
                webUserController.getLoggedInstitution().getPdhsArea(),
                yesterdayStart,
                yesterdayEnd,
                itemApplicationController.getRat(),
                null,
                itemApplicationController.getPcrPositive(),
                null);

//      Calculate today's positive PCR percentage
        if (this.todayPcr != 0 || this.todayPcr != null) {
        	double tempRate = ((double) this.todayPositivePcr/this.todayPcr) * 100;
        	this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayPcrPositiveRate = "0.00%";
        }
//      Calculate today's RAT percentage
        if (this.todayRat != 0 || this.todayRat != null) {
        	double tempRate = ((double) this.todayPositiveRat/this.todayRat) * 100;
        	this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.todayRatPositiveRate = "0.00%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.yesterdayPcr != 0 || this.yesterdayPcr != null) {
        	double tempRate = ((double) this.yesterdayPositivePcr/this.yesterdayPcr) * 100;
        	this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayPcrPositiveRate = "0.00%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0 || this.yesterdayRat != null) {
        	double tempRate = ((double) this.yesterdayPositiveRat/this.yesterdayRat) * 100;
        	this.yesterdayRatPositiveRate = df.format(tempRate) + "%";
        } else {
        	this.yesterdayRatPositiveRate = "0.00%";
        }
    }

    public void prepareLabDashboard() {
        String j;
        Map m;

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.sentToLabAt between :fd and :td "
                + " and e.receivedAtLab is null "
                + " and e.referalInstitution=:lab";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        samplesToReceive = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.sampledAt between :fd and :td "
                + " and e.referalInstitution=:lab";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        samplesReceived = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.resultEnteredAt between :fd and :td "
                + " and e.referalInstitution=:lab";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        samplesResultEntered = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.resultEnteredAt between :fd and :td "
                + " and e.referalInstitution=:lab";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        samplesResultEntered = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.resultReviewedAt between :fd and :td "
                + " and e.referalInstitution=:lab";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        samplesResultReviewed = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.resultConfirmedAt between :fd and :td "
                + " and e.referalInstitution=:lab";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        samplesResultsConfirmed = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

        j = "select count(e) "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.resultConfirmedAt between :fd and :td "
                + " and e.referalInstitution=:lab "
                + " and e.pcrResult=:pos";
        m = new HashMap();
        m.put("fd", CommonController.startOfTheDate(fromDate));
        m.put("td", CommonController.endOfTheDate(toDate));
        m.put("lab", webUserController.getLoggedInstitution());
        m.put("pos", itemApplicationController.getPcrPositive());
        samplesPositive = encounterFacade.countByJpql(j, m, TemporalType.TIMESTAMP);

    }

    public String toCalculateNumbers() {
        return "/systemAdmin/calculate_numbers";
    }

    

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardController() {
    }

    //    Generates a hashmap that will give PCR and RAT investigations of each MOH under a given RDHS area


    public void calculateNumbers() {
        covidDataHolder.calculateNumbers(fromDate, toDate);
    }

    public NumbersFacade getNumbersFacade() {
        return numbersFacade;
    }

    public void setNumbersFacade(NumbersFacade numbersFacade) {
        this.numbersFacade = numbersFacade;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public DocumentFacade getEncounterFacade() {
        return encounterFacade;
    }

    public void setEncounterFacade(DocumentFacade encounterFacade) {
        this.encounterFacade = encounterFacade;
    }

    public DocumentController getEncounterController() {
        return encounterController;
    }

    public void setEncounterController(DocumentController encounterController) {
        this.encounterController = encounterController;
    }

    public List<InstitutionCount> getIcs() {
        return ics;
    }

    public void setIcs(List<InstitutionCount> ics) {
        this.ics = ics;
    }

    public List<CovidData> getCovidDatasForMohs() {
        covidDatasForMohs = covidDataHolder.getCovidDatasForMohs();
        return covidDatasForMohs;
    }

    public List<CovidData> getCovidDatasForAreas() {
        return covidDatasForAreas;
    }

    public List<CovidData> getCovidDatasForLabs() {
        return covidDatasForLabs;
    }

    public List<CovidData> getCovidDatasForHospitals() {
        return covidDatasForHospitals;
    }

    public List<CovidData> getCovidDatasForRdhs() {
        return covidDatasForRdhs;
    }

    public List<CovidData> getCovidDatasForPdhs() {
        return covidDatasForPdhs;
    }

    public List<CovidData> getCovidDatasForCountry() {
        return covidDatasForCountry;
    }

    public CovidDataHolder getCovidDataHolder() {
        return covidDataHolder;
    }

    public void setCovidDataHolder(CovidDataHolder covidDataHolder) {
        this.covidDataHolder = covidDataHolder;
    }

    public ItemController getItemController() {
        return itemController;
    }

    public void setItemController(ItemController itemController) {
        this.itemController = itemController;
    }

    public DashboardApplicationController getDashboardApplicationController() {
        return dashboardApplicationController;
    }

    public void setDashboardApplicationController(DashboardApplicationController dashboardApplicationController) {
        this.dashboardApplicationController = dashboardApplicationController;
    }

    public Long getSamplesReceived() {
        return samplesReceived;
    }

    public void setSamplesReceived(Long samplesReceived) {
        this.samplesReceived = samplesReceived;
    }

    public Long getSamplesRejected() {
        return samplesRejected;
    }

    public void setSamplesRejected(Long samplesRejected) {
        this.samplesRejected = samplesRejected;
    }

    public Long getSamplesResultEntered() {
        return samplesResultEntered;
    }

    public void setSamplesResultEntered(Long samplesResultEntered) {
        this.samplesResultEntered = samplesResultEntered;
    }

    public Long getSamplesResultReviewed() {
        return samplesResultReviewed;
    }

    public void setSamplesResultReviewed(Long samplesResultReviewed) {
        this.samplesResultReviewed = samplesResultReviewed;
    }

    public Long getSamplesResultsConfirmed() {
        return samplesResultsConfirmed;
    }

    public void setSamplesResultsConfirmed(Long samplesResultsConfirmed) {
        this.samplesResultsConfirmed = samplesResultsConfirmed;
    }

    public Long getSamplesPositive() {
        return samplesPositive;
    }

    public void setSamplesPositive(Long samplesPositive) {
        this.samplesPositive = samplesPositive;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public ItemApplicationController getItemApplicationController() {
        return itemApplicationController;
    }

    public void setItemApplicationController(ItemApplicationController itemApplicationController) {
        this.itemApplicationController = itemApplicationController;
    }

    public Long getSamplesToReceive() {
        return samplesToReceive;
    }

    public void setSamplesToReceive(Long samplesToReceive) {
        this.samplesToReceive = samplesToReceive;
    }

    public Long getTodayPcr() {
        if (todayPcr == null) {
            prepareMohDashboard();
        }
        return todayPcr;
    }

    public Long getTodayRat() {
        if (todayRat == null) {
            prepareMohDashboard();
        }
        return todayRat;
    }

    public Long getTodayPositivePcr() {
        if (todayPositivePcr == null) {
            prepareMohDashboard();
        }
        return todayPositivePcr;
    }

    public Long getTodayPositiveRat() {
        if (todayPositiveRat == null) {
            prepareMohDashboard();
        }
        return todayPositiveRat;
    }

    public Long getYesterdayPcr() {
        if (yesterdayPcr == null) {
            prepareMohDashboard();
        }
        return yesterdayPcr;
    }

    public void setYesterdayPcr(Long yesterdayPcr) {
        this.yesterdayPcr = yesterdayPcr;
    }


    /**
	 * @return the todayPcrPositiveRate
	 */
	public String getTodayPcrPositiveRate() {
		if (this.todayPcr == null) {
			this.prepareMohDashboard();
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
			this.prepareMohDashboard();
		}
		return todayRatPositiveRate;
	}

//    Getter and setter for patients with no moh area
    public Long getPcrPatientsWithNoMohArea() {
        return this.pcrPatientsWithNoMohArea;
    }
//  getter for rat patients with no moh area
    public Long getRatPatientsWithNoMohArea() {
        return this.ratPatientsWithNoMohArea;
    }

    public Long getFirstContactsWithNoMOHArea() {
        return firstContactsWithNoMOHArea;
    }

    public Long getSamplesAwaitingDispatch() {
        return samplesAwaitingDispatch;
    }

    /**
	 * @param todayRatPositiveRate the todayRatPositiveRate to set
	 */
	public void setTodayRatPositiveRate(String todayRatPositiveRate) {
		this.todayRatPositiveRate = todayRatPositiveRate;
	}

//	Getter for the mohInstegiationHashmap

    public JSONObject getInvestigationHashmap() {
        return investigationHashmap;
    }

    /**
	 * @return the yesterdayPcrPositiveRate
	 */
	public String getYesterdayPcrPositiveRate() {
		if (this.yesterdayPcr == null) {
			this.prepareMohDashboard();
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
			this.prepareMohDashboard();
		}
		return yesterdayRatPositiveRate;
	}

	/**
	 * @param yesterdayRatPositiveRate the yesterdayRatPositiveRate to set
	 */
	public void setYesterdayRatPositiveRate(String yesterdayRatPositiveRate) {
		this.yesterdayRatPositiveRate = yesterdayRatPositiveRate;
	}

	public Long getYesterdayRat() {
        if (yesterdayRat == null) {
            prepareMohDashboard();
        }
        return yesterdayRat;
    }

    public void setYesterdayRat(Long yesterdayRat) {
        this.yesterdayRat = yesterdayRat;
    }

    public Long getYesterdayPositivePcr() {
        if (yesterdayPositivePcr == null) {
            prepareMohDashboard();
        }
        return yesterdayPositivePcr;
    }

    public void setYesterdayPositivePcr(Long yesterdayPositivePcr) {
        this.yesterdayPositivePcr = yesterdayPositivePcr;
    }

    public Long getYesterdayPositiveRat() {
        if (yesterdayPositiveRat == null) {
            prepareMohDashboard();
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

    public List<InstitutionCount> getOrderingCategories() {
        return orderingCategories;
    }

    public void setOrderingCategories(List<InstitutionCount> orderingCategories) {
        this.orderingCategories = orderingCategories;
    }

    public CovidData getMyCovidData() {
        if(myCovidData==null){
            myCovidData = findMyCovidData();
        }
        return myCovidData;
    }

    public void setMyCovidData(CovidData myCovidData) {
        this.myCovidData = myCovidData;
    }

    private CovidData findMyCovidData() {
        CovidData cd  = dashboardApplicationController.findMyCovidData(webUserController.getLoggedUser());
        return cd;
    }

}
