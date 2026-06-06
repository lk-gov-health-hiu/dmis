/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.letter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.gov.health.phsp.bean.ApiKeyController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.entity.DocumentHistory;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.entity.Item;
import lk.gov.health.phsp.entity.Upload;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.DocumentGenerationType;
import lk.gov.health.phsp.enums.DocumentType;
import lk.gov.health.phsp.enums.HistoryType;
import lk.gov.health.phsp.enums.UploadType;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.facade.DocumentHistoryFacade;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.ItemFacade;
import lk.gov.health.phsp.facade.UploadFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.ws.common.ApiResponseDto;

/**
 * REST API for the letter lifecycle: create, read, update, delete, assign,
 * forward, receive, record actions, attach images/PDFs, and search.
 *
 * <p>All endpoints require a valid {@code Api-Key} header. The actor — the
 * {@link WebUser} treated as the caller for audit fields, ownership transfers
 * and {@code DocumentHistory} entries — is resolved in this order:</p>
 * <ol>
 *   <li>The {@code X-Acting-User-Id} header, if present and resolves to a
 *       non-retired {@link WebUser}.</li>
 *   <li>{@link ApiKey#getCreatedBy()} on the key — useful when the key is
 *       provisioned per user.</li>
 * </ol>
 * <p>State-changing endpoints (POST/PUT/DELETE) require an actor and return
 * {@code 400} if neither source resolves.</p>
 */
