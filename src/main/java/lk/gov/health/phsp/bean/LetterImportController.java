/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.ejb.LetterImportConfig;
import lk.gov.health.phsp.ejb.LetterImportService;
import lk.gov.health.phsp.ejb.PdfSplitService;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.LetterImportBatch;
import lk.gov.health.phsp.entity.LetterImportItem;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.LetterImportItemStatus;
import lk.gov.health.phsp.enums.LetterImportStatus;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.LetterImportBatchFacade;
import lk.gov.health.phsp.facade.LetterImportItemFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import org.apache.commons.io.IOUtils;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

/**
 * Drives the letter-import page: upload a multi-letter PDF, watch background
 * processing, then review each detected letter and accept (create a letter),
 * edit, discard, or skip it.
 */
@Named
@SessionScoped
public class LetterImportController implements Serializable {

    private static final Logger LOG = Logger.getLogger(LetterImportController.class.getName());
    private static final long serialVersionUID = 1L;

    @EJB
    private LetterImportConfig config;
    @EJB
    private LetterImportBatchFacade batchFacade;
    @EJB
    private LetterImportItemFacade itemFacade;
    @EJB
    private DocumentFacade documentFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private PdfSplitService pdfSplitService;
    @EJB
    private LetterImportService letterImportService;

    @Inject
    private WebUserController webUserController;
    @Inject
    private ClaudeApiKeyController claudeApiKeyController;

    private UploadedFile file;
    private LetterImportBatch batch;
    private List<LetterImportItem> items;
    private int currentIndex;

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    public String toLetterImport() {
        file = null;
        batch = null;
        items = new ArrayList<>();
        currentIndex = 0;
        return "/document/letter_import?faces-redirect=true";
    }

    // -------------------------------------------------------------------------
    // Upload + processing
    // -------------------------------------------------------------------------

    public void handleUpload(FileUploadEvent event) {
        this.file = event.getFile();
        JsfUtil.addSuccessMessage("Uploaded " + file.getFileName() + ". Click Start to process.");
    }

    public void startImport() {
        WebUser user = webUserController.getLoggedUser();
        if (user == null) {
            JsfUtil.addErrorMessage("No logged-in user.");
            return;
        }
        if (file == null) {
            JsfUtil.addErrorMessage("Please choose a PDF file first.");
            return;
        }
        String apiKey = claudeApiKeyController.getActiveDecryptedKey(user);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Set your Claude API key first (account menu → My Claude API Key).");
            return;
        }

        byte[] bytes;
        try (InputStream in = file.getInputStream()) {
            bytes = IOUtils.toByteArray(in);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed reading uploaded PDF", ex);
            JsfUtil.addErrorMessage("Could not read the uploaded file.");
            return;
        }

        LetterImportBatch b = new LetterImportBatch();
        b.setOwner(user);
        b.setInstitution(webUserController.getLoggedInstitution());
        b.setOriginalFileName(file.getFileName());
        b.setPdfBytes(bytes);
        b.setStatus(LetterImportStatus.NEW);
        b.setModel(resolveModel());
        b.setCreatedBy(user);
        b.setCreatedAt(new Date());
        batchFacade.create(b);
        this.batch = b;
        this.items = new ArrayList<>();
        this.currentIndex = 0;

