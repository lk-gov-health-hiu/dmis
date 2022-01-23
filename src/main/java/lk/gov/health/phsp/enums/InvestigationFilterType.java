package lk.gov.health.phsp.enums;

/**
 * @author Rukshan Ranatunge <arkruka@gmail.com>
 */
public enum InvestigationFilterType {
  CREATEDAT("Ordered Date & Time"),
  SAMPLEDAT("Sampled Date & Time"),
  RESULTSAT("Results Issued Date & Time");

  private String label;

  private InvestigationFilterType(String label) {
    this.label = label;
  }

  public String getLabel(){
    return this.label;
  }

  public String getCode(){
    switch(this){
        case CREATEDAT:
            return "createdat";
        case SAMPLEDAT:
            return "sampledat";
        case RESULTSAT:
            return "resultsat";
        default:
            return "E";
    }
  }
}