@Path("letters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LetterResource {

    private static final String ACTING_USER_HEADER = "X-Acting-User-Id";
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    @Inject
    private ApiKeyController apiKeyController;

    @EJB
    private DocumentFacade documentFacade;
    @EJB
    private DocumentHistoryFacade documentHistoryFacade;
    @EJB
    private UploadFacade uploadFacade;
    @EJB
    private WebUserFacade webUserFacade;
    @EJB
    private InstitutionFacade institutionFacade;
    @EJB
    private ItemFacade itemFacade;

    // ---------------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------------

    /**
     * {@code POST /api/letters} — create a new letter. The {@code documentType}
     * is forced to {@link DocumentType#Letter} regardless of what the caller
     * sends. Writes a {@link HistoryType#Letter_Created} history entry.
     */
    @POST
    public Response createLetter(@HeaderParam("Api-Key") String apiKey,
                                 @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                 LetterCreateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user. Set " + ACTING_USER_HEADER
                    + " header or ensure the Api-Key has a createdBy user.");
        }
        if (dto == null || isBlank(dto.getDocumentName())) {
            return badRequest("documentName is required");
        }

        Document d = new Document();
        d.setDocumentType(DocumentType.Letter);
        d.setRetired(false);
        d.setCreatedAt(new Date());
        d.setCreatedBy(actor);
        d.setCreatedInstitution(actor.getInstitution());
        d.setCurrentInstitution(actor.getInstitution());
        d.setCurrentOwner(actor);

        Response err = applyDtoToDocument(dto, d, true);
        if (err != null) {
            return err;
        }

        documentFacade.create(d);

        writeHistory(d, actor, HistoryType.Letter_Created, "Letter created via API");

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponseDto.success(toMap(d)))
                .build();
    }

    /**
     * {@code GET /api/letters/{id}}.
     */
    @GET
    @Path("{id}")
    public Response getLetter(@HeaderParam("Api-Key") String apiKey,
                              @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        return Response.ok(ApiResponseDto.success(toMap(d))).build();
    }

    /**
     * {@code PUT /api/letters/{id}} — partial update. Only fields present in
     * the body are touched; {@code null} fields are ignored.
     */
    @PUT
    @Path("{id}")
    public Response updateLetter(@HeaderParam("Api-Key") String apiKey,
                                 @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                 @PathParam("id") Long id,
                                 LetterUpdateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        if (dto == null) {
            return badRequest("Request body is required");
        }

        Response err = applyDtoToDocument(dto, d, false);
        if (err != null) {
            return err;
        }

        d.setLastEditBy(actor);
        d.setLastEditeAt(new Date());
        documentFacade.edit(d);

        return Response.ok(ApiResponseDto.success(toMap(d))).build();
    }

    /**
     * {@code DELETE /api/letters/{id}} — soft-retire.
     */
    @DELETE
    @Path("{id}")
    public Response deleteLetter(@HeaderParam("Api-Key") String apiKey,
                                 @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                 @PathParam("id") Long id,
                                 @QueryParam("comments") String comments) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        d.setRetired(true);
        d.setRetiredAt(new Date());
        d.setRetiredBy(actor);
        d.setRetireComments(comments);
        documentFacade.edit(d);
        return Response.ok(ApiResponseDto.success(toMap(d))).build();
    }

    /**
     * {@code POST /api/letters/{id}/complete} — mark the letter as completed.
     */
    @POST
    @Path("{id}/complete")
    public Response completeLetter(@HeaderParam("Api-Key") String apiKey,
                                   @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                   @PathParam("id") Long id,
                                   LetterActionDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        d.setCompleted(true);
        d.setCompletedAt(new Date());
        d.setCompletedBy(actor);
        documentFacade.edit(d);

        String note = (dto != null && dto.getComments() != null) ? dto.getComments() : "Marked complete via API";
        writeHistory(d, actor, HistoryType.Letter_Status_Changed, note);

        return Response.ok(ApiResponseDto.success(toMap(d))).build();
    }

    // ---------------------------------------------------------------------
    // Workflow: assign / forward / receive / action
    // ---------------------------------------------------------------------

    /**
     * {@code POST /api/letters/{id}/assign} — assign ownership to another
     * user. Mirrors {@code LetterController.assignTo()}.
     */
    @POST
    @Path("{id}/assign")
    public Response assignLetter(@HeaderParam("Api-Key") String apiKey,
                                 @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                 @PathParam("id") Long id,
                                 LetterAssignDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        if (dto == null || dto.getToWebUserId() == null) {
            return badRequest("toWebUserId is required");
        }
        WebUser to = webUserFacade.find(dto.getToWebUserId());
        if (to == null || to.isRetired()) {
            return badRequest("Unknown toWebUserId: " + dto.getToWebUserId());
        }

        Item minute = lookupItem(dto.getMinuteItemId());

        DocumentHistory h = new DocumentHistory();
        h.setHistoryType(HistoryType.Letter_Assigned);
        h.setDocument(d);
        h.setFromUser(d.getCurrentOwner());
        h.setToUser(to);
        h.setItem(minute);
        h.setComments(dto.getComments());
        h.setInstitution(actor.getInstitution());
        saveHistory(h, actor);

        d.setCurrentOwner(to);
        d.setCompleted(false);
        d.setLastEditBy(actor);
        d.setLastEditeAt(new Date());
        documentFacade.edit(d);

        return Response.ok(ApiResponseDto.success(toMap(d))).build();
    }

    /**
     * {@code POST /api/letters/{id}/unassign} — retire the most recent active
     * assignment for the letter and restore {@code currentOwner} to whoever
     * held it before that assignment. Mirrors
     * {@code LetterController.removeAssignment()}.
     */
    @POST
    @Path("{id}/unassign")
    public Response unassignLetter(@HeaderParam("Api-Key") String apiKey,
                                   @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                   @PathParam("id") Long id,
                                   LetterActionDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }

        String jpql = "SELECT h FROM DocumentHistory h "
                + "WHERE h.retired = false "
                + "AND h.document = :doc "
                + "AND h.historyType = :ht "
                + "ORDER BY h.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("doc", d);
        params.put("ht", HistoryType.Letter_Assigned);
        DocumentHistory assignment = documentHistoryFacade.findFirstByJpql(jpql, params);
        if (assignment == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponseDto.error(409, "No active assignment to remove"))
                    .build();
        }

        String note = (dto != null && dto.getComments() != null)
                ? dto.getComments()
                : "Assignment removed via API";

        assignment.setRetired(true);
        assignment.setRetiredAt(new Date());
        assignment.setRetiredBy(actor);
        assignment.setRetireComments(note);
        documentHistoryFacade.edit(assignment);

        DocumentHistory hx = new DocumentHistory();
        hx.setHistoryType(HistoryType.Letter_Assignment_Removed);
        hx.setDocument(d);
        hx.setToUser(assignment.getToUser());
        hx.setFromUser(assignment.getFromUser());
        hx.setInstitution(actor.getInstitution());
        hx.setComments(note);
        saveHistory(hx, actor);

        d.setCurrentOwner(assignment.getFromUser());
        d.setLastEditBy(actor);
        d.setLastEditeAt(new Date());
        documentFacade.edit(d);

        Map<String, Object> result = new HashMap<>();
        result.put("letter", toMap(d));
        result.put("removedAssignmentId", assignment.getId());
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * {@code POST /api/letters/assign} — bulk assign. Mirrors
     * {@code LetterController.assignMultipleLetters()}.
     */
    @POST
    @Path("assign")
    public Response assignMultiple(@HeaderParam("Api-Key") String apiKey,
                                   @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                   LetterAssignDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        if (dto == null || dto.getToWebUserId() == null) {
            return badRequest("toWebUserId is required");
        }
        if (dto.getLetterIds() == null || dto.getLetterIds().isEmpty()) {
            return badRequest("letterIds is required and must be non-empty");
        }
        WebUser to = webUserFacade.find(dto.getToWebUserId());
        if (to == null || to.isRetired()) {
            return badRequest("Unknown toWebUserId: " + dto.getToWebUserId());
        }
        Item minute = lookupItem(dto.getMinuteItemId());

        List<Map<String, Object>> assigned = new ArrayList<>();
        List<Map<String, Object>> skipped = new ArrayList<>();
        for (Long lid : dto.getLetterIds()) {
            Document d = documentFacade.find(lid);
            if (d == null || d.isRetired() || !isLetter(d)) {
                Map<String, Object> s = new HashMap<>();
                s.put("id", lid);
                s.put("reason", "not found or not a letter");
                skipped.add(s);
                continue;
            }
            DocumentHistory h = new DocumentHistory();
            h.setHistoryType(HistoryType.Letter_Assigned);
            h.setDocument(d);
            h.setFromUser(d.getCurrentOwner());
            h.setToUser(to);
            h.setItem(minute);
            h.setComments(dto.getComments());
            h.setInstitution(actor.getInstitution());
            saveHistory(h, actor);

            d.setCurrentOwner(to);
            d.setCompleted(false);
            d.setLastEditBy(actor);
            d.setLastEditeAt(new Date());
            documentFacade.edit(d);
            assigned.add(toMap(d));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("assigned", assigned);
        result.put("skipped", skipped);
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * {@code POST /api/letters/{id}/forward} — forward or copy-forward to
     * another user or institution. Mirrors
     * {@code LetterController.forwardOrCopyTo()}.
     */
    @POST
    @Path("{id}/forward")
    public Response forwardLetter(@HeaderParam("Api-Key") String apiKey,
                                  @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                  @PathParam("id") Long id,
                                  LetterForwardDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        if (dto == null) {
            return badRequest("Request body is required");
        }
        boolean hasIns = dto.getToInstitutionId() != null;
        boolean hasUser = dto.getToWebUserId() != null;
        if (hasIns == hasUser) {
            return badRequest("Provide exactly one of toInstitutionId or toWebUserId");
        }

        DocumentHistory h = new DocumentHistory();
        h.setHistoryType(HistoryType.Letter_Copy_or_Forward);
        h.setDocument(d);
        h.setFromUser(d.getCurrentOwner());
        h.setFromInstitution(actor.getInstitution());
        h.setInstitution(actor.getInstitution());
        h.setItem(lookupItem(dto.getMinuteItemId()));
        h.setComments(dto.getComments());

        if (hasUser) {
            WebUser to = webUserFacade.find(dto.getToWebUserId());
            if (to == null || to.isRetired()) {
                return badRequest("Unknown toWebUserId: " + dto.getToWebUserId());
            }
            h.setToUser(to);
            h.setToInstitution(to.getInstitution());
        } else {
            Institution toIns = institutionFacade.find(dto.getToInstitutionId());
            if (toIns == null || toIns.isRetired()) {
                return badRequest("Unknown toInstitutionId: " + dto.getToInstitutionId());
            }
            h.setToInstitution(toIns);
        }

        saveHistory(h, actor);

        d.setCompleted(false);
        d.setLastEditBy(actor);
        d.setLastEditeAt(new Date());
        documentFacade.edit(d);

        return Response.ok(ApiResponseDto.success(toMap(d))).build();
    }

    /**
     * {@code POST /api/letters/{id}/receive} — accept the most recent
     * outstanding assignment or copy/forward that names the acting user as the
     * recipient. Mirrors {@code LetterController.toAcceptAssignedLetter()} and
     * {@code receiveLetterCopiedOrForwardedToMe()}.
     */
    @POST
    @Path("{id}/receive")
    public Response receiveLetter(@HeaderParam("Api-Key") String apiKey,
                                  @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                  @PathParam("id") Long id,
                                  LetterActionDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }

        String jpql = "SELECT h FROM DocumentHistory h "
                + "WHERE h.retired = false "
                + "AND h.document = :doc "
                + "AND h.toUser = :tu "
                + "AND h.completed = false "
                + "AND (h.historyType = :ha OR h.historyType = :hc) "
                + "ORDER BY h.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("doc", d);
        params.put("tu", actor);
        params.put("ha", HistoryType.Letter_Assigned);
        params.put("hc", HistoryType.Letter_Copy_or_Forward);
        DocumentHistory dh = documentHistoryFacade.findFirstByJpql(jpql, params);
        if (dh == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponseDto.error(409,
                            "No outstanding assignment or copy/forward addressed to the acting user"))
                    .build();
        }

        dh.setCompleted(true);
        dh.setCompletedAt(new Date());
        dh.setCompletedBy(actor);
        if (dto != null && dto.getComments() != null) {
            dh.setComments(dto.getComments());
        }
        documentHistoryFacade.edit(dh);

        if (dh.getHistoryType() == HistoryType.Letter_Assigned) {
            d.setCompleted(true);
            d.setCompletedAt(new Date());
            d.setCompletedBy(actor);
            documentFacade.edit(d);
        }

        writeHistory(d, actor, HistoryType.Letter_Received,
                dto != null ? dto.getComments() : null);

        Map<String, Object> result = new HashMap<>();
        result.put("letter", toMap(d));
        result.put("acceptedHistoryId", dh.getId());
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * {@code POST /api/letters/{id}/actions} — record an action taken on the
     * letter. Mirrors {@code LetterController.recordActionTaken()}.
     */
    @POST
    @Path("{id}/actions")
    public Response recordAction(@HeaderParam("Api-Key") String apiKey,
                                 @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                 @PathParam("id") Long id,
                                 LetterActionDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        if (dto == null || isBlank(dto.getComments())) {
            return badRequest("comments is required");
        }

        DocumentHistory h = new DocumentHistory();
        h.setHistoryType(HistoryType.Letter_Action_Taken);
        h.setDocument(d);
        h.setFromUser(actor);
        h.setInstitution(actor.getInstitution());
        h.setItem(lookupItem(dto.getItemId()));
        h.setComments(dto.getComments());
        saveHistory(h, actor);

        Map<String, Object> result = new HashMap<>();
        result.put("letter", toMap(d));
        result.put("actionId", h.getId());
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    // ---------------------------------------------------------------------
    // Attachments
    // ---------------------------------------------------------------------

    /**
     * {@code POST /api/letters/{id}/attachments} — attach an image or PDF.
     * The binary is decoded from {@link LetterAttachmentDto#getBase64()}.
     */
    @POST
    @Path("{id}/attachments")
    public Response addAttachment(@HeaderParam("Api-Key") String apiKey,
                                  @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                  @PathParam("id") Long id,
                                  LetterAttachmentDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        if (dto == null || isBlank(dto.getBase64())) {
            return badRequest("base64 is required");
        }
        if (isBlank(dto.getFileName())) {
            return badRequest("fileName is required");
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(dto.getBase64().trim());
        } catch (IllegalArgumentException e) {
            return badRequest("Invalid base64 payload: " + e.getMessage());
        }

        Upload u = new Upload();
        u.setDocument(d);
        u.setBaImage(bytes);
        u.setInstitution(actor.getInstitution());
        u.setFileName(dto.getFileName().trim());
        u.setFileType(dto.getFileType());
        u.setComments(dto.getComments());
        if (!isBlank(dto.getUploadType())) {
            try {
                u.setUploadType(UploadType.valueOf(dto.getUploadType().trim()));
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown uploadType: " + dto.getUploadType());
            }
        }
        u.setCreatedAt(new Date());
        u.setCreater(actor);
        u.setRetired(false);
        uploadFacade.create(u);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponseDto.success(toMapUpload(u, false)))
                .build();
    }

    /**
     * {@code GET /api/letters/{id}/attachments} — list attachment metadata.
     */
    @GET
    @Path("{id}/attachments")
    public Response listAttachments(@HeaderParam("Api-Key") String apiKey,
                                    @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Document d = documentFacade.find(id);
        if (d == null || d.isRetired() || !isLetter(d)) {
            return notFound("Letter not found");
        }
        String jpql = "SELECT u FROM Upload u WHERE u.retired = false AND u.document = :d ORDER BY u.id";
        Map<String, Object> params = new HashMap<>();
        params.put("d", d);
        List<Upload> uploads = uploadFacade.findByJpql(jpql, params);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Upload u : uploads) {
            out.add(toMapUpload(u, false));
        }
        return Response.ok(ApiResponseDto.success(out)).build();
    }

    /**
     * {@code GET /api/letters/{id}/attachments/{uploadId}} — return the
     * attachment binary streamed with its original {@code fileType} as the
     * {@code Content-Type}. Falls back to {@code application/octet-stream}.
     */
    @GET
    @Path("{id}/attachments/{uploadId}")
    @Produces(MediaType.WILDCARD)
    public Response getAttachment(@HeaderParam("Api-Key") String apiKey,
                                  @PathParam("id") Long id,
                                  @PathParam("uploadId") Long uploadId) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Upload u = uploadFacade.find(uploadId);
        if (u == null || u.isRetired() || u.getDocument() == null
                || !id.equals(u.getDocument().getId())) {
            return notFound("Attachment not found");
        }
        String type = u.getFileType() != null ? u.getFileType() : MediaType.APPLICATION_OCTET_STREAM;
        Response.ResponseBuilder rb = Response.ok(u.getBaImage(), type);
        if (u.getFileName() != null) {
            rb.header("Content-Disposition", "inline; filename=\"" + u.getFileName() + "\"");
        }
        return rb.build();
    }

    /**
     * {@code DELETE /api/letters/{id}/attachments/{uploadId}} — soft-retire.
     */
    @DELETE
    @Path("{id}/attachments/{uploadId}")
    public Response deleteAttachment(@HeaderParam("Api-Key") String apiKey,
                                     @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                     @PathParam("id") Long id,
                                     @PathParam("uploadId") Long uploadId) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        Upload u = uploadFacade.find(uploadId);
        if (u == null || u.isRetired() || u.getDocument() == null
                || !id.equals(u.getDocument().getId())) {
            return notFound("Attachment not found");
        }
        u.setRetired(true);
        u.setRetiredAt(new Date());
        u.setRetirer(actor);
        uploadFacade.edit(u);
        return Response.ok(ApiResponseDto.success(toMapUpload(u, false))).build();
    }

    // ---------------------------------------------------------------------
    // Search + inbox
    // ---------------------------------------------------------------------

    /**
     * {@code GET /api/letters} — search. All query parameters are optional;
     * with no parameters returns the most recent letters (up to {@code size}).
     */
    @GET
    public Response search(@HeaderParam("Api-Key") String apiKey,
                           @QueryParam("q") String q,
                           @QueryParam("documentNumber") String documentNumber,
                           @QueryParam("documentCode") String documentCode,
                           @QueryParam("registrationNo") String registrationNo,
                           @QueryParam("fromInstitution") Long fromInstitutionId,
                           @QueryParam("toInstitution") Long toInstitutionId,
                           @QueryParam("currentInstitution") Long currentInstitutionId,
                           @QueryParam("currentOwner") Long currentOwnerId,
                           @QueryParam("toWebUser") Long toWebUserId,
                           @QueryParam("letterStatus") Long letterStatusId,
                           @QueryParam("documentGenerationType") String generationType,
                           @QueryParam("fromDate") String fromDate,
                           @QueryParam("toDate") String toDate,
                           @QueryParam("dateField") @DefaultValue("documentDate") String dateField,
                           @QueryParam("completed") Boolean completed,
                           @QueryParam("retired") @DefaultValue("false") boolean includeRetired,
                           @QueryParam("size") Integer size,
                           @QueryParam("page") @DefaultValue("0") int page) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }

        if (!"documentDate".equals(dateField) && !"receivedDate".equals(dateField)
                && !"createdAt".equals(dateField)) {
            return badRequest("dateField must be one of: documentDate, receivedDate, createdAt");
        }

        int pageSize = (size != null && size > 0 && size <= MAX_PAGE_SIZE) ? size : DEFAULT_PAGE_SIZE;
        int firstResult = Math.max(page, 0) * pageSize;

        StringBuilder jpql = new StringBuilder("SELECT d FROM Document d WHERE d.documentType = :dt");
        Map<String, Object> params = new HashMap<>();
        params.put("dt", DocumentType.Letter);
        if (!includeRetired) {
            jpql.append(" AND d.retired = false");
        }
        if (!isBlank(q)) {
            jpql.append(" AND (lower(d.documentName) LIKE :q OR lower(d.documentNumber) LIKE :q "
                    + "OR lower(d.documentCode) LIKE :q OR lower(d.registrationNo) LIKE :q "
                    + "OR lower(d.senderName) LIKE :q)");
            params.put("q", "%" + q.trim().toLowerCase() + "%");
        }
        if (!isBlank(documentNumber)) {
            jpql.append(" AND d.documentNumber = :dn");
            params.put("dn", documentNumber.trim());
        }
        if (!isBlank(documentCode)) {
            jpql.append(" AND d.documentCode = :dc");
            params.put("dc", documentCode.trim());
        }
        if (!isBlank(registrationNo)) {
            jpql.append(" AND d.registrationNo = :rn");
            params.put("rn", registrationNo.trim());
        }
        if (fromInstitutionId != null) {
            jpql.append(" AND d.fromInstitution.id = :fi");
            params.put("fi", fromInstitutionId);
        }
        if (toInstitutionId != null) {
            jpql.append(" AND d.toInstitution.id = :ti");
            params.put("ti", toInstitutionId);
        }
        if (currentInstitutionId != null) {
            jpql.append(" AND d.currentInstitution.id = :ci");
            params.put("ci", currentInstitutionId);
        }
        if (currentOwnerId != null) {
            jpql.append(" AND d.currentOwner.id = :co");
            params.put("co", currentOwnerId);
        }
        if (toWebUserId != null) {
            jpql.append(" AND d.toWebUser.id = :tu");
            params.put("tu", toWebUserId);
        }
        if (letterStatusId != null) {
            jpql.append(" AND d.letterStatus.id = :ls");
            params.put("ls", letterStatusId);
        }
        if (!isBlank(generationType)) {
            DocumentGenerationType gt;
            try {
                gt = DocumentGenerationType.valueOf(generationType.trim());
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown documentGenerationType: " + generationType);
            }
            jpql.append(" AND d.documentGenerationType = :gt");
            params.put("gt", gt);
        }
        if (completed != null) {
            jpql.append(" AND d.completed = :cmp");
            params.put("cmp", completed);
        }
        Date fd = parseDate(fromDate);
        Date td = parseDate(toDate);
        if (fromDate != null && fd == null) {
            return badRequest("fromDate must be ISO-8601 (yyyy-MM-dd)");
        }
        if (toDate != null && td == null) {
            return badRequest("toDate must be ISO-8601 (yyyy-MM-dd)");
        }
        if (fd != null) {
            jpql.append(" AND d.").append(dateField).append(" >= :fd");
            params.put("fd", fd);
        }
        if (td != null) {
            jpql.append(" AND d.").append(dateField).append(" <= :td");
            params.put("td", endOfDay(td));
        }
        jpql.append(" ORDER BY d.").append(dateField).append(" DESC, d.id DESC");

        List<Document> results = documentFacade.findByJpql(jpql.toString(), params, firstResult + pageSize);
        List<Document> windowed = results.size() > firstResult
                ? results.subList(firstResult, Math.min(firstResult + pageSize, results.size()))
                : new ArrayList<Document>();

        List<Map<String, Object>> data = new ArrayList<>();
        for (Document d : windowed) {
            data.add(toMap(d));
        }
        Map<String, Object> envelope = new HashMap<>();
        envelope.put("data", data);
        envelope.put("page", page);
        envelope.put("size", pageSize);
        envelope.put("returned", data.size());
        return Response.ok(ApiResponseDto.success(envelope)).build();
    }

    /**
     * {@code GET /api/letters/inbox/assigned-to-me} — outstanding letters
     * assigned to the acting user (not yet accepted).
     */
    @GET
    @Path("inbox/assigned-to-me")
    public Response inboxAssignedToMe(@HeaderParam("Api-Key") String apiKey,
                                      @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                      @QueryParam("size") Integer size) {
        return inboxByHistory(apiKey, actingUserId, HistoryType.Letter_Assigned, false, false, size);
    }

    /**
     * {@code GET /api/letters/inbox/forwarded-to-me} — outstanding letters
     * copy-forwarded to the acting user (not yet received).
     */
    @GET
    @Path("inbox/forwarded-to-me")
    public Response inboxForwardedToMe(@HeaderParam("Api-Key") String apiKey,
                                       @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                       @QueryParam("size") Integer size) {
        return inboxByHistory(apiKey, actingUserId, HistoryType.Letter_Copy_or_Forward, false, false, size);
    }

    /**
     * {@code GET /api/letters/inbox/to-receive} — letters where the acting
     * user is named as the recipient on an outstanding assignment OR
     * copy-forward.
     */
    @GET
    @Path("inbox/to-receive")
    public Response inboxToReceive(@HeaderParam("Api-Key") String apiKey,
                                   @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                   @QueryParam("size") Integer size) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        int pageSize = (size != null && size > 0 && size <= MAX_PAGE_SIZE) ? size : DEFAULT_PAGE_SIZE;
        String jpql = "SELECT h FROM DocumentHistory h "
                + "WHERE h.retired = false AND h.completed = false AND h.toUser = :tu "
                + "AND (h.historyType = :ha OR h.historyType = :hc) "
                + "ORDER BY h.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("tu", actor);
        params.put("ha", HistoryType.Letter_Assigned);
        params.put("hc", HistoryType.Letter_Copy_or_Forward);
        List<DocumentHistory> hs = documentHistoryFacade.findByJpql(jpql, params, pageSize);
        return Response.ok(ApiResponseDto.success(mapHistories(hs))).build();
    }

    /**
     * {@code GET /api/letters/inbox/received-today} — assignments and
     * copy-forwards accepted by the acting user since the start of today.
     */
    @GET
    @Path("inbox/received-today")
    public Response inboxReceivedToday(@HeaderParam("Api-Key") String apiKey,
                                       @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                       @QueryParam("size") Integer size) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        int pageSize = (size != null && size > 0 && size <= MAX_PAGE_SIZE) ? size : DEFAULT_PAGE_SIZE;
        Date todayStart = startOfDay(new Date());
        String jpql = "SELECT h FROM DocumentHistory h "
                + "WHERE h.retired = false AND h.completed = true "
                + "AND h.completedBy = :u AND h.completedAt >= :start "
                + "AND (h.historyType = :ha OR h.historyType = :hc) "
                + "ORDER BY h.completedAt DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("u", actor);
        params.put("start", todayStart);
        params.put("ha", HistoryType.Letter_Assigned);
        params.put("hc", HistoryType.Letter_Copy_or_Forward);
        List<DocumentHistory> hs = documentHistoryFacade.findByJpql(jpql, params, pageSize);
        return Response.ok(ApiResponseDto.success(mapHistories(hs))).build();
    }

    /**
     * {@code GET /api/letters/inbox/accepted} — all accepted assignments and
     * copy-forwards for the acting user.
     */
    @GET
    @Path("inbox/accepted")
    public Response inboxAccepted(@HeaderParam("Api-Key") String apiKey,
                                  @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                  @QueryParam("size") Integer size) {
        return inboxByHistory(apiKey, actingUserId, null, true, true, size);
    }

    /**
     * {@code GET /api/letters/inbox/awaiting-institution} — letters
     * copy-forwarded (or added by a mail branch) to an institution that have
     * not yet been received. Mirrors
     * {@code LetterController.fillLettersToReceive()}.
     *
     * <p>By default uses the acting user's institution; pass
     * {@code ?institutionId=<id>} to scope to a different one.</p>
     */
    @GET
    @Path("inbox/awaiting-institution")
    public Response inboxAwaitingInstitution(@HeaderParam("Api-Key") String apiKey,
                                             @HeaderParam(ACTING_USER_HEADER) Long actingUserId,
                                             @QueryParam("institutionId") Long institutionId,
                                             @QueryParam("size") Integer size) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution scope;
        if (institutionId != null) {
            scope = institutionFacade.find(institutionId);
            if (scope == null || scope.isRetired()) {
                return badRequest("Unknown institutionId: " + institutionId);
            }
        } else {
            WebUser actor = resolveActor(key, actingUserId);
            if (actor == null) {
                return badRequest("Cannot resolve acting user; pass institutionId or "
                        + ACTING_USER_HEADER + " header");
            }
            scope = actor.getInstitution();
            if (scope == null) {
                return badRequest("Acting user has no institution");
            }
        }

        int pageSize = (size != null && size > 0 && size <= MAX_PAGE_SIZE) ? size : DEFAULT_PAGE_SIZE;
        String jpql = "SELECT h FROM DocumentHistory h "
                + "WHERE h.retired = false "
                + "AND h.completed = false "
                + "AND (h.historyType = :hc OR h.historyType = :hm) "
                + "AND (h.toInstitution = :ins OR h.toUser.institution = :ins) "
                + "ORDER BY h.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("ins", scope);
        params.put("hc", HistoryType.Letter_Copy_or_Forward);
        params.put("hm", HistoryType.Letter_added_by_mail_branch);
        List<DocumentHistory> hs = documentHistoryFacade.findByJpql(jpql, params, pageSize);
        return Response.ok(ApiResponseDto.success(mapHistories(hs))).build();
    }

    private Response inboxByHistory(String apiKey, Long actingUserId, HistoryType type,
                                    boolean completed, boolean anyType, Integer size) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser actor = resolveActor(key, actingUserId);
        if (actor == null) {
            return badRequest("Cannot resolve acting user");
        }
        int pageSize = (size != null && size > 0 && size <= MAX_PAGE_SIZE) ? size : DEFAULT_PAGE_SIZE;
        StringBuilder jpql = new StringBuilder(
                "SELECT h FROM DocumentHistory h WHERE h.retired = false AND h.toUser = :tu");
        Map<String, Object> params = new HashMap<>();
        params.put("tu", actor);
        jpql.append(" AND h.completed = :c");
        params.put("c", completed);
        if (anyType) {
            jpql.append(" AND (h.historyType = :ha OR h.historyType = :hc)");
            params.put("ha", HistoryType.Letter_Assigned);
            params.put("hc", HistoryType.Letter_Copy_or_Forward);
        } else {
            jpql.append(" AND h.historyType = :ht");
            params.put("ht", type);
        }
        jpql.append(" ORDER BY h.id DESC");
        List<DocumentHistory> hs = documentHistoryFacade.findByJpql(jpql.toString(), params, pageSize);
        return Response.ok(ApiResponseDto.success(mapHistories(hs))).build();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private WebUser resolveActor(ApiKey key, Long actingUserId) {
        if (actingUserId != null) {
            WebUser u = webUserFacade.find(actingUserId);
            if (u != null && !u.isRetired()) {
                return u;
            }
        }
        WebUser keyOwner = key.getCreatedBy();
        return (keyOwner != null && !keyOwner.isRetired()) ? keyOwner : null;
    }

    private Item lookupItem(Long id) {
        return id != null ? itemFacade.find(id) : null;
    }

    private boolean isLetter(Document d) {
        return d.getDocumentType() == DocumentType.Letter;
    }

    private void writeHistory(Document d, WebUser actor, HistoryType type, String comments) {
        DocumentHistory h = new DocumentHistory();
        h.setDocument(d);
        h.setHistoryType(type);
        h.setComments(comments);
        h.setInstitution(actor.getInstitution());
        h.setFromUser(actor);
        saveHistory(h, actor);
    }

    private void saveHistory(DocumentHistory h, WebUser actor) {
        h.setCreatedAt(new Date());
        h.setCreatedBy(actor);
        h.setRetired(false);
        documentHistoryFacade.create(h);
    }

    /**
     * Apply DTO fields to a {@link Document}. On create ({@code includeAll}
     * true) blank strings are still copied; on update only non-null fields
     * are written.
     */
    private Response applyDtoToDocument(LetterCreateDto dto, Document d, boolean creating) {
        if (creating || dto.getDocumentName() != null) {
            d.setDocumentName(dto.getDocumentName());
        }
        if (dto.getDocumentNumber() != null) {
            d.setDocumentNumber(dto.getDocumentNumber());
        }
        if (dto.getDocumentCode() != null) {
            d.setDocumentCode(dto.getDocumentCode());
        }
        if (dto.getComments() != null) {
            d.setComments(dto.getComments());
        }
        if (dto.getSenderName() != null) {
            d.setSenderName(dto.getSenderName());
        }
        if (dto.getRegistrationNo() != null) {
            d.setRegistrationNo(dto.getRegistrationNo());
        }

        if (dto.getDocumentDate() != null) {
            Date dd = parseDate(dto.getDocumentDate());
            if (dd == null && !dto.getDocumentDate().isEmpty()) {
                return badRequest("documentDate must be ISO-8601 (yyyy-MM-dd)");
            }
            d.setDocumentDate(dd);
        }
        if (dto.getReceivedDate() != null) {
            Date rd = parseDate(dto.getReceivedDate());
            if (rd == null && !dto.getReceivedDate().isEmpty()) {
                return badRequest("receivedDate must be ISO-8601 (yyyy-MM-dd)");
            }
            d.setReceivedDate(rd);
        }

        if (dto.getDocumentGenerationType() != null && !dto.getDocumentGenerationType().isEmpty()) {
            try {
                d.setDocumentGenerationType(DocumentGenerationType.valueOf(dto.getDocumentGenerationType().trim()));
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown documentGenerationType: " + dto.getDocumentGenerationType());
            }
        }

        if (dto.getDocumentLanguageId() != null) {
            d.setDocumentLanguage(lookupItem(dto.getDocumentLanguageId()));
        }
        if (dto.getLetterStatusId() != null) {
            d.setLetterStatus(lookupItem(dto.getLetterStatusId()));
        }
        if (dto.getReceivedAsId() != null) {
            d.setReceivedAs(lookupItem(dto.getReceivedAsId()));
        }

        if (dto.getReferenceDocumentId() != null) {
            Document ref = documentFacade.find(dto.getReferenceDocumentId());
            if (ref == null) {
                return badRequest("Unknown referenceDocumentId: " + dto.getReferenceDocumentId());
            }
            d.setReferenceDocument(ref);
        }
        if (dto.getParentDocumentId() != null) {
            Document parent = documentFacade.find(dto.getParentDocumentId());
            if (parent == null) {
                return badRequest("Unknown parentDocumentId: " + dto.getParentDocumentId());
            }
            d.setParentDocument(parent);
        }

        if (dto.getInstitutionId() != null) {
            Institution ins = institutionFacade.find(dto.getInstitutionId());
            if (ins == null) {
                return badRequest("Unknown institutionId: " + dto.getInstitutionId());
            }
            d.setInstitution(ins);
        }
        if (dto.getInstitutionUnitId() != null) {
            Institution unit = institutionFacade.find(dto.getInstitutionUnitId());
            if (unit == null) {
                return badRequest("Unknown institutionUnitId: " + dto.getInstitutionUnitId());
            }
            d.setInstitutionUnit(unit);
        }
        if (dto.getOwnerId() != null) {
            WebUser owner = webUserFacade.find(dto.getOwnerId());
            if (owner == null) {
                return badRequest("Unknown ownerId: " + dto.getOwnerId());
            }
            d.setOwner(owner);
            if (creating) {
                d.setCurrentOwner(owner);
            }
        }
        if (dto.getFromInstitutionId() != null) {
            Institution fi = institutionFacade.find(dto.getFromInstitutionId());
            if (fi == null) {
                return badRequest("Unknown fromInstitutionId: " + dto.getFromInstitutionId());
            }
            d.setFromInstitution(fi);
        }
        if (dto.getFromWebUserId() != null) {
            WebUser fu = webUserFacade.find(dto.getFromWebUserId());
            if (fu == null) {
                return badRequest("Unknown fromWebUserId: " + dto.getFromWebUserId());
            }
            d.setFromWebUser(fu);
        }
        if (dto.getToInstitutionId() != null) {
            Institution ti = institutionFacade.find(dto.getToInstitutionId());
            if (ti == null) {
                return badRequest("Unknown toInstitutionId: " + dto.getToInstitutionId());
            }
            d.setToInstitution(ti);
        }
        if (dto.getToWebUserId() != null) {
            WebUser tu = webUserFacade.find(dto.getToWebUserId());
            if (tu == null) {
                return badRequest("Unknown toWebUserId: " + dto.getToWebUserId());
            }
            d.setToWebUser(tu);
        }
        return null;
    }

    private Map<String, Object> toMap(Document d) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", d.getId());
        m.put("documentType", d.getDocumentType() != null ? d.getDocumentType().name() : null);
        m.put("documentName", d.getDocumentName());
        m.put("documentNumber", d.getDocumentNumber());
        m.put("documentCode", d.getDocumentCode());
        m.put("comments", d.getComments());
        m.put("documentDate", d.getDocumentDate());
        m.put("receivedDate", d.getReceivedDate());
        m.put("documentGenerationType", d.getDocumentGenerationType() != null ? d.getDocumentGenerationType().name() : null);
        m.put("registrationNo", d.getRegistrationNo());
        m.put("senderName", d.getSenderName());

        m.put("documentLanguage", refMap(d.getDocumentLanguage()));
        m.put("letterStatus", refMap(d.getLetterStatus()));
        m.put("receivedAs", refMap(d.getReceivedAs()));

        m.put("institution", refMap(d.getInstitution()));
        m.put("institutionUnit", refMap(d.getInstitutionUnit()));
        m.put("owner", refMap(d.getOwner()));
        m.put("currentInstitution", refMap(d.getCurrentInstitution()));
        m.put("currentOwner", refMap(d.getCurrentOwner()));

        m.put("fromInstitution", refMap(d.getFromInstitution()));
        m.put("fromWebUser", refMap(d.getFromWebUser()));
        m.put("toInstitution", refMap(d.getToInstitution()));
        m.put("toWebUser", refMap(d.getToWebUser()));

        m.put("referenceDocumentId", d.getReferenceDocument() != null ? d.getReferenceDocument().getId() : null);
        m.put("parentDocumentId", d.getParentDocument() != null ? d.getParentDocument().getId() : null);

        m.put("createdAt", d.getCreatedAt());
        m.put("createdBy", refMap(d.getCreatedBy()));
        m.put("completed", d.isCompleted());
        m.put("completedAt", d.getCompletedAt());
        m.put("completedBy", refMap(d.getCompletedBy()));
        m.put("retired", d.isRetired());
        return m;
    }

    private Map<String, Object> refMap(Institution i) {
        if (i == null) {
            return null;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", i.getId());
        m.put("name", i.getName());
        return m;
    }

    private Map<String, Object> refMap(WebUser u) {
        if (u == null) {
            return null;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        return m;
    }

    private Map<String, Object> refMap(Item it) {
        if (it == null) {
            return null;
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", it.getId());
        m.put("name", it.getName());
        m.put("code", it.getCode());
        return m;
    }

    private Map<String, Object> toMapUpload(Upload u, boolean includeBytes) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("documentId", u.getDocument() != null ? u.getDocument().getId() : null);
        m.put("fileName", u.getFileName());
        m.put("fileType", u.getFileType());
        m.put("uploadType", u.getUploadType() != null ? u.getUploadType().name() : null);
        m.put("comments", u.getComments());
        m.put("createdAt", u.getCreatedAt());
        m.put("createdBy", refMap(u.getCreater()));
        m.put("retired", u.isRetired());
        m.put("size", u.getBaImage() != null ? u.getBaImage().length : 0);
        if (includeBytes && u.getBaImage() != null) {
            m.put("base64", Base64.getEncoder().encodeToString(u.getBaImage()));
        }
        return m;
    }

    private List<Map<String, Object>> mapHistories(List<DocumentHistory> hs) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (hs == null) {
            return out;
        }
        for (DocumentHistory h : hs) {
            Map<String, Object> m = new HashMap<>();
            m.put("historyId", h.getId());
            m.put("historyType", h.getHistoryType() != null ? h.getHistoryType().name() : null);
            m.put("comments", h.getComments());
            m.put("createdAt", h.getCreatedAt());
            m.put("createdBy", refMap(h.getCreatedBy()));
            m.put("fromUser", refMap(h.getFromUser()));
            m.put("toUser", refMap(h.getToUser()));
            m.put("fromInstitution", refMap(h.getFromInstitution()));
            m.put("toInstitution", refMap(h.getToInstitution()));
            m.put("institution", refMap(h.getInstitution()));
            m.put("completed", h.isCompleted());
            m.put("completedAt", h.getCompletedAt());
            m.put("completedBy", refMap(h.getCompletedBy()));
            m.put("item", refMap(h.getItem()));
            if (h.getDocument() != null) {
                m.put("letter", toMap(h.getDocument()));
            }
            out.add(m);
        }
        return out;
    }

    private static Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(s.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    private static Date startOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    private static Date endOfDay(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                .build();
    }

    private static Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(ApiResponseDto.error(404, message))
                .build();
    }

    private static Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponseDto.error(400, message))
                .build();
    }

}
