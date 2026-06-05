/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.io.Serializable;
import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import lk.gov.health.phsp.entity.LetterImportBatch;
import lk.gov.health.phsp.entity.LetterImportItem;
import lk.gov.health.phsp.enums.LetterImportStatus;
import lk.gov.health.phsp.facade.LetterImportBatchFacade;
import lk.gov.health.phsp.facade.LetterImportItemFacade;

/**
 * Short, independent ({@link TransactionAttributeType#REQUIRES_NEW}) database
 * writes used by the long-running, transaction-less {@code LetterImportService}
 * orchestration. Each method commits on its own so the polling review UI sees
 * progress as letters are processed, and so a slow Claude call never holds a
 * transaction open.
 */
@Stateless
public class LetterImportTxBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private LetterImportBatchFacade batchFacade;
    @EJB
    private LetterImportItemFacade itemFacade;

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markProcessing(Long batchId, int pageCount, int letterCount) {
        LetterImportBatch batch = batchFacade.find(batchId);
        if (batch == null) {
            return;
        }
        batch.setStatus(LetterImportStatus.PROCESSING);
        batch.setPageCount(pageCount);
        batch.setLetterCount(letterCount);
        batch.setProcessedCount(0);
        batch.setTotalInputTokens(0L);
        batch.setTotalOutputTokens(0L);
        batchFacade.edit(batch);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveItem(LetterImportItem item) {
        item.setCreatedAt(new Date());
        itemFacade.create(item);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recordProgress(Long batchId, long addInputTokens, long addOutputTokens) {
        LetterImportBatch batch = batchFacade.find(batchId);
        if (batch == null) {
            return;
        }
        int processed = batch.getProcessedCount() != null ? batch.getProcessedCount() : 0;
        long in = batch.getTotalInputTokens() != null ? batch.getTotalInputTokens() : 0L;
        long out = batch.getTotalOutputTokens() != null ? batch.getTotalOutputTokens() : 0L;
        batch.setProcessedCount(processed + 1);
        batch.setTotalInputTokens(in + addInputTokens);
        batch.setTotalOutputTokens(out + addOutputTokens);
        batchFacade.edit(batch);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markReady(Long batchId) {
        LetterImportBatch batch = batchFacade.find(batchId);
        if (batch == null) {
            return;
        }
        batch.setStatus(LetterImportStatus.READY);
        batchFacade.edit(batch);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void markFailed(Long batchId, String error) {
        LetterImportBatch batch = batchFacade.find(batchId);
        if (batch == null) {
            return;
        }
        batch.setStatus(LetterImportStatus.FAILED);
        batch.setErrorMessage(error);
        batchFacade.edit(batch);
    }
}
