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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AreaFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class AreaApplicationController {

    @EJB
    private AreaFacade areaFacade;

    private List<Area> gnAreas;
    private List<Area> provinces;
    private List<Area> districts;
    private List<Area> rdhsAreas;
    private List<Area> pdhsAreas;
    private List<Area> mohAreas;
    private List<Area> phiAreas;
    private List<Area> allAreas;

    private List<AreaType> covidMonitoringAreaTypes;

    /**
     * Creates a new instance of AreaApplicationController
     */
    public AreaApplicationController() {
    }

    public void invalidateItems() {
        gnAreas = null;
        provinces = null;
        districts = null;
        rdhsAreas = null;
        pdhsAreas = null;
        mohAreas = null;
        phiAreas = null;
        allAreas = null;
    }

    public List<Area> getGnAreas() {
        if (gnAreas == null) {
            gnAreas = getAllGnAreas();
        }
        return gnAreas;
    }

    public List<Area> getDistricts() {
        if (districts == null) {
            districts = getAllDistricts();
        }
        return districts;
    }

    public List<Area> getProvinces() {
        if (provinces == null) {
            provinces = getAllProvinces();
        }
        return provinces;
    }

    public List<Area> getPdhsAreas() {
        if (pdhsAreas == null) {
            pdhsAreas = getAllPdhsAreas();
        }
        return pdhsAreas;
    }

    public List<Area> getRdhsAreas() {
        if (rdhsAreas == null) {
            rdhsAreas = getAllRdhsAreas();
        }
        return rdhsAreas;
    }

    public List<Area> getMohAreas() {
        if (mohAreas == null) {
            mohAreas = getAllMohAreas();
        }
        return mohAreas;
    }

    public List<Area> getPhiAreas() {
        if (phiAreas == null) {
            phiAreas = getAllPhiAreas();
        }
        return phiAreas;
    }

    public List<Area> getAllAreas() {
        if (allAreas == null) {
            allAreas = fillAllAreas();
        }
        return allAreas;
    }

    private List<Area> fillAllAreas() {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null";
        j += " order by a.name";
        return areaFacade.findByJpql(j, m);
    }

    public List<Area> getAllGnAreas() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.GN) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllMohAreas() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.MOH) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllPhiAreas() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.PHI) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllDistricts() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.District) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllProvinces() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.Province) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllPdhsAreas() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.PdhsArea) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllRdhsAreas() {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == AreaType.RdhsAra) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllAreas(AreaType at) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() != null && a.getType().equals(at)) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> getAllChildren(Area parentArea) {
        List<Area> tas = new ArrayList<>();
        tas.add(parentArea);
        for (Area a : getAllAreas()) {
            if (a.getParentArea() != null && a.getParentArea().equals(parentArea)) {
                tas.addAll(getAllChildren(a));
            }
        }
        return tas;
    }

    public List<Area> getMohAreasOfAnRdhs(Area rdhs) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas(AreaType.MOH)) {
            if (rdhs.getType() == AreaType.RdhsAra) {
                if (a.getParentArea() != null && a.getParentArea().equals(rdhs)) {
                    tas.add(a);
                } else if (a.getRdhsArea() != null && a.getRdhsArea().equals(rdhs)) {
                    tas.add(a);
                }
            } else if (rdhs.getType() == AreaType.District) {
                if (a.getParentArea() != null && a.getParentArea().equals(rdhs)) {
                    tas.add(a);
                } else if (a.getDistrict() != null && a.getDistrict().equals(rdhs)) {
                    tas.add(a);
                }
            }
        }
        return tas;
    }

    public List<Area> getPhiAreasOfMoh(Area moh) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas(AreaType.PHI)) {
            if (a.getParentArea() != null && a.getParentArea().equals(moh)) {
                tas.add(a);
            } else if (a.getMoh() != null && a.getMoh().equals(moh)) {
                tas.add(a);
            }
        }
        return tas;
    }

    public boolean saveArea(Area a, WebUser u) {
        if (a == null) {
            return false;
        }
        if (a.getId() == null) {
            a.setCreatedAt(new Date());
            a.setCreatedBy(u);
            areaFacade.create(a);
            invalidateItems();
            getAllAreas();
            return true;
        } else {
            a.setLastEditBy(u);
            a.setLastEditeAt(new Date());
            areaFacade.edit(a);
            return true;
        }
    }

    public List<Area> getAllAreas(List<AreaType> ats) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getType() == null) {
                continue;
            }
            boolean canInclude = false;
            for (AreaType at : ats) {
                if (a.getType().equals(at)) {
                    canInclude = true;
                }
            }
            if (canInclude) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeGnAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getGnAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeDistricts(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getDistricts()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getAllAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public Area getArea(Long id) {
        Area tas = null;
        if (id == null) {
            return tas;
        }
        for (Area a : getAllAreas()) {
            if (a.getId().equals(id)) {
                tas = a;
            }
        }
        return tas;
    }

    public List<Area> completeProvinces(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getProvinces()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completePdhsAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getPdhsAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeRdhsAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getRdhsAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeMohAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getMohAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completePhiAreas(String qry) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getPhiAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeGnAreas(String qry, Area dsArea) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getGnAreas()) {
            if (a.getName().toLowerCase().contains(qry.trim().toLowerCase())) {
                if (a.getParentArea().equals(dsArea)) {
                    tas.add(a);
                }
            }
        }
        return tas;
    }

    public List<Area> completeGnAreasOfDistrict(String qry, Area district) {
        List<Area> tas = new ArrayList<>();
        qry = qry.trim().toLowerCase();
        for (Area a : getGnAreas()) {
            if (a.getName().toLowerCase().contains(qry)) {
                if (a.getParentArea() != null & a.getParentArea().getParentArea() != null && a.getParentArea().getParentArea().equals(district)) {
                    tas.add(a);
                    continue;
                }
                if (a.getDistrict().equals(district)) {
                    tas.add(a);
                }
            }
        }
        return tas;
    }

    public List<Area> completePhiAreasOfMoh(String qry, Area moh) {
        List<Area> tas = new ArrayList<>();
        qry = qry.trim().toLowerCase();
        for (Area a : getPhiAreas()) {
            if (a.getName().toLowerCase().contains(qry)) {
                if (a.getParentArea() != null && a.getParentArea().equals(moh)) {
                    tas.add(a);
                    continue;
                }
                if (a.getParentArea() != null
                        && a.getParentArea().getParentArea() != null
                        && a.getParentArea().getParentArea().equals(moh)) {
                    tas.add(a);
                    continue;
                }
                if (a.getMoh() != null && a.getMoh().equals(moh)) {
                    tas.add(a);
                }
            }
        }
        return tas;
    }

    public List<Area> listPhiAreasOfMoh(Area moh) {
        List<Area> tas = new ArrayList<>();
        for (Area a : getPhiAreas()) {
            if (a.getParentArea() != null && a.getParentArea().equals(moh)) {
                tas.add(a);
                continue;
            }
            if (a.getParentArea() != null
                    && a.getParentArea().getParentArea() != null
                    && a.getParentArea().getParentArea().equals(moh)) {
                tas.add(a);
                continue;
            }
            if (a.getMoh().equals(moh)) {
                tas.add(a);
            }
        }
        return tas;
    }

    public List<Area> completeMohAreasOfDistrict(String qry, Area district) {
        List<Area> tas = new ArrayList<>();
        qry = qry.trim().toLowerCase();
        for (Area a : getMohAreas()) {
            if (a.getName().toLowerCase().contains(qry)) {
                if (a.getDistrict().equals(district)) {
                    tas.add(a);
                }
            }
        }
        return tas;
    }

    public List<AreaType> getCovidMonitoringAreaTypes() {
        if (covidMonitoringAreaTypes == null) {
            covidMonitoringAreaTypes = new ArrayList<>();
            covidMonitoringAreaTypes.add(AreaType.MOH);
            covidMonitoringAreaTypes.add(AreaType.District);
            covidMonitoringAreaTypes.add(AreaType.Province);
            covidMonitoringAreaTypes.add(AreaType.National);
        }
        return covidMonitoringAreaTypes;
    }

}
