package lk.gov.health.phsp.ejb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.TemporalType;
import lk.gov.health.phsp.bean.CommonController;
import lk.gov.health.phsp.entity.Sms;
import lk.gov.health.phsp.facade.SmsFacade;

/**
 *
 * @author Buddhika
 */
@Stateless
public class SmsManagerEjb {

    @EJB
    SmsFacade smsFacade;

    @SuppressWarnings("unused")
    @Schedule(second = "19", minute = "*", hour = "*", persistent = false)
    public void myTimer() {
        sendSmsAwaitingToSendInDatabase();
    }

    private void sendSmsAwaitingToSendInDatabase() {
        String j = "Select e from Sms e where e.sentSuccessfully=false and e.retired=false and e.createdAt>:d";
        Map m = new HashMap();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        m.put("d", c.getTime());
        List<Sms> smses = getSmsFacade().findByJpql(j, m, TemporalType.DATE);
        for (Sms e : smses) {
            e.setSentSuccessfully(Boolean.TRUE);
            e.setAwaitingSending(false);
            getSmsFacade().edit(e);
            sendSms(e.getReceipientNumber(), e.getSendingMessage());
            e.setSentSuccessfully(true);
            e.setSentAt(new Date());
            getSmsFacade().edit(e);
        }
    }

    public String executePost(String targetURL, Map<String, String> parameters) {
        System.out.println("executePost");
        System.out.println("parameters = " + parameters);
        System.out.println("targetURL = " + targetURL);

        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "?";
        }
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String pVal;
            System.out.println("m.getValue() = " + m.getValue());
            try {
                pVal = java.net.URLEncoder.encode(m.getValue().toString(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                pVal = "";
                Logger.getLogger(SmsManagerEjb.class.getName()).log(Level.SEVERE, null, ex);
            }
            String pPara = (String) m.getKey();
            System.out.println("pPara = " + pPara);
            targetURL += pPara + "=" + pVal.toString() + "&";
        }
        System.out.println("targetURL = " + targetURL);
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "last=true";
        }
        String inputLine = "";
        try {
            URL hh = new URL(targetURL);
            URLConnection connection = hh.openConnection();
            String redirect = connection.getHeaderField("Location");
            System.out.println("redirect = " + redirect);
            if (redirect != null) {
                connection = new URL(redirect).openConnection();
            }
            redirect = connection.getHeaderField("Location");
            System.out.println("redirect = " + redirect);
            if (redirect != null) {
                connection = new URL(redirect).openConnection();
            }
            redirect = connection.getHeaderField("Location");
            System.out.println("redirect = " + redirect);
            if (redirect != null) {
                connection = new URL(redirect).openConnection();
            }
            redirect = connection.getHeaderField("Location");
            System.out.println("redirect = " + redirect);
            if (redirect != null) {
                connection = new URL(redirect).openConnection();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            System.out.println();
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
        } catch (Exception e) {
            System.out.println("e = " + e);
        }
        return inputLine;
    }

    public String executePostOld(String targetURL, Map<String, String> parameters) {
        System.out.println("executePost");
        System.out.println("parameters = " + parameters);
        System.out.println("targetURL = " + targetURL);
        HttpURLConnection connection = null;
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "?";
        }
        Set s = parameters.entrySet();
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry m = (Map.Entry) it.next();
            String pVal;
            System.out.println("m.getValue() = " + m.getValue());
            try {
                pVal = java.net.URLEncoder.encode(m.getValue().toString(), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                pVal = "";
                Logger.getLogger(SmsManagerEjb.class.getName()).log(Level.SEVERE, null, ex);
            }
            String pPara = (String) m.getKey();
            System.out.println("pPara = " + pPara);
            targetURL += pPara + "=" + pVal.toString() + "&";
        }
        System.out.println("targetURL = " + targetURL);
        if (parameters != null && !parameters.isEmpty()) {
            targetURL += "last=true";
        }
        try {
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(targetURL);
            wr.flush();
            wr.close();
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
        String pattern = "yyyyMMMMdd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String strDate = simpleDateFormat.format(new Date());
        String decKey = CommonController.encrypt(strDate);
        Map<String, String> m = new HashMap();
        m.put("number", number);
        m.put("message", message);
        m.put("key", decKey);
        String res = executePost("https://hims.health.gov.lk/sms-mw", m);
        System.out.println("res = " + res);
        if (res == null) {
            return false;
        } else if (res.toUpperCase().contains("200")) {
            return true;
        } else {
            return false;
        }

    }

    public SmsFacade getSmsFacade() {
        return smsFacade;
    }

}
