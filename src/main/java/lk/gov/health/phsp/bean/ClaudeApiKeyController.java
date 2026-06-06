/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.ejb.CryptoService;
import lk.gov.health.phsp.entity.UserClaudeApiKey;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.UserClaudeApiKeyFacade;

/**
 * Self-service management of the logged-in user's personal Claude API key.
 *
 * <p>The raw key is encrypted via {@link CryptoService} before persistence and
 * is never read back into the page; only a masked hint is shown. Other features
 * (letter import) obtain the usable key through
 * {@link #getActiveDecryptedKey(WebUser)}.</p>
 */
@Named
@SessionScoped
public class ClaudeApiKeyController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserClaudeApiKeyFacade userClaudeApiKeyFacade;
    @EJB
    private CryptoService cryptoService;
    @Inject
    private WebUserController webUserController;

    /** Transient raw key typed by the user; never persisted as-is. */
    private String rawKeyInput;
    private String modelOverride;
    private UserClaudeApiKey activeKey;

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    public String toManageMyClaudeApiKey() {
        rawKeyInput = null;
        loadActiveKey();
        modelOverride = activeKey != null ? activeKey.getModelOverride() : null;
        return "/change_my_claude_api_key?faces-redirect=true";
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    public void saveKey() {
        WebUser user = webUserController.getLoggedUser();
        if (user == null) {
            JsfUtil.addErrorMessage("No logged-in user.");
            return;
        }
        if (rawKeyInput == null || rawKeyInput.trim().isEmpty()) {
            JsfUtil.addErrorMessage("Please paste your Claude API key.");
            return;
        }
        String raw = rawKeyInput.trim();

        loadActiveKey();
        if (activeKey != null) {
            retire(activeKey, "Replaced by a new key", user);
        }

        UserClaudeApiKey key = new UserClaudeApiKey();
        key.setOwner(user);
        key.setEncryptedKey(cryptoService.encrypt(raw));
        key.setLast4(raw.length() >= 4 ? raw.substring(raw.length() - 4) : raw);
        key.setModelOverride(modelOverride != null && !modelOverride.trim().isEmpty()
                ? modelOverride.trim() : null);
        key.setActive(true);
        key.setCreatedBy(user);
        key.setCreatedAt(new Date());
        userClaudeApiKeyFacade.create(key);

        // Drop the raw value from memory as soon as it is stored.
        rawKeyInput = null;
        activeKey = key;
        JsfUtil.addSuccessMessage("Your Claude API key has been saved securely.");
    }

    public void removeKey() {
        WebUser user = webUserController.getLoggedUser();
        loadActiveKey();
        if (activeKey == null) {
            JsfUtil.addErrorMessage("No key to remove.");
            return;
        }
        retire(activeKey, "Removed by user", user);
        activeKey = null;
        modelOverride = null;
        JsfUtil.addSuccessMessage("Your Claude API key has been removed.");
    }

    private void retire(UserClaudeApiKey key, String comment, WebUser user) {
        key.setActive(false);
        key.setRetired(true);
        key.setRetiredBy(user);
        key.setRetiredAt(new Date());
        key.setRetireComments(comment);
        userClaudeApiKeyFacade.edit(key);
    }

    // -------------------------------------------------------------------------
    // Lookup helpers (used here and by the letter-import feature)
    // -------------------------------------------------------------------------

    private void loadActiveKey() {
        activeKey = findActiveKey(webUserController.getLoggedUser());
    }

    public UserClaudeApiKey findActiveKey(WebUser user) {
        if (user == null) {
            return null;
        }
        String jpql = "SELECT k FROM UserClaudeApiKey k "
                + "WHERE k.owner = :owner AND k.active = true AND k.retired = false "
                + "ORDER BY k.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("owner", user);
        List<UserClaudeApiKey> results = userClaudeApiKeyFacade.findByJpql(jpql, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Returns the decrypted, usable Claude API key for the given user, or
     * {@code null} if they have not configured one.
     */
    public String getActiveDecryptedKey(WebUser user) {
        UserClaudeApiKey key = findActiveKey(user);
        if (key == null || key.getEncryptedKey() == null) {
            return null;
        }
        return cryptoService.decrypt(key.getEncryptedKey());
    }

    // -------------------------------------------------------------------------
    // View state
    // -------------------------------------------------------------------------

    public boolean isKeyConfigured() {
        if (activeKey == null) {
            loadActiveKey();
        }
        return activeKey != null;
    }

    public String getMaskedKey() {
        if (activeKey == null) {
            loadActiveKey();
        }
        if (activeKey == null) {
            return "Not set";
        }
        return "sk-ant-••••••••••••" + (activeKey.getLast4() != null ? activeKey.getLast4() : "");
    }

    public String getRawKeyInput() { return rawKeyInput; }
    public void setRawKeyInput(String rawKeyInput) { this.rawKeyInput = rawKeyInput; }

    public String getModelOverride() { return modelOverride; }
    public void setModelOverride(String modelOverride) { this.modelOverride = modelOverride; }

    public UserClaudeApiKey getActiveKey() {
        if (activeKey == null) {
            loadActiveKey();
        }
        return activeKey;
    }
}
