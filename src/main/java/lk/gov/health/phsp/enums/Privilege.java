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
    Institutional_Mail_Management("Institutional Mail Management"),
    Mail_Branch_Mail_Management("Mail Branch Mail Management"),
    System_Administration("System Administration"),
    Institution_Administration("Institution Administration"),
    User("User"),
    //File Management
    Add_File("Add File"),
    Edit_File("Edit File"),
    Transfer_File("Transfer File"),
    Receive_File("Receive File"),
    Search_File("Search File"),
    Retire_File("Retire File"),
    //Institutional Mail Management
    Add_Letter("Add Letter"),
    Edit_Letter("Edit Letter"),
    Assign_Letter("Assign Letter"),
    Transfer_Letter("Transfer Letter"),
    Receive_Letter("Receive Letter"),
    Retire_Letter("Retire Letter"),
    Search_Letter("Search Letter"),
    Add_Actions_To_Letter("Add Actions"),
    Remove_Actions_To_Letter("Remove Actions"),
    //Mail Branch Mail Management
    Add_Letter_Postal_Branch("Add Letter"),
    Edit_Letter_Postal_Branch("Edit Letter"),
    Retire_Letter_Postal_Branch("Retire Letter"),
    Search_Letter_Postal_Branch("Search Letter"),
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
    //Deprecated
    @Deprecated
    Letter_Management("Letter Management"),
    @Deprecated
    HR_Management("HR Management"),
    @Deprecated
    Inventory_Management("Inventory Management"),
    @Deprecated
    Finance_Management("Finance Management"),
    @Deprecated
    Audit_Management("Audit Management"),
    @Deprecated
    Client_Management("Patient Management"),
    @Deprecated
    Encounter_Management("Order Management"),
    @Deprecated
    Appointment_Management("Appointment Management"),
    @Deprecated
    Sample_Management("Sample Management"),
    @Deprecated
    Lab_Management("Lab Management"),
    @Deprecated
    Pharmacy_Management("Pharmacy Management"),
    @Deprecated
    Add_Client("Add Cases"),
    @Deprecated
    Add_Tests("Add Tests"),
    @Deprecated
    Mark_Tests("Mark Tests"),
    @Deprecated
    Submit_Returns("Submit Returns"),
    @Deprecated
    Search_any_Client_by_IDs("Search any Client by IDs"),
    @Deprecated
    Search_any_Client_by_Details("Search any Client by Details"),
    @Deprecated
    Search_any_client_by_ID_of_Authorised_Areas("Search any client by ID of Authorised Areas"),
    @Deprecated
    Search_any_client_by_Details_of_Authorised_Areas("Search any client by Details of Authorised Areas"),
    @Deprecated
    Search_any_client_by_ID_of_Authorised_Institutions("Search any client by ID of Authorised Institutions"),
    @Deprecated
    Search_any_client_by_Details_of_Authorised_Institutions("Search any client by Details of Authorised Institutions"),
    @Deprecated
    Manage_Institution_Metadata("Manage Institution Metadata"),
    @Deprecated
    Dispatch_Samples("Dispatch Samples"),
    @Deprecated
    Divert_Samples("Divert Samples"),
    @Deprecated
    View_Orders("View Orders"),
    @Deprecated
    Receive_Samples("Receive Samples"),
    @Deprecated
    Enter_Results("Enter Results"),
    @Deprecated
    Review_Results("Review Results"),
    @Deprecated
    Confirm_Results("Approve Results"),
    @Deprecated
    Print_Results("Approve Results"),
    @Deprecated
    Lab_Reports("Lab Reports"),
    ;

    public final String label;

    private Privilege(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
