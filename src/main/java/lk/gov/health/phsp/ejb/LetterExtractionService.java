/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import lk.gov.health.phsp.pojcs.AnthropicResponse;
import lk.gov.health.phsp.pojcs.LetterExtractionResult;

/**
 * Turns a single-letter PDF segment into structured {@link LetterExtractionResult}
 * metadata by asking Claude to OCR it and resolve the sending institution /
 * signatory against DMIS via the {@link AnthropicApiService} grounding tools.
 */
@Stateless
public class LetterExtractionService implements Serializable {

    private static final Logger LOG = Logger.getLogger(LetterExtractionService.class.getName());
    private static final long serialVersionUID = 1L;

    private static final int MAX_TOKENS = 2048;

    private static final String SYSTEM_PROMPT =
            "You extract registry metadata from a SINGLE scanned letter supplied as a PDF. "
            + "Sri Lankan government letters may be in Sinhala, Tamil, or English. "
            + "OCR the letter, then resolve names to DMIS records:\n"
            + "- Call institution_search to find the id of the SENDING institution (the one that "
            + "issued the letter, usually in the letterhead).\n"
            + "- Call staff_search to find the id of the named signatory/sender if a person is named.\n"
            + "Only set a resolved id when a returned match is clearly the same entity; otherwise leave it null.\n\n"
            + "Respond with ONLY a single JSON object (no markdown, no commentary) with exactly these keys:\n"
            + "{\n"
            + "  \"subject\": string,                 // letter subject/title, or a short summary\n"
            + "  \"letterDate\": string|null,         // date on the letter as yyyy-MM-dd, else null\n"
            + "  \"senderInstitutionName\": string|null,\n"
            + "  \"resolvedInstitutionId\": number|null,\n"
            + "  \"senderName\": string|null,\n"
            + "  \"resolvedStaffId\": number|null,\n"
            + "  \"registrationNo\": string|null,     // sender's outgoing/reference number if printed\n"
            + "  \"referenceNo\": string|null,        // any 'your reference' number if printed\n"
            + "  \"confidence\": number               // 0..1, your confidence in this extraction\n"
            + "}";

    @EJB
    private AnthropicApiService anthropicApiService;

    /**
     * Extracts metadata from one letter segment. Never throws: on any failure a
     * result with {@code errorMessage} set is returned so the reviewer can fill
     * the fields manually.
     *
     * @param subPdfBytes a standalone PDF containing only this letter's pages
     * @param apiKey      the user's Claude API key
     * @param model       Claude model id (or null for the service default)
     */
    public LetterExtractionResult extractFromSegment(byte[] subPdfBytes, String apiKey, String model) {
        LetterExtractionResult result = new LetterExtractionResult();
        if (subPdfBytes == null || subPdfBytes.length == 0) {
            result.setErrorMessage("Empty PDF segment.");
            return result;
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            result.setErrorMessage("No Claude API key configured for this user.");
            return result;
        }
        try {
            String base64 = Base64.getEncoder().encodeToString(subPdfBytes);
            AnthropicResponse response = anthropicApiService.sendMessage(
                    apiKey, model, MAX_TOKENS, SYSTEM_PROMPT,
                    "Extract the metadata for this letter as specified.",
                    base64, "application/pdf");

            result.setInputTokens(response.getInputTokens());
            result.setOutputTokens(response.getOutputTokens());

            String content = response.getContent();
            result.setRawJson(content);
            parseInto(content, result);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Letter extraction failed", e);
            result.setErrorMessage("Extraction failed: " + e.getMessage());
        }
        return result;
    }

    private void parseInto(String content, LetterExtractionResult result) {
        String json = stripToJson(content);
        if (json == null) {
            result.setErrorMessage("Claude did not return JSON. Raw: "
                    + (content == null ? "null" : truncate(content)));
            return;
        }
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject obj = reader.readObject();
            result.setSubject(getString(obj, "subject"));
            result.setLetterDate(getString(obj, "letterDate"));
            result.setSenderInstitutionName(getString(obj, "senderInstitutionName"));
            result.setResolvedInstitutionId(getLong(obj, "resolvedInstitutionId"));
            result.setSenderName(getString(obj, "senderName"));
            result.setResolvedStaffId(getLong(obj, "resolvedStaffId"));
            result.setRegistrationNo(getString(obj, "registrationNo"));
            result.setReferenceNo(getString(obj, "referenceNo"));
            if (obj.containsKey("confidence") && !obj.isNull("confidence")) {
                try {
                    result.setConfidence(obj.getJsonNumber("confidence").doubleValue());
                } catch (Exception ignore) {
                    // leave confidence at 0
                }
            }
        } catch (Exception e) {
            result.setErrorMessage("Could not parse Claude JSON: " + e.getMessage());
        }
    }

    /**
     * Extracts the first {@code { ... }} JSON object from Claude's text,
     * tolerating ```json fences or stray prose around it.
     */
    private String stripToJson(String content) {
        if (content == null) {
            return null;
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            return null;
        }
        return content.substring(start, end + 1);
    }

    private String getString(JsonObject obj, String key) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            return null;
        }
        try {
            return obj.getString(key);
        } catch (Exception e) {
            // value present but not a string
            JsonValue v = obj.get(key);
            return v != null ? v.toString() : null;
        }
    }

    private Long getLong(JsonObject obj, String key) {
        if (!obj.containsKey(key) || obj.isNull(key)) {
            return null;
        }
        try {
            return obj.getJsonNumber(key).longValue();
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String s) {
        return s.length() > 300 ? s.substring(0, 300) + "..." : s;
    }
}
