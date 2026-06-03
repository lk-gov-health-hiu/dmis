/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

/**
 * Request body for {@code POST /api/letters/{id}/forward}.
 *
 * <p>Exactly one of {@link #toInstitutionId} or {@link #toWebUserId} must be
 * provided — the recipient is recorded on the resulting {@code DocumentHistory}
 * entry as either an institution or a user.</p>
 */
public class LetterForwardDto {

    private Long toInstitutionId;
    private Long toWebUserId;
    private Long minuteItemId;
    private String comments;

    public Long getToInstitutionId() { return toInstitutionId; }
    public void setToInstitutionId(Long toInstitutionId) { this.toInstitutionId = toInstitutionId; }

    public Long getToWebUserId() { return toWebUserId; }
    public void setToWebUserId(Long toWebUserId) { this.toWebUserId = toWebUserId; }

    public Long getMinuteItemId() { return minuteItemId; }
    public void setMinuteItemId(Long minuteItemId) { this.minuteItemId = minuteItemId; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

}
