/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.institution;

public class InstitutionUpdateDto {

    private String name;
    private String sname;
    private String tname;
    private String code;
    private String institutionType;
    private String address;
    private String phone;
    private String mobile;
    private String fax;
    private String email;
    private String web;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }

    public String getTname() { return tname; }
    public void setTname(String tname) { this.tname = tname; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getInstitutionType() { return institutionType; }
    public void setInstitutionType(String institutionType) { this.institutionType = institutionType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }

}
