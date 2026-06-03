/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

/**
 * Request body for {@code POST /api/letters/{id}/attachments}.
 *
 * <p>The binary is sent as a Base64-encoded string in {@link #base64} so the
 * endpoint can stay {@code application/json} and avoid a new multipart
 * dependency. Typical use:</p>
 *
 * <pre>{@code
 * { "fileName": "scan.pdf",
 *   "fileType": "application/pdf",
 *   "base64":   "<base64 of bytes>" }
 * }</pre>
 */
public class LetterAttachmentDto {

    private String fileName;
    private String fileType;
    private String uploadType;
    private String comments;
    private String base64;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getUploadType() { return uploadType; }
    public void setUploadType(String uploadType) { this.uploadType = uploadType; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getBase64() { return base64; }
    public void setBase64(String base64) { this.base64 = base64; }

}
