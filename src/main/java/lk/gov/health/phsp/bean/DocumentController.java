package lk.gov.health.phsp.bean;

import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DocumentFacade;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import lk.gov.health.phsp.entity.DocumentHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.facade.DocumentHistoryFacade;

@Named
@SessionScoped
public class DocumentController implements Serializable {

    @EJB
    private DocumentFacade documentFacade;

    @EJB
    DocumentHistoryFacade documentHxFacade;

    private List<Document> items = null;
    private List<Document> selectedItems = null;
    private Document selected;
    private DocumentHistory selectedHistory;
    private List<DocumentHistory> selectedDocumentHistories;
    @Inject
    private WebUserController webUserController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    ItemApplicationController itemApplicationController;

    private Institution institution;
    private WebUser webUser;

    public DocumentController() {
    }

    public void addEncounterDateFromEncounterTime() {
        String j = "select e from Encounter e "
                + " where e.encounterDate is null";
        List<Document> es = getFacade().findByJpql(j);
        for (Document e : es) {
            e.setDocumentDate(e.getCreatedAt());
            getFacade().edit(e);
        }
    }

    public String createClinicEnrollNumber(Institution clinic) {
        String j = "select count(e) from Encounter e "
                + " where e.institution=:ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
//        j = "select count(e) from Document e ";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", DocumentType.Register);
        m.put("ins", clinic);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 1l;
        } else {
            c += 1;
        }
        SimpleDateFormat format = new SimpleDateFormat("yy");
        String yy = format.format(new Date());
        return clinic.getCode() + "/" + yy + "/" + c;
    }

    public String createTestNumber(Institution clinic) {
        String j = "select count(e) from Encounter e "
                + " where e.institution=:ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
//        j = "select count(e) from Document e ";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", DocumentType.Letter);
        m.put("ins", clinic);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 1l;
        } else {
            c += 1;
        }
