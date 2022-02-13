package lk.gov.health.phsp.enums;

/**
 * @author Rukshan Ranatunge <arkruka@gmail.com>
 */
public enum SearchFilterType {
  SYSTEM_DATE("System Entry Date"),
  DOCUMENT_DATE("Document Date"),
  RECEIVED_DATE("Received Date");

  private String label;

  private SearchFilterType(String label) {
    this.label = label;
  }

  public String getLabel(){
    return this.label;
  }

  public String getCode(){
    switch(this){
        case SYSTEM_DATE:
            return "createdAt";
        case DOCUMENT_DATE:
            return "documentDate";
        case RECEIVED_DATE:
            return "receivedDate";
        default:
            return "E";
    }
  }
}



