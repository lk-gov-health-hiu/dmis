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
package lk.gov.health.phsp.pojcs;

import be.ceau.chart.BarChart;
import be.ceau.chart.color.Color;
import be.ceau.chart.data.BarData;
import be.ceau.chart.dataset.BarDataset;
import be.ceau.chart.options.BarOptions;
import java.util.Date;
import java.util.List;
import javax.json.Json;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.WebUserRoleLevel;


/**
 *
 * @author buddhika
 */
public class CovidData {

    private WebUserRoleLevel type;
    
    private Institution institution;
    private Area area;

    private Date date;
    private String reportingTimePeriod;
    private String positivesBySubArea;

    private Date from;
    private Date to;
    
    
    private Long dailyPositives;
    private Long ratPositives;
    private Long pcrPositives;
    
    private List<OrderingCategoryResults> orderingCategoryResults;
    private List<InstitutionCount> subAreaCounts;

    public Long getDailyPositives() {
        return dailyPositives;
    }

    public void setDailyPositives(Long dailyPositives) {
        this.dailyPositives = dailyPositives;
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

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getReportingTimePeriod() {
        return reportingTimePeriod;
    }

    public void setReportingTimePeriod(String reportingTimePeriod) {
        this.reportingTimePeriod = reportingTimePeriod;
    }

    public String getPositivesBySubArea() {
        BarOptions options = new BarOptions();
        options.setResponsive(true);
        BarDataset dataset = new BarDataset()
                .setLabel("sample chart")
                .setData(65, 59, 80, 81, 56, 55, 40)
                .addBackgroundColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.GRAY, Color.BLACK)
                .setBorderWidth(2);
        BarData data = new BarData()
                .addLabels("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                .addDataset(dataset);
        BarChart barChart= new BarChart(data);
        barChart.setHorizontal();
//        barChart.setOptions(options);
        positivesBySubArea =barChart.toJson();
        return positivesBySubArea;
    }

    public WebUserRoleLevel getType() {
        return type;
    }

    public void setType(WebUserRoleLevel type) {
        this.type = type;
    }

    public Long getRatPositives() {
        return ratPositives;
    }

    public void setRatPositives(Long ratPositives) {
        this.ratPositives = ratPositives;
    }

    public Long getPcrPositives() {
        return pcrPositives;
    }

    public void setPcrPositives(Long pcrPositives) {
        this.pcrPositives = pcrPositives;
    }

    public List<OrderingCategoryResults> getOrderingCategoryResults() {
        return orderingCategoryResults;
    }

    public void setOrderingCategoryResults(List<OrderingCategoryResults> orderingCategoryResults) {
        this.orderingCategoryResults = orderingCategoryResults;
    }

    public List<InstitutionCount> getSubAreaCounts() {
        return subAreaCounts;
    }

    public void setSubAreaCounts(List<InstitutionCount> subAreaCounts) {
        this.subAreaCounts = subAreaCounts;
    }

   

}
