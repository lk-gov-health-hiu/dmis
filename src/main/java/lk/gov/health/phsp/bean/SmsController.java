/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lk.gov.health.phsp.bean;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import javax.ejb.EJB;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Sms;
import lk.gov.health.phsp.facade.SmsFacade;

/**
 *
 * @author Dushan
 */
@Named(value = "smsController")
@SessionScoped
public class SmsController implements Serializable {

    @EJB
    SmsFacade smsFacade;

    @Inject
    WebUserController webUserController;
    @Inject
    CommonController commonFunctions;

    List<Sms> smses;
    List<SmsSummeryRow> smsSummeryRows;

    private Date fromDate;
    private Date toDate;
    private Institution institution;

    /**
     * Creates a new instance of SmsController
     */
    public SmsController() {
    }

    public String listSentSms() {
        String j = "select s "
                + " from Sms s "
                + " where s.retired=false "
                + " and s.createdAt between :fd and :td ";
        Map m = new HashMap();
        m.put("fd", getFromDate());
        m.put("td", getToDate());
        if (institution == null) {
            j += " and s.institution in :inss ";
            m.put("inss", webUserController.getLoggableInstitutions());
        } else {
            j += " and s.institution=:ins";
            m.put("ins", institution);
        }
        smses = smsFacade.findByJpql(j, m);
        return "/sms/sent_successfully";
    }

    public static String executePost(String targetURL, Map<String, Object> parameters) {
        HttpURLConnection connection = null;
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "?";
        }
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            Object pVal = m.getValue();
            String pPara = (String) m.getKey();
            targetURL += pPara + "=" + pVal.toString() + "&";
        }
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "last=true";
        }
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setUseCaches(false);
            connection.setDoOutput(true);
            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(targetURL);
            wr.flush();
            wr.close();

            //Get Response  
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean sendSms(String number, String message) {
        Map m = new HashMap();
        m.put("tnum", number);
        m.put("msg", message);
        m.put("apikey", "XAOHBFRNKKODDCNOUYB4587GDDS63DHJ");
        String res = executePost("http://192.168.202.4:6080/sms", m);
        if (res == null) {
            return false;
        } else if (res.toUpperCase().contains("200")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean sendSmsPromo(String number, String message) {
        Map m = new HashMap();
        m.put("tnum", number);
        m.put("msg", message);
        m.put("apikey", "XAOHBFRNKKODDCNOUYB4587GDDS63DHJ");
        String res = executePost("http://192.168.202.4:6080/sms", m);
        if (res == null) {
            return false;
        } else if (res.toUpperCase().contains("200")) {
            return true;
        } else {
            return false;
        }

    }

    public List<SmsSummeryRow> getSmsSummeryRows() {
        return smsSummeryRows;
    }

    public void setSmsSummeryRows(List<SmsSummeryRow> smsSummeryRows) {
        this.smsSummeryRows = smsSummeryRows;
    }

    public CommonController getCommonFunctions() {
        return commonFunctions;
    }

    public Date getFromDate() {
        if (fromDate == null) {
            fromDate = CommonController.startOfTheDate();
        }
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        if (toDate == null) {
            toDate = CommonController.endOfTheDate();
        }
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public class SmsSummeryRow {

        String smsType;
        long count;

        public String getSmsType() {
            return smsType;
        }

        public void setSmsType(String smsType) {
            this.smsType = smsType;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }

    //---------Getters and Setters
    public WebUserController getWebUserController() {
        return webUserController;
    }

    public SmsFacade getSmsFacade() {
        return smsFacade;
    }

    public void setSmsFacade(SmsFacade smsFacade) {
        this.smsFacade = smsFacade;
    }

    public List<Sms> getSmses() {
        return smses;
    }

    public void setSmses(List<Sms> smses) {
        this.smses = smses;
    }

}
