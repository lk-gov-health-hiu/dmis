package lk.gov.health.phsp.bean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.InstitutionFacade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.WebUserRoleLevel;
import lk.gov.health.phsp.facade.AreaFacade;
import org.primefaces.model.file.UploadedFile;

@Named
@SessionScoped
public class InstitutionController implements Serializable {

    @EJB
    private InstitutionFacade ejbFacade;

    @EJB
    private AreaFacade areaFacade;

    @Inject
    private WebUserController webUserController;

    @Inject
    private ApplicationController applicationController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    private UserTransactionController userTransactionController;

    private List<Institution> items = null;
    private Institution selected;
    private Institution deleting;
    private List<Institution> myClinics;
    private List<Area> gnAreasOfSelected;
    private Area area;
    private Area removingArea;

    private InstitutionType institutionType;
    private Institution parent;
    private Area province;
    private Area pdhsArea;
    private Area district;
    private Area rdhsArea;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    private UploadedFile file;

    public Institution getInstitutionById(Long id) {
        return getFacade().find(id);
    }

    public Institution findHospital(Institution unit) {
        if (unit == null) {
            return null;
        }
        switch (unit.getInstitutionType()) {
            case Base_Hospital:
            case District_General_Hospital:
            case Divisional_Hospital:
            case National_Hospital:
            case Teaching_Hospital:
            case Primary_Medical_Care_Unit:
                return unit;
            case Clinic:
            case MOH_Office:
            case Ministry_of_Health:
            case Other:
            case Partner:

            case Private_Sector_Institute:
            case Provincial_Department_of_Health_Services:
            case Regional_Department_of_Health_Department:
            case Stake_Holder:
            case Unit:
            case Ward:
            default:
                if (unit.getParent() != null) {
                    return findHospital(unit.getParent());
                } else {
                    return null;
                }
        }
    }

