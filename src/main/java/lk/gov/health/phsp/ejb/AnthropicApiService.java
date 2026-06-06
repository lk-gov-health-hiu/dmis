/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 *
 * Adapted from the HMIS project's com.divudi.service.AnthropicApiService,
 * trimmed for DMIS and given DMIS-specific grounding tools.
 */
package lk.gov.health.phsp.ejb;

import java.io.Serializable;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.pojcs.AnthropicResponse;

/**
 * Thin client for the Anthropic (Claude) Messages API, with an agentic
 * tool-use loop exposing two DMIS grounding tools — {@code institution_search}
 * and {@code staff_search} — so Claude can resolve a free-text institution or
 * sender name to a real DMIS entity id while extracting letter metadata.
 *
 * <p>The key is always supplied per call (each user's own key); this service
 * holds no application-wide key.</p>
 *
 * <p><strong>Runtime note:</strong> this uses the JDK {@code java.net.http}
 * client (Java 11+), exactly as the HMIS reference does. The Maven build keeps
 * {@code source/target = 1.8}, so it must be compiled and run on a JDK/JRE 11
 * or newer. If the deployment JRE is genuinely Java 8, swap the HTTP calls to
 * the already-present {@code unirest-java} dependency.</p>
 */
@Stateless
public class AnthropicApiService implements Serializable {

    private static final Logger LOG = Logger.getLogger(AnthropicApiService.class.getName());
    private static final long serialVersionUID = 1L;

    private static final String MESSAGES_ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";
    private static final int MAX_TOOL_ITERATIONS = 8;
    private static final int DEFAULT_SEARCH_LIMIT = 10;

    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private WebUserFacade webUserFacade;

    /**
     * Sends a single user message (optionally with a base64 attachment) to
     * Claude and runs the agentic tool loop until Claude produces a final
     * answer. Returns the final text plus accumulated token usage.
     *
     * @param apiKey             the user's Anthropic API key
     * @param model              Claude model id (caller supplies a default)
     * @param maxTokens          max response tokens
     * @param systemPrompt       system prompt
     * @param userMessage        the user text
     * @param attachmentBase64   optional base64 attachment (PDF or image)
     * @param attachmentMimeType optional MIME type, e.g. application/pdf
     */
    public AnthropicResponse sendMessage(
            String apiKey,
            String model,
            int maxTokens,
            String systemPrompt,
            String userMessage,
            String attachmentBase64,
            String attachmentMimeType) {

        try {
            List<JsonObject> messages = new ArrayList<>();
            messages.add(buildUserMessage(userMessage, attachmentBase64, attachmentMimeType));

            JsonArray tools = buildToolsArray();
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            long totalInputTokens = 0L;
            long totalOutputTokens = 0L;

            final long loopDeadlineMs = System.currentTimeMillis() + (5 * 60 * 1000L);
            final long perRequestMaxMs = 120_000L;

            for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
                long remainingMs = loopDeadlineMs - System.currentTimeMillis();
                if (remainingMs <= 0) {
                    return new AnthropicResponse(
                            "Request timed out: the agentic loop exceeded the 5-minute deadline.",
                            totalInputTokens, totalOutputTokens);
                }
                long requestTimeoutMs = Math.min(perRequestMaxMs, remainingMs);

                JsonArrayBuilder messagesBuilder = Json.createArrayBuilder();
                for (JsonObject msg : messages) {
                    messagesBuilder.add(msg);
                }

                JsonObject requestBody = Json.createObjectBuilder()
                        .add("model", model != null ? model : "claude-sonnet-4-6")
                        .add("max_tokens", maxTokens > 0 ? maxTokens : 4096)
                        .add("system", systemPrompt != null ? systemPrompt : "")
                        .add("tools", tools)
                        .add("messages", messagesBuilder.build())
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(MESSAGES_ENDPOINT))
                        .timeout(Duration.ofMillis(requestTimeoutMs))
                        .header("Content-Type", "application/json")
                        .header("x-api-key", apiKey)
                        .header("anthropic-version", ANTHROPIC_VERSION)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    LOG.log(Level.WARNING, "Anthropic API error {0}: {1}",
                            new Object[]{response.statusCode(), response.body()});
                    return new AnthropicResponse(
                            "Error from AI service (HTTP " + response.statusCode() + "): " + response.body(),
                            totalInputTokens, totalOutputTokens);
                }

