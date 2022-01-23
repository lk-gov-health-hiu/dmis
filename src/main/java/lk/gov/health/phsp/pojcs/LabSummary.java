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

import lk.gov.health.phsp.entity.Institution;

/**
 *
 * @author buddhika
 */
public class LabSummary {
    private Institution institution;
    private Long ordered;
    private Long sampled;
    private Long received;
    private Long dataEntered;
    private Long reviewed;
    private Long confirmed;
    private Long printed;
    private Long toDispatch;
    private Long toReceive;
    private Long toEnterData;
    private Long toReview;
    private Long toConfirm;
    private Long toPrint;

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public Long getOrdered() {
        return ordered;
    }

    public void setOrdered(Long ordered) {
        this.ordered = ordered;
    }

    public Long getSampled() {
        return sampled;
    }

    public void setSampled(Long sampled) {
        this.sampled = sampled;
    }

    public Long getReceived() {
        return received;
    }

    public void setReceived(Long received) {
        this.received = received;
    }

    public Long getDataEntered() {
        return dataEntered;
    }

    public void setDataEntered(Long dataEntered) {
        this.dataEntered = dataEntered;
    }

    public Long getReviewed() {
        return reviewed;
    }

    public void setReviewed(Long reviewed) {
        this.reviewed = reviewed;
    }

    public Long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Long confirmed) {
        this.confirmed = confirmed;
    }

    public Long getPrinted() {
        return printed;
    }

    public void setPrinted(Long printed) {
        this.printed = printed;
    }

    public Long getToDispatch() {
        return toDispatch;
    }

    public void setToDispatch(Long toDispatch) {
        this.toDispatch = toDispatch;
    }

    public Long getToReceive() {
        return toReceive;
    }

    public void setToReceive(Long toReceive) {
        this.toReceive = toReceive;
    }

    public Long getToEnterData() {
        return toEnterData;
    }

    public void setToEnterData(Long toEnterData) {
        this.toEnterData = toEnterData;
    }

    public Long getToReview() {
        return toReview;
    }

    public void setToReview(Long toReview) {
        this.toReview = toReview;
    }

    public Long getToConfirm() {
        return toConfirm;
    }

    public void setToConfirm(Long toConfirm) {
        this.toConfirm = toConfirm;
    }

    public Long getToPrint() {
        return toPrint;
    }

    public void setToPrint(Long toPrint) {
        this.toPrint = toPrint;
    }
    
    
}
