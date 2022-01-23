/*
 * The MIT License
 *
 * Copyright 2021 buddhika.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.Institution;

/**
 *
 * @author buddhika
 */
public class ReportHolder {
    private List<Document> encounters;
    private Institution ins;
    private Date dateOfReceipt;
    private Date dateOfReport;
    private Date dateOfCollection;
    private int pageNo;
    private int totalPages;
    private String id;

    
    
    public List<Document> getEncounters() {
        if(encounters==null){
            encounters = new ArrayList<>();
        }
        return encounters;
    }

    public void setEncounters(List<Document> encounters) {
        this.encounters = encounters;
    }

    public Institution getIns() {
        return ins;
    }

    public void setIns(Institution ins) {
        this.ins = ins;
    }

    public Date getDateOfReceipt() {
        return dateOfReceipt;
    }

    public void setDateOfReceipt(Date dateOfReceipt) {
        this.dateOfReceipt = dateOfReceipt;
    }

    public Date getDateOfReport() {
        return dateOfReport;
    }

    public void setDateOfReport(Date dateOfReport) {
        this.dateOfReport = dateOfReport;
    }

    public Date getDateOfCollection() {
        return dateOfCollection;
    }

    public void setDateOfCollection(Date dateOfCollection) {
        this.dateOfCollection = dateOfCollection;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public String getId() {
        id="|";
        if(ins!=null){
            id += ins.getId() + "|";
        }else{
            id += "null|";
        }
        if(dateOfReport!=null){
            id+=CommonController.dateTimeToString(dateOfReport, "ddMMyy")+ "|";
        }else{
            id+="null|";
        }
        if(dateOfReceipt!=null){
            id+=CommonController.dateTimeToString(dateOfReceipt, "ddMMyy")+ "|";
        }else{
            id+="null|";
        }
        if(dateOfCollection!=null){
            id+=CommonController.dateTimeToString(dateOfCollection, "ddMMyy")+ "|";
        }else{
            id+="null|";
        }
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    
    
    
}