    public void addGnToPmc() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No PMC is selected");
            return;
        }
        if (area == null) {
            JsfUtil.addErrorMessage("No GN is selected");
            return;
        }
        area.setPmci(selected);
        getAreaFacade().edit(area);
        area = null;
        fillGnAreasOfSelected();
        JsfUtil.addSuccessMessage("Successfully added.");
        userTransactionController.recordTransaction("Add Gn To Pmc");
    }

    public String toAddInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        fillItems();
        return "/institution/institution";
    }

    public String toImportInstitution() {
        selected = new Institution();
        userTransactionController.recordTransaction("To Add Institution");
        return "/institution/import";
    }

    public String toEditInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        return "/institution/institution";
    }

    public boolean thisIsAParentInstitution(Institution checkingInstitution) {
        boolean flag = false;
        if (checkingInstitution == null) {
            return false;
        }
        for (Institution i : getItems()) {
            if (i.getParent() != null && i.getParent().equals(checkingInstitution)) {
                flag = true;
                return flag;
            }
        }
        return flag;
    }

    public String deleteInstitution() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select");
            return "";
        }
        if (thisIsAParentInstitution(deleting)) {
            JsfUtil.addErrorMessage("Can't delete. This has child institutions.");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetirer(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        JsfUtil.addSuccessMessage("Deleted");
        institutionApplicationController.getInstitutions().remove(deleting);
        fillItems();
        return "/institution/list";
    }

    public String toListInstitutions() {
        userTransactionController.recordTransaction("To List Institutions");
        return "/institution/list";
    }

    public String toSearchInstitutions() {
        return "/institution/search";
    }

    public void removeGnFromPmc() {
        if (removingArea == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return;
        }
        removingArea.setPmci(null);
        getAreaFacade().edit(removingArea);
        fillGnAreasOfSelected();
        removingArea = null;
        userTransactionController.recordTransaction("Remove Gn From Pmc");
    }

    public void fillGnAreasOfSelected() {
        if (selected == null) {
            gnAreasOfSelected = new ArrayList<>();
            return;
        }
        String j = "select a from Area a where a.retired=false "
                + " and a.type=:t "
                + " and a.pmci=:p "
                + " order by a.name";
        Map m = new HashMap();
        m.put("t", AreaType.GN);
        m.put("p", selected);
        gnAreasOfSelected = areaFacade.findByJpql(j, m);
        userTransactionController.recordTransaction("Fill Gn Areas Of Selected");
    }

    public List<Area> findDrainingGnAreas(Institution ins) {
        List<Area> gns;
        if (ins == null) {
            gns = new ArrayList<>();
            return gns;
        }
        String j = "select a from Area a where a.retired=false "
                + " and a.type=:t "
                + " and a.pmci=:p "
                + " order by a.name";
        Map m = new HashMap();
        m.put("t", AreaType.GN);
        m.put("p", ins);
        gns = areaFacade.findByJpql(j, m);
        return gns;
    }

    public InstitutionController() {
    }

    public Institution getSelected() {
        return selected;
    }

    public void setSelected(Institution selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private InstitutionFacade getFacade() {
        return ejbFacade;
    }

    public List<Institution> findChildrenPmcis(Institution ins) {
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins) && i.isPmci()) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenPmcis(i));
            }
        }
        return tins;
    }

    public List<Institution> findChildrenPmcis(Institution ins, String qry) {
        if (qry == null || qry.trim().equals("")) {
            return null;
        }
        qry = qry.toLowerCase();
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        List<Institution> cins = new ArrayList<>();
        for (Institution i : allIns) {
            if (i.getParent() == null) {
                continue;
            }
            if (i.getParent().equals(ins) && i.isPmci()) {
                cins.add(i);
            }
        }
        List<Institution> tins = new ArrayList<>();
        tins.addAll(cins);
        if (cins.isEmpty()) {
            return tins;
        } else {
            for (Institution i : cins) {
                tins.addAll(findChildrenPmcis(i));
            }
        }
        List<Institution> ttins = new ArrayList<>();
        for (Institution i : tins) {
            if (i.getName().toLowerCase().contains(qry)) {
                ttins.add(i);
            }
        }
        return ttins;
    }

    public List<Institution> findInstitutions(InstitutionType type) {
        return institutionApplicationController.findInstitutions(type);
    }

    public List<Institution> findInstitutions(Area area, InstitutionType type) {
        List<Institution> cins = institutionApplicationController.getInstitutions();
        List<Institution> tins = new ArrayList<>();
        for (Institution i : cins) {
            if (type != null) {
                if (i.getInstitutionType() == null) {
                    continue;
                }
                if (!i.getInstitutionType().equals(type)) {
                    continue;
                }
            }
            if (area.getType() == AreaType.District) {
                if (i.getDistrict() == null) {
                    continue;
                }
                if (i.getDistrict().equals(area)) {
                    tins.add(i);
                }
            } else if (area.getType() == AreaType.Province) {
                if (i.getProvince() == null) {
                    continue;
                }
                if (i.getProvince().equals(area)) {
                    tins.add(i);
                }
            }

        }
        return tins;
    }

    public List<Institution> completeInstitutions(String nameQry) {
        List<InstitutionType> ts = Arrays.asList(InstitutionType.values());
        if (ts == null) {
            ts = new ArrayList<>();
        }
        return fillInstitutions(ts, nameQry, null);
    }

    public List<Institution> completeHlClinics(String nameQry) {
        return fillInstitutions(InstitutionType.Clinic, nameQry, null);
    }

    public List<Institution> completeClinics(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Clinic);
        its.add(InstitutionType.Cardiology_Clinic);
        its.add(InstitutionType.Medical_Clinic);
        its.add(InstitutionType.Other_Clinic);
        its.add(InstitutionType.Surgical_Clinic);
        return fillInstitutions(its, qry, null);
    }

    public List<Institution> completeLab(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.Lab);
        return fillInstitutions(its, qry, null);
    }

    public List<Institution> completeMohs(String qry) {
        List<InstitutionType> its = new ArrayList<>();
        its.add(InstitutionType.MOH_Office);
        return fillInstitutions(its, qry, null);
    }

    public List<InstitutionType> hospitalInstitutionTypes() {
        List<InstitutionType> ts = new ArrayList<>();
        InstitutionType[] ta = InstitutionType.values();
        for (InstitutionType t : ta) {
            switch (t) {
                case Base_Hospital:
                case District_General_Hospital:
                case National_Hospital:
                case Primary_Medical_Care_Unit:
                case Private_Sector_Institute:
                case Teaching_Hospital:
                case Divisional_Hospital:
                    ts.add(t);
                    break;
            }
        }
        return ts;
    }

    public List<Institution> completeHospitals(String nameQry) {
        return fillInstitutions(hospitalInstitutionTypes(), nameQry, null);
    }

    public List<Institution> completeRdhs(String nameQry) {
        return fillInstitutions(InstitutionType.Regional_Department_of_Health_Department, nameQry, null);
    }

    public List<Institution> completePdhs(String nameQry) {
        return fillInstitutions(InstitutionType.Provincial_Department_of_Health_Services, nameQry, null);
    }

    public List<Institution> completeProcedureRooms(String nameQry) {
        return fillInstitutions(InstitutionType.Procedure_Room, nameQry, null);
    }

    public Institution findInstitutionByName(String name) {
        if (name == null || name.trim().equals("")) {
            return null;
        }
        Institution ni = null;
        for (Institution i : institutionApplicationController.getInstitutions()) {
            if (i.getName() != null && i.getName().equalsIgnoreCase(name)) {
                if (ni != null) {
                    // // System.out.println("Duplicate Institution Name : " + name);
                }
                ni = i;
            }
        }
        return ni;
//        String j = "Select i from Institution i where i.retired=:ret ";
//        Map m = new HashMap();
//        if (name != null) {
//            j += " and lower(i.name)=:n ";
//            m.put("n", name.trim().toLowerCase());
//        }
//        m.put("ret", false);
//        return getFacade().findFirstByJpql(j, m);
    }

