/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

import java.util.List;

/**
 * Request body for assign endpoints.
 *
 * <p>For {@code POST /api/letters/{id}/assign}, {@link #toWebUserId} is required
 * and {@link #letterIds} is ignored. For the bulk variant
 * {@code POST /api/letters/assign}, both {@link #toWebUserId} and
 * {@link #letterIds} are required.</p>
 */
public class LetterAssignDto {

    private Long toWebUserId;
    private Long minuteItemId;
    private String comments;
    private List<Long> letterIds;

    public Long getToWebUserId() { return toWebUserId; }
    public void setToWebUserId(Long toWebUserId) { this.toWebUserId = toWebUserId; }

    public Long getMinuteItemId() { return minuteItemId; }
    public void setMinuteItemId(Long minuteItemId) { this.minuteItemId = minuteItemId; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public List<Long> getLetterIds() { return letterIds; }
    public void setLetterIds(List<Long> letterIds) { this.letterIds = letterIds; }

}
