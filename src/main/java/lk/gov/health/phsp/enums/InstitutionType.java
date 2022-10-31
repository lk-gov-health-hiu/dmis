package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum InstitutionType {
    Ministry_of_Health("Ministry of Health"),
    Other_Ministry("Other Ministry"),
    Provincial_General_Hospital("Provincial General Hospital"),
    Provincial_Department_of_Health_Services("Provincial Department of Health Services"),
    Regional_Department_of_Health_Department("Regional Department of Health Department"),
    Hospital("Hospital"),
    National_Hospital("National Hospital"),
    Teaching_Hospital("Teaching Hospital"),
    District_General_Hospital("District General Hospital"),
    Base_Hospital("Base Hospital"),
    Divisional_Hospital("Divisional Hospital"),
    Primary_Medical_Care_Unit("Primary Medical Care Unit"),
    MOH_Office("MOH Office"),
    Clinic("Clinic"),
    OPD("OPD"),
    MailBranch("Mail Branch"),
    @Deprecated
    Medical_Clinic("Medical Clinic"),
    @Deprecated
    Surgical_Clinic("Surgical Clinic"),
    @Deprecated
    Cardiology_Clinic("Cardiology Clinic"),
    @Deprecated
    Other_Clinic("Other Clinic"),
    Intermediate_Care_Centre("Intermediate Care Centre"),
    Unit("Unit"),
    Ward("Ward"),
    @Deprecated
    Procedure_Room("Procedure Room"),
    @Deprecated
    Pharmacy("Pharmacy"),
    Lab("Laboratory"),
    Mobile_Lab("Mobile Lab"),
    Stake_Holder("Stake Holder"),
    Partner("Partner"),
    Private_Sector_Institute("Private Sector Institute"),
    Private_Sector_Labatory("Private Sector Laboratory"),
    Other("Other"),
    @Deprecated
    Ward_Clinic("Ward Clinic");

    private final String label;

    private InstitutionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
