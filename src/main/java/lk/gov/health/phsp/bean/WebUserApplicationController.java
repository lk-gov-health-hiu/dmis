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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.PrivilegeTreeNode;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.facade.util.JsfUtil;
import org.primefaces.model.TreeNode;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class WebUserApplicationController {

    /*
    EJBs
     */
    @EJB
    private WebUserFacade facade;

    @Inject
    private UserTransactionController userTransactionController;
    private List<WebUser> items = null;

    private TreeNode allPrivilegeRoot;
    private TreeNode hospitalPrivilegeRoot;
    private TreeNode labPrivilegeRoot;
    private TreeNode provincialPrivilegeRoot;
    private TreeNode regionalPrivilegeRoot;
    private TreeNode mohPrivilegeRoot;

    /**
     * Creates a new instance of WebUserApplicationController
     */
    public WebUserApplicationController() {
    }

    public WebUser getWebUser(String userName, String password) {
        if(userName==null || password==null){
            return null;
        }
        WebUser loggedUser;
        String temSQL;
        temSQL = "SELECT u FROM WebUser u WHERE lower(u.name)=:userName and u.retired =:ret";
        Map m = new HashMap();
        m.put("userName", userName.trim().toLowerCase());
        m.put("ret", false);
        loggedUser = facade.findFirstByJpql(temSQL, m);
        if (loggedUser == null) {
            return null;
        }
        if (CommonController.matchPassword(password, loggedUser.getWebUserPassword())) {
            return loggedUser;
        } else {
            loggedUser = null;
            return loggedUser;
        }
    }

    private void createAllPrivilege() {
        allPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Patient Management", allPrivilegeRoot, Privilege.Client_Management);
        TreeNode sampleManagement = new PrivilegeTreeNode("Sample Management", allPrivilegeRoot, Privilege.Sample_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", allPrivilegeRoot, Privilege.Lab_Management);
        TreeNode user = new PrivilegeTreeNode("User", allPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("Institution Administration", allPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", allPrivilegeRoot, Privilege.Monitoring_and_evaluation);
        TreeNode systemAdministration = new PrivilegeTreeNode("System Administration", allPrivilegeRoot, Privilege.System_Administration);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Cases", clientManagement, Privilege.Add_Client);
        TreeNode add_Tests = new PrivilegeTreeNode("Add Tests", clientManagement, Privilege.Add_Tests);
        TreeNode enter_Results = new PrivilegeTreeNode("Enter Results", clientManagement, Privilege.Enter_Results);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);

        //Lab Management
        TreeNode receive_samples = new PrivilegeTreeNode("Receive Samples", labManagement, Privilege.Receive_Samples);
        TreeNode enter_results_lab = new PrivilegeTreeNode("Enter Results", labManagement, Privilege.Enter_Results);
        TreeNode review_Results = new PrivilegeTreeNode("Review Results", labManagement, Privilege.Review_Results);
        TreeNode confirm_results = new PrivilegeTreeNode("Confirm Results", labManagement, Privilege.Confirm_Results);
        TreeNode print_results = new PrivilegeTreeNode("Print Results", labManagement, Privilege.Print_Results);
        TreeNode view_orders = new PrivilegeTreeNode("View Orders", labManagement, Privilege.View_Orders);
        TreeNode manage_Lab_Reports = new PrivilegeTreeNode("Lab Reports", labManagement, Privilege.Lab_Reports);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Institution Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Institution_Metadata = new PrivilegeTreeNode("Manage Institution Metadata", institutionAdministration, Privilege.Manage_Institution_Metadata);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Authorised Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Authorised Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);

        //System Administration
        TreeNode manage_Users = new PrivilegeTreeNode("Manage Users", systemAdministration, Privilege.Manage_Users);
        TreeNode manage_Metadata = new PrivilegeTreeNode("Manage Metadata", systemAdministration, Privilege.Manage_Metadata);
        TreeNode manage_Area = new PrivilegeTreeNode("Manage Area", systemAdministration, Privilege.Manage_Area);
        TreeNode manage_Institutions = new PrivilegeTreeNode("Manage Institutions", systemAdministration, Privilege.Manage_Institutions);
        TreeNode manage_Forms = new PrivilegeTreeNode("Manage Forms", systemAdministration, Privilege.Manage_Forms);

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

        //Sample Management
        TreeNode dispatch_samples = new PrivilegeTreeNode("Dispatch Samples", sampleManagement, Privilege.Dispatch_Samples);
        TreeNode divert_samples = new PrivilegeTreeNode("Divert Samples", sampleManagement, Privilege.Divert_Samples);

    }

    private void createProvincialPrivilege() {
        provincialPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Patient Management", provincialPrivilegeRoot, Privilege.Client_Management);
        TreeNode sampleManagement = new PrivilegeTreeNode("Sample Management", provincialPrivilegeRoot, Privilege.Sample_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", provincialPrivilegeRoot, Privilege.Lab_Management);
        TreeNode user = new PrivilegeTreeNode("User", provincialPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("System Administration", provincialPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", provincialPrivilegeRoot, Privilege.Monitoring_and_evaluation);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Cases", clientManagement, Privilege.Add_Client);
        TreeNode add_Tests = new PrivilegeTreeNode("Add Tests", clientManagement, Privilege.Add_Tests);
        TreeNode enter_Results = new PrivilegeTreeNode("Enter Results", clientManagement, Privilege.Enter_Results);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);

        //Lab Management
        TreeNode receive_samples = new PrivilegeTreeNode("Receive Samples", labManagement, Privilege.Receive_Samples);
        TreeNode enter_results_lab = new PrivilegeTreeNode("Enter Results", labManagement, Privilege.Enter_Results);
        TreeNode review_Results = new PrivilegeTreeNode("Review Results", labManagement, Privilege.Review_Results);
        TreeNode confirm_results = new PrivilegeTreeNode("Confirm Results", labManagement, Privilege.Confirm_Results);
        TreeNode print_results = new PrivilegeTreeNode("Print Results", labManagement, Privilege.Print_Results);
        TreeNode view_orders = new PrivilegeTreeNode("View Orders", labManagement, Privilege.View_Orders);
        TreeNode manage_Lab_Reports = new PrivilegeTreeNode("Lab Reports", labManagement, Privilege.Lab_Reports);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Institution Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Institution_Metadata = new PrivilegeTreeNode("Manage Institution Metadata", institutionAdministration, Privilege.Manage_Institution_Metadata);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Authorised Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Authorised Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

        //Sample Management
        TreeNode dispatch_samples = new PrivilegeTreeNode("Dispatch Samples", sampleManagement, Privilege.Dispatch_Samples);
        TreeNode divert_samples = new PrivilegeTreeNode("Divert Samples", sampleManagement, Privilege.Divert_Samples);

    }

    private void createRegionalPrivilege() {
        regionalPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Patient Management", regionalPrivilegeRoot, Privilege.Client_Management);
        TreeNode sampleManagement = new PrivilegeTreeNode("Sample Management", regionalPrivilegeRoot, Privilege.Sample_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", regionalPrivilegeRoot, Privilege.Lab_Management);
        TreeNode user = new PrivilegeTreeNode("User", regionalPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("System Administration", regionalPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", regionalPrivilegeRoot, Privilege.Monitoring_and_evaluation);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Cases", clientManagement, Privilege.Add_Client);
        TreeNode add_Tests = new PrivilegeTreeNode("Add Tests", clientManagement, Privilege.Add_Tests);
        TreeNode enter_Results = new PrivilegeTreeNode("Enter Results", clientManagement, Privilege.Enter_Results);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);

        //Lab Management
        TreeNode receive_samples = new PrivilegeTreeNode("Receive Samples", labManagement, Privilege.Receive_Samples);
        TreeNode enter_results_lab = new PrivilegeTreeNode("Enter Results", labManagement, Privilege.Enter_Results);
        TreeNode review_Results = new PrivilegeTreeNode("Review Results", labManagement, Privilege.Review_Results);
        TreeNode confirm_results = new PrivilegeTreeNode("Confirm Results", labManagement, Privilege.Confirm_Results);
        TreeNode print_results = new PrivilegeTreeNode("Print Results", labManagement, Privilege.Print_Results);
        TreeNode view_orders = new PrivilegeTreeNode("View Orders", labManagement, Privilege.View_Orders);
        TreeNode manage_Lab_Reports = new PrivilegeTreeNode("Lab Reports", labManagement, Privilege.Lab_Reports);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Institution Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Institution_Metadata = new PrivilegeTreeNode("Manage Institution Metadata", institutionAdministration, Privilege.Manage_Institution_Metadata);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Authorised Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Authorised Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

        //Sample Management
        TreeNode dispatch_samples = new PrivilegeTreeNode("Dispatch Samples", sampleManagement, Privilege.Dispatch_Samples);
        TreeNode divert_samples = new PrivilegeTreeNode("Divert Samples", sampleManagement, Privilege.Divert_Samples);

    }

    private void createHospitalPrivilege() {
        hospitalPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Patient Management", hospitalPrivilegeRoot, Privilege.Client_Management);
        TreeNode sampleManagement = new PrivilegeTreeNode("Sample Management", hospitalPrivilegeRoot, Privilege.Sample_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", hospitalPrivilegeRoot, Privilege.Lab_Management);
        TreeNode user = new PrivilegeTreeNode("User", hospitalPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("System Administration", hospitalPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", hospitalPrivilegeRoot, Privilege.Monitoring_and_evaluation);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Cases", clientManagement, Privilege.Add_Client);
        TreeNode add_Tests = new PrivilegeTreeNode("Add Tests", clientManagement, Privilege.Add_Tests);
        TreeNode enter_Results = new PrivilegeTreeNode("Enter Results", clientManagement, Privilege.Enter_Results);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);

        //Lab Management
        TreeNode receive_samples = new PrivilegeTreeNode("Receive Samples", labManagement, Privilege.Receive_Samples);
        TreeNode enter_results_lab = new PrivilegeTreeNode("Enter Results", labManagement, Privilege.Enter_Results);
        TreeNode review_Results = new PrivilegeTreeNode("Review Results", labManagement, Privilege.Review_Results);
        TreeNode confirm_results = new PrivilegeTreeNode("Confirm Results", labManagement, Privilege.Confirm_Results);
        TreeNode print_results = new PrivilegeTreeNode("Print Results", labManagement, Privilege.Print_Results);
        TreeNode view_orders = new PrivilegeTreeNode("View Orders", labManagement, Privilege.View_Orders);
        TreeNode manage_Lab_Reports = new PrivilegeTreeNode("Lab Reports", labManagement, Privilege.Lab_Reports);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

        //Sample Management
        TreeNode dispatch_samples = new PrivilegeTreeNode("Dispatch Samples", sampleManagement, Privilege.Dispatch_Samples);
        TreeNode divert_samples = new PrivilegeTreeNode("Divert Samples", sampleManagement, Privilege.Divert_Samples);

    }

    private void createLabPrivilege() {
        labPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Patient Management", labPrivilegeRoot, Privilege.Client_Management);
        TreeNode sampleManagement = new PrivilegeTreeNode("Sample Management", labPrivilegeRoot, Privilege.Sample_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", labPrivilegeRoot, Privilege.Lab_Management);
        TreeNode user = new PrivilegeTreeNode("User", labPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("Institution Administration", labPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", labPrivilegeRoot, Privilege.Monitoring_and_evaluation);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Cases", clientManagement, Privilege.Add_Client);
        TreeNode add_Tests = new PrivilegeTreeNode("Add Tests", clientManagement, Privilege.Add_Tests);
        TreeNode enter_Results = new PrivilegeTreeNode("Enter Results", clientManagement, Privilege.Enter_Results);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);

        //Lab Management
        TreeNode receive_samples = new PrivilegeTreeNode("Receive Samples", labManagement, Privilege.Receive_Samples);
        TreeNode enter_results_lab = new PrivilegeTreeNode("Enter Results", labManagement, Privilege.Enter_Results);
        TreeNode review_Results = new PrivilegeTreeNode("Review Results", labManagement, Privilege.Review_Results);
        TreeNode confirm_results = new PrivilegeTreeNode("Confirm Results", labManagement, Privilege.Confirm_Results);
        TreeNode print_results = new PrivilegeTreeNode("Print Results", labManagement, Privilege.Print_Results);
        TreeNode view_orders = new PrivilegeTreeNode("View Orders", labManagement, Privilege.View_Orders);
        TreeNode manage_Lab_Reports = new PrivilegeTreeNode("Lab Reports", labManagement, Privilege.Lab_Reports);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Institution Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Institution_Metadata = new PrivilegeTreeNode("Manage Institution Metadata", institutionAdministration, Privilege.Manage_Institution_Metadata);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Authorised Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Authorised Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

        //Sample Management
        TreeNode dispatch_samples = new PrivilegeTreeNode("Dispatch Samples", sampleManagement, Privilege.Dispatch_Samples);
        TreeNode divert_samples = new PrivilegeTreeNode("Divert Samples", sampleManagement, Privilege.Divert_Samples);

    }

    private void createMohPrivilege() {
        mohPrivilegeRoot = new PrivilegeTreeNode("Root", null);

        TreeNode clientManagement = new PrivilegeTreeNode("Patient Management", mohPrivilegeRoot, Privilege.Client_Management);
        TreeNode sampleManagement = new PrivilegeTreeNode("Sample Management", mohPrivilegeRoot, Privilege.Sample_Management);
        TreeNode labManagement = new PrivilegeTreeNode("Lab Management", mohPrivilegeRoot, Privilege.Lab_Management);
        TreeNode user = new PrivilegeTreeNode("User", mohPrivilegeRoot, Privilege.Manage_Users);
        TreeNode institutionAdministration = new PrivilegeTreeNode("Institution Administration", mohPrivilegeRoot, Privilege.Institution_Administration);
        TreeNode me = new PrivilegeTreeNode("Monitoring and Evaluation", mohPrivilegeRoot, Privilege.Monitoring_and_evaluation);

        //Client Management
        TreeNode add_Client = new PrivilegeTreeNode("Add Cases", clientManagement, Privilege.Add_Client);
        TreeNode add_Tests = new PrivilegeTreeNode("Add Tests", clientManagement, Privilege.Add_Tests);
        TreeNode enter_Results = new PrivilegeTreeNode("Enter Results", clientManagement, Privilege.Enter_Results);
        TreeNode search_any_Client_by_IDs = new PrivilegeTreeNode("Search any Client by IDs", clientManagement, Privilege.Search_any_Client_by_IDs);
        TreeNode search_any_Client_by_Details = new PrivilegeTreeNode("Search any Client by Details", clientManagement, Privilege.Search_any_Client_by_Details);

        //Lab Management
        TreeNode receive_samples = new PrivilegeTreeNode("Receive Samples", labManagement, Privilege.Receive_Samples);
        TreeNode enter_results_lab = new PrivilegeTreeNode("Enter Results", labManagement, Privilege.Enter_Results);
        TreeNode review_Results = new PrivilegeTreeNode("Review Results", labManagement, Privilege.Review_Results);
        TreeNode confirm_results = new PrivilegeTreeNode("Confirm Results", labManagement, Privilege.Confirm_Results);
        TreeNode print_results = new PrivilegeTreeNode("Print Results", labManagement, Privilege.Print_Results);
        TreeNode view_orders = new PrivilegeTreeNode("View Orders", labManagement, Privilege.View_Orders);
        TreeNode manage_Lab_Reports = new PrivilegeTreeNode("Lab Reports", labManagement, Privilege.Lab_Reports);

        //Institution Administration
        TreeNode manage_Institution_Users = new PrivilegeTreeNode("Manage Institution Users", institutionAdministration, Privilege.Manage_Institution_Users);
        TreeNode manage_Institution_Metadata = new PrivilegeTreeNode("Manage Institution Metadata", institutionAdministration, Privilege.Manage_Institution_Metadata);
        TreeNode manage_Authorised_Areas = new PrivilegeTreeNode("Manage Authorised Areas", institutionAdministration, Privilege.Manage_Authorised_Areas);
        TreeNode manage_Authorised_Institutions = new PrivilegeTreeNode("Manage Authorised Institutions", institutionAdministration, Privilege.Manage_Authorised_Institutions);

        //Monitoring and Evaluation
        TreeNode me_Users = new PrivilegeTreeNode("View Reports", me, Privilege.Monitoring_and_evaluation_reports);

        //Sample Management
        TreeNode dispatch_samples = new PrivilegeTreeNode("Dispatch Samples", sampleManagement, Privilege.Dispatch_Samples);
        TreeNode divert_samples = new PrivilegeTreeNode("Divert Samples", sampleManagement, Privilege.Divert_Samples);

    }

    public List<WebUser> getItems() {
        if (items == null) {
            fillWebUsers();
        }
        return items;
    }

    public void setItems(List<WebUser> items) {
        this.items = items;
    }

    private void fillWebUsers() {
        String j = "select u from WebUser u "
                + " where u.retired=false ";
        items = facade.findByJpql(j);
        userTransactionController.recordTransaction("To List All Users");
    }

    public void resetWebUsers() {
        items = null;
    }

    public TreeNode getAllPrivilegeRoot() {
        if (allPrivilegeRoot == null) {
            createAllPrivilege();
        }
        return allPrivilegeRoot;
    }

    public TreeNode getHospitalPrivilegeRoot() {
        if (hospitalPrivilegeRoot == null) {
            createHospitalPrivilege();
        }
        return hospitalPrivilegeRoot;
    }

    public TreeNode getLabPrivilegeRoot() {
        if (labPrivilegeRoot == null) {
            createLabPrivilege();
        }
        return labPrivilegeRoot;
    }

    public TreeNode getProvincialPrivilegeRoot() {
        if (provincialPrivilegeRoot == null) {
            createProvincialPrivilege();
        }
        return provincialPrivilegeRoot;
    }

    public TreeNode getRegionalPrivilegeRoot() {
        if (regionalPrivilegeRoot == null) {
            createRegionalPrivilege();
        }
        return regionalPrivilegeRoot;
    }

    public TreeNode getMohPrivilegeRoot() {
        return mohPrivilegeRoot;
    }

}
