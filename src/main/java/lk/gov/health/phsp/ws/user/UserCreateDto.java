/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.user;

public class UserCreateDto {

    private String username;
    private String password;
    private String name;
    private String code;
    private String email;
    private String telNo;
    private String webUserRole;
    private Long institutionId;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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
