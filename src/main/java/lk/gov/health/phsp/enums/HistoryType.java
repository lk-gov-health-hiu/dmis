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
 * @author User
 */
public enum HistoryType {
    From_List("From List"),
    To_List("To List"),
    Through_List("Through List"),
    File_Created("File Created"),
    File_Institution_Transfer("File Transfer"),
    File_Owner_Transfer("File Owner Change"),
    Letter_added_by_mail_branch("Letter added by Mail Branch"),
    Letter_Created("Letter Added from Institute"),
    Letter_Generated("Letter Generated"),
    Letter_Assigned("Letter Assigned"),
    Letter_Copy_or_Forward("Letter Copy or Forward"),
    @Deprecated
    Letter_Copy_or_Forward_Accepted("Letter Copy or Forward Accepted"),
    @Deprecated
    Letter_Assigning_Accepted("Letter Assigning Accepted"),
    Letter_Received("Letter Received"),
    Letter_Sent("Letter Send"),
    Letter_Action_Taken("Letter Action Taken");
    
    private final String label;    
    private HistoryType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
}

