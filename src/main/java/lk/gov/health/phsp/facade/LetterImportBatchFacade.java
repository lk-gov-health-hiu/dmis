/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.facade;

import lk.gov.health.phsp.entity.LetterImportBatch;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class LetterImportBatchFacade extends AbstractFacade<LetterImportBatch> {

    @PersistenceContext(unitName = "hmisPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public LetterImportBatchFacade() {
        super(LetterImportBatch.class);
    }

}
