/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.enums;

/**
 *
 * @author www.divudi.com
 */
public enum Privilege {
    //Main Menu Privileges
    File_Management("File Management"),
    Letter_Management("Letter Management"),
    HR_Management("HR Management"),
    Inventory_Management("Inventory Management"),
    Finance_Management("Finance Management"),
    Audit_Management("Audit Management"),
    User("User"),
    Institution_Administration("Institution Administration"),
    System_Administration("System Administration"),
    //File Management
    Add_File("Add File"),
    Edit_File("Edit File"),
    Transfer_File("Transfer File"),
    Receive_File("Receive File"),
    Search_File("Search File"),
    Retire_File("Retire File"),
    //Letter Management
    Add_Letter("Add Letter"),
    Edit_Letter("Edit Letter"),
    Assign_Letter("Assign Letter"),
    Transfer_Letter("Transfer Letter"),
    Receive_Letter("Receive Letter"),
    Retire_Letter("Retire Letter"),
    Search_Letter("Search Letter"),
    Add_Actions_To_Letter("Add Actions"),
    Remove_Actions_To_Letter("Remove Actions"),
    //Institution Administration
    Manage_Institution_Users("Manage Institution Users"),
    Manage_Authorised_Areas("Manage Authorised Areas"),
    Manage_Authorised_Institutions("Manage Authorised Institutions"),
    //System Administration
    Manage_Users("Manage Users"),
    Manage_Metadata("Manage Metadata"),
    Manage_Area("Manage Area"),
    Manage_Institutions("Manage Institutions"),
    Manage_Forms("Manage Forms"),
    //Monitoring and Evaluation
    Monitoring_and_evaluation("Monitoring & Evaluation"),
    Monitoring_and_evaluation_reports("Monitoring & Evaluation Reports"),
    View_individual_data("View Individual Data"),
    View_aggragate_date("View Aggregate Data"),
    //Sample Management
    ;

    public final String label;

    private Privilege(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
