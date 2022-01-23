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
package lk.gov.health.phsp.bean;

import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.facade.DocumentFacade;

/**
 *
 * @author buddhika
 */
@Named
@ApplicationScoped
public class EncounterApplicationController {

    @EJB
    DocumentFacade encounterFacade;

    /**
     * Creates a new instance of EncounterApplicationController
     */
    public EncounterApplicationController() {
    }

    public void save(Document e) {
        if (e == null) {
            return;
        }
        if (e.getId() == null) {
            encounterFacade.create(e);
        } else {
            encounterFacade.edit(e);
        }
    }

    public Document getEncounter(Long id) {
        System.out.println("getEncounter");
        System.out.println("id = " + id);
        
        Document e = null;
        if (id == null) {
            return e;
        }
        String j = "select e "
                + " from Encounter e "
                + " where e.id=:eid";
        Map m = new HashMap();
        m.put("eid", id);
        System.out.println("m = " + m);
        System.out.println("j = " + j);
        e = encounterFacade.findFirstByJpql(j, m);
        System.out.println("e = " + e);
        return e;
    }

}
