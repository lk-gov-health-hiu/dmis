/*
 * The MIT License
 *
 * Copyright 2019 Dr M H B Ariyaratne<buddhika.ari@gmail.com>.
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
package lk.gov.health.phsp.enums;

/**
 *
 * @author Dr M H B Ariyaratne
 */
public enum WebUserRole {
    //National
    System_Administrator("System Administrator"),
    Super_User("Super User"),
    User("User"),
    ChiefEpidemiologist("Chief Epidemiologist"),
    Epidemiologist("Epidemiologist"),
    Lab_National("Lab National"),
    //PD Level
    Pdhs("PDHS"),
    Provincial_Admin("Provincial System Administrator"),
    Pdhs_Staff("PDHS Staff"),
    //RD Level
    Rdhs("RDHS"),
    Regional_Admin("Regional System Administrator"),
    Re("Regional Epidemiologist"),
    Rdhs_Staff("RDHS Staff"),
    //MOH Level
    Moh("MOH"),
    Amoh("AMOH"),
    Sphi("SPHI"),
    Phns("PHNS"),
    Sphm("SPHM"),
    Phi("PHI"),
    Phm("PHM"),
    MohStaff("MOH Staff"),
    //Hospital Staff
    Hospital_Admin("Hospital System Administrator"),
    Hospital_User("Hospital User"),
    Doctor("Hospital Doctor"),
    Nurse("Nurse"),
    //Lab staff
    Lab_Admin("Lab System Administrator"),
    Lab_Consultant("Lab Consultant"),
    Lab_Mo("Lab Medical Officer"),
    Lab_Mlt("MLT"),
    Lab_User("Lab Assistant"),
    //Client
    Client("Client");
   
    private final String label;

    private WebUserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
