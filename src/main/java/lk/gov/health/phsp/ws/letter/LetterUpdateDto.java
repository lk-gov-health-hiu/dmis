/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

/**
 * Request body for PUT /api/letters/{id}. Same shape as {@link LetterCreateDto};
 * only non-null fields are applied (partial update). Sending an explicit
 * {@code null} keeps the existing value unchanged.
 */
public class LetterUpdateDto extends LetterCreateDto {
}
