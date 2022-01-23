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
package lk.gov.health.phsp.ejb;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.ClientEncounterComponentItem;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Numbers;
import lk.gov.health.phsp.enums.EncounterType;
import lk.gov.health.phsp.facade.ClientEncounterComponentItemFacade;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.NumbersFacade;
import lk.gov.health.phsp.pojcs.CovidData;
import lk.gov.health.phsp.pojcs.InstitutionCount;

/**
 *
 * @author buddhika
 */
@Singleton
public class CovidDataHolder {

    @EJB
    DocumentFacade encounterFacade;
    @EJB
    ClientEncounterComponentItemFacade clientEncounterComponentItemFacade;
    @EJB
    NumbersFacade numbersFacade;

    private List<CovidData> covidDatasForMohs;
    private List<CovidData> covidDatasForAreas;
    private List<CovidData> covidDatasForLabs;
    private List<CovidData> covidDatasForHospitals;
    private List<CovidData> covidDatasForRdhs;
    private List<CovidData> covidDatasForPdhs;
    private List<CovidData> covidDatasForCountry;
    List<Institution> mohs;
    List<Institution> hospitals;
    List<Institution> labs;
    List<Area> areas;

    Item pcr;
    Item rat;
    Item testType;
    Item orderingCategory;

    @Schedule(month = "*", hour = "2", dayOfMonth = "*", year = "*", minute = "1", second = "0", persistent = false)
    public void generateCovidCountsForToday() {
        calculateNumbers();
       
    }

    @Asynchronous
    public void generateCovidCountsAsync(Item pcr, Item rat, Item testType, Item orderingCategory, List<Institution> mohs, List<Institution> hospitals, List<Institution> labs, List<Area> areas) {
        this.pcr = pcr;
        this.rat = rat;
        this.testType = testType;
        this.orderingCategory = orderingCategory;
        this.mohs = mohs;
        this.hospitals = hospitals;
        this.areas = areas;
        this.labs = labs;
       
    }

  
    public void calculateNumbers() {
        calculateNumbers(new Date());
    }

    public void calculateNumbers(Date date) {
        calculateCasesAndTestNumbers(date, date);
    }

    public void calculateNumbers(Date fromDate, Date toDate) {
        calculateNumbersByOrderedTest(fromDate, toDate);
        calculateCasesAndTestNumbers(fromDate, toDate);
    }

    public void calculateNumbersByOrderedTest(Date fromDate, Date toDate) {
        // // System.out.println("calculateNumbersByOrderedTest");
        ClientEncounterComponentItem i = new ClientEncounterComponentItem();
        i.getItemEncounter();
        i.getItem();
        i.getItemValue();
        List<Item> items = new ArrayList<>();
        items.add(testType);
        items.add(orderingCategory);

        String j = "select  new lk.gov.health.phsp.pojcs.InstitutionCount(count(e), e.institution, e.encounterDate, e.encounterType, i.item, i.itemValue)  "
                + " from  ClientEncounterComponentItem i join i.itemEncounter e"
                + " where e.retired=false "
                + " and e.encounterDate between :fd and :td "
                + " and i.item in :items "
                + " and e.encounterType=:test"
                + " group by e.encounterDate, e.institution, e.encounterType, i.item, i.itemValue";
        Map m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("items", items);
        m.put("test", EncounterType.Test_Enrollment);
        // // System.out.println("m = " + m);
        // // System.out.println("j = " + j);
        List<Object> os = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : os) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                // // System.out.println("ic = " + ic);
                String name = "";

                if (ic.getEncounerType() == null) {
                    continue;
                }
                switch (ic.getEncounerType()) {
                    case Test_Enrollment:
                        if (ic.getItemValue() == null) {
                            continue;
                        }
                        if (ic.getItemValue().equals(rat)) {
                            name = "rat orders";
                        } else if (ic.getItemValue().equals(pcr)) {
                            name = "pcr orders";
                        } else {
                            name = "";
                        }
                        break;
                    default:
                        name = "";
                }

