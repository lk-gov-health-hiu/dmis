package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import lk.gov.health.phsp.facade.util.JsfUtil.PersistAction;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.enums.RelationshipType;
import lk.gov.health.phsp.enums.WebUserRole;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Polygon;

@Named
@SessionScoped
public class AreaController implements Serializable {

    @EJB
    private AreaFacade ejbFacade;
    private List<Area> items = null;
    List<Area> mohAreas = null;
    List<Area> phiAreas = null;
    List<Area> rdhsAreas = null;
    List<Area> pdhsAreas = null;

    private List<Area> gnAreas = null;
    private List<Area> dsAreas = null;
    private List<Area> provinces = null;
    private List<Area> districts = null;
    private Area selected;
    private Area deleting;

    private Area district;
    private Area province;
    private Area rdhs;
    private Area pdhs;
    private Area area;

    private Institution rdhsInstitution;
    
    private String bulkText;

    private UploadedFile file;

    @Inject
    private WebUserController webUserController;
    @Inject
    private CommonController commonController;
    @Inject
    private InstitutionController institutionController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    PersonController personController;

    @Inject
    AreaApplicationController areaApplicationController;

    private MapModel polygonModel;

    private String successMessage;
    private String failureMessage;
    private String startMessage;

    private Integer gnNameColumnNumber;
    private Integer gnCodeColumnNumber;
    private Integer gnUidColumnNumber;
    private Integer institutionColumnNumber;
    private Integer dataColumnNumber;
    private Integer dsdNameColumnNumber;
    private Integer districtNameColumnNumber;
    private Integer provinceNameColumnNumber;
    private Integer totalPopulationColumnNumber;
    private Integer malePopulationColumnNumber;
    private Integer femalePopulationColumnNumber;
    private Integer areaColumnNumber;
    private Integer startRow = 1;
    private Integer year;

    private RelationshipType rt;
    private RelationshipType[] rts;

    public String listGnAreas() {
        items = getAreas(AreaType.GN, null);
        return "/area/gn_list";
    }

    public String toAddArea() {
        selected = new Area();
        userTransactionController.recordTransaction("Add Area By SysAdmin");
        return "/area/area";
    }

