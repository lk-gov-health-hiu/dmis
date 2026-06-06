/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import lk.gov.health.phsp.entity.LetterImportBatch;
import lk.gov.health.phsp.entity.LetterImportItem;
import lk.gov.health.phsp.facade.LetterImportBatchFacade;
import lk.gov.health.phsp.facade.LetterImportItemFacade;

/**
 * Periodically purges old letter-import batches (and their items) so the
 * temporarily-held PDF blobs and rendered previews do not accumulate. Runs a
 * few times a day; retention is governed by {@link LetterImportConfig}.
 */
@Singleton
public class LetterImportCleanupBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(LetterImportCleanupBean.class.getName());
    private static final long serialVersionUID = 1L;

    @EJB
    private LetterImportBatchFacade batchFacade;
    @EJB
    private LetterImportItemFacade itemFacade;
    @EJB
    private LetterImportConfig config;

    /** Runs every 6 hours; not persistent so it never queues missed runs. */
    @Schedule(hour = "*/6", minute = "0", second = "0", persistent = false)
    public void purgeOldBatches() {
        try {
            int retentionDays = config.getRetentionDays();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, -Math.max(1, retentionDays));
            Date cutoff = cal.getTime();

            Map<String, Object> params = new HashMap<>();
            params.put("cutoff", cutoff);
            List<LetterImportBatch> old = batchFacade.findByJpql(
                    "SELECT b FROM LetterImportBatch b WHERE b.createdAt < :cutoff",
                    params, javax.persistence.TemporalType.TIMESTAMP);

            int batches = 0;
            int items = 0;
            for (LetterImportBatch batch : old) {
                Map<String, Object> ip = new HashMap<>();
                ip.put("b", batch);
                List<LetterImportItem> children = itemFacade.findByJpql(
                        "SELECT i FROM LetterImportItem i WHERE i.batch = :b", ip);
                for (LetterImportItem item : children) {
                    itemFacade.remove(item);
                    items++;
                }
                batchFacade.remove(batch);
                batches++;
            }
            if (batches > 0) {
                LOG.log(Level.INFO, "Letter-import cleanup removed {0} batches and {1} items older than {2} days",
                        new Object[]{batches, items, retentionDays});
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Letter-import cleanup failed", e);
        }
    }
}
