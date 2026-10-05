/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lk.gov.health.phsp.bean.util.JsfUtil;
import lk.gov.health.phsp.entity.FavouriteInstitution;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.facade.FavouriteInstitutionFacade;

/**
 * Lets any user maintain their own institution's list of favourite
 * institutions, which the Our Letter To/Copy autocompletes can be narrowed to.
 * Always scoped to the logged-in institution.
 */
@Named
@SessionScoped
public class FavouriteInstitutionController implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private FavouriteInstitutionFacade favouriteInstitutionFacade;
    @Inject
    private WebUserController webUserController;

    private Institution institutionToAdd;
    private List<FavouriteInstitution> items;

    public String toManageFavouriteInstitutions() {
        institutionToAdd = null;
        items = null;
        return "/institution/favourite_institutions?faces-redirect=true";
    }

    public void add() {
        Institution owner = webUserController.getLoggedInstitution();
        if (owner == null) {
            JsfUtil.addErrorMessage("No logged-in institution.");
            return;
        }
        if (institutionToAdd == null) {
            JsfUtil.addErrorMessage("Select an institution to add.");
            return;
        }
        for (FavouriteInstitution f : getItems()) {
            if (institutionToAdd.equals(f.getFavourite())) {
                JsfUtil.addErrorMessage(institutionToAdd.getName() + " is already a favourite.");
                return;
            }
        }
        FavouriteInstitution f = new FavouriteInstitution();
        f.setInstitution(owner);
        f.setFavourite(institutionToAdd);
        f.setCreatedBy(webUserController.getLoggedUser());
        f.setCreatedAt(new Date());
        favouriteInstitutionFacade.create(f);
        JsfUtil.addSuccessMessage(institutionToAdd.getName() + " added to favourites.");
        institutionToAdd = null;
        items = null;
    }

    public void remove(FavouriteInstitution f) {
        if (f == null || f.getInstitution() == null
                || !f.getInstitution().equals(webUserController.getLoggedInstitution())) {
            JsfUtil.addErrorMessage("You can only change your own institution's favourites.");
            return;
        }
        f.setRetired(true);
        f.setRetiredBy(webUserController.getLoggedUser());
        f.setRetiredAt(new Date());
        favouriteInstitutionFacade.edit(f);
        JsfUtil.addSuccessMessage(f.getFavourite().getName() + " removed from favourites.");
        items = null;
    }

    /** Favourite institutions of {@code owner}, by name; queried fresh so other users' changes show. */
    public List<Institution> findFavourites(Institution owner) {
        List<Institution> res = new ArrayList<>();
        for (FavouriteInstitution f : findFavouriteRecords(owner)) {
            res.add(f.getFavourite());
        }
        return res;
    }

    private List<FavouriteInstitution> findFavouriteRecords(Institution owner) {
        if (owner == null) {
            return new ArrayList<>();
        }
        String j = "select f from FavouriteInstitution f "
                + " where f.retired=:ret "
                + " and f.institution=:owner "
                + " and f.favourite.retired=:ret "
                + " order by f.favourite.name";
        Map<String, Object> m = new HashMap<>();
        m.put("ret", false);
        m.put("owner", owner);
        return favouriteInstitutionFacade.findByJpql(j, m);
    }

    public List<FavouriteInstitution> getItems() {
        if (items == null) {
            items = findFavouriteRecords(webUserController.getLoggedInstitution());
        }
        return items;
    }

    public Institution getInstitutionToAdd() {
        return institutionToAdd;
    }

    public void setInstitutionToAdd(Institution institutionToAdd) {
        this.institutionToAdd = institutionToAdd;
    }

}
