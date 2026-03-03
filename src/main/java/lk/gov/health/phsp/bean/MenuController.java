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
import java.util.Date;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.DocumentHistory;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.enums.Privilege;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
public class MenuController implements Serializable {

    @Inject
    private WebUserController webUserController;
    @Inject
    WebUserApplicationController webUserApplicationController;
    @Inject
    FileController fileController;
    @Inject
    LetterController letterController;

    @Inject
    InstitutionController institutionController;
    @Inject
    PreferenceController preferenceController;
    @Inject
    DashboardController dashboardController;

    /**
     * Creates a new instance of MenuController
     */
    public MenuController() {
    }

    public String toIndex() {
        dashboardController.preparePersonalDashboard();
        return "/index?faces-redirect=true";
    }

    public String toFileAddNew() {
        Document nd = new Document();
        nd.setDocumentType(DocumentType.File);
        nd.setDocumentDate(new Date());
        nd.setReceivedDate(new Date());
        nd.setInstitution(webUserController.getLoggedInstitution());
        nd.setInstitutionUnit(webUserController.getLoggedInstitution());
        nd.setOwner(webUserController.getLoggedUser());
        nd.setCurrentInstitution(webUserController.getLoggedInstitution());
        nd.setCurrentOwner(webUserController.getLoggedUser());
        fileController.setSelected(nd);

        DocumentHistory ndh = new DocumentHistory();
        ndh.setHistoryType(HistoryType.File_Created);
        return "/document/file?faces-redirect=true";
    }

    public String toLetterAddNew() {
        Document nd = new Document();
        nd.setDocumentType(DocumentType.Letter);
        nd.setDocumentDate(new Date());
        nd.setReceivedDate(new Date());
        nd.setInstitution(webUserController.getLoggedInstitution());
        nd.setInstitutionUnit(webUserController.getLoggedInstitution());
        nd.setOwner(webUserController.getLoggedUser());
        nd.setCurrentInstitution(webUserController.getLoggedInstitution());
        nd.setCurrentOwner(webUserController.getLoggedUser());
        nd.setReceivedDate(new Date());
        letterController.setSelected(nd);

        letterController.setNewHx(true);
        DocumentHistory ndh = new DocumentHistory();
        ndh.setHistoryType(HistoryType.Letter_Created);
        ndh.setInstitution(webUserController.getLoggedInstitution());
        return "/document/letter?faces-redirect=true";
    }

    public String toFileSearch() {
        fileController.setItems(null);
        fileController.setSearchTerm("");
        fileController.setSelected(null);
        return "/document/file_search?faces-redirect=true";
    }

    public String toLetterSearch() {
        letterController.setItems(null);
        letterController.setSearchTerm("");
        letterController.setSelected(null);
        letterController.listLastLettersReceived();
        return "/document/letter_search?faces-redirect=true";
    }

    public String toLetterSearchByDate() {
        letterController.setItems(null);
        letterController.setSelected(null);
        letterController.setFromDate(CommonController.startOfTheMonth());
        letterController.setToDate(CommonController.endOfTheMonth());
        return "/document/letter_search_by_date?faces-redirect=true";
    }

    public String toFileLedger() {
        fileController.setItems(null);
        return "/document/file_ledger?faces-redirect=true";
    }

    public String toReceiveFile() {
        return "/document/file_receive?faces-redirect=true";
    }

    public String toViewRequest() {
        return "/common/request_view?faces-redirect=true";
    }

    public String toViewPatient() {
        return "/common/client_view?faces-redirect=true";
    }

    public String toViewResult() {
        return "/common/result_view?faces-redirect=true";
    }

    public String toReportsIndex() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/reports_index?faces-redirect=true";
            case Institutional:
                return "/institution/reports_index?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toSearch() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/search?faces-redirect=true";
            case Institutional:
                return "/institution/search?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toAdministrationIndex() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/admin/index?faces-redirect=true";
            case Institutional:
                return "/institution/admin/index?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toAdministrationIndexFirstLogin() {
        return "/national/admin/index?faces-redirect=true";
    }

    public String toPreferences() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        preferenceController.preparePreferences();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/regional/institution/preferences?faces-redirect=true";
            case National:
                return "/national/admin/preferences?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toAddNewUser() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        webUserController.prepareToAddNewUser();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/admin/user_new?faces-redirect=true";
            case Institutional:
                return "/institution/admin/user_new?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toAddNewInstitution() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        institutionController.prepareToAddNewInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/admin/institution?faces-redirect=true";
            case Institutional:
                return "/institution/admin/institution?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toAddNewInstitutionAtLetterEntry() {
        institutionController.prepareToAddNewInstitution();
        return "/institution/institution_at_letter_entry?faces-redirect=true";
    }

    public String toListUsersFirstLogin() {
        webUserController.prepareListingAllUsers();
        return "/national/admin/user_list?faces-redirect=true";
    }

    public String toListUsers() {
        boolean privileged = false;
        boolean national = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
                national = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        if (national) {
            webUserController.prepareListingAllUsers();
        } else {
            webUserController.prepareListingUsersUnderMe();
        }
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/institution/admin/user_list?faces-redirect=true";
            case National:
                return "/national/admin/user_list?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toListInstitutions() {
        boolean privileged = false;
        boolean national = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
                national = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        institutionController.prepareToListInstitution();
        System.out.println("webUserController.getLoggedUser().getWebUserRoleLevel() = " + webUserController.getLoggedUser().getWebUserRoleLevel());
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/institution/admin/institution_list?faces-redirect=true";
            case National:
                return "/national/admin/institution_list?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toListInstitutionsWithUsers() {
        boolean privileged = false;
        boolean national = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
                national = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        institutionController.prepareToListInstitution();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/institution/admin/institution_list_with_users?faces-redirect=true";
            case National:
                return "/national/admin/institution_list_with_users?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toPrivileges() {
        boolean privileged = false;
        boolean national = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
                national = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                webUserController.preparePrivileges(webUserApplicationController.getRegionalPrivilegeRoot());
                return "/institution/admin/privileges?faces-redirect=true";
            case National:
                webUserController.preparePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/privileges?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toPrivilegesFirstLogin() {
        webUserController.preparePrivileges(webUserApplicationController.getAllPrivilegeRoot());
        return "/national/admin/privileges?faces-redirect=true";
    }

    public String toEditUser() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/institution/admin/user_edit?faces-redirect=true";
            case National:
                return "/national/admin/user_edit?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toEditInstitution() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/institution/admin/institution?faces-redirect=true";
            case National:
                return "/national/admin/institution?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toEditPassword() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }
        webUserController.prepareEditPassword();
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                return "/institution/admin/user_password?faces-redirect=true";
            case National:
                return "/national/admin/user_password?faces-redirect=true";
            default:
                return "";
        }
    }

    public String toUserPrivileges() {
        boolean privileged = false;
        for (UserPrivilege up : webUserController.getLoggedUserPrivileges()) {
            if (up.getPrivilege() == Privilege.Institution_Administration) {
                privileged = true;
            }
            if (up.getPrivilege() == Privilege.System_Administration) {
                privileged = true;
            }
        }
        if (!privileged) {
            JsfUtil.addErrorMessage("You are NOT autherized");
            return "";
        }

        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Institutional:
                webUserController.prepareManagePrivileges(webUserApplicationController.getRegionalPrivilegeRoot());
                return "/regional/admin/user_privileges?faces-redirect=true";
            case National:
                webUserController.prepareManagePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/user_privileges?faces-redirect=true";
            default:
                return "";
        }
    }

}