                JsonObject responseJson;
                try (JsonReader reader = Json.createReader(new StringReader(response.body()))) {
                    responseJson = reader.readObject();
                }

                JsonObject usage = responseJson.getJsonObject("usage");
                if (usage != null) {
                    totalInputTokens += usage.getInt("input_tokens", 0);
                    totalOutputTokens += usage.getInt("output_tokens", 0);
                }

                String stopReason = responseJson.getString("stop_reason", "end_turn");
                JsonArray contentArray = responseJson.getJsonArray("content");

                if ("end_turn".equals(stopReason) || !"tool_use".equals(stopReason)) {
                    return new AnthropicResponse(
                            extractTextFromContent(contentArray),
                            totalInputTokens, totalOutputTokens);
                }

                // stop_reason == tool_use: echo assistant turn, then answer each tool call.
                messages.add(Json.createObjectBuilder()
                        .add("role", "assistant")
                        .add("content", contentArray)
                        .build());

                JsonArrayBuilder toolResultsBuilder = Json.createArrayBuilder();
                for (int i = 0; i < contentArray.size(); i++) {
                    JsonObject block = contentArray.getJsonObject(i);
                    if ("tool_use".equals(block.getString("type", ""))) {
                        String toolId = block.getString("id", "");
                        String toolName = block.getString("name", "");
                        JsonObject toolInput = block.containsKey("input")
                                ? block.getJsonObject("input")
                                : Json.createObjectBuilder().build();
                        String result = executeToolCall(toolName, toolInput);
                        toolResultsBuilder.add(Json.createObjectBuilder()
                                .add("type", "tool_result")
                                .add("tool_use_id", toolId)
                                .add("content", result));
                    }
                }
                messages.add(Json.createObjectBuilder()
                        .add("role", "user")
                        .add("content", toolResultsBuilder.build())
                        .build());
            }

            return new AnthropicResponse(
                    "The AI reached the maximum number of tool-use steps.",
                    totalInputTokens, totalOutputTokens);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.SEVERE, "Anthropic API call interrupted", e);
            return new AnthropicResponse("Request was interrupted. Please try again.", 0L, 0L);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error calling Anthropic API", e);
            return new AnthropicResponse("Error communicating with AI service: " + e.getMessage(), 0L, 0L);
        }
    }

    // -------------------------------------------------------------------------
    // Message / content helpers
    // -------------------------------------------------------------------------

    private JsonObject buildUserMessage(String userMessage, String attachmentBase64, String attachmentMimeType) {
        if (attachmentBase64 != null && !attachmentBase64.isEmpty() && attachmentMimeType != null) {
            JsonArrayBuilder contentBuilder = Json.createArrayBuilder();
            JsonObject source = Json.createObjectBuilder()
                    .add("type", "base64")
                    .add("media_type", attachmentMimeType)
                    .add("data", attachmentBase64)
                    .build();
            if (attachmentMimeType.startsWith("image/")) {
                contentBuilder.add(Json.createObjectBuilder().add("type", "image").add("source", source));
            } else {
                contentBuilder.add(Json.createObjectBuilder().add("type", "document").add("source", source));
            }
            if (userMessage != null && !userMessage.trim().isEmpty()) {
                contentBuilder.add(Json.createObjectBuilder().add("type", "text").add("text", userMessage));
            }
            return Json.createObjectBuilder().add("role", "user").add("content", contentBuilder.build()).build();
        }
        return Json.createObjectBuilder()
                .add("role", "user")
                .add("content", userMessage != null ? userMessage : "")
                .build();
    }

    private String extractTextFromContent(JsonArray contentArray) {
        if (contentArray == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contentArray.size(); i++) {
            JsonObject block = contentArray.getJsonObject(i);
            if ("text".equals(block.getString("type", ""))) {
                sb.append(block.getString("text", ""));
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Tools
    // -------------------------------------------------------------------------

    private JsonArray buildToolsArray() {
        JsonObject institutionSearch = Json.createObjectBuilder()
                .add("name", "institution_search")
                .add("description",
                        "Search DMIS institutions by name (case-insensitive contains). "
                        + "Use this to resolve the sending institution printed on a letter to a "
                        + "real DMIS institution id. Returns id, name, and type for each match.")
                .add("input_schema", Json.createObjectBuilder()
                        .add("type", "object")
                        .add("properties", Json.createObjectBuilder()
                                .add("query", Json.createObjectBuilder()
                                        .add("type", "string")
                                        .add("description", "Institution name or fragment to search for.")))
                        .add("required", Json.createArrayBuilder().add("query")))
                .build();

        JsonObject staffSearch = Json.createObjectBuilder()
                .add("name", "staff_search")
                .add("description",
                        "Search DMIS staff/users by name (case-insensitive contains). "
                        + "Use this to resolve the named sender or signatory of a letter to a real "
                        + "DMIS user id. Returns id and name for each match.")
                .add("input_schema", Json.createObjectBuilder()
                        .add("type", "object")
                        .add("properties", Json.createObjectBuilder()
                                .add("query", Json.createObjectBuilder()
                                        .add("type", "string")
                                        .add("description", "Person/user name or fragment to search for.")))
                        .add("required", Json.createArrayBuilder().add("query")))
                .build();

        return Json.createArrayBuilder().add(institutionSearch).add(staffSearch).build();
    }

    private String executeToolCall(String toolName, JsonObject toolInput) {
        try {
            String query = toolInput.containsKey("query") ? toolInput.getString("query", "") : "";
            switch (toolName) {
                case "institution_search":
                    return searchInstitutions(query);
                case "staff_search":
                    return searchStaff(query);
                default:
                    return "{\"error\":\"Unknown tool: " + toolName + "\"}";
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Tool " + toolName + " failed", e);
            return "{\"error\":\"" + escape(e.getMessage()) + "\"}";
        }
    }

    private String searchInstitutions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "{\"results\":[]}";
        }
        String jpql = "SELECT i FROM Institution i WHERE i.retired = false "
                + "AND lower(i.name) LIKE :q ORDER BY i.name";
        Map<String, Object> params = new HashMap<>();
        params.put("q", "%" + query.trim().toLowerCase() + "%");
        List<Institution> matches = institutionFacade.findByJpql(jpql, params, DEFAULT_SEARCH_LIMIT);

        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (Institution i : matches) {
            JsonObject o = Json.createObjectBuilder()
                    .add("id", i.getId())
                    .add("name", i.getName() != null ? i.getName() : "")
                    .add("type", i.getInstitutionType() != null ? i.getInstitutionType().toString() : "")
                    .build();
            arr.add(o);
        }
        return Json.createObjectBuilder().add("results", arr).build().toString();
    }

    private String searchStaff(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "{\"results\":[]}";
        }
        String jpql = "SELECT u FROM WebUser u WHERE u.retired = false "
                + "AND (lower(u.name) LIKE :q OR lower(u.person.name) LIKE :q) ORDER BY u.name";
        Map<String, Object> params = new HashMap<>();
        params.put("q", "%" + query.trim().toLowerCase() + "%");
        List<WebUser> matches = webUserFacade.findByJpql(jpql, params, DEFAULT_SEARCH_LIMIT);

        JsonArrayBuilder arr = Json.createArrayBuilder();
        for (WebUser u : matches) {
            String name = u.getPerson() != null && u.getPerson().getName() != null
                    ? u.getPerson().getName() : u.getName();
            JsonObject o = Json.createObjectBuilder()
                    .add("id", u.getId())
                    .add("name", name != null ? name : "")
                    .build();
            arr.add(o);
        }
        return Json.createObjectBuilder().add("results", arr).build().toString();
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
