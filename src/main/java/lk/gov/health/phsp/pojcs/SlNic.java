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

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author buddhika
 */
public class SlNic {

    private String nic;
    private boolean oldNic;
    private boolean newNic;
    private Date dateOfBirth;
    private String sex;

    public void processNic() {
        // // System.out.println("processNic");
        oldNic = false;
        newNic = false;
        if (nic == null || nic.trim().equals("")) {
            return;
        }
        nic = nic.trim().toLowerCase();
        if (nic.length() == 10) {
            String lastCharactor = nic.substring(9);
            // // System.out.println("lastCharactor = " + lastCharactor);
            if (!lastCharactor.equals("v") && !lastCharactor.equals("x")) {
                return;
            }
            String strYear = nic.substring(0, 2);
            Integer year = 0;
            Integer dates = 0;
            try {
                year = Integer.parseInt(strYear);
                String strDates = nic.substring(2, 5);
                dates = Integer.parseInt(strDates);
            } catch (NumberFormatException e) {
                return;
            }

            if (dates > 499) {
                sex = "Female";
                dates = dates - 500;
            } else {
                sex = "Male";
            }
            Calendar c = Calendar.getInstance();
            year = year + 1900;
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DATE, 1);
            c.set(Calendar.HOUR_OF_DAY, 1);
            c.add(Calendar.DATE, dates - 2);
            dateOfBirth = c.getTime();
        } else if (nic.length() == 12) {
            String strYear = nic.substring(0, 4);
            Integer year = 0;
            Integer dates = 0;
            try {
                year = Integer.parseInt(strYear);

                String strDates = nic.substring(4, 7);
                // // System.out.println("strDates = " + strDates);
                dates = Integer.parseInt(strDates);
                if (dates > 499) {
                    sex = "Female";
                    dates = dates - 500;
                } else {
                    sex = "Male";
                }
            } catch (Exception e) {
                return;
            }
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, 0);
            c.set(Calendar.DATE, 1);
            c.set(Calendar.HOUR_OF_DAY, 1);
            c.add(Calendar.DATE, dates - 2);
            dateOfBirth = c.getTime();
        }
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
        processNic();
    }

    public boolean isOldNic() {
        return oldNic;
    }

    public void setOldNic(boolean oldNic) {
        this.oldNic = oldNic;
    }

    public boolean isNewNic() {
        return newNic;
    }

    public void setNewNic(boolean newNic) {
        this.newNic = newNic;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

}