                if (!name.trim().equals("")) {
                    Numbers n = new Numbers();
                    n.setInstitution(ic.getInstitution());
                    n.setNumberDate(ic.getDate());
                    n.setName(name);
                    n.setCount(ic.getCount());
                    Numbers nn = save(n);
                }
            }
        }
    }

    public void calculateCasesAndTestNumbers(Date fromDate, Date toDate) {
        String j = "select  new lk.gov.health.phsp.pojcs.InstitutionCount(count(e), e.institution, e.encounterDate, e.encounterType)  "
                + " from Encounter e "
                + " where e.retired=false "
                + " and e.encounterDate between :fd and :td "
                + " group by e.encounterDate, e.institution, e.encounterType";
        Map m = new HashMap();
        m.put("fd", fromDate);
        m.put("td", toDate);
        List<Object> os = encounterFacade.findObjectByJpql(j, m, TemporalType.DATE);
        for (Object o : os) {
            if (o instanceof InstitutionCount) {
                InstitutionCount ic = (InstitutionCount) o;
                String name = "";
                switch (ic.getEncounerType()) {
                    case Case_Enrollment:
                        name = "cases";
                        break;
                    case Death:
                        name = "deaths";
                        break;
                    case Test_Enrollment:
                        name = "test orders";
                        break;
                    default:
                        name = "";
                }

                if (!name.trim().equals("")) {
                    Numbers n = new Numbers();
                    n.setInstitution(ic.getInstitution());
                    n.setNumberDate(ic.getDate());
                    n.setName(name);
                    n.setCount(ic.getCount());
                    Numbers nn = save(n);
                }
            }
        }
    }

    public Numbers save(Numbers numbers) {
        if (numbers == null) {
            return null;
        }
        if (numbers.getId() != null) {
            numbersFacade.edit(numbers);
            return numbers;
        }
        Numbers databaseNumbers;
        Map m = new HashMap();
        String j = "select n "
                + " from Numbers n "
                + " where n.numberDate=:d "
                + " and n.name=:name ";
        m.put("d", numbers.getNumberDate());
        m.put("name", numbers.getName());
        if (numbers.getInstitution() != null) {
            j += " and n.institution=:ins ";
            m.put("ins", numbers.getInstitution());
        } else if (numbers.getArea() != null) {
            j += " and n.area=:area ";
            m.put("area", numbers.getArea());
        } else {
            return null;
        }
        j += " order by n.id desc";
        databaseNumbers = numbersFacade.findFirstByJpql(j, m);
        if (databaseNumbers == null) {
            numbersFacade.create(numbers);
            return numbers;
        } else {
            databaseNumbers.setCount(numbers.getCount());
            numbersFacade.edit(databaseNumbers);
            return databaseNumbers;
        }
    }

    public Long findCount(Institution ins, Area a, Date date, String name) {
        // // System.out.println("findCount");
        Numbers databaseNumbers;
        Map m = new HashMap();
        String j = "select n "
                + " from Numbers n "
                + " where n.numberDate=:d "
                + " and n.name=:name ";
        m.put("d", date);
        m.put("name", name);
        if (ins != null) {
            j += " and n.institution=:ins ";
            m.put("ins", ins);
        } else if (a != null) {
            j += " and n.area=:area ";
            m.put("area", a);
        } else {
            return null;
        }
        j += " order by n.id desc";
        databaseNumbers = numbersFacade.findFirstByJpql(j, m);
        if (databaseNumbers == null) {
            return 0l;
        } else {
            Long c = databaseNumbers.getCount();
            if (c == null) {
                c = 0l;
            }
            return c;
        }
    }

    public Long findCount(Institution ins, Area a, Date fromDate, Date toDate, String name) {
        // // System.out.println("findCountForPeriod");
        // // System.out.println("name = " + name);
        // // System.out.println("fromDate = " + fromDate);
        // // System.out.println("toDate = " + toDate);
        Map m = new HashMap();
        String j = "select sum(n.count) "
                + " from Numbers n "
                + " where n.numberDate between :fd and :td "
                + " and n.name=:name ";
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("name", name);
        if (ins != null) {
            j += " and n.institution=:ins ";
            m.put("ins", ins);
            // // System.out.println("ins Name = " + ins.getName());
            // // System.out.println("ins ID = " + ins.getId());
        } else if (a != null) {
            j += " and n.area=:area ";
            m.put("area", a);
        } else {
            return null;
        }
        Long count = numbersFacade.findLongByJpql(j, m);
        // // System.out.println("count = " + count);
        return count;
    }

    public List<CovidData> getCovidDatasForMohs() {
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

}