    public String toEditAreaForSysAdmin() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an Area to Edit");
            return "";
        }
        return "/area/area";
    }

    public String deleteAreaForSysAdmin() {
        if (deleting == null) {
            JsfUtil.addErrorMessage("Please select an Area to Delete");
            return "";
        }
        deleting.setRetired(true);
        deleting.setRetiredAt(new Date());
        deleting.setRetiredBy(webUserController.getLoggedUser());
        getFacade().edit(deleting);
        items = null;
        deleting = null;
        return toListAreasForSysAdmin();
    }

    public String saveOrUpdateAreaForSystemAdmin() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an Area");
            return "";
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setLastEditBy(webUserController.getLoggedUser());
            selected.setLastEditeAt(new Date());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        items = null;
        selected = null;
        userTransactionController.recordTransaction("save Or Update Area For SystemAdmin");
        return toListAreasForSysAdmin();
    }

    public String saveOrUpdatePhiAreaForIndAdmin() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Please select an Area");
            return "";
        }
        if (selected.getId() == null) {
            selected.setCreatedAt(new Date());
            selected.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(selected);
            JsfUtil.addSuccessMessage("Saved");
        } else {
            selected.setLastEditBy(webUserController.getLoggedUser());
            selected.setLastEditeAt(new Date());
            getFacade().edit(selected);
            JsfUtil.addSuccessMessage("Updated");
        }
        items = null;
        selected = null;
        areaApplicationController.invalidateItems();
        return toListPhiAreasForInsAdmin();
    }

    
    public String toListAreasForSysAdmin() {
        String j = "select a from Area a where a.retired=:ret order by a.name";
        Map m = new HashMap();
        m.put("ret", false);
        items = getFacade().findByJpql(j, m);
        userTransactionController.recordTransaction("List Areas By SysAdmin");
        return "/area/list";
    }
    
    public String toListPhiAreasForInsAdmin() {
        items = areaApplicationController.listPhiAreasOfMoh(webUserController.getLoggedInstitution().getMohArea());
        return "/area/list_phi";
    }

    public String toBrowseAreasForSysAdmin() {
        userTransactionController.recordTransaction("Browse Areas By SysAdmin");
        return "/area/browse";
    }

    public String toSearchAreasForSysAdmin() {
        userTransactionController.recordTransaction("Search Areas By SysAdmin");
        return "/area/search";
    }

  

    public List<Area> getMohAreas() {
        if (mohAreas == null) {
            mohAreas = getAreas(AreaType.MOH, null);
        }
        return mohAreas;
    }

    public List<Area> getMohAreas(Area district) {
        mohAreas = getAreas(AreaType.MOH, district);

        return mohAreas;
    }

    public List<Area> getMohAreasOfADistrict(Area district) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";

        if (district != null) {
            j += " and a.district=:pa ";
            m.put("pa", district);
        }
        j += " order by a.name";

        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public List<Area> getMohAreasOfRdhs(Area rdhs) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        if (rdhs != null) {
            j += " and a.rdhsArea=:pa ";
            m.put("pa", rdhs);
        }
        j += " order by a.name";

        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public void setMohAreas(List<Area> mohAreas) {
        this.mohAreas = mohAreas;
    }

    public List<Area> getPhiAreas() {
        if (phiAreas == null) {
            phiAreas = getAreas(AreaType.PHI, null);
        }
        return phiAreas;
    }

    public void setPhiAreas(List<Area> phiAreas) {
        this.phiAreas = phiAreas;
    }

    public List<Area> getRdhsAreas() {
        if (rdhsAreas == null) {
            rdhsAreas = getAreas(AreaType.District, null);
        }
        return rdhsAreas;
    }

    public List<Area> rdhsAreas(Area province) {
        return getAreas(AreaType.District, province);
    }

    public void setRdhsAreas(List<Area> rdhsAreas) {
        this.rdhsAreas = rdhsAreas;
    }

    public List<Area> getPdhsAreas() {
        if (pdhsAreas == null) {
            pdhsAreas = getAreas(AreaType.Province, null);
        }
        return pdhsAreas;
    }

    public void setPdhsAreas(List<Area> pdhsAreas) {
        this.pdhsAreas = pdhsAreas;
    }

    public Area getAreaById(Long id) {
        return getFacade().find(id);
    }

    public List<Area> getGnAreasOfMoh(Area mohArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.GN);
        j += " and a.moh=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public Area getNationalArea() {
        String j = "select a from Area a "
                + " where "
                + " a.type=:t "
                + " and a.retired=false"
                + " order by a.id desc";
        Map m = new HashMap();
        m.put("t", AreaType.National);
        Area a = getFacade().findFirstByJpql(j, m);
        if (a == null) {
            a = new Area();
            a.setName("Sri Lanka");
            a.setCode("LK");
            a.setType(AreaType.National);
            a.setCreatedAt(new Date());
            a.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(a);
            List<Area> ps = getAreas(AreaType.Province, null);
            for (Area p : ps) {
                p.setParentArea(a);
                getFacade().edit(p);
            }
        }
        return a;
    }

    public List<Area> getGnAreasOfPhm(Area mohArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.PHM);
        j += " and a.moh=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public List<Area> getDistrictsOfAProvince(Area province) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.District);
        j += " and a.parentArea=:p ";
        m.put("p", province);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

    public List<Area> getPhmAreasOfMoh(Area mohArea) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        j += " and a.type=:t";
        m.put("t", AreaType.PHM);
        j += " and a.moh=:moh ";
        m.put("moh", mohArea);
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

   
    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    

    public String toAddProvince() {
        selected = new Area();
        selected.setType(AreaType.Province);
        return "/area/add_province";
    }

    public String toAddDistrict() {
        selected = new Area();
        selected.setType(AreaType.District);
        return "/area/add_district";
    }

    public String toAddMhoArea() {
        selected = new Area();
        selected.setType(AreaType.MOH);
        return "/area/add_moh";
    }

    public String toEducationalZones() {
        selected = new Area();
        return "/area/add_educational_zones";
    }

    public String toAddPhiArea() {
        selected = new Area();
        selected.setType(AreaType.PHI);
        return "/area/add_phi";
    }
    
    public String toAddPhiAreaForInsAdmin() {
        selected = new Area();
        selected.setType(AreaType.PHI);
        selected.setParentArea(webUserController.getLoggedInstitution().getMohArea());
        selected.setMoh(webUserController.getLoggedInstitution().getMohArea());
        selected.setDistrict(webUserController.getLoggedInstitution().getDistrict());
        selected.setRdhsArea(webUserController.getLoggedInstitution().getRdhsArea());
        selected.setProvince(webUserController.getLoggedInstitution().getProvince());
        selected.setPdhsArea(webUserController.getLoggedInstitution().getProvince());
        return "/area/area_phi_ins";
    }

    public String toAddGnArea() {
        selected = new Area();
        selected.setType(AreaType.GN);
        return "/area/add_gn";
    }

    public String saveNewProvince() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New Province Saved");
        return "/area/index";
    }

    public String saveNewDistrict() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New District Saved");
        return "/area/index";
    }

    public String saveNewMoh() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;

        JsfUtil.addSuccessMessage("New MOH Area Saved");
        return "/area/index";
    }

    public String saveNewEducationalZone() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New Educational Zone Saved");
        return "/area/index";
    }

    public String saveNewPhi() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New PHI Area Saved");
        return "/area/index";
    }

    public String saveNewGn() {
        selected.setCreatedAt(new Date());
        getFacade().create(selected);
        selected = null;
        items = null;
        JsfUtil.addSuccessMessage("New GN Area Saved");
        return "/area/index";
    }

    public List<Area> getAreas(Area superArea) {
        return getAreas(null,superArea, null);
    }
    
    public List<Area> getAreas(AreaType areaType, Area superArea) {
        return getAreas(areaType, superArea, null);
    }

    public List<Area> getAreas(AreaType areaType, Area parentArea, Area grandParentArea) {
        return getAreas(areaType, parentArea, grandParentArea, null);
    }

    public List<Area> getAreas(AreaType areaType, Area parentArea, Area grandParentArea, String qry) {
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.name is not null ";
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        if (parentArea != null) {
            j += " and a.parentArea=:pa ";
            m.put("pa", parentArea);
        }
        if (grandParentArea != null) {
            j += " and a.parentArea.parentArea=:gpa ";
            m.put("gpa", grandParentArea);
        }
        if (qry != null) {
            j += " and lower(a.name) like :qry ";
            m.put("qry", "%" + qry.toLowerCase() + "%");
        }
        j += " order by a.name";
        List<Area> areas = getFacade().findByJpql(j, m);
        return areas;
    }

