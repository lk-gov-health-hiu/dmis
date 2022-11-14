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
package lk.gov.health.phsp.bean;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.pojcs.Nameable;

/**
 *
 * @author buddhika
 */
@Named
@SessionScoped
@FacesConverter(value = "nameableConverter1",managed = true)
public class NameableConverter1 implements Serializable, Converter<Nameable> {

    @Inject
    InstitutionFacade institutionFacade;
    @Inject
    WebUserFacade webUserFacade;

    /**
     * Creates a new instance of NameableConverter
     */
    public NameableConverter1() {
    }

    @Override
    public Nameable getAsObject(FacesContext fc, UIComponent uic, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }
        System.out.println("value = " + value);
        System.out.println("institutionFacade = " + institutionFacade);
        Institution ins = institutionFacade.find(getKey(value));
        if (ins != null) {
            return ins;
        }
        WebUser u = webUserFacade.find(getKey(value));
        return u;
    }

    java.lang.Long getKey(String value) {
        java.lang.Long key;
        key = Long.valueOf(value);
        return key;
    }

    String getStringKey(java.lang.Long value) {
        StringBuilder sb = new StringBuilder();
        sb.append(value);
        return sb.toString();
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Nameable t) {
        return getStringKey(t.getId());
    }

}
