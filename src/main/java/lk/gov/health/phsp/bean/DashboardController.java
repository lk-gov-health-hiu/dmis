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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.util.JsfUtil;

import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import org.json.JSONObject;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.legend.LegendLabel;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class DashboardController implements Serializable {

    @EJB
    private DocumentFacade encounterFacade;

    @Inject
    private FileController encounterController;
    @Inject
    private ItemController itemController;
    @Inject
    private DashboardApplicationController dashboardApplicationController;
    @Inject
    private WebUserController webUserController;
    @Inject
    private ItemApplicationController itemApplicationController;
    @Inject
    LetterController letterController;

    private Date fromDate;
    private Date toDate;
    private List<InstitutionCount> ics;

    private Long receivedLettersThroughSystemToday;
    private Long myLettersToAccept;
    private Long lettersToReceive;
    private Long lettersEntered;
    private Long lettersAccepted;

    // New dashboard counts
    private Long myLettersToAcceptAll;
    private Long myLettersAcceptedToday;
    private Long copyForwardsToMyInstitutionToReceive;
    private Long copyForwardsReceivedToday;
    private Long copyForwardsSentByMyInstitutionLast7Days;
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

    private BarChartModel copyForwardsSentChart;

    // National dashboard fields
    // Letters added
    private Long nationalLettersAllTime;
    private Long nationalLettersLastYear;
    private Long nationalLettersLast30Days;
    // Copy/forwards
    private Long nationalCopyForwardsAllTime;
    private Long nationalCopyForwardsLast30Days;
    private Long nationalCopyForwardsPendingAllTime;
    private Long nationalCopyForwardsReceivedAllTime;
    private Long nationalCopyForwardsPending30Days;
    private Long nationalCopyForwardsReceived30Days;
    private String nationalCopyForwardsReceivedPercentageAllTime;
    private String nationalCopyForwardsReceivedPercentage30Days;
    // Assignments
    private Long nationalAssignmentsAllTime;
    private Long nationalAssignmentsLast30Days;
    private Long nationalAssignmentsPendingAllTime;
    private Long nationalAssignmentsAcceptedAllTime;
    private Long nationalAssignmentsPending30Days;
    private Long nationalAssignmentsAccepted30Days;
    private String nationalAssignedAcceptedPercentageAllTime;
    private String nationalAssignedAcceptedPercentage30Days;
    // Charts
    private BarChartModel nationalLettersByInstitutionChart;
    private BarChartModel nationalCopyForwardsByInstitutionChart;
    private LineChartModel nationalWeeklyLettersChart;
    // Print report data (per institution, last 30 days)
    private List<InstitutionCount> nationalPrintLetterRows;
    private List<InstitutionCount> nationalPrintCopyForwardRows;
    private String nationalReportGeneratedAt;

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
        return "/national/ordering_categories?faces-redirect=true";
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
        return "/national/ordering_categories?faces-redirect=true";
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
        return "/national/ordering_categories?faces-redirect=true";
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
        return "/national/ordering_categories?faces-redirect=true";
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
        return "/national/ordering_categories?faces-redirect=true";
    }

    public void prepareMohDashboard() {
        Calendar c = Calendar.getInstance();
        Date now = c.getTime();
        Date todayStart = CommonController.startOfTheDate();

        c.add(Calendar.DATE, -1);

        Date yesterdayStart = CommonController.startOfTheDate(c.getTime());
        Date yesterdayEnd = CommonController.endOfTheDate(c.getTime());

        receivedLettersThroughSystemToday = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        myLettersToAccept = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        lettersAccepted = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCount(webUserController.getLoggedInstitution(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        lettersToReceive = dashboardApplicationController.getConfirmedCount(webUserController.getLoggedInstitution().getMohArea(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        lettersEntered = dashboardApplicationController.getConfirmedCount(webUserController.getLoggedInstitution().getMohArea(),
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
        if (this.receivedLettersThroughSystemToday != 0) {
            double tempRate = ((double) this.lettersToReceive / this.receivedLettersThroughSystemToday) * 100;
            this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
            this.todayPcrPositiveRate = "0.00%";
        }
//      Calculate today's RAT percentage
        if (this.myLettersToAccept != 0) {
            double tempRate = ((double) this.lettersEntered / this.myLettersToAccept) * 100;
            this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
            this.todayRatPositiveRate = "0.00%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.lettersAccepted != 0) {
            double tempRate = ((double) this.yesterdayPositivePcr / this.lettersAccepted) * 100;
            this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
            this.yesterdayPcrPositiveRate = "0.00%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0) {
            double tempRate = ((double) this.yesterdayPositiveRat / this.yesterdayRat) * 100;
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

    public void preparePersonalDashboard() {
        // Letters assigned to me - all pending (no date filter)
        myLettersToAcceptAll = letterController.countMyLettersToAcceptAll();

        // Letters assigned to me and accepted today
        myLettersAcceptedToday = letterController.countMyLettersAcceptedToday();

        // Copy/forwards to my institution - pending to receive
        copyForwardsToMyInstitutionToReceive = letterController.countCopyForwardsToMyInstitutionToReceive();

        // Copy/forwards received today
        copyForwardsReceivedToday = letterController.countCopyForwardsReceivedToday();

        // Copy/forwards sent by my institution in last 7 days
        copyForwardsSentByMyInstitutionLast7Days = letterController.countCopyForwardsSentByMyInstitutionLast7Days();

        // Keep old counts for backward compatibility
        myLettersToAccept = myLettersToAcceptAll;
        lettersAccepted = myLettersAcceptedToday;

        // Prepare chart for top institutions copy-forwards sent to
        createCopyForwardsSentChart();
    }

    public void prepareNationalDashboard() {
        // Date boundaries
        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = CommonController.startOfTheDate(cal30.getTime());

        Calendar cal365 = Calendar.getInstance();
        cal365.add(Calendar.DAY_OF_MONTH, -365);
        Date oneYearAgo = CommonController.startOfTheDate(cal365.getTime());

        Date now = CommonController.endOfTheDate();

        // Letters added: all time, last year, last 30 days
        nationalLettersAllTime = letterController.countNationalLetters(null, null);
        nationalLettersLastYear = letterController.countNationalLetters(oneYearAgo, now);
        nationalLettersLast30Days = letterController.countNationalLetters(thirtyDaysAgo, now);

        // Copy/forwards: all time counts
        nationalCopyForwardsAllTime = letterController.countNationalCopyForwards(null, null, null);
        nationalCopyForwardsPendingAllTime = letterController.countNationalCopyForwards(null, null, false);
        nationalCopyForwardsReceivedAllTime = letterController.countNationalCopyForwards(null, null, true);
        // Copy/forwards: last 30 days
        nationalCopyForwardsLast30Days = letterController.countNationalCopyForwards(thirtyDaysAgo, now, null);
        nationalCopyForwardsPending30Days = letterController.countNationalCopyForwards(thirtyDaysAgo, now, false);
        nationalCopyForwardsReceived30Days = letterController.countNationalCopyForwards(thirtyDaysAgo, now, true);

        // Assignments: all time counts
        nationalAssignmentsAllTime = letterController.countNationalAssignments(null, null, null);
        nationalAssignmentsPendingAllTime = letterController.countNationalAssignments(null, null, false);
        nationalAssignmentsAcceptedAllTime = letterController.countNationalAssignments(null, null, true);
        // Assignments: last 30 days
        nationalAssignmentsLast30Days = letterController.countNationalAssignments(thirtyDaysAgo, now, null);
        nationalAssignmentsPending30Days = letterController.countNationalAssignments(thirtyDaysAgo, now, false);
        nationalAssignmentsAccepted30Days = letterController.countNationalAssignments(thirtyDaysAgo, now, true);

        // Percentages - all time
        if (nationalAssignmentsAllTime != null && nationalAssignmentsAllTime > 0) {
            nationalAssignedAcceptedPercentageAllTime = df.format(((double) nationalAssignmentsAcceptedAllTime / nationalAssignmentsAllTime) * 100) + "%";
        } else {
            nationalAssignedAcceptedPercentageAllTime = "0.00%";
        }
        if (nationalCopyForwardsAllTime != null && nationalCopyForwardsAllTime > 0) {
            nationalCopyForwardsReceivedPercentageAllTime = df.format(((double) nationalCopyForwardsReceivedAllTime / nationalCopyForwardsAllTime) * 100) + "%";
        } else {
            nationalCopyForwardsReceivedPercentageAllTime = "0.00%";
        }

        // Percentages - last 30 days
        if (nationalAssignmentsLast30Days != null && nationalAssignmentsLast30Days > 0) {
            nationalAssignedAcceptedPercentage30Days = df.format(((double) nationalAssignmentsAccepted30Days / nationalAssignmentsLast30Days) * 100) + "%";
        } else {
            nationalAssignedAcceptedPercentage30Days = "0.00%";
        }
        if (nationalCopyForwardsLast30Days != null && nationalCopyForwardsLast30Days > 0) {
            nationalCopyForwardsReceivedPercentage30Days = df.format(((double) nationalCopyForwardsReceived30Days / nationalCopyForwardsLast30Days) * 100) + "%";
        } else {
            nationalCopyForwardsReceivedPercentage30Days = "0.00%";
        }

        // Charts use last 30 days data
        createNationalLettersByInstitutionChart(thirtyDaysAgo, now);
        createNationalCopyForwardsByInstitutionChart(thirtyDaysAgo, now);
        createNationalWeeklyLettersChart();

        // Reset print report data so it is refreshed on next access
        nationalPrintLetterRows = null;
        nationalPrintCopyForwardRows = null;
        nationalReportGeneratedAt = null;
    }

    private void createNationalLettersByInstitutionChart(Date fd, Date td) {
        nationalLettersByInstitutionChart = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Letters Added (Last 30 Days)");
        dataSet.setBackgroundColor("rgba(9, 132, 227, 0.8)");
        dataSet.setBorderColor("rgb(9, 132, 227)");
        dataSet.setBorderWidth(1);

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        List<Object[]> topInstitutions = letterController.getTopInstitutionsByLetterCount(10, fd, td);
        if (topInstitutions != null) {
            for (Object[] row : topInstitutions) {
                Institution inst = (Institution) row[0];
                Long count = (Long) row[1];
                String instName = inst.getName();
                if (instName != null && instName.length() > 25) {
                    instName = instName.substring(0, 22) + "...";
                }
                labels.add(instName != null ? instName : "Unknown");
                values.add(count != null ? count : 0);
            }
        }

        dataSet.setData(values);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        nationalLettersByInstitutionChart.setData(data);

        BarChartOptions options = new BarChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Top 10 Institutions - Letters Added (Last 30 Days)");
        options.setTitle(title);
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        options.setLegend(legend);
        nationalLettersByInstitutionChart.setOptions(options);
    }

    private void createNationalCopyForwardsByInstitutionChart(Date fd, Date td) {
        nationalCopyForwardsByInstitutionChart = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet acceptedDataSet = new BarChartDataSet();
        acceptedDataSet.setLabel("Received");
        acceptedDataSet.setBackgroundColor("rgba(0, 184, 148, 0.8)");
        acceptedDataSet.setBorderColor("rgb(0, 184, 148)");
        acceptedDataSet.setBorderWidth(1);

        BarChartDataSet pendingDataSet = new BarChartDataSet();
        pendingDataSet.setLabel("Pending");
        pendingDataSet.setBackgroundColor("rgba(253, 203, 110, 0.8)");
        pendingDataSet.setBorderColor("rgb(253, 203, 110)");
        pendingDataSet.setBorderWidth(1);

        List<Number> acceptedValues = new ArrayList<>();
        List<Number> pendingValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        List<Object[]> topInstitutions = letterController.getTopInstitutionsByCopyForwards(1000, fd, td);
        if (topInstitutions != null) {
            Map<Long, Object[]> byReceiver = new LinkedHashMap<>();
            for (Object[] row : topInstitutions) {
                Institution toInst = (Institution) row[1];
                Long accepted = row[2] != null ? (Long) row[2] : 0L;
                Long pending = row[3] != null ? (Long) row[3] : 0L;
                if (toInst == null || toInst.getId() == null) {
                    continue;
                }
                Object[] agg = byReceiver.get(toInst.getId());
                if (agg == null) {
                    agg = new Object[]{toInst, 0L, 0L};
                    byReceiver.put(toInst.getId(), agg);
                }
                agg[1] = ((Long) agg[1]) + accepted;
                agg[2] = ((Long) agg[2]) + pending;
            }
            List<Object[]> aggregated = new ArrayList<>(byReceiver.values());
            aggregated.sort((a, b) -> {
                Long totalA = ((Long) a[1]) + ((Long) a[2]);
                Long totalB = ((Long) b[1]) + ((Long) b[2]);
                return totalB.compareTo(totalA);
            });
            int chartLimit = Math.min(10, aggregated.size());
            for (int i = 0; i < chartLimit; i++) {
                Object[] row = aggregated.get(i);
                Institution inst = (Institution) row[0];
                Long accepted = (Long) row[1];
                Long pending = (Long) row[2];
                String instName = inst.getName();
                if (instName != null && instName.length() > 25) {
                    instName = instName.substring(0, 22) + "...";
                }
                labels.add(instName != null ? instName : "Unknown");
                acceptedValues.add(accepted != null ? accepted : 0);
                pendingValues.add(pending != null ? pending : 0);
            }
        }

        acceptedDataSet.setData(acceptedValues);
        pendingDataSet.setData(pendingValues);
        data.addChartDataSet(acceptedDataSet);
        data.addChartDataSet(pendingDataSet);
        data.setLabels(labels);
        nationalCopyForwardsByInstitutionChart.setData(data);

        BarChartOptions options = new BarChartOptions();
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxesX = new CartesianLinearAxes();
        linearAxesX.setStacked(true);
        cScales.addXAxesData(linearAxesX);
        CartesianLinearAxes linearAxesY = new CartesianLinearAxes();
        linearAxesY.setStacked(true);
        cScales.addYAxesData(linearAxesY);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Top 10 Institutions - Copy/Forwards (Last 30 Days)");
        options.setTitle(title);
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontColor("#495057");
        legend.setLabels(legendLabels);
        options.setLegend(legend);
        nationalCopyForwardsByInstitutionChart.setOptions(options);
    }

    private void createNationalWeeklyLettersChart() {
        nationalWeeklyLettersChart = new LineChartModel();
        ChartData data = new ChartData();

        LineChartDataSet dataSet = new LineChartDataSet();
        dataSet.setLabel("Letters Added");
        dataSet.setBackgroundColor("rgba(9, 132, 227, 0.2)");
        dataSet.setBorderColor("rgb(9, 132, 227)");
        dataSet.setFill(true);

        List<Object> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM");
        List<InstitutionCount> weeklyCounts = letterController.getWeeklyLetterCounts(12);
        if (weeklyCounts != null) {
            for (InstitutionCount wc : weeklyCounts) {
                labels.add(sdf.format(wc.getDate()));
                values.add(wc.getCount());
            }
        }

        dataSet.setData(values);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        nationalWeeklyLettersChart.setData(data);

        LineChartOptions options = new LineChartOptions();
        Title title = new Title();
        title.setDisplay(true);
        title.setText("Weekly Letters Added (Last 12 Weeks)");
        options.setTitle(title);
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        options.setLegend(legend);
        nationalWeeklyLettersChart.setOptions(options);
    }

    private void createCopyForwardsSentChart() {
        copyForwardsSentChart = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet acceptedDataSet = new BarChartDataSet();
        acceptedDataSet.setLabel("Accepted");
        acceptedDataSet.setBackgroundColor("rgba(0, 184, 148, 0.8)");
        acceptedDataSet.setBorderColor("rgb(0, 184, 148)");
        acceptedDataSet.setBorderWidth(1);

        BarChartDataSet pendingDataSet = new BarChartDataSet();
        pendingDataSet.setLabel("Pending");
        pendingDataSet.setBackgroundColor("rgba(253, 203, 110, 0.8)");
        pendingDataSet.setBorderColor("rgb(253, 203, 110)");
        pendingDataSet.setBorderWidth(1);

        List<Number> acceptedValues = new ArrayList<>();
        List<Number> pendingValues = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        List<Object[]> topInstitutions = letterController.getTopInstitutionsCopyForwardsSentByMyInstitution(10);

        if (topInstitutions != null) {
            for (Object[] row : topInstitutions) {
                Institution inst = (Institution) row[0];
                Long accepted = (Long) row[1];
                Long pending = (Long) row[2];

                String instName = inst.getName();
                if (instName != null && instName.length() > 20) {
                    instName = instName.substring(0, 17) + "...";
                }
                labels.add(instName != null ? instName : "Unknown");
                acceptedValues.add(accepted != null ? accepted : 0);
                pendingValues.add(pending != null ? pending : 0);
            }
        }

        acceptedDataSet.setData(acceptedValues);
        pendingDataSet.setData(pendingValues);

        data.addChartDataSet(acceptedDataSet);
        data.addChartDataSet(pendingDataSet);
        data.setLabels(labels);

        copyForwardsSentChart.setData(data);

        // Options
        BarChartOptions options = new BarChartOptions();

        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes linearAxesX = new CartesianLinearAxes();
        linearAxesX.setStacked(true);
        cScales.addXAxesData(linearAxesX);

        CartesianLinearAxes linearAxesY = new CartesianLinearAxes();
        linearAxesY.setStacked(true);
        cScales.addYAxesData(linearAxesY);
        options.setScales(cScales);

        Title title = new Title();
        title.setDisplay(true);
        title.setText("Top 10 Institutions - Copy/Forwards Sent");
        options.setTitle(title);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        LegendLabel legendLabels = new LegendLabel();
        legendLabels.setFontColor("#495057");
        legend.setLabels(legendLabels);
        options.setLegend(legend);

        copyForwardsSentChart.setOptions(options);
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

        receivedLettersThroughSystemToday = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        myLettersToAccept = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        lettersAccepted = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getRdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        lettersToReceive = dashboardApplicationController.getConfirmedCount(
                webUserController.getLoggedInstitution().getRdhsArea().getDistrict(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        lettersEntered = dashboardApplicationController.getConfirmedCount(
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
        if (this.receivedLettersThroughSystemToday != 0) {
            double tempRate = ((double) this.lettersToReceive / this.receivedLettersThroughSystemToday) * 100;
            this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
            this.todayPcrPositiveRate = "0.00%";
        }
//      Calculate today's RAT percentage
        if (this.myLettersToAccept != 0) {
            double tempRate = ((double) this.lettersEntered / this.myLettersToAccept) * 100;
            this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
            this.todayRatPositiveRate = "0.00%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.lettersAccepted != 0) {
            double tempRate = ((double) this.yesterdayPositivePcr / this.lettersAccepted) * 100;
            this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
            this.yesterdayPcrPositiveRate = "0.00%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0) {
            double tempRate = ((double) this.yesterdayPositiveRat / this.yesterdayRat) * 100;
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

        receivedLettersThroughSystemToday = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), todayStart, now,
                itemApplicationController.getPcr(), null, null, null);
        myLettersToAccept = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), todayStart, now,
                itemApplicationController.getRat(), null, null, null);
        lettersAccepted = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getPcr(), null, null, null);
        yesterdayRat = dashboardApplicationController.getOrderCountArea(webUserController.getLoggedInstitution().getPdhsArea(), yesterdayStart, yesterdayEnd,
                itemApplicationController.getRat(), null, null, null);

        lettersToReceive = dashboardApplicationController.getConfirmedCountArea(
                webUserController.getLoggedInstitution().getPdhsArea(),
                todayStart,
                now,
                itemApplicationController.getPcr(),
                null,
                itemApplicationController.getPcrPositive(),
                null);
        lettersEntered = dashboardApplicationController.getConfirmedCountArea(
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
        if (this.receivedLettersThroughSystemToday != 0 || this.receivedLettersThroughSystemToday != null) {
            double tempRate = ((double) this.lettersToReceive / this.receivedLettersThroughSystemToday) * 100;
            this.todayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
            this.todayPcrPositiveRate = "0.00%";
        }
//      Calculate today's RAT percentage
        if (this.myLettersToAccept != 0 || this.myLettersToAccept != null) {
            double tempRate = ((double) this.lettersEntered / this.myLettersToAccept) * 100;
            this.todayRatPositiveRate = df.format(tempRate) + "%";
        } else {
            this.todayRatPositiveRate = "0.00%";
        }
//        Calculate yesterday's PCR positive percentage
        if (this.lettersAccepted != 0 || this.lettersAccepted != null) {
            double tempRate = ((double) this.yesterdayPositivePcr / this.lettersAccepted) * 100;
            this.yesterdayPcrPositiveRate = df.format(tempRate) + "%";
        } else {
            this.yesterdayPcrPositiveRate = "0.00%";
        }
//        Calculates yesterday's Rat positive percentage
        if (this.yesterdayRat != 0 || this.yesterdayRat != null) {
            double tempRate = ((double) this.yesterdayPositiveRat / this.yesterdayRat) * 100;
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
        return "/systemAdmin/calculate_numbers?faces-redirect=true";
    }

    /**
     * Creates a new instance of DashboardController
     */
    public DashboardController() {
    }

    //    Generates a hashmap that will give PCR and RAT investigations of each MOH under a given RDHS area
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

    public FileController getEncounterController() {
        return encounterController;
    }

    public void setEncounterController(FileController encounterController) {
        this.encounterController = encounterController;
    }

    public List<InstitutionCount> getIcs() {
        return ics;
    }

    public void setIcs(List<InstitutionCount> ics) {
        this.ics = ics;
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

    public Long getReceivedLettersThroughSystemToday() {
        if (receivedLettersThroughSystemToday == null) {
            prepareMohDashboard();
        }
        return receivedLettersThroughSystemToday;
    }

    public Long getMyLettersToAccept() {
        if (myLettersToAccept == null) {
            prepareMohDashboard();
        }
        return myLettersToAccept;
    }

    public Long getLettersToReceive() {
        if (lettersToReceive == null) {
            prepareMohDashboard();
        }
        return lettersToReceive;
    }

    public Long getLettersEntered() {
        if (lettersEntered == null) {
            prepareMohDashboard();
        }
        return lettersEntered;
    }

    public Long getLettersAccepted() {
        if (lettersAccepted == null) {
            prepareMohDashboard();
        }
        return lettersAccepted;
    }

    public void setLettersAccepted(Long lettersAccepted) {
        this.lettersAccepted = lettersAccepted;
    }

    /**
     * @return the todayPcrPositiveRate
     */
    public String getTodayPcrPositiveRate() {
        if (this.receivedLettersThroughSystemToday == null) {
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
        if (this.myLettersToAccept == null) {
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
        if (this.lettersAccepted == null) {
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
        if (getLettersAccepted() != null && getYesterdayRat() != null) {
            yesterdayTests = getLettersAccepted() + getYesterdayRat();
        } else if (getLettersAccepted() != null) {
            yesterdayTests = getLettersAccepted();
        } else if (getYesterdayRat() != null) {
            yesterdayTests = getYesterdayRat();
        } else {
            yesterdayTests = 0l;
        }
        return yesterdayTests;
    }

    public Long getTodaysTests() {
        if (getReceivedLettersThroughSystemToday() != null && getMyLettersToAccept() != null) {
            todaysTests = getReceivedLettersThroughSystemToday() + getMyLettersToAccept();
        } else if (getReceivedLettersThroughSystemToday() != null) {
            todaysTests = getReceivedLettersThroughSystemToday();
        } else if (getMyLettersToAccept() != null) {
            todaysTests = getMyLettersToAccept();
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

    public Long getMyLettersToAcceptAll() {
        return myLettersToAcceptAll;
    }

    public Long getMyLettersAcceptedToday() {
        return myLettersAcceptedToday;
    }

    public Long getCopyForwardsToMyInstitutionToReceive() {
        return copyForwardsToMyInstitutionToReceive;
    }

    public Long getCopyForwardsReceivedToday() {
        return copyForwardsReceivedToday;
    }

    public Long getCopyForwardsSentByMyInstitutionLast7Days() {
        return copyForwardsSentByMyInstitutionLast7Days;
    }

    public BarChartModel getCopyForwardsSentChart() {
        if (copyForwardsSentChart == null) {
            createCopyForwardsSentChart();
        }
        return copyForwardsSentChart;
    }

    public Long getNationalLettersAllTime() {
        return nationalLettersAllTime;
    }

    public Long getNationalLettersLastYear() {
        return nationalLettersLastYear;
    }

    public Long getNationalLettersLast30Days() {
        return nationalLettersLast30Days;
    }

    public Long getNationalCopyForwardsAllTime() {
        return nationalCopyForwardsAllTime;
    }

    public Long getNationalCopyForwardsLast30Days() {
        return nationalCopyForwardsLast30Days;
    }

    public Long getNationalCopyForwardsPendingAllTime() {
        return nationalCopyForwardsPendingAllTime;
    }

    public Long getNationalCopyForwardsReceivedAllTime() {
        return nationalCopyForwardsReceivedAllTime;
    }

    public Long getNationalCopyForwardsPending30Days() {
        return nationalCopyForwardsPending30Days;
    }

    public Long getNationalCopyForwardsReceived30Days() {
        return nationalCopyForwardsReceived30Days;
    }

    public String getNationalCopyForwardsReceivedPercentageAllTime() {
        return nationalCopyForwardsReceivedPercentageAllTime;
    }

    public String getNationalCopyForwardsReceivedPercentage30Days() {
        return nationalCopyForwardsReceivedPercentage30Days;
    }

    public Long getNationalAssignmentsAllTime() {
        return nationalAssignmentsAllTime;
    }

    public Long getNationalAssignmentsLast30Days() {
        return nationalAssignmentsLast30Days;
    }

    public Long getNationalAssignmentsPendingAllTime() {
        return nationalAssignmentsPendingAllTime;
    }

    public Long getNationalAssignmentsAcceptedAllTime() {
        return nationalAssignmentsAcceptedAllTime;
    }

    public Long getNationalAssignmentsPending30Days() {
        return nationalAssignmentsPending30Days;
    }

    public Long getNationalAssignmentsAccepted30Days() {
        return nationalAssignmentsAccepted30Days;
    }

    public String getNationalAssignedAcceptedPercentageAllTime() {
        return nationalAssignedAcceptedPercentageAllTime;
    }

    public String getNationalAssignedAcceptedPercentage30Days() {
        return nationalAssignedAcceptedPercentage30Days;
    }

    public BarChartModel getNationalLettersByInstitutionChart() {
        return nationalLettersByInstitutionChart;
    }

    public BarChartModel getNationalCopyForwardsByInstitutionChart() {
        return nationalCopyForwardsByInstitutionChart;
    }

    public LineChartModel getNationalWeeklyLettersChart() {
        return nationalWeeklyLettersChart;
    }

    public void prepareNationalPrintReport() {
        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = CommonController.startOfTheDate(cal30.getTime());
        Date now = CommonController.endOfTheDate();

        nationalReportGeneratedAt = new java.text.SimpleDateFormat("dd MMMM yyyy, hh:mm a").format(new Date());

        // Summary totals (last 30 days)
        nationalLettersLast30Days = letterController.countNationalLetters(thirtyDaysAgo, now);
        nationalCopyForwardsLast30Days = letterController.countNationalCopyForwards(thirtyDaysAgo, now, null);
        nationalCopyForwardsPending30Days = letterController.countNationalCopyForwards(thirtyDaysAgo, now, false);
        nationalCopyForwardsReceived30Days = letterController.countNationalCopyForwards(thirtyDaysAgo, now, true);
        nationalAssignmentsLast30Days = letterController.countNationalAssignments(thirtyDaysAgo, now, null);
        nationalAssignmentsPending30Days = letterController.countNationalAssignments(thirtyDaysAgo, now, false);
        nationalAssignmentsAccepted30Days = letterController.countNationalAssignments(thirtyDaysAgo, now, true);
        if (nationalAssignmentsLast30Days != null && nationalAssignmentsLast30Days > 0) {
            nationalAssignedAcceptedPercentage30Days = df.format(((double) nationalAssignmentsAccepted30Days / nationalAssignmentsLast30Days) * 100) + "%";
        } else {
            nationalAssignedAcceptedPercentage30Days = "0.00%";
        }
        if (nationalCopyForwardsLast30Days != null && nationalCopyForwardsLast30Days > 0) {
            nationalCopyForwardsReceivedPercentage30Days = df.format(((double) nationalCopyForwardsReceived30Days / nationalCopyForwardsLast30Days) * 100) + "%";
        } else {
            nationalCopyForwardsReceivedPercentage30Days = "0.00%";
        }

        List<Object[]> rawLetterRows = letterController.getTopInstitutionsByLetterCount(100, thirtyDaysAgo, now);
        nationalPrintLetterRows = new ArrayList<>();
        if (rawLetterRows != null) {
            for (Object[] row : rawLetterRows) {
                InstitutionCount ic = new InstitutionCount();
                ic.setInstitution((Institution) row[0]);
                ic.setCount((Long) row[1]);
                nationalPrintLetterRows.add(ic);
            }
        }

        List<Object[]> rawCfRows = letterController.getTopInstitutionsByCopyForwards(100, thirtyDaysAgo, now);
        nationalPrintCopyForwardRows = new ArrayList<>();
        if (rawCfRows != null) {
            for (Object[] row : rawCfRows) {
                InstitutionCount ic = new InstitutionCount();
                ic.setReferralInstitution((Institution) row[0]);
                ic.setInstitution((Institution) row[1]);
                Long received = row[2] != null ? (Long) row[2] : 0L;
                Long pending = row[3] != null ? (Long) row[3] : 0L;
                ic.setReceivedCount(received);
                ic.setPendingCount(pending);
                long total = received + pending;
                ic.setCount(total);
                if (total > 0) {
                    ic.setPositiveRate(df.format(((double) received / total) * 100) + "%");
                } else {
                    ic.setPositiveRate("0.00%");
                }
                nationalPrintCopyForwardRows.add(ic);
            }
        }
    }

    public String toNationalPerformanceReport() {
        prepareNationalPrintReport();
        return "/national/performance_report?faces-redirect=true";
    }

    public List<InstitutionCount> getNationalPrintLetterRows() {
        return nationalPrintLetterRows;
    }

    public List<InstitutionCount> getNationalPrintCopyForwardRows() {
        return nationalPrintCopyForwardRows;
    }

    public String getNationalReportGeneratedAt() {
        return nationalReportGeneratedAt;
    }

}
