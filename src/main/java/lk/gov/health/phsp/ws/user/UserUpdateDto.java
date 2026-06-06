/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.user;

public class UserUpdateDto {

    private String name;
    private String code;
    private String email;
    private String telNo;
    private String webUserRole;
    private Long institutionId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelNo() { return telNo; }
    public void setTelNo(String telNo) { this.telNo = telNo; }

    public String getWebUserRole() { return webUserRole; }
    public void setWebUserRole(String webUserRole) { this.webUserRole = webUserRole; }

    public Long getInstitutionId() { return institutionId; }
    public void setInstitutionId(Long institutionId) { this.institutionId = institutionId; }

}
