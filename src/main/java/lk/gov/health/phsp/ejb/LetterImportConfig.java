/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.io.Serializable;
import javax.ejb.Stateless;

/**
 * Tunable settings for the letter-import feature.
 *
 * <p>DMIS has no application ConfigOption store, so each value is resolved from
 * a JVM system property, then an environment variable, then a built-in default.
 * This keeps operators in control without a new configuration UI.</p>
 *
 * <table>
 *   <tr><th>Setting</th><th>System property</th><th>Env var</th><th>Default</th></tr>
 *   <tr><td>Default Claude model</td><td>dmis.letterImport.model</td><td>DMIS_LETTER_IMPORT_MODEL</td><td>claude-sonnet-4-6</td></tr>
 *   <tr><td>Blank-page threshold</td><td>dmis.letterImport.blankThreshold</td><td>DMIS_LETTER_IMPORT_BLANK_THRESHOLD</td><td>0.005</td></tr>
 *   <tr><td>Render DPI</td><td>dmis.letterImport.renderDpi</td><td>DMIS_LETTER_IMPORT_RENDER_DPI</td><td>150</td></tr>
 *   <tr><td>Max pages per PDF</td><td>dmis.letterImport.maxPages</td><td>DMIS_LETTER_IMPORT_MAX_PAGES</td><td>500</td></tr>
 *   <tr><td>Batch retention days</td><td>dmis.letterImport.retentionDays</td><td>DMIS_LETTER_IMPORT_RETENTION_DAYS</td><td>7</td></tr>
 * </table>
 */
@Stateless
public class LetterImportConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getDefaultModel() {
        return resolve("dmis.letterImport.model", "DMIS_LETTER_IMPORT_MODEL", "claude-sonnet-4-6");
    }

    public double getBlankThreshold() {
        return resolveDouble("dmis.letterImport.blankThreshold",
                "DMIS_LETTER_IMPORT_BLANK_THRESHOLD", PdfSplitService.DEFAULT_BLANK_THRESHOLD);
    }

    public int getRenderDpi() {
        return resolveInt("dmis.letterImport.renderDpi",
                "DMIS_LETTER_IMPORT_RENDER_DPI", PdfSplitService.DEFAULT_RENDER_DPI);
    }

    public int getMaxPages() {
        return resolveInt("dmis.letterImport.maxPages", "DMIS_LETTER_IMPORT_MAX_PAGES", 500);
    }

    public int getRetentionDays() {
        return resolveInt("dmis.letterImport.retentionDays", "DMIS_LETTER_IMPORT_RETENTION_DAYS", 7);
    }

    // -------------------------------------------------------------------------

    private String resolve(String systemProperty, String envVar, String fallback) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envVar);
        }
        return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
    }

    private int resolveInt(String systemProperty, String envVar, int fallback) {
        try {
            return Integer.parseInt(resolve(systemProperty, envVar, Integer.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private double resolveDouble(String systemProperty, String envVar, double fallback) {
        try {
            return Double.parseDouble(resolve(systemProperty, envVar, Double.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