        letterImportService.processBatch(b.getId(), apiKey, b.getModel());
        JsfUtil.addSuccessMessage("Processing started. Letters will appear as they are recognised.");
    }

    private String resolveModel() {
        String override = claudeApiKeyController.getActiveKey() != null
                ? claudeApiKeyController.getActiveKey().getModelOverride() : null;
        return override != null && !override.trim().isEmpty() ? override.trim() : config.getDefaultModel();
    }

    /** Polled by the page while a batch is processing. */
    public void refreshStatus() {
        if (batch == null) {
            return;
        }
        batch = batchFacade.find(batch.getId());
        if (batch != null && batch.getStatus() == LetterImportStatus.READY && (items == null || items.isEmpty())) {
            loadItems();
        }
    }

    private void loadItems() {
        Map<String, Object> params = new HashMap<>();
        params.put("b", batch);
        items = itemFacade.findByJpql(
                "SELECT i FROM LetterImportItem i WHERE i.batch = :b ORDER BY i.segmentIndex",
                params);
        if (items == null) {
            items = new ArrayList<>();
        }
        currentIndex = 0;
    }

    public boolean isProcessing() {
        return batch != null
                && (batch.getStatus() == LetterImportStatus.NEW
                || batch.getStatus() == LetterImportStatus.PROCESSING);
    }

    public boolean isReady() {
        return batch != null && batch.getStatus() == LetterImportStatus.READY;
    }

    public boolean isFailed() {
        return batch != null && batch.getStatus() == LetterImportStatus.FAILED;
    }

    public int getProgressPercent() {
        if (batch == null || batch.getLetterCount() == null || batch.getLetterCount() == 0) {
            return 0;
        }
        int processed = batch.getProcessedCount() != null ? batch.getProcessedCount() : 0;
        return (int) Math.round((processed * 100.0) / batch.getLetterCount());
    }

    // -------------------------------------------------------------------------
    // Review actions
    // -------------------------------------------------------------------------

    public LetterImportItem getCurrentItem() {
        if (items == null || items.isEmpty() || currentIndex < 0 || currentIndex >= items.size()) {
            return null;
        }
        return items.get(currentIndex);
    }

    public void next() {
        if (items != null && currentIndex < items.size() - 1) {
            currentIndex++;
        }
    }

    public void previous() {
        if (currentIndex > 0) {
            currentIndex--;
        }
    }

    public boolean isHasNext() {
        return items != null && currentIndex < items.size() - 1;
    }

    public boolean isHasPrevious() {
        return currentIndex > 0;
    }

    public void saveCurrentEdits() {
        LetterImportItem item = getCurrentItem();
        if (item == null) {
            return;
        }
        itemFacade.edit(item);
        JsfUtil.addSuccessMessage("Saved.");
    }

    public void acceptCurrent() {
        LetterImportItem item = getCurrentItem();
        if (item == null) {
            return;
        }
        if (item.getStatus() == LetterImportItemStatus.ACCEPTED) {
            JsfUtil.addErrorMessage("This letter was already accepted.");
            return;
        }
        WebUser user = webUserController.getLoggedUser();
        Institution loggedInstitution = webUserController.getLoggedInstitution();

        Document doc = new Document();
        doc.setDocumentType(DocumentType.Letter);
        doc.setDocumentName(item.getSubject());
        doc.setDocumentDate(item.getLetterDate());
        doc.setReceivedDate(item.getReceivedDate() != null ? item.getReceivedDate() : new Date());
        doc.setSenderName(item.getSenderName());
        doc.setRegistrationNo(item.getRegistrationNo());
        doc.setFromInstitution(item.getResolvedInstitution());
        doc.setFromWebUser(item.getResolvedStaff());
        doc.setInstitution(loggedInstitution);
        doc.setCurrentInstitution(loggedInstitution);
        doc.setCreatedInstitution(loggedInstitution);
        doc.setOwner(user);
        doc.setCurrentOwner(user);
        doc.setCreatedBy(user);
        doc.setCreatedAt(new Date());
        documentFacade.create(doc);

        attachRenderedPages(item, doc, user, loggedInstitution);

        item.setStatus(LetterImportItemStatus.ACCEPTED);
        item.setCreatedDocument(doc);
        item.setDecidedAt(new Date());
        itemFacade.edit(item);

        refreshBatchDoneState();
        JsfUtil.addSuccessMessage("Letter created.");
        advancePastDecided();
    }

    private void attachRenderedPages(LetterImportItem item, Document doc, WebUser user, Institution institution) {
        byte[] pdf = batch.getPdfBytes();
        int start = item.getStartPage() != null ? item.getStartPage() : 0;
        int end = item.getEndPage() != null ? item.getEndPage() : start;
        for (int page = start; page <= end; page++) {
            try {
                byte[] png = pdfSplitService.renderPageToPng(pdf, page);
                Upload up = new Upload();
                up.setDocument(doc);
                up.setBaImage(png);
                up.setFileName("letter-" + doc.getId() + "-p" + (page - start + 1) + ".png");
                up.setFileType("image/png");
                up.setInstitution(institution);
                up.setCreatedAt(new Date());
                up.setCreater(user);
                uploadFacade.create(up);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed attaching page " + page, e);
            }
        }
    }

    public void discardCurrent() {
        LetterImportItem item = getCurrentItem();
        if (item == null) {
            return;
        }
        item.setStatus(LetterImportItemStatus.DISCARDED);
        item.setDecidedAt(new Date());
        itemFacade.edit(item);
        refreshBatchDoneState();
        JsfUtil.addSuccessMessage("Letter discarded.");
        advancePastDecided();
    }

    public void skipCurrent() {
        next();
    }

    private void advancePastDecided() {
        if (isHasNext()) {
            next();
        }
    }

    private void refreshBatchDoneState() {
        if (batch == null) {
            return;
        }
        boolean allDecided = true;
        for (LetterImportItem i : items) {
            if (i.getStatus() == LetterImportItemStatus.PENDING) {
                allDecided = false;
                break;
            }
        }
        if (allDecided) {
            batch.setStatus(LetterImportStatus.DONE);
            batchFacade.edit(batch);
        }
    }

    // -------------------------------------------------------------------------
    // Autocomplete
    // -------------------------------------------------------------------------

    public List<Institution> completeInstitution(String query) {
        Map<String, Object> params = new HashMap<>();
        String jpql = "SELECT i FROM Institution i WHERE i.retired = false";
        if (query != null && !query.trim().isEmpty()) {
            jpql += " AND lower(i.name) LIKE :q";
            params.put("q", "%" + query.trim().toLowerCase() + "%");
        }
        jpql += " ORDER BY i.name";
        return institutionFacade.findByJpql(jpql, params, 20);
    }

    // -------------------------------------------------------------------------
    // Counts
    // -------------------------------------------------------------------------

    public int getPendingCount() {
        if (items == null) {
            return 0;
        }
        int count = 0;
        for (LetterImportItem i : items) {
            if (i.getStatus() == LetterImportItemStatus.PENDING) {
                count++;
            }
        }
        return count;
    }

    public int getAcceptedCount() {
        if (items == null) {
            return 0;
        }
        int count = 0;
        for (LetterImportItem i : items) {
            if (i.getStatus() == LetterImportItemStatus.ACCEPTED) {
                count++;
            }
        }
        return count;
    }

    // -------------------------------------------------------------------------
    // Getters / setters
    // -------------------------------------------------------------------------

    public UploadedFile getFile() { return file; }
    public void setFile(UploadedFile file) { this.file = file; }

    public LetterImportBatch getBatch() { return batch; }

    public List<LetterImportItem> getItems() { return items; }

    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int currentIndex) { this.currentIndex = currentIndex; }

    public int getItemCount() { return items == null ? 0 : items.size(); }

    /**
     * The current letter's preview image as a data URI, for direct rendering in
     * the page without a streaming endpoint. Empty string when no image.
     */
    public String getCurrentPreviewDataUri() {
        LetterImportItem item = getCurrentItem();
        if (item == null || item.getPreviewImage() == null || item.getPreviewImage().length == 0) {
            return "";
        }
        String type = item.getPreviewContentType() != null ? item.getPreviewContentType() : "image/png";
        return "data:" + type + ";base64,"
                + java.util.Base64.getEncoder().encodeToString(item.getPreviewImage());
    }
}