//        SimpleDateFormat format = new SimpleDateFormat("yy");
//        String yy = format.format(new Date());
        if (clinic.getCode() == null || clinic.getCode().trim().equals("")) {
            if (clinic.getName() != null) {
                clinic.setCode(clinic.getName().substring(0, 2));
            }
        }
        return clinic.getCode() + "/" + String.format("%03d", c);
    }

    public String createCaseNumber(Institution clinic) {
        String j = "select count(e) from Encounter e "
                + " where e.institution=:ins "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
//        j = "select count(e) from Document e ";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", DocumentType.File);
        m.put("ins", clinic);
        Long c = getFacade().findLongByJpql(j, m);
        if (c == null) {
            c = 1l;
        } else {
            c += 1;
        }
//        SimpleDateFormat format = new SimpleDateFormat("yy");
//        String yy = format.format(new Date());
        return clinic.getCode() + "/" + String.format("%03d", c);
    }

    public Long countOfEncounters(List<Institution> clinics, DocumentType ec) {
        String j = "select count(e) from Encounter e "
                + " where e.retired=:ret "
                + " and e.encounterType=:ec "
                + " and e.createdAt>:d";
        Map m = new HashMap();
        m.put("d", CommonController.startOfTheYear());
        m.put("ec", ec);
        m.put("ret", false);
        if (clinics != null && !clinics.isEmpty()) {
            m.put("ins", clinics);
            j += " and e.institution in :ins ";
        }
        Long c = getFacade().findLongByJpql(j, m);
        return c;
    }

    public Document getInstitutionTypeEncounter(Institution institution, DocumentType ec, Date d) {
        String j = "select e from Encounter e "
                + " where e.encounterType=:ec "
                + " and e.institution=:ins "
                + " and e.encounterDate=:d";
        Map m = new HashMap();
        m.put("ins", institution);
        m.put("ec", ec);
        m.put("d", d);
        Document e = getFacade().findFirstByJpql(j, m);
        if (e == null) {
            e = new Document();
            e.setDocumentDate(d);
            e.setDocumentType(ec);
            e.setInstitution(institution);
            e.setCreatedInstitution(institution);
            e.setCreatedAt(new Date());
            e.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(e);
        } else {
            e.setRetired(true);
            getFacade().edit(e);
        }
        return e;
    }

    public void retireSelectedEncounter() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing Selected");
            return;
        }
        selected.setRetired(true);
        selected.setRetiredAt(new Date());
        selected.setRetiredBy(webUserController.getLoggedUser());
        JsfUtil.addSuccessMessage("Retired Successfully");
        userTransactionController.recordTransaction("Retire Selected Encounter");
        selected = null;
    }

    public Document getSelected() {
        return selected;
    }

    public void setSelected(Document selected) {
        this.selected = selected;
    }

    protected void setEmbeddableKeys() {
    }

    protected void initializeEmbeddableKey() {
    }

    private DocumentFacade getFacade() {
        return documentFacade;
    }

    public Document prepareCreate() {
        selected = new Document();
        initializeEmbeddableKey();
        return selected;
    }

    public void save() {
        save(selected);
    }

    public void transferOutFile() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select an institution to transfer out");
            return;
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return;
        }

        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.File_Institution_Transfer);
        docHx.setDocument(selected);
        docHx.setFromInstitution(selected.getCurrentInstitution());
        docHx.setToInstitution(institution);
        saveDocumentHx(docHx);

        JsfUtil.addSuccessMessage("Transferred out successfully");
    }

    public void transferOutOwnershipFile() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select an institution to transfer out");
            return;
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return;
        }

        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.File_Owner_Transfer);
        docHx.setDocument(selected);
        docHx.setFromUser(selected.getCurrentOwner());
        docHx.setToUser(webUser);

        saveDocumentHx(docHx);

        JsfUtil.addSuccessMessage("Transferred out successfully");
    }

    public String saveAndViewFile() {
        boolean newHx = false;
        if (selected.getId() == null) {
            newHx = true;
        }
        save(selected);
        if (newHx) {
            if (selectedHistory == null) {
                selectedHistory = new DocumentHistory();
                selectedHistory.setHistoryType(HistoryType.File_Created);
            }
            selectedHistory.setToInstitution(selected.getCurrentInstitution());
            selectedHistory.setToUser(selected.getCurrentOwner());
            selectedHistory.setCompleted(true);
            selectedHistory.setCompletedAt(new Date());
            selectedHistory.setCompletedBy(webUserController.getLoggedUser());
            selectedHistory.setDocument(selected);
            saveDocumentHx(selectedHistory);
        }
        return viewFile();
    }

    public void saveDocumentHx(DocumentHistory hx) {
        if (hx == null) {
            return;
        }
        if (hx.getId() == null) {
            hx.setCreatedAt(new Date());
            hx.setCreatedBy(webUserController.getLoggedUser());
            documentHxFacade.create(hx);
        } else {
            documentHxFacade.edit(hx);
        }
    }

    public String viewFile() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.document=:doc "
                + " order by h.id";
        Map m = new HashMap();
        m.put("doc", selected);
        selectedDocumentHistories = documentHxFacade.findByJpql(j, m);
        return "/document/file_view";
    }

    public void save(Document e) {
        if (e == null) {
            return;
        }
        if (e.getId() == null) {
            e.setCreatedAt(new Date());
            e.setCreatedBy(webUserController.getLoggedUser());
            getFacade().create(e);
        } else {
            e.setLastEditBy(webUserController.getLoggedUser());
            e.setLastEditeAt(new Date());
            getFacade().edit(e);
        }
    }

    public void create() {
        persist(PersistAction.CREATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterCreated"));
        if (!JsfUtil.isValidationFailed()) {
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public void update() {
        persist(PersistAction.UPDATE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterUpdated"));
    }

    public void destroy() {
        persist(PersistAction.DELETE, ResourceBundle.getBundle("/BundleClinical").getString("EncounterDeleted"));
        if (!JsfUtil.isValidationFailed()) {
            selected = null; // Remove selection
            items = null;    // Invalidate list of items to trigger re-query.
        }
    }

    public List<Document> getItems(String jpql, Map m) {
        return getFacade().findByJpql(jpql, m);
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

    public Document getEncounter(java.lang.Long id) {
        return getFacade().find(id);
    }

    public List<Document> getItemsAvailableSelectMany() {
        return getFacade().findAll();
    }

    public List<Document> getItemsAvailableSelectOne() {
        return getFacade().findAll();
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public lk.gov.health.phsp.facade.DocumentFacade getDocumentFacade() {
        return documentFacade;
    }

    public List<Document> getItems() {
        return items;
    }

    public void setItems(List<Document> items) {
        this.items = items;
    }

    public List<Document> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(List<Document> selectedItems) {
        this.selectedItems = selectedItems;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public DocumentHistory getSelectedHistory() {
        return selectedHistory;
    }

    public void setSelectedHistory(DocumentHistory selectedHistory) {
        this.selectedHistory = selectedHistory;
    }

    public WebUser getWebUser() {
        return webUser;
    }

    public void setWebUser(WebUser webUser) {
        this.webUser = webUser;
    }

    public List<DocumentHistory> getSelectedDocumentHistories() {
        return selectedDocumentHistories;
    }

    public void setSelectedDocumentHistories(List<DocumentHistory> selectedDocumentHistories) {
        this.selectedDocumentHistories = selectedDocumentHistories;
    }
    
    

    @FacesConverter(forClass = Document.class)
    public static class EncounterControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            DocumentController controller = (DocumentController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "encounterController");
            return controller.getEncounter(getKey(value));
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
            if (object instanceof Document) {
                Document o = (Document) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Document.class.getName()});
                return null;
            }
        }

    }

}