//    public List<Area> getAreas(AreaType areaType, Area parentArea, String qry) {
//        String j;
//        Map m = new HashMap();
//        j = "select a "
//                + " from Area a "
//                + " where a.name is not null ";
//        if (areaType != null) {
//            j += " and a.type=:t";
//            m.put("t", areaType);
//        }
//        if (parentArea != null) {
//            j += " and a.parentArea=:pa ";
//            m.put("pa", parentArea);
//        }
//        if (qry != null) {
//            j += " and lower(a.name) like :qry ";
//            m.put("qry", "%" + qry.toLowerCase() + "%");
//        }
//        j += " order by a.name";
//        List<Area> areas = getFacade().findByJpql(j, m);
//        return areas;
//    }
    public List<Area> completeProvinces(String qry) {
        return getAreas(qry, AreaType.Province);
    }

    public List<Area> completeDistricts(String qry) {
        return getAreas(qry, AreaType.District);
    }

    public List<Area> completeMoh(String qry) {
        return getAreas(qry, AreaType.MOH);
    }

    public List<Area> completePhm(String qry) {
        return getAreas(qry, AreaType.PHM);
    }

    public List<Area> completeGn(String qry) {
        return getAreas(qry, AreaType.GN);
    }

    public List<Area> completeAreas(String qry) {
        return getAreas(qry, null);
    }

    public List<Area> completeDsAreas(String qry) {
        return getAreas(qry, AreaType.DsArea);
    }

    public List<Area> completeGnAreas(String qry) {
        return getAreas(qry, AreaType.GN);
    }

    public List<Area> completePdhsAreas(String qry) {
        return getAreas(qry, AreaType.PdhsArea);
    }

    public List<Area> completeRdhsAreas(String qry) {
        return getAreas(qry, AreaType.RdhsAra);
    }

    public List<Area> completePhiAreas(String qry) {
        return getAreas(qry, AreaType.PHI);
    }

    public List<Area> completeMohAreas(String qry) {
        return getAreas(qry, AreaType.MOH);
    }

    public List<Area> completePhmAreas(String qry) {
        return getAreas(qry, AreaType.PHM);
    }

    public List<Area> getAreas(String qry, AreaType areaType) {
        if (areaType != null) {
            switch (areaType) {
                case District:
                    return areaApplicationController.completeDistricts(qry);
                case GN:
                    return areaApplicationController.completeGnAreas(qry);
                case MOH:
                    return areaApplicationController.completeMohAreas(qry);
                case PHI:
                    return areaApplicationController.completePhiAreas(qry);
                case PdhsArea:
                    return areaApplicationController.completePdhsAreas(qry);
                case Province:
                    return areaApplicationController.completeProvinces(qry);
                case RdhsAra:
                    return areaApplicationController.completeRdhsAreas(qry);
            }
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.name) like :n   ";
        m.put("n", "%" + qry.toUpperCase() + "%");
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.code";
        return getFacade().findByJpql(j, m, 30);
    }

    public Area getAreaByCode(String code, AreaType areaType) {
        if (code.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.retired=:ret "
                + " and upper(a.code)=:n  ";
        m.put("n", code.toUpperCase());
        m.put("ret", false);
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.id desc";
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getAreaByUid(Long code, AreaType areaType) {
        if (code == null) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where a.retired=:ret "
                + " and a.areauid=:n  ";
        m.put("n", code);
        m.put("ret", false);
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.id desc";
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getAreaByName(String nameOrCode, AreaType areaType, boolean createNew, Area parentArea) {
        if (nameOrCode.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.name)=:n  ";
        m.put("n", nameOrCode.toUpperCase());
        if (areaType != null) {
            j += " and a.type=:t";
            m.put("t", areaType);
        }
        j += " order by a.code";

        Area ta = getFacade().findFirstByJpql(j, m);
        if (ta == null && createNew) {
            ta = new Area();
            ta.setName(nameOrCode);
            ta.setType(areaType);
            ta.setCreatedAt(new Date());
            ta.setCreatedBy(webUserController.getLoggedUser());
            ta.setParentArea(parentArea);
            getFacade().create(ta);
        }
        return ta;
    }

    public Area getGnAreaByCode(String code) {
        AreaType areaType = AreaType.GN;
        if (code.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.code)=:n  ";
        m.put("n", code.toUpperCase());

        j += " and a.type=:t";
        m.put("t", areaType);
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getGnAreaByName(String name) {
        AreaType areaType = AreaType.GN;
        if (name.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.name)=:n  ";
        m.put("n", name.toUpperCase());

        j += " and a.type=:t";
        m.put("t", areaType);
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getGnAreaByNameAndCode(String name, String code) {
        AreaType areaType = AreaType.GN;
        if (name.trim().equals("")) {
            return null;
        }
        String j;
        Map m = new HashMap();
        j = "select a "
                + " from Area a "
                + " where upper(a.name)=:n "
                + " and upper(a.code)=:c ";
        m.put("n", name.toUpperCase());
        m.put("c", code.toUpperCase());
        j += " and a.type=:t";
        m.put("t", areaType);
        Area ta = getFacade().findFirstByJpql(j, m);
        return ta;
    }

    public Area getAreaByCodeIfNotName(String nameOrCode, AreaType areaType) {
        if(nameOrCode==null){
            return null;
        }
        if (nameOrCode.trim().equals("")) {
            return null;
        }
        Area ta = null;
        for(Area a: areaApplicationController.getAllAreas()){
            if(areaType==null || (a.getType()!=null && a.getType().equals(areaType))){
                if(a.getName()!=null && a.getName().equalsIgnoreCase(nameOrCode)){
                    return a;
                }
                if(a.getCode()!=null && a.getCode().equals(nameOrCode)){
                    return a;
                }
                if(a.getCode()!=null &&   a.getCode().toLowerCase().contains(nameOrCode.trim().toLowerCase())){
                    ta=a;
                }
                if(a.getName()!=null && a.getName().toLowerCase().contains(nameOrCode.trim().toLowerCase())){
                    ta=a;
                }
            }
        }
        return ta;
    }

    public Area getAreaByCodeAndName(String code, String name, AreaType areaType, boolean createNew, Area parentArea) {
        try {
            if (code.trim().equals("")) {
                return null;
            }
            String j;
            Map m = new HashMap();
            j = "select a "
                    + " from Area a "
                    + " where upper(a.name) =:n and upper(a.code) =:c ";
            m.put("c", code.toUpperCase());
            m.put("n", name.toUpperCase());
            if (areaType != null) {
                j += " and a.type=:t";
                m.put("t", areaType);
            }
            j += " order by a.code";

            Area ta = getFacade().findFirstByJpql(j, m);
            if (ta == null && createNew) {
                ta = new Area();
                ta.setCode(code);
                ta.setType(areaType);
                ta.setCreatedAt(new Date());
                ta.setCreatedBy(webUserController.getLoggedUser());
                ta.setParentArea(parentArea);
                getFacade().create(ta);
            }
            return ta;
        } catch (Exception e) {
            return null;
        }
    }

    public AreaController() {
    }

    public Area getSelected() {
        return selected;
    }

    public void setSelected(Area selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private AreaFacade getFacade() {
        return ejbFacade;
    }

    public Area prepareCreate() {
        selected = new Area();
        initializeEmbeddableKey();
        return selected;
    }

    public void create() {
        persist(PersistAction.CREATE, "Created");
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
            provinces = null;
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, "Updated");
    }

    public void destroy() {
        persist(PersistAction.DELETE, "Deleted");
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
            provinces = null;
        }
    }

    public List<Area> getItems() {
//        if (items == null) {
//            items = getFacade().findAll();
//        }
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
                    JsfUtil.addErrorMessage(ex, "Error");
                }
            } catch (Exception ex) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                JsfUtil.addErrorMessage(ex, "Error");
            }
        }
    }

    public List<Area> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Area> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public MapModel getPolygonModel() {
        return polygonModel;
    }

    public void onPolygonSelect(OverlaySelectEvent event) {
        JsfUtil.addSuccessMessage("Selected");
    }

    // <editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public List<Area> getProvinces() {
        if (provinces == null) {
            provinces = getAreas(AreaType.Province, null);
        }
        return provinces;
    }

    public void setProvinces(List<Area> provinces) {
        this.provinces = provinces;
    }

    public List<Area> getDsAreas() {
        if (dsAreas == null) {
            dsAreas = getAreas(AreaType.DsArea, null);
        }
        return dsAreas;
    }

    public void setDsAreas(List<Area> dsAreas) {
        this.dsAreas = dsAreas;
    }

   

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public void setWebUserController(WebUserController webUserController) {
        this.webUserController = webUserController;
    }

    public Integer getGnNameColumnNumber() {
        return gnNameColumnNumber;
    }

    public void setGnNameColumnNumber(Integer gnNameColumnNumber) {
        this.gnNameColumnNumber = gnNameColumnNumber;
    }

    public Integer getGnCodeColumnNumber() {
        return gnCodeColumnNumber;
    }

    public void setGnCodeColumnNumber(Integer gnCodeColumnNumber) {
        this.gnCodeColumnNumber = gnCodeColumnNumber;
    }

    public Integer getDsdNameColumnNumber() {
        return dsdNameColumnNumber;
    }

    public void setDsdNameColumnNumber(Integer dsdNameColumnNumber) {
        this.dsdNameColumnNumber = dsdNameColumnNumber;
    }

    public Integer getDistrictNameColumnNumber() {
        return districtNameColumnNumber;
    }

    public void setDistrictNameColumnNumber(Integer districtNameColumnNumber) {
        this.districtNameColumnNumber = districtNameColumnNumber;
    }

    public Integer getProvinceNameColumnNumber() {
        return provinceNameColumnNumber;
    }

    public void setProvinceNameColumnNumber(Integer provinceNameColumnNumber) {
        this.provinceNameColumnNumber = provinceNameColumnNumber;
    }

    public Integer getTotalPopulationColumnNumber() {
        return totalPopulationColumnNumber;
    }

    public void setTotalPopulationColumnNumber(Integer totalPopulationColumnNumber) {
        this.totalPopulationColumnNumber = totalPopulationColumnNumber;
    }

    public Integer getMalePopulationColumnNumber() {
        return malePopulationColumnNumber;
    }

    public void setMalePopulationColumnNumber(Integer malePopulationColumnNumber) {
        this.malePopulationColumnNumber = malePopulationColumnNumber;
    }

    public Integer getFemalePopulationColumnNumber() {
        return femalePopulationColumnNumber;
    }

    public void setFemalePopulationColumnNumber(Integer femalePopulationColumnNumber) {
        this.femalePopulationColumnNumber = femalePopulationColumnNumber;
    }

    public Integer getAreaColumnNumber() {
        return areaColumnNumber;
    }

    public void setAreaColumnNumber(Integer areaColumnNumber) {
        this.areaColumnNumber = areaColumnNumber;
    }

    public AreaFacade getEjbFacade() {
        return ejbFacade;
    }

    public void setEjbFacade(AreaFacade ejbFacade) {
        this.ejbFacade = ejbFacade;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
    }

    public List<Area> getGnAreas() {
        gnAreas = getAreas(AreaType.GN, null);
        return gnAreas;
    }

    public List<Area> getGnAreas(Area parentArea, AreaType type) {
        gnAreas = getAreas(AreaType.GN, null);
        return gnAreas;
    }

    public void setGnAreas(List<Area> gnAreas) {
        this.gnAreas = gnAreas;
    }
    
    

    public List<Area> getDistricts() {
        if (districts == null) {
            districts = getAreas(AreaType.District, null);
        }
        return districts;
    }

    public void setDistricts(List<Area> districts) {
        this.districts = districts;
    }

    public CommonController getCommonController() {
        return commonController;
    }

    public Integer getYear() {

        if (year == null || year == 0) {
            year = CommonController.getYear(new Date());
        }
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Area getDeleting() {
        return deleting;
    }

    public void setDeleting(Area deleting) {
        this.deleting = deleting;
    }

    public Integer getGnUidColumnNumber() {
        return gnUidColumnNumber;
    }

    public void setGnUidColumnNumber(Integer gnUidColumnNumber) {
        this.gnUidColumnNumber = gnUidColumnNumber;
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

    public Integer getInstitutionColumnNumber() {
        return institutionColumnNumber;
    }

    public void setInstitutionColumnNumber(Integer institutionColumnNumber) {
        this.institutionColumnNumber = institutionColumnNumber;
    }

    
    
    public InstitutionController getInstitutionController() {
        return institutionController;
    }

    public String getStartMessage() {
        return startMessage;
    }

    public void setStartMessage(String startMessage) {
        this.startMessage = startMessage;
    }

    public Integer getDataColumnNumber() {
        return dataColumnNumber;
    }

    public void setDataColumnNumber(Integer dataColumnNumber) {
        this.dataColumnNumber = dataColumnNumber;
    }

    public RelationshipType[] getRts() {
        if (rts == null) {
            rts = new RelationshipType[]{RelationshipType.Empanelled_Female_Population,
                RelationshipType.Empanelled_Male_Population,
                RelationshipType.Empanelled_Population,
                RelationshipType.Estimated_Midyear_Female_Population,
                RelationshipType.Estimated_Midyear_Male_Population,
                RelationshipType.Estimated_Midyear_Population,
                RelationshipType.Over_35_Female_Population,
                RelationshipType.Over_35_Male_Population,
                RelationshipType.Over_35_Population,
                RelationshipType.Annual_Target_Female_Population,
                RelationshipType.Annual_Target_Male_Population,
                RelationshipType.Annual_Target_Population};
        }
        return rts;
    }

    public void setRts(RelationshipType[] rts) {
        this.rts = rts;
    }

    public RelationshipType getRt() {
        return rt;
    }

    public void setRt(RelationshipType rt) {
        this.rt = rt;
    }

    public Area getDistrict() {
        return district;
    }

    public void setDistrict(Area district) {
        this.district = district;
    }

    public Area getProvince() {
        return province;
    }

    public void setProvince(Area province) {
        this.province = province;
    }

    public Area getRdhs() {
        return rdhs;
    }

    public void setRdhs(Area rdhs) {
        this.rdhs = rdhs;
    }

    public Area getPdhs() {
        return pdhs;
    }

    public void setPdhs(Area pdhs) {
        this.pdhs = pdhs;
    }

    public String getBulkText() {
        return bulkText;
    }

    public void setBulkText(String bulkText) {
        this.bulkText = bulkText;
    }

    public Institution getRdhsInstitution() {
        return rdhsInstitution;
    }

    public void setRdhsInstitution(Institution rdhsInstitution) {
        this.rdhsInstitution = rdhsInstitution;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Converters">
    @FacesConverter(forClass = Area.class)
    public static class AreaControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getFacade().find(getKey(value));
        }

        java.lang.Long getKey(String value) {
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
            } catch (NumberFormatException e) {
                key = 0l;
            }
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Area.class.getName()});
                return null;
            }
        }

    }

    @FacesConverter(value = "areaConverter")
    public static class AreaConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            AreaController controller = (AreaController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "areaController");
            return controller.getFacade().find(getKey(value));
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
            if (object instanceof Area) {
                Area o = (Area) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Area.class.getName()});
                return null;
            }
        }

    }

    // </editor-fold>
}
