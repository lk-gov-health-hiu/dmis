package lk.gov.health.phsp.bean;

import java.io.IOException;
import java.io.InputStream;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.bean.util.JsfUtil.PersistAction;
import lk.gov.health.phsp.facade.DocumentFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.entity.DocumentHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.enums.SearchFilterType;
import lk.gov.health.phsp.facade.DocumentHistoryFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.pojcs.InstitutionCount;
import lk.gov.health.phsp.pojcs.Nameable;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.file.UploadedFile;

@Named
@SessionScoped
public class LetterController implements Serializable {

    @EJB
    private DocumentFacade documentFacade;

    @EJB
    DocumentHistoryFacade documentHxFacade;

    @EJB
    InstitutionFacade institutionFacade;

    @EJB
    WebUserFacade webUserFacade;

    @EJB
    UploadFacade uploadFacade;

    private List<Document> items = null;
    private List<Document> selectedItems = null;
    private Document selected;
    private DocumentHistory selectedHistory;
    private DocumentHistory deletingHistory;
    private List<DocumentHistory> selectedDocumentHistories;
    private List<Upload> selectedUploads;
    private List<DocumentHistory> documentHistories;
    private List<DocumentHistory> listedToAcceptCopyForwards;
    private List<InstitutionCount> institutionCounts;
    DocumentHistory selectedToAcceptCopyForwards;
    @Inject
    private WebUserController webUserController;
    @Inject
    private UserTransactionController userTransactionController;
    @Inject
    ItemApplicationController itemApplicationController;
    @Inject
    MenuController menuController;
    @Inject
    InstitutionApplicationController institutionApplicationController;
    @Inject
    WebUserApplicationController webUserApplicationController;

    private Institution institution;
    private WebUser webUser;
    private Nameable webUserCopy;
    private Nameable searchUserOrIns;
    private Item minute;
    private String searchTerm;
    private String comments;
    private Date fromDate;
    private Date toDate;
    private SearchFilterType searchFilterType;

    private UploadedFile file;

    private Upload removingUpload;

    private boolean newHx;

    public LetterController() {
    }

    public String removeUpload() {
        if (removingUpload == null) {
            JsfUtil.addErrorMessage("Nothing to remove");
            return "";
        }
        uploadFacade.remove(removingUpload);
        return toLetterView();
    }

    public void saveCurrentDocument() {
        System.out.println("saveCurrentDocument = " + selected);
        if (selected == null) {
            return;
        }
        save(selected);
    }

    public void saveCurrentDocumentAjax(AjaxBehaviorEvent event) {
        System.out.println("saveCurrentDocumentAjax = " + selected);
        if (selected == null) {
            return;
        }
        save(selected);
    }

    public List<Nameable> completeInsOrUsersByWords(String nameQry) {
        List<Nameable> resIns = new ArrayList<>();
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
                if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getSname() != null && i.getSname().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else if (i.getTname() != null && i.getTname().toLowerCase().contains(word)) {
                    thisWordMatch = true;
                } else {
                    thisWordMatch = false;
                }
                if (thisWordMatch == false) {
                    allWordsMatch = false;
                }
            }

