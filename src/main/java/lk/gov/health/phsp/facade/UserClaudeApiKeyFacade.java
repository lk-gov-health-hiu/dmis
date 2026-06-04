/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.UserClaudeApiKey;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class UserClaudeApiKeyFacade extends AbstractFacade<UserClaudeApiKey> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserClaudeApiKeyFacade() {
        super(UserClaudeApiKey.class);
    }

}