//    public Institution findInstitutionById(Long id) {
//        String j = "Select i from Institution i where i.retired=:ret ";
//        Map m = new HashMap();
//        if (id != null) {
//            j += " and i.id=:n ";
//            m.put("n", id);
//        }
//        m.put("ret", false);
//        return getFacade().findFirstByJpql(j, m);
//    }
//
//    public List<Institution> completePmcis(String nameQry) {
//        String j = "Select i from Institution i where i.retired=false and i.pmci=true ";
//        Map m = new HashMap();
//        if (nameQry != null) {
//            j += " and lower(i.name) like :n ";
//            m.put("n", "%" + nameQry.trim().toLowerCase() + "%");
//        }
//        j += " order by i.name";
//        return getFacade().findByJpql(j, m);
//    }
    public void fillItems() {
        if (institutionApplicationController.getInstitutions() != null) {
            items = institutionApplicationController.getInstitutions();
            return;
        }
    }

    public void resetAllInstitutions() {
        items = null;
        institutionApplicationController.resetAllInstitutions();
        items = institutionApplicationController.getInstitutions();
    }

    public List<Institution> fillInstitutions(InstitutionType type, String nameQry, Institution parent) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();

        for (Institution i : allIns) {
            boolean canInclude = true;
            if (parent != null) {
                if (i.getParent() == null) {
                    canInclude = false;
                } else {
                    if (!i.getParent().equals(parent)) {
                        canInclude = false;
                    }
                }
            }
            if (type != null) {
                if (i.getInstitutionType() == null) {
                    canInclude = false;
                } else {
                    if (!i.getInstitutionType().equals(type)) {
                        canInclude = false;
                    }
                }
            }
            if (i.getName() == null || i.getName().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }
            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public List<Institution> fillInstitutions(List<InstitutionType> types, String nameQry, Institution parent) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();

        for (Institution i : allIns) {
            boolean canInclude = true;
            if (parent != null) {
                if (i.getParent() == null) {
                    canInclude = false;
                } else {
                    if (!i.getParent().equals(parent)) {
                        canInclude = false;
                    }
                }
            }
            boolean typeFound = false;
            for (InstitutionType type : types) {
                if (type != null) {
                    if (i.getInstitutionType() != null && i.getInstitutionType().equals(type)) {
                        typeFound = true;
                    }
                }
            }
            if (!typeFound) {
                canInclude = false;
            }
            if (i.getName() == null || i.getName().trim().equals("")) {
                canInclude = false;
            } else {
                if (!i.getName().toLowerCase().contains(nameQry.trim().toLowerCase())) {
                    canInclude = false;
                }
            }
            if (canInclude) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public List<Institution> completeInstitutionsByWords(String nameQry) {
        List<Institution> resIns = new ArrayList<>();
        if (nameQry == null) {
            return resIns;
        }
        if (nameQry.trim().equals("")) {
            return resIns;
        }
        List<Institution> allIns = institutionApplicationController.getInstitutions();
        nameQry = nameQry.trim();
        String words[] = nameQry.split("\\s+");

        for (Institution i : allIns) {
             boolean allWordsMatch = true;

            for (String word : words) {
                boolean thisWordMatch;
                word = word.trim().toLowerCase();
                if (i.getName() != null  && i.getName().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                }else if (i.getSname() != null  && i.getSname().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                }else if (i.getTname() != null  && i.getTname().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                }else{
                    thisWordMatch=false;
                }
                if(thisWordMatch==false){
                    allWordsMatch=false;
                }
            }

            if (allWordsMatch) {
                resIns.add(i);
            }
        }
        return resIns;
    }

    public Institution prepareCreate() {
        selected = new Institution();
        initializeEmbeddableKey();
        return selected;
    }

    public void prepareToAddNewInstitution() {
        selected = new Institution();
    }

    public void prepareToListInstitution() {
        if (webUserController.getLoggedUser() == null) {
            items = null;
        }
        if (webUserController.getLoggedUser().getWebUserRoleLevel() == WebUserRoleLevel.National
                || webUserController.getLoggedUser().getWebUserRoleLevel() == WebUserRoleLevel.Institutional) {
            items = institutionApplicationController.getInstitutions();
        } else {
            items = webUserController.findAutherizedInstitutions();
        }
    }

    public void saveOrUpdateInstitution() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing to select");
            return;
        }
        if (selected.getName() == null || selected.getName().trim().equals("")) {
            JsfUtil.addErrorMessage("Name is required");
            return;
        }

        if (selected.getTname() == null || selected.getTname().trim().equals("")) {
            selected.setTname(selected.getName());
        }

        if (selected.getSname() == null || selected.getSname().trim().equals("")) {
            selected.setSname(selected.getName());
        }

        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreater(webUserController.getLoggedUser());
            getFacade().create(selected);

            institutionApplicationController.getInstitutions().add(selected);
            items = null;
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setEditedAt(new Date());
            selected.setEditer(webUserController.getLoggedUser());
            getFacade().edit(selected);
            items = null;
            JsfUtil.addSuccessMessage("Updates");
        }
    }

    public void save(Institution ins) {
        if (ins == null) {
            return;
        }
        if (ins.getId() == null) {
            ins.setCreatedAt(new Date());
            ins.setCreater(webUserController.getLoggedUser());
            getFacade().create(ins);
            institutionApplicationController.getInstitutions().add(ins);
            items = null;
        } else {
            ins.setEditedAt(new Date());
            ins.setEditer(webUserController.getLoggedUser());
            getFacade().edit(ins);
            items = null;
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionCreated"));
        if (!JsfUtil.isValidationFailed()) {
            institutionApplicationController.getInstitutions().add(selected);
            fillItems();
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("InstitutionDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Institution> getItems() {
        if (items == null) {
            fillItems();
        }
        return items;
    }

    private void persist(PersistAction persistAction, String successMessage) {
        if (selected != null) {
            setEmbeddableKeys();
            try {
                if (persistAction != PersistAction.DELETE) {
                    getFacade().edit(selected);
                } else {
                    getFacade().remove(selected);
                }
                JsfUtil.addSuccessMessage(successMessage);
            } catch (EJBException ex) {
                String msg = "";
                Throwable cause = ex.getCause();
                if (cause != null) {
                    msg = cause.getLocalizedMessage();
                }
                if (msg.length() > 0) {
                    JsfUtil.addErrorMessage(msg);
                } else {
                    JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, ResourceBundle.getBundle("/BundleClinical").getString("PersistenceErrorOccured"));
            }
        }
    }

    public Institution getInstitution(java.lang.Long id) {
        Institution ni = null;
        for (Institution i : institutionApplicationController.getInstitutions()) {
            if (i.getId() != null && i.getId().equals(id)) {
                ni = i;
            }
        }
        return ni;
    }

    public void refreshMyInstitutions() {
        userTransactionController.recordTransaction("refresh My Institutions");
        myClinics = null;
    }

    public List<Institution> getMyClinics() {
        if (myClinics == null) {
            myClinics = new ArrayList<>();
            int count = 0;
            for (Institution i : webUserController.getLoggableInstitutions()) {
                if (i.getInstitutionType().equals(InstitutionType.Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Medical_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Surgical_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Other_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Cardiology_Clinic)
                        || i.getInstitutionType().equals(InstitutionType.Ward_Clinic)) {
                    myClinics.add(i);
                    count++;
                }
                if (count > 50) {
                    return myClinics;
                }
            }
        }
        return myClinics;
    }

    public lk.gov.health.phsp.facade.InstitutionFacade getEjbFacade() {
        return ejbFacade;
    }

    public AreaFacade getAreaFacade() {
        return areaFacade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public List<Area> getGnAreasOfSelected() {
        if (gnAreasOfSelected == null) {
            gnAreasOfSelected = new ArrayList<>();
        }
        return gnAreasOfSelected;
    }

    public void setGnAreasOfSelected(List<Area> gnAreasOfSelected) {
        this.gnAreasOfSelected = gnAreasOfSelected;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Area getRemovingArea() {
        return removingArea;
    }

    public void setRemovingArea(Area removingArea) {
        this.removingArea = removingArea;

    }

    public void setMyClinics(List<Institution> myClinics) {
        this.myClinics = myClinics;
    }

    public Institution getDeleting() {
        return deleting;
    }

    public void setDeleting(Institution deleting) {
        this.deleting = deleting;
    }

    public ApplicationController getApplicationController() {
        return applicationController;
    }

    public void setApplicationController(ApplicationController applicationController) {
        this.applicationController = applicationController;
    }

    public UserTransactionController getUserTransactionController() {
        return userTransactionController;
    }

    public void setUserTransactionController(UserTransactionController userTransactionController) {
        this.userTransactionController = userTransactionController;
    }

    public InstitutionType getInstitutionType() {
        return institutionType;
    }

    public void setInstitutionType(InstitutionType institutionType) {
        this.institutionType = institutionType;
    }

    public Institution getParent() {
        return parent;
    }

    public void setParent(Institution parent) {
        this.parent = parent;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getPdhsArea() {
        return pdhsArea;
    }

    public void setPdhsArea(Area pdhsArea) {
        this.pdhsArea = pdhsArea;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getRdhsArea() {
        return rdhsArea;
    }

    public void setRdhsArea(Area rdhsArea) {
        this.rdhsArea = rdhsArea;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    @FacesConverter(forClass = Institution.class)
    public static class InstitutionControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            InstitutionController controller = (InstitutionController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "institutionController");
            return controller.getInstitution(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            key = Long.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Long value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Institution) {
                Institution o = (Institution) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Institution.class.getName()});
                return null;
            }
        }

    }

}
