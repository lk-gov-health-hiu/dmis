/*
 * The MIT License
 *
 * Copyright 2022 buddhika.
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
package lk.gov.health.phsp.pojcs;

/**
 *
 * @author buddhika
 */
public interface Nameable {
    
    public Long getId();
    public void setId(Long id);
    
    public String getName();
    public void setName(String name) ;
    
    public String getInsName();
    public void setInsName(String name) ;
    
    public String getCode();
    public void setCode(String code);
    
    public String getAddress() ;
    public void setAddress(String address) ;

    public String getFax() ;
    public void setFax(String fax) ;

    public String getEmail() ;
    public void setEmail(String email);

    public String getPhone() ;
    public void setPhone(String phone) ;

    public String getMobile() ;
    public void setMobile(String mobile) ;

    public String getTname() ;
    public void setTname(String tname) ;

    public String getSname() ;
    public void setSname(String sname);

    public String getDisplayName();
    
    public Boolean getInstitute();
    public Boolean getWebUser();
    
    @Override
    public String toString();
    
}
