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

import lk.gov.health.phsp.entity.Item;

/**
 *
 * @author buddhika
 */
public class OrderingCategoryResults {
    private Item orderingCategory;
    private Long ordered;
    private Long positives;
    private Double positivityRate;

    public OrderingCategoryResults() {
    }

    public Item getOrderingCategory() {
        return orderingCategory;
    }

    public void setOrderingCategory(Item orderingCategory) {
        this.orderingCategory = orderingCategory;
    }

    public Long getOrdered() {
        return ordered;
    }

    public void setOrdered(Long ordered) {
        this.ordered = ordered;
    }

    public Double getPositivityRate() {
        return positivityRate;
    }

    public void setPositivityRate(Double positivityRate) {
        this.positivityRate = positivityRate;
    }

    public Long getPositives() {
        return positives;
    }

    public void setPositives(Long positives) {
        this.positives = positives;
    }
    

    
}
