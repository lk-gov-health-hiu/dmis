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
import java.util.ArrayList;
import java.util.Date;
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.DocumentHistory;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.enums.DocumentGenerationType;
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
        return "/index";
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
        return "/document/file";
    }

    public String toLetterAddNewReceivedLetter() {
        Document nd = new Document();
        nd.setDocumentType(DocumentType.Letter);
        nd.setDocumentGenerationType(DocumentGenerationType.Received_by_institution);
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
        ndh.setToInstitution(webUserController.getLoggedInstitution());
        return "/document/letter";
    }
    
    
    public String toUnitLetterAdd() {
        Document nd = new Document();
        nd.setDocumentType(DocumentType.Letter);
        nd.setDocumentGenerationType(DocumentGenerationType.Received_by_institution);
        nd.setDocumentDate(new Date());
        nd.setReceivedDate(new Date());
        nd.setInstitution(webUserController.getLoggedInstitution());
        nd.setInstitutionUnit(webUserController.getLoggedInstitution());
        nd.setOwner(webUserController.getLoggedUser());
        nd.setCurrentInstitution(webUserController.getLoggedInstitution());
        nd.setCurrentOwner(webUserController.getLoggedUser());
        nd.setReceivedDate(new Date());
        letterController.setSelected(nd);
        return "/document/unit_letter_add";
    }
    
    public String toLetterMailBranchAddNew() {
        Document nd = new Document();
        nd.setDocumentType(DocumentType.Letter);
        nd.setDocumentGenerationType(DocumentGenerationType.Received_by_mail_branch);
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
        ndh.setHistoryType(HistoryType.Letter_added_by_mail_branch);
        ndh.setInstitution(webUserController.getLoggedInstitution());
        return "/document/letter_mail_branch";
    }
    
    public String toLetterGenerateNew() {
        Document nd = new Document();
        nd.setDocumentType(DocumentType.Letter);
        nd.setDocumentGenerationType(DocumentGenerationType.Generated_by_system);
        nd.setDocumentDate(new Date());
        nd.setReceivedDate(new Date());
        nd.setInstitution(webUserController.getLoggedInstitution());
        nd.setInstitutionUnit(webUserController.getLoggedInstitution());
        nd.setFromInstitution(webUserController.getLoggedInstitution());
        nd.setOwner(webUserController.getLoggedUser());
        nd.setCurrentInstitution(webUserController.getLoggedInstitution());
        nd.setCurrentOwner(webUserController.getLoggedUser());
        nd.setReceivedDate(new Date());
        letterController.setSelected(nd);

        letterController.setNewHx(true);
        DocumentHistory ndh = new DocumentHistory();
        ndh.setHistoryType(HistoryType.Letter_Generated);
        ndh.setInstitution(webUserController.getLoggedInstitution());
        
        letterController.setToInsOrUser(new ArrayList<>());
        letterController.setSelectedDocumentHistories(new ArrayList<>());
        
        return "/document/letter_generate";
    }

    public String toFileSearch() {
        fileController.setItems(null);
        fileController.setSearchTerm("");
        fileController.setSelected(null);
        return "/document/file_search";
    }

    public String toLetterSearch() {
        letterController.setItems(null);
        letterController.setSearchTerm("");
        letterController.setSelected(null);
        letterController.listLastLettersReceived();
        return "/document/letter_search";
    }

    public String toFileLedger() {
        fileController.setItems(null);
        return "/document/file_ledger";
    }

    public String toReceiveFile() {
        return "/document/file_receive";
    }

    public String toViewRequest() {
        return "/common/request_view";
    }

    public String toViewPatient() {
        return "/common/client_view";
    }

    public String toViewResult() {
        return "/common/result_view";
    }

    public String toReportsIndex() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/reports_index";
            case Institutional:
                return "/institution/reports_index";
            default:
                return "";
        }
    }

    public String toSearch() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case National:
                return "/national/search";
            case Institutional:
                return "/institution/search";
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
                return "/national/admin/index";
            case Institutional:
                return "/institution/admin/index";
            default:
                return "";
        }
    }

    public String toAdministrationIndexFirstLogin() {
        return "/national/admin/index";
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
                return "/regional/institution/preferences";
            case National:
                return "/national/admin/preferences";
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
                return "/national/admin/user_new";
            case Institutional:
                return "/institution/admin/user_new";
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
                return "/national/admin/institution";
            case Institutional:
                return "/institution/admin/institution";
            default:
                return "";
        }
    }

    public String toAddNewInstitutionAtLetterEntry() {
        institutionController.prepareToAddNewInstitution();
        return "/institution/institution_at_letter_entry";
    }

    public String toListUsersFirstLogin() {
        webUserController.prepareListingAllUsers();
        return "/national/admin/user_list";
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
                return "/institution/admin/user_list";
            case National:
                return "/national/admin/user_list";
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
                return "/institution/admin/institution_list";
            case National:
                return "/national/admin/institution_list";
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
                return "/institution/admin/institution_list_with_users";
            case National:
                return "/national/admin/institution_list_with_users";
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
                return "/institution/admin/privileges";
            case National:
                webUserController.preparePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/privileges";
            default:
                return "";
        }
    }

    public String toPrivilegesFirstLogin() {
        webUserController.preparePrivileges(webUserApplicationController.getAllPrivilegeRoot());
        return "/national/admin/privileges";
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
                return "/institution/admin/user_edit";
            case National:
                return "/national/admin/user_edit";
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
                return "/institution/admin/institution";
            case National:
                return "/national/admin/institution";
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
                return "/institution/admin/user_password";
            case National:
                return "/national/admin/user_password";
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
                return "/regional/admin/user_privileges";
            case National:
                webUserController.prepareManagePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/user_privileges";
            default:
                return "";
        }
    }

}