            if (allWordsMatch) {
                resIns.add(i);
            }
        }

        List<WebUser> allUsrs = webUserApplicationController.getItems();

        for (WebUser i : allUsrs) {

            if (i.isPubliclyListed()) {
                boolean allWordsMatch = true;

                for (String word : words) {
                    boolean thisWordMatch;
                    word = word.trim().toLowerCase();
                    if (i.getName() != null && i.getName().toLowerCase().contains(word)) {
                        thisWordMatch = true;
                    } else if (i.getCode() != null && i.getCode().toLowerCase().contains(word)) {
                        thisWordMatch = true;
                    } else if (i.getPerson() != null && i.getPerson().getName() != null && i.getPerson().getName().toLowerCase().contains(word)) {
                        thisWordMatch = true;
                    } else {
                        thisWordMatch = false;
                    }
                    if (thisWordMatch == false) {
                        allWordsMatch = false;
                    }
                }
                if (allWordsMatch) {
                    resIns.add(i);
                }
            }

        }

        resIns.sort(Comparator.comparing(Nameable::getName));

        return resIns;

    }

    public String deleteDocumentHistory() {
        if (deletingHistory == null) {
            JsfUtil.addErrorMessage("Nothing to delete");
            return "";
        }
        deletingHistory.setRetired(true);
        deletingHistory.setRetiredAt(new Date());
        deletingHistory.setRetiredBy(webUserController.getLoggedUser());
        saveDocumentHx(deletingHistory);
        return toLetterView();
    }

    public String uploadLetterImageOrPdf() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Nothing selected");
            return "";
        }
        if (file == null) {
            JsfUtil.addErrorMessage("No file");
            return "";
        }
        try {
            InputStream input = file.getInputStream();
            byte[] bytes = IOUtils.toByteArray(input);
            Upload u = new Upload();
            u.setDocument(selected);
            u.setBaImage(bytes);
            u.setInstitution(webUserController.getLoggedInstitution());
            u.setFileName(file.getFileName());
            u.setFileType(file.getContentType());
            save(u);
        } catch (IOException ex) {
            Logger.getLogger(LetterController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return toLetterView();
    }

    private void save(Upload up) {
        if (up == null) {
            return;
        }
        if (up.getId() == null) {
            up.setCreatedAt(new Date());
            up.setCreater(webUserController.getLoggedUser());
            uploadFacade.create(up);
        } else {
            uploadFacade.edit(up);
        }
    }

    public void searchLetter() {
        System.out.println("searchLetter");
        if (searchTerm == null || searchTerm.trim().equals("")) {
            JsfUtil.addErrorMessage("No Search Term");
            return;
        }
        String j;
        Map m;

        String noSpaceStr = searchTerm.replaceAll("\\s", ""); // using built in method  

        Long tid = null;
        try {
            tid = Long.valueOf(noSpaceStr);

        } catch (Exception e) {
            tid = null;
        }

        if (tid != null && tid != 0l) {
            j = "select d "
                    + " from Document d "
                    + " where d.id=:tid ";
            m = new HashMap();
            m.put("tid", tid);
            System.out.println("By ID");
            System.out.println("m = " + m);
            System.out.println("j = " + j);
            items = documentFacade.findByJpql(j, m);
            System.out.println("items = " + items.size());
            if (items != null && !items.isEmpty()) {
                return;
            }
        }

        j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.documentNumber=:dn"
                + " order by d.documentDate desc";

        m = new HashMap();
        m.put("dt", DocumentType.Letter);
        m.put("dn", searchTerm.trim());
        items = documentFacade.findByJpql(j, m);
        System.out.println("By Name");
        System.out.println("m = " + m);
        System.out.println("j = " + j);

        if (items != null && !items.isEmpty()) {
            System.out.println("items = " + items.size());
            return;
        }

        j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.institution=:ins "
                + " and (d.documentNumber like :dn "
                + " or d.documentName like :dn "
                + " or d.documentCode like :dn "
                + " or d.registrationNo like :dn "
                + " or d.senderName like :dn)"
                + " order by d.documentDate desc";

        m = new HashMap();
        m.put("dt", DocumentType.Letter);
        m.put("ins", webUserController.getLoggedInstitution());
        m.put("dn", "%" + searchTerm.trim() + "%");
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        items = documentFacade.findByJpql(j, m);
        System.out.println("items = " + items.size());
    }

    public void searchLetterByInsOrUser() {
        if (searchUserOrIns == null) {
            JsfUtil.addErrorMessage("No Search User or Institution");
            return;
        }

        String j;
        Map m = new HashMap();

        j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.institution=:ins ";

        m.put("ins", webUserController.getLoggedInstitution());
        m.put("dt", DocumentType.Letter);
        if (searchUserOrIns instanceof WebUser) {
            j += " and d.fromWebUser=:fu ";
            m.put("fu", (WebUser) searchUserOrIns);
        } else if (searchUserOrIns instanceof Institution) {
            j += " and d.fromInstitution=:fu ";
            m.put("fu", (Institution) searchUserOrIns);
        }

        j += " order by d.documentDate desc";

        items = documentFacade.findByJpql(j, m);

    }

    public void searchLetterByDate() {
        if (fromDate == null || toDate == null) {
            JsfUtil.addErrorMessage("Please select both From Date and To Date");
            return;
        }
        if (searchFilterType == null) {
            searchFilterType = SearchFilterType.DOCUMENT_DATE;
        }
        String j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.institution=:ins ";
        j += " and (d." + searchFilterType.getCode() + " between :fd and :td ) ";
        j += " order by d." + searchFilterType.getCode() + " desc";
        Map m = new HashMap();
        m.put("dt", DocumentType.Letter);
        m.put("ins", webUserController.getLoggedInstitution());
        m.put("fd", fromDate);
        m.put("td", toDate);
        items = documentFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public String toListLetters() {
        items = null;
        return "/document/letter_list";
    }

    public void listLetters() {
        if (searchFilterType == null) {
            searchFilterType = SearchFilterType.SYSTEM_DATE;
        }
        String j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.institution=:ins ";
        j += " and (d." + searchFilterType.getCode() + " between :fd and :td ) ";
        j += " order by d." + searchFilterType.getCode();
        Map m = new HashMap();
        m.put("dt", DocumentType.Letter);
        m.put("ins", webUserController.getLoggedInstitution());
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        items = documentFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public void listLettersReceived() {
        if (searchFilterType == null) {
            searchFilterType = SearchFilterType.SYSTEM_DATE;
        }
        String j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.institution=:ins ";
        j += " and (d." + searchFilterType.getCode() + " between :fd and :td ) ";
        j += " order by d." + searchFilterType.getCode();
        Map m = new HashMap();
        m.put("dt", DocumentType.Letter);
        m.put("ins", webUserController.getLoggedInstitution());
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        items = documentFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public void listLastLettersReceived() {
        int numberToList = 100;
        searchFilterType = SearchFilterType.SYSTEM_DATE;
        String j = "select d "
                + " from Document d "
                + " where d.retired=false "
                + " and d.documentType=:dt "
                + " and d.institution=:ins ";
        j += " and (d." + searchFilterType.getCode() + " between :fd and :td ) ";
        j += " order by d.id desc";
        Map m = new HashMap();
        m.put("dt", DocumentType.Letter);
        m.put("ins", webUserController.getLoggedInstitution());
        m.put("fd", CommonController.startOfYesterday());
        m.put("td", CommonController.endOfYear());
        items = documentFacade.findByJpql(j, m, TemporalType.TIMESTAMP, numberToList);
    }

    public void fillMyLettersToAccept() {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toUser=:tu "
                + " and h.completed=:c ";
        j += " and h.createdAt between :fd and :td ";
        m.put("tu", webUserController.getLoggedUser());
        m.put("ht", HistoryType.Letter_Assigned);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("c", false);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public Long countMyLettersToAccept(Date fd, Date td) {
        Long c = 0l;
        Map m = new HashMap();
        String j = "select count(h) "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toUser=:tu "
                + " and h.completed=:c ";
        j += " and h.createdAt between :fd and :td ";
        m.put("tu", webUserController.getLoggedUser());
        m.put("ht", HistoryType.Letter_Assigned);
        m.put("fd", fd);
        m.put("td", td);
        m.put("c", false);
        c = documentHxFacade.countByJpql(j, m, TemporalType.TIMESTAMP);
        return c;
    }

    public Long countMyLettersAccepted(Date fd, Date td) {
        Long c = 0l;
        Map m = new HashMap();
        String j = "select count(h) "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toUser=:tu "
                + " and h.completed=:c ";
        j += " and h.createdAt between :fd and :td "
                + " order by h.id desc";
        m.put("tu", webUserController.getLoggedUser());
        m.put("ht", HistoryType.Letter_Assigned);
        m.put("fd", fd);
        m.put("td", td);
        m.put("c", true);
        c = documentHxFacade.countByJpql(j, m, TemporalType.TIMESTAMP);
        return c;
    }

    public void fillLettersToAssign() {
        System.out.println("fillLettersToAssign");
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and "
                + " ("
                + " (h.institution=:ins and h.historyType=:lc) "
                + " or "
                + " ((h.toInstitution=:ins or h.toUser.institution=:ins) and h.historyType=:lr) "
                + " )";
        j += " and h.createdAt between :fd and :td "
                + " order by h.id";

        //and (h.toInstitution=:ti or h.toUser=:tu)
        m.put("ins", webUserController.getLoggedInstitution());

        m.put("lc", HistoryType.Letter_Created);
        m.put("lr", HistoryType.Letter_Copy_or_Forward);

        m.put("fd", fromDate);
        m.put("td", toDate);
        System.out.println("j = " + j);
        System.out.println("m = " + m);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        System.out.println("documentHistories = " + documentHistories.size());
    }

    public void fillCopyForwardedLettersToMeToReceive() {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and (h.toUser=:tu or h.toInstitution=:ti) "
                + " and h.completed=false ";
        j += " and h.createdAt between :fd and :td "
                + " order by h.id";
        m.put("tu", webUserController.getLoggedUser());
        m.put("ti", webUserController.getLoggedInstitution());
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("fd", fromDate);
        m.put("td", toDate);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public void fillMyAcceptedLetters() {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toUser=:tu "
                + " and h.completed=true ";
        j += " and h.createdAt between :fd and :td "
                + " order by h.id";
        m.put("tu", webUserController.getLoggedUser());
        m.put("ht", HistoryType.Letter_Assigned);
        m.put("fd", fromDate);
        m.put("td", toDate);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public void fillMyReceivedLetters() {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and (h.toUser=:tu or h.toInstitution=:ti) "
                + " and h.completed=true ";
        j += " and h.createdAt between :fd and :td "
                + " order by h.id";
        m.put("tu", webUserController.getLoggedUser());
        m.put("ti", webUserController.getLoggedInstitution());
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("fd", fromDate);
        m.put("td", toDate);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
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

    public String transferOutFile() {
        if (institution == null) {
            JsfUtil.addErrorMessage("Select an institution to transfer out");
            return "";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return "";
        }

        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.Letter_Sent);
        docHx.setDocument(selected);
        docHx.setFromInstitution(selected.getCurrentInstitution());
        docHx.setToInstitution(institution);
        docHx.setInstitution(webUserController.getLoggedInstitution());
        saveDocumentHx(docHx);

        selected.setCompleted(false);
        save(selected);

        institution = null;

        JsfUtil.addSuccessMessage("Letter Sent successfully");
        return toLetterView();
    }

    public String assignTo() {
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a letter");
            return "";
        }

        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.Letter_Assigned);
        docHx.setDocument(selected);
        docHx.setFromUser(selected.getCurrentOwner());
        docHx.setToUser(webUser);
        docHx.setItem(minute);
        docHx.setComments(comments);
        docHx.setInstitution(webUserController.getLoggedInstitution());
        saveDocumentHx(docHx);

        selected.setCurrentOwner(webUser);
        selected.setCompleted(false);
        documentFacade.edit(selected);

        comments = "";

        JsfUtil.addSuccessMessage("Letter assigned successfully");
        return toLetterView();
    }

    public String assignMultipleLetters() {
        if (webUser == null) {
            JsfUtil.addErrorMessage("Select a user");
            return "";
        }
        if (selectedDocumentHistories == null || selectedDocumentHistories.isEmpty()) {
            JsfUtil.addErrorMessage("Select letters");
            return "";
        }

        for (DocumentHistory lds : selectedDocumentHistories) {
            DocumentHistory docHx = new DocumentHistory();
            docHx.setHistoryType(HistoryType.Letter_Assigned);
            docHx.setDocument(lds.getDocument());
            docHx.setToUser(webUser);
            docHx.setItem(minute);
            docHx.setInstitution(webUserController.getLoggedInstitution());
            saveDocumentHx(docHx);

            lds.getDocument().setCurrentOwner(webUser);
            lds.getDocument().setCompleted(false);
            documentFacade.edit(lds.getDocument());
        }

        selectedDocumentHistories = null;

        JsfUtil.addSuccessMessage("Letters assigned successfully");
        return toAssignMultipleLetters();
    }

    public String forwardOrCopyTo() {
        if (webUserCopy == null) {
            JsfUtil.addErrorMessage("Select a user to transfer ownership");
            return "";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return "";
        }
        save(selected);
        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.Letter_Copy_or_Forward);
        docHx.setDocument(selected);
        docHx.setFromUser(selected.getCurrentOwner());
        docHx.setToInsOrUser(webUserCopy);
        docHx.setComments(comments);
        docHx.setItem(minute);
        docHx.setInstitution(webUserController.getLoggedInstitution());
        saveDocumentHx(docHx);

        selected.setCompleted(false);
        documentFacade.edit(selected);

        minute = null;
        webUserCopy = null;
        comments = "";

        JsfUtil.addSuccessMessage("Letter copied/forwarded successfully");
        return toLetterView();
    }

    public String forwardOrCopyToAndNew() {
        if (webUserCopy == null) {
            JsfUtil.addErrorMessage("Select a user to transfer ownership");
            return "";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return "";
        }

        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.Letter_Copy_or_Forward);
        docHx.setDocument(selected);
        docHx.setFromUser(selected.getCurrentOwner());
        docHx.setToInsOrUser(webUserCopy);
        docHx.setComments(comments);
        docHx.setItem(minute);
        docHx.setInstitution(webUserController.getLoggedInstitution());

        saveDocumentHx(docHx);

        selected.setCompleted(false);
        documentFacade.edit(selected);

        minute = null;
        webUserCopy = null;
        comments = "";

        JsfUtil.addSuccessMessage("Letter copied/forwarded successfully");
        return menuController.toLetterAddNew();
    }

    public String recordActionTaken() {
        if (comments == null || comments.trim().equals("")) {
            JsfUtil.addErrorMessage("Enter details of the action.");
            return "";
        }
        if (selected == null) {
            JsfUtil.addErrorMessage("Select a file");
            return "";
        }

        DocumentHistory docHx = new DocumentHistory();
        docHx.setHistoryType(HistoryType.Letter_Action_Taken);
        docHx.setDocument(selected);
        docHx.setComments(comments);
        docHx.setInstitution(webUserController.getLoggedInstitution());
        saveDocumentHx(docHx);

        comments = "";

        JsfUtil.addSuccessMessage("Action Recorded");
        return toLetterView();
    }

    public String saveAndView() {
        if (selected.getId() == null) {
            newHx = true;
        }
        save(selected);
        if (newHx) {
            if (selectedHistory == null) {
                selectedHistory = new DocumentHistory();
                selectedHistory.setHistoryType(HistoryType.Letter_Created);
                selectedHistory.setInstitution(webUserController.getLoggedInstitution());
            }
            selectedHistory.setInstitution(webUserController.getLoggedInstitution());
            selectedHistory.setToInstitution(selected.getCurrentInstitution());
            selectedHistory.setCompleted(true);
            selectedHistory.setCompletedAt(new Date());
            selectedHistory.setCompletedBy(webUserController.getLoggedUser());
            selectedHistory.setDocument(selected);
            saveDocumentHx(selectedHistory);
        }
        return toLetterView();
    }

    public String saveAndNew() {
        if (selected.getId() == null) {
            newHx = true;
        }
        save(selected);
        if (newHx) {
            if (selectedHistory == null) {
                selectedHistory = new DocumentHistory();
                selectedHistory.setHistoryType(HistoryType.Letter_Created);
            }
            selectedHistory.setToInstitution(selected.getCurrentInstitution());
            selectedHistory.setCompleted(true);
            selectedHistory.setCompletedAt(new Date());
            selectedHistory.setCompletedBy(webUserController.getLoggedUser());
            selectedHistory.setDocument(selected);
            saveDocumentHx(selectedHistory);
        }
        return menuController.toLetterAddNew();
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

    public String toLetterEdit() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        newHx = false;
        return "/document/letter";
    }

    public String toReportsLetterCopyForwardActions() {
        documentHistories = null;
        return "/institution/letter_copy_forward_register";
    }

    public String toReportsLetterAcceptRegister() {
        documentHistories = null;
        return "/institution/letter_accept_register";
    }

    public String toReportsLetterReceived() {
        documentHistories = null;
        return "/institution/letter_receive_register";
    }

    public String toReportsInstitutionCountsLetters() {
        institutionCounts = null;
        return "/national/institution_counts_letters";
    }

    public String toReportsDailyCountsLetters() {
        institutionCounts = null;
        return "/national/daily_counts_letters";
    }

    public String toAssignMultipleLetters() {
        items = null;
        return "/institution/assign_multiple_letters";
    }

    public void fillForwardCopyActions() {
        documentHistories = findDocumentHistories(fromDate, toDate, HistoryType.Letter_Copy_or_Forward, webUserController.getLoggedInstitution(), webUserCopy);
    }

    public void fillLetterAcceptRegister() {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toInstitution=:ti "
                + " and h.completed=true ";
        if (webUserCopy != null) {
            if (webUserCopy instanceof WebUser) {
                j += " and h.fromUser=:fu ";
                m.put("fu", webUserCopy);
            } else if (webUserCopy instanceof Institution) {
                j += " and h.fromInstitution=:fi ";
                m.put("fi", webUserCopy);
            }
        }
        j += " and h.completedAt between :fd and :td "
                + " order by h.id";
        m.put("ti", webUserController.getLoggedInstitution());
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("fd", fromDate);
        m.put("td", toDate);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        if (documentHistories == null) {
            documentHistories = new ArrayList<>();
        }

        m = new HashMap();
        j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toUser in :uss "
                + " and h.completed=true ";
        if (webUserCopy != null) {
            if (webUserCopy instanceof WebUser) {
                j += " and h.fromUser=:fu ";
                m.put("fu", webUserCopy);
            } else if (webUserCopy instanceof Institution) {
                j += " and h.fromInstitution=:fi ";
                m.put("fi", webUserCopy);
            }
        }

        j += " and h.completedAt between :fd and :td "
                + " order by h.id";

        m.put("uss", webUserController.getUsersForMyInstitute());
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("fd", fromDate);
        m.put("td", toDate);

        List<DocumentHistory> tdhx = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        if (tdhx != null) {
            documentHistories.addAll(tdhx);
        }

    }

    public String toAcceptForwardCopyLettersToReceive() {
        documentHistories = null;
        return "/institution/accept_copy_forwards";
    }

    public String toAcceptMyAssignedLetters() {
        documentHistories = null;
        setFromDate(CommonController.startOfTheMonth());
        setToDate(CommonController.endOfTheMonth());
        fillMyLettersToAccept();
        return "/institution/accept_my_assigned_letters";
    }

    public String toAcceptCopyForwardedLettersToMe() {
        documentHistories = null;
        return "/institution/accept_my_copy_forwarded_letters";
    }

    public String toAcceptedMyAssignedLetters() {
        documentHistories = null;
        return "/institution/accepted_my_assigned_letters";
    }

    public String toAcceptedCopyForwardedLettersToMe() {
        documentHistories = null;
        return "/institution/accepted_my_copy_forwarded_letters";
    }

    public void acceptSelectedHistoryForCopyForward() {
        if (selectedHistory == null) {
            JsfUtil.addErrorMessage("Nothing to accept");
            return;
        }
        selectedHistory.setCompleted(true);
        selectedHistory.setCompletedAt(new Date());
        selectedHistory.setCompletedBy(webUserController.getLoggedUser());
        saveDocumentHx(selectedHistory);

        DocumentHistory ndh = new DocumentHistory();
        ndh.setDocument(selectedHistory.getDocument());
        ndh.setHistoryType(HistoryType.Letter_Copy_or_Forward_Accepted);
        ndh.setInstitution(webUserController.getLoggedInstitution());
        ndh.setToInstitution(webUserController.getLoggedInstitution());
        ndh.setFromInstitution(selectedHistory.getFromInstitution());
        ndh.setCompleted(true);
        ndh.setCompletedAt(new Date());
        ndh.setCompletedBy(webUserController.getLoggedUser());
        ndh.setDocument(selected);
        saveDocumentHx(ndh);

        documentHistories.remove(selectedHistory);

        selectedHistory = null;
    }

    public void fillForwardCopyLettersToReceive() {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toInstitution=:ti "
                + " and h.completed=:c ";
        if (webUserCopy != null) {
            if (webUserCopy instanceof WebUser) {
                j += " and h.fromUser=:fu ";
                m.put("fu", webUserCopy);
            } else if (webUserCopy instanceof Institution) {
                j += " and h.fromInstitution=:fi ";
                m.put("fi", webUserCopy);
            }
        }

        j += " and h.createdAt between :fd and :td "
                + " order by h.id desc";

        m.put("ti", webUserController.getLoggedInstitution());
        System.out.println("webUserController.getLoggedInstitution() = " + webUserController.getLoggedInstitution());
//        m.put("uti", webUserController.getLoggedUser().getInstitution());
        System.out.println("webUserController.getLoggedUser().getInstitution() = " + webUserController.getLoggedUser().getInstitution());
//        m.put("tu", webUserController.getLoggedUser());
        System.out.println("webUserController.getLoggedUser() = " + webUserController.getLoggedUser());
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("fd", fromDate);
        System.out.println("fromDate = " + fromDate);
        System.out.println("toDate = " + toDate);
        m.put("td", toDate);
        m.put("c", false);
        System.out.println("j = " + j);
        System.out.println("m = " + m);
        documentHistories = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);

        if (documentHistories == null) {
            documentHistories = new ArrayList<>();
        }

        m = new HashMap();
        j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType =:ht "
                + " and h.toUser in :us "
                + " and h.completed=:c ";
        if (webUserCopy != null) {
            if (webUserCopy instanceof WebUser) {
                j += " and h.fromUser=:fu ";
                m.put("fu", webUserCopy);
            } else if (webUserCopy instanceof Institution) {
                j += " and h.fromInstitution=:fi ";
                m.put("fi", webUserCopy);
            }
        }

        j += " and h.createdAt between :fd and :td "
                + " order by h.id desc";

        m.put("us", webUserController.getUsersForMyInstitute());
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("fd", fromDate);
        m.put("td", toDate);
        m.put("c", false);
        System.out.println("j = " + j);
        System.out.println("m = " + m);

        List<DocumentHistory> uihxs = documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
        if (uihxs != null) {
            documentHistories.addAll(uihxs);
        }

    }

    public List<DocumentHistory> findDocumentHistories(Date fd, Date td, HistoryType ht, Institution i, Nameable u) {
        List<HistoryType> hts = new ArrayList<>();
        if (ht != null) {
            hts.add(ht);
        } else {
            Collections.addAll(hts, HistoryType.values());
        }
        return findDocumentHistories(fd, td, hts, i, u);
    }

    public List<DocumentHistory> findDocumentHistories(Date fd, Date td, List<HistoryType> hts, Institution i, Nameable u) {
        Map m = new HashMap();
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.historyType in :hts "
                + " and h.document.institution=:i ";
        if (u != null) {
            if (u instanceof WebUser) {
                j += " and h.toUser=:u ";
                m.put("u", u);
            } else if (u instanceof Institution) {
                j += " and h.toInstitution=:ti ";
                m.put("ti", u);
            }
        }
        j += " and h.createdAt between :fd and :td "
                + " order by h.id";

        m.put("i", i);
        m.put("hts", hts);
        m.put("fd", fd);
        m.put("td", td);

        return documentHxFacade.findByJpql(j, m, TemporalType.TIMESTAMP);
    }

    public String toLetterView() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.document=:doc "
                + " and (h.institution=:ins or h.fromInstitution=:ins or h.toInstitution=:ins) "
                + " order by h.id desc";
        Map m = new HashMap();
        m.put("doc", selected);
        m.put("ins", webUserController.getLoggedInstitution());
        selectedDocumentHistories = documentHxFacade.findByJpql(j, m);

        j = "select h "
                + " from Upload h "
                + " where h.retired=false "
                + " and h.document=:doc "
                + " order by h.id";
        m = new HashMap();
        m.put("doc", selected);
//        m.put("ins", webUserController.getLoggedInstitution());
        //TODO: Need to allow only original upload
//        if (selected.getFromInstitution() != null) {
//            m.put("fins", selected.getFromInstitution());
//        } else {
//            m.put("fins", webUserController.getLoggedInstitution());
//        }
        selectedUploads = uploadFacade.findByJpql(j, m);

        return "/document/letter_view";
    }

    public String toAcceptAssignedLetter() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.document=:doc "
                + " and h.historyType=:ht "
                + " and h.toUser=:tu "
                + " order by h.id desc";
        Map m = new HashMap();
        m.put("doc", selected);
        m.put("ht", HistoryType.Letter_Assigned);
        m.put("tu", webUserController.getLoggedUser());
        DocumentHistory dh = documentHxFacade.findFirstByJpql(j, m);
        if (dh != null) {
            dh.setCompleted(true);
            dh.setCompletedAt(new Date());
            dh.setCompletedBy(webUserController.getLoggedUser());
            saveDocumentHx(dh);
            JsfUtil.addSuccessMessage("Letter Accepted.");
            selected.setCompleted(true);
            selected.setCompletedAt(new Date());
            selected.setCompletedBy(webUserController.getLoggedUser());
            save(selected);
        } else {
            JsfUtil.addErrorMessage("Error.");
        }
        return toLetterView();
    }

    public String toAcceptCopyForwardedLetter() {
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.document.retired=false "
                + " and h.historyType=:ht "
                + " and h.toUser=:tu "
                + " and h.completed=:com "
                + " order by h.id desc";
        Map m = new HashMap();
        m.put("doc", selected);
        m.put("ht", HistoryType.Letter_Copy_or_Forward);
        m.put("tu", webUserController.getLoggedUser());
        m.put("com", false);
        listedToAcceptCopyForwards = documentHxFacade.findByJpql(j, m);
        return "";
    }

    public void acceptSelectedCopyForwardedLetter() {
        if (selectedToAcceptCopyForwards == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return;
        }

        selectedToAcceptCopyForwards.setCompleted(true);
        selectedToAcceptCopyForwards.setCompletedAt(new Date());
        selectedToAcceptCopyForwards.setCompletedBy(webUserController.getLoggedUser());
        saveDocumentHx(selectedToAcceptCopyForwards);

        listedToAcceptCopyForwards.remove(selectedToAcceptCopyForwards);

    }

    public void acceptMyAssignedLetter() {
        if (selectedHistory == null) {
            JsfUtil.addErrorMessage("No Letter Selected");
            return;
        }

        selectedHistory.setCompleted(true);
        selectedHistory.setCompletedAt(new Date());
        selectedHistory.setCompletedBy(webUserController.getLoggedUser());
        saveDocumentHx(selectedHistory);

        documentHistories.remove(selectedHistory);

        JsfUtil.addSuccessMessage("Accepted Successfully.");

    }

    public void receiveLetterCopiedOrForwardedToMe() {
        if (selectedHistory == null) {
            JsfUtil.addErrorMessage("No Letter Selected");
            return;
        }

        selectedHistory.setCompleted(true);
        selectedHistory.setCompletedAt(new Date());
        selectedHistory.setCompletedBy(webUserController.getLoggedUser());
        saveDocumentHx(selectedHistory);

        documentHistories.remove(selectedHistory);

        JsfUtil.addSuccessMessage("Received Successfully.");

    }

    public String toReverseAcceptMyLetter() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        String j = "select h "
                + " from DocumentHistory h "
                + " where h.retired=false "
                + " and h.document=:doc "
                + " and h.historyType=:ht "
                + " and h.toUser=:tu "
                + " order by h.id desc";
        Map m = new HashMap();
        m.put("doc", selected);
        m.put("ht", HistoryType.Letter_Assigned);
        m.put("tu", webUserController.getLoggedUser());
        DocumentHistory dh = documentHxFacade.findFirstByJpql(j, m);
        selected.setCompleted(false);
        save(selected);
        if (dh != null) {
            dh.setCompleted(false);
            saveDocumentHx(dh);
            JsfUtil.addSuccessMessage("Letter Accepted.");
        } else {
            JsfUtil.addErrorMessage("Error.");
        }
        return toLetterView();
    }

    public String toAssignAndAcceptLetterMySelf() {
        if (selected == null) {
            JsfUtil.addErrorMessage("No File Selected");
            return "";
        }
        DocumentHistory dh = new DocumentHistory();
        dh.setDocument(selected);
        dh.setInstitution(webUserController.getLoggedInstitution());
        dh.setHistoryType(HistoryType.Letter_Assigned);
        dh.setToUser(webUserController.getLoggedUser());
        saveDocumentHx(dh);
        dh.setCompletedBy(webUserController.getLoggedUser());
        dh.setCompleted(true);
        dh.setCompletedAt(new Date());
        JsfUtil.addSuccessMessage("Letter Accepted.");
        saveDocumentHx(dh);
        return toLetterView();
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

    public Nameable getNameable(java.lang.Long id) {
        Institution i = institutionFacade.find(id);
        if (i != null) {
            return i;
        }
        WebUser u = webUserFacade.find(id);
        if (u != null) {
            return u;
        }
        return null;
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

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonController.endOfTheDate();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public SearchFilterType getSearchFilterType() {
        return searchFilterType;
    }

    public void setSearchFilterType(SearchFilterType searchFilterType) {
        this.searchFilterType = searchFilterType;
    }

    public Nameable getWebUserCopy() {
        return webUserCopy;
    }

    public void setWebUserCopy(Nameable webUserCopy) {
        this.webUserCopy = webUserCopy;
    }

    public Item getMinute() {
        return minute;
    }

    public void setMinute(Item minute) {
        this.minute = minute;
    }

    public List<DocumentHistory> getDocumentHistories() {
        return documentHistories;
    }

    public void setDocumentHistories(List<DocumentHistory> documentHistories) {
        this.documentHistories = documentHistories;
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public List<Upload> getSelectedUploads() {
        return selectedUploads;
    }

    public void setSelectedUploads(List<Upload> selectedUploads) {
        this.selectedUploads = selectedUploads;
    }

    public Upload getRemovingUpload() {
        return removingUpload;
    }

    public void setRemovingUpload(Upload removingUpload) {
        this.removingUpload = removingUpload;
    }

    public DocumentHistory getDeletingHistory() {
        return deletingHistory;
    }

    public void setDeletingHistory(DocumentHistory deletingHistory) {
        this.deletingHistory = deletingHistory;
    }

    public List<DocumentHistory> getListedToAcceptCopyForwards() {
        return listedToAcceptCopyForwards;
    }

    public void setListedToAcceptCopyForwards(List<DocumentHistory> listedToAcceptCopyForwards) {
        this.listedToAcceptCopyForwards = listedToAcceptCopyForwards;
    }

//    @Deprecated
    public void tmpAddInsToDocHx() {
        System.out.println("tmpAddInsToDocHx");
        List<DocumentHistory> tdhs = documentHxFacade.findAll();
        System.out.println("tdhs = " + tdhs.size());
        for (DocumentHistory tdh : tdhs) {
            if (tdh.getInstitution() == null) {
                tdh.setInstitution(tdh.getDocument().getInstitution());
            }
        }
    }

    public void tmpAddInsToUpload() {
        List<Upload> tdhs = uploadFacade.findAll();
        for (Upload tdh : tdhs) {
            if (tdh.getInstitution() == null) {
                if (tdh.getDocument() != null && tdh.getDocument().getInstitution() != null) {
                    tdh.setInstitution(tdh.getDocument().getInstitution());
                    uploadFacade.edit(tdh);
                }
            }
        }
    }

    public boolean isNewHx() {
        return newHx;
    }

    public void setNewHx(boolean newHx) {
        this.newHx = newHx;
    }

    public Nameable getSearchUserOrIns() {
        return searchUserOrIns;
    }

    public void setSearchUserOrIns(Nameable searchUserOrIns) {
        this.searchUserOrIns = searchUserOrIns;
    }

    public List<InstitutionCount> getInstitutionCounts() {
        return institutionCounts;
    }

    public void setInstitutionCounts(List<InstitutionCount> institutionCounts) {
        this.institutionCounts = institutionCounts;
    }

    @FacesConverter(forClass = Nameable.class)
    public static class NameableConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            LetterController controller = (LetterController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "letterController");
            return controller.getNameable(getKey(value));
        }

        java.lang.Long getKey(String value) {
            if (value == null) {
                return 0l;
            }
            if (value.trim().equals("")) {
                return 0l;
            }
            java.lang.Long key;
            try {
                key = Long.valueOf(value);
                return key;
            } catch (Exception e) {
                return 0l;
            }
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
            if (object instanceof Nameable) {
                Nameable o = (Nameable) object;
                return getStringKey(o.getId());
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Nameable.class.getName()});
                return null;
            }
        }

    }

}
