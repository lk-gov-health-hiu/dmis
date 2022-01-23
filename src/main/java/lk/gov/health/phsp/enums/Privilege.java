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
    Client_Management("Patient Management"),
    Encounter_Management("Order Management"),
    @Deprecated
    Appointment_Management("Appointment Management"),
    Sample_Management("Sample Management"),
    Lab_Management("Lab Management"),
    @Deprecated
    Pharmacy_Management("Pharmacy Management"),
    User("User"),
    Institution_Administration("Institution Administration"),
    System_Administration("System Administration"),
    //Client Management
    Add_Client("Add Cases"),
    Add_Tests("Add Tests"),
    Mark_Tests("Mark Tests"),
    Submit_Returns("Submit Returns"),
    Search_any_Client_by_IDs("Search any Client by IDs"),
    Search_any_Client_by_Details("Search any Client by Details"),
    @Deprecated
    Search_any_client_by_ID_of_Authorised_Areas("Search any client by ID of Authorised Areas"),
    @Deprecated
    Search_any_client_by_Details_of_Authorised_Areas("Search any client by Details of Authorised Areas"),
    @Deprecated
    Search_any_client_by_ID_of_Authorised_Institutions("Search any client by ID of Authorised Institutions"),
    @Deprecated
    Search_any_client_by_Details_of_Authorised_Institutions("Search any client by Details of Authorised Institutions"),
    //Institution Administration
    Manage_Institution_Users("Manage Institution Users"),
    @Deprecated
    Manage_Institution_Metadata("Manage Institution Metadata"),
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
    Dispatch_Samples("Dispatch Samples"),
    Divert_Samples("Divert Samples"),
    //Lab Management
    View_Orders("View Orders"),
    Receive_Samples("Receive Samples"),
    Enter_Results("Enter Results"),
    Review_Results("Review Results"),
    Confirm_Results("Approve Results"),
    Print_Results("Approve Results"),
    Lab_Reports("Lab Reports"),;

    public final String label;

    private Privilege(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
