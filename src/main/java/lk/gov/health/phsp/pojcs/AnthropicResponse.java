/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.pojcs;

import java.io.Serializable;

/**
 * Result of a call to the Anthropic Messages API: the final assistant text plus
 * the accumulated token usage across any agentic tool-use iterations.
 */
public class AnthropicResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String content;
    private final long inputTokens;
    private final long outputTokens;

    public AnthropicResponse(String content, long inputTokens, long outputTokens) {
        this.content = content;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
    }

    public String getContent() {
        return content;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }
}
