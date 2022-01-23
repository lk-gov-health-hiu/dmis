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
import javax.inject.Inject;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.UserPrivilege;
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
    InstitutionController institutionController;
    @Inject
    PreferenceController preferenceController;

    /**
     * Creates a new instance of MenuController
     */
    public MenuController() {
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
            case Regional:
                return "/regional/reports_index";
            case National:
                return "/national/reports_index";
            case Hospital:
                return "/hospital/reports_index";
            case Lab:
                return "/lab/reports_index";
            case National_Lab:
                return "/national/lab_reports_index";
            case Moh:
                return "/moh/reports_index";
            case Provincial:
                return "/provincial/reports_index";
            default:
                return "";
        }
    }

    public String toSearch() {
        switch (webUserController.getLoggedUser().getWebUserRoleLevel()) {
            case Regional:
                return "/regional/search";
            case National:
                return "/national/search";
            case Hospital:
                return "/hospital/search";
            case Lab:
                return "/lab/search";
            case National_Lab:
                return "/national/search";
            case Moh:
                return "/moh/search";
            case Provincial:
                return "/provincial/search";
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
            case Regional:
                return "/regional/admin/index";
            case National:
                return "/national/admin/index";
            case Hospital:
                return "/hospital/admin/index";
            case Lab:
                return "/lab/admin/index";
            case National_Lab:
                return "/national/admin/index";
            case Moh:
                return "/moh/admin/index";
            case Provincial:
                return "/provincial/admin/index";
            default:
                return "";
        }
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
            case Regional:
                return "/regional/admin/preferences";
            case National:
                return "/national/admin/preferences";
            case Hospital:
                return "/hospital/admin/preferences";
            case Lab:
                return "/lab/admin/preferences";
            case National_Lab:
                return "/national/admin/preferences";
            case Moh:
                return "/moh/admin/preferences";
            case Provincial:
                return "/provincial/admin/preferences";
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
            case Regional:
                return "/regional/admin/user_new";
            case National:
                return "/national/admin/user_new";
            case Hospital:
                return "/hospital/admin/user_new";
            case Lab:
                return "/lab/admin/user_new";
            case National_Lab:
                return "/national/admin/user_new";
            case Moh:
                return "/moh/admin/user_new";
            case Provincial:
                return "/provincial/admin/user_new";
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
            case Regional:
                return "/regional/admin/institution";
            case National:
                return "/national/admin/institution";
            case Hospital:
                return "/hospital/admin/institution";
            case Lab:
                return "/lab/admin/institution";
            case National_Lab:
                return "/national/admin/institution";
            case Moh:
                return "/moh/admin/institution";
            case Provincial:
                return "/provincial/admin/institution";
            default:
                return "";
        }
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
            case Regional:
                return "/regional/admin/user_list";
            case National:
                return "/national/admin/user_list";
            case Hospital:
                return "/hospital/admin/user_list";
            case Lab:
                return "/lab/admin/user_list";
            case National_Lab:
                return "/national/admin/user_list";
            case Moh:
                return "/moh/admin/user_list";
            case Provincial:
                return "/provincial/admin/user_list";
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
            case Regional:
                return "/regional/admin/institution_list";
            case National:
                return "/national/admin/institution_list";
            case Hospital:
                return "/hospital/admin/institution_list";
            case Lab:
                return "/lab/admin/institution_list";
            case National_Lab:
                return "/national/admin/institution_list";
            case Moh:
                return "/moh/admin/institution_list";
            case Provincial:
                System.out.println("provincial");
                return "/provincial/admin/institution_list";
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
            case Regional:
                return "/regional/admin/institution_list_with_users";
            case National:
                return "/national/admin/institution_list_with_users";
            case Hospital:
                return "/hospital/admin/institution_list_with_users";
            case Lab:
                return "/lab/admin/institution_list_with_users";
            case National_Lab:
                return "/national/admin/institution_list_with_users";
            case Moh:
                return "/moh/admin/institution_list_with_users";
            case Provincial:
                return "/provincial/admin/institution_list_with_users";
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
            case Regional:
                webUserController.preparePrivileges(webUserApplicationController.getRegionalPrivilegeRoot());
                return "/regional/admin/privileges";
            case National:
                webUserController.preparePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/privileges";
            case Hospital:
                webUserController.preparePrivileges(webUserApplicationController.getHospitalPrivilegeRoot());
                return "/hospital/admin/privileges";
            case Lab:
                webUserController.preparePrivileges(webUserApplicationController.getLabPrivilegeRoot());
                return "/lab/admin/privileges";
            case National_Lab:
                webUserController.preparePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/privileges";
            case Moh:
                webUserController.preparePrivileges(webUserApplicationController.getMohPrivilegeRoot());
                return "/moh/admin/privileges";
            case Provincial:
                webUserController.preparePrivileges(webUserApplicationController.getProvincialPrivilegeRoot());
                return "/provincial/admin/privileges";
            default:
                return "";
        }
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
            case Regional:
                return "/regional/admin/user_edit";
            case National:
                return "/national/admin/user_edit";
            case Hospital:
                return "/hospital/admin/user_edit";
            case Lab:
                return "/lab/admin/user_edit";
            case National_Lab:
                return "/national/admin/user_edit";
            case Moh:
                return "/moh/admin/user_edit";
            case Provincial:
                return "/provincial/admin/user_edit";
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
            case Regional:
                return "/regional/admin/institution";
            case National:
                return "/national/admin/institution";
            case Hospital:
                return "/hospital/admin/institution";
            case Lab:
                return "/lab/admin/institution";
            case National_Lab:
                return "/national/admin/institution";
            case Moh:
                return "/moh/admin/institution";
            case Provincial:
                return "/provincial/admin/institution";
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
            case Regional:
                return "/regional/admin/user_password";
            case National:
                return "/national/admin/user_password";
            case Hospital:
                return "/hospital/admin/user_password";
            case Lab:
                return "/lab/admin/user_password";
            case National_Lab:
                return "/national/admin/user_password";
            case Moh:
                return "/moh/admin/user_password";
            case Provincial:
                return "/provincial/admin/user_password";
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
            case Regional:
                webUserController.prepareManagePrivileges(webUserApplicationController.getRegionalPrivilegeRoot());
                return "/regional/admin/user_privileges";
            case National:
                webUserController.prepareManagePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/user_privileges";
            case Hospital:
                webUserController.prepareManagePrivileges(webUserApplicationController.getHospitalPrivilegeRoot());
                return "/hospital/admin/user_privileges";
            case Lab:
                webUserController.prepareManagePrivileges(webUserApplicationController.getLabPrivilegeRoot());
                return "/lab/admin/user_privileges";
            case National_Lab:
                webUserController.prepareManagePrivileges(webUserApplicationController.getAllPrivilegeRoot());
                return "/national/admin/user_privileges";
            case Moh:
                webUserController.prepareManagePrivileges(webUserApplicationController.getMohPrivilegeRoot());
                return "/moh/admin/user_privileges";
            case Provincial:
                webUserController.prepareManagePrivileges(webUserApplicationController.getProvincialPrivilegeRoot());
                return "/provincial/admin/user_privileges";
            default:
                return "";
        }
    }

}
