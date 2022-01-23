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
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import lk.gov.health.phsp.entity.UserColumn;
import lk.gov.health.phsp.facade.UserColumnFacade;

/**
 *
 * @author buddhika
 */
@Named
@RequestScoped
public class UserColumnController {

    @EJB
    private UserColumnFacade facade;

    @Inject
    private WebUserController webUserController;

    private FacesContext facesContext;

    /**
     * Creates a new instance of UserColumnController
     */
    public UserColumnController() {
    }

    public void markChecked(String colName) {
        String j = "select u "
                + " from UserColumn u "
                + " where u.webUser=:wu "
                + " and u.viewId=:v "
                + " and u.columnName=:c";
        Map m = new HashMap();
        String viewId = getFacesContext().getViewRoot().getViewId();
        m.put("wu", getWebUserController().getLoggedUser());
        m.put("v", viewId);
        m.put("c", colName);
        UserColumn uc = getFacade().findFirstByJpql(j, m);
        if (uc == null) {
            uc = new UserColumn();
            uc.setColumnName(colName);
            uc.setDisplay(Boolean.TRUE);
            uc.setViewId(viewId);
            uc.setWebUser(getWebUserController().getLoggedUser());
            getFacade().create(uc);
        } else {
            uc.setDisplay(true);
            getFacade().edit(uc);
        }
    }

    public void markUnChecked(String colName) {
        String j = "select u "
                + " from UserColumn u "
                + " where u.webUser=:wu "
                + " and u.viewId=:v "
                + " and u.columnName=:c";
        Map m = new HashMap();
        String viewId = getFacesContext().getViewRoot().getViewId();
        m.put("wu", getWebUserController().getLoggedUser());
        m.put("v", viewId);
        m.put("c", colName);
        UserColumn uc = getFacade().findFirstByJpql(j, m);
        if (uc == null) {
            uc = new UserColumn();
            uc.setColumnName(colName);
            uc.setDisplay(false);
            uc.setViewId(viewId);
            uc.setWebUser(getWebUserController().getLoggedUser());
            getFacade().create(uc);
        } else {
            uc.setDisplay(false);
            getFacade().edit(uc);
        }
    }

    public boolean displayColumn(String colName) {
        boolean display ;
        String j = "select u "
                + " from UserColumn u "
                + " where u.webUser=:wu "
                + " and u.viewId=:v "
                + " and u.columnName=:c";
        Map m = new HashMap();
        String viewId = getFacesContext().getViewRoot().getViewId();
        m.put("wu", getWebUserController().getLoggedUser());
        m.put("v", viewId);
        m.put("c", colName);
        UserColumn uc = getFacade().findFirstByJpql(j, m);
        if (uc == null) {
            uc = new UserColumn();
            uc.setColumnName(colName);
            uc.setDisplay(Boolean.TRUE);
            uc.setViewId(viewId);
            uc.setWebUser(getWebUserController().getLoggedUser());
            getFacade().create(uc);
        }
        if (uc.getDisplay() == null) {
            uc.setDisplay(Boolean.TRUE);
            getFacade().edit(uc);
            display = true;
        } else {
            display = uc.getDisplay();
        }
        return display;
    }

    public UserColumnFacade getFacade() {
        return facade;
    }

    public WebUserController getWebUserController() {
        return webUserController;
    }

    public FacesContext getFacesContext() {
        if (facesContext == null) {
            facesContext = FacesContext.getCurrentInstance();
        }
        return facesContext;
    }

}
