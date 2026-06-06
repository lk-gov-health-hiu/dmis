/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

/**
 * Request body for {@code POST /api/letters/{id}/actions} and
 * {@code POST /api/letters/{id}/receive} / {@code .../complete} — anything that
 * just records a comment plus an optional {@code Item} reference.
 */
public class LetterActionDto {

    private String comments;
    private Long itemId;

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

}
