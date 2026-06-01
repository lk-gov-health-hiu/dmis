/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.document;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.gov.health.phsp.bean.ApiKeyController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.Document;
import lk.gov.health.phsp.facade.DocumentFacade;
import lk.gov.health.phsp.ws.common.ApiResponseDto;

@Path("documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentResource {

    @Inject
    private ApiKeyController apiKeyController;

    @EJB
    private DocumentFacade documentFacade;

    /**
     * GET /api/documents?page=0&size=20
     * Header: Api-Key: <key>
     */
    @GET
    public Response listDocuments(@HeaderParam("Api-Key") String apiKey,
                                  @QueryParam("size") Integer size) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        int pageSize = (size != null && size > 0 && size <= 100) ? size : 20;

        String jpql = "SELECT d FROM Document d WHERE d.retired = false ORDER BY d.createdAt DESC";
        Map<String, Object> params = new HashMap<>();
        List<Document> documents = documentFacade.findByJpql(jpql, params, pageSize);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Document d : documents) {
            result.add(toMap(d));
        }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * GET /api/documents/{id}
     * Header: Api-Key: <key>
     */
    @GET
    @Path("{id}")
    public Response getDocument(@HeaderParam("Api-Key") String apiKey,
                                @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        Document document = documentFacade.find(id);
        if (document == null || document.isRetired()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponseDto.error(404, "Document not found"))
                    .build();
        }
        return Response.ok(ApiResponseDto.success(toMap(document))).build();
    }

    /**
     * POST /api/documents
     * Header: Api-Key: <key>
     * Body: {"documentName":"...","documentNumber":"...","documentCode":"...","comments":"..."}
     */
    @POST
    public Response createDocument(@HeaderParam("Api-Key") String apiKey,
                                   DocumentCreateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        if (dto == null || dto.getDocumentName() == null || dto.getDocumentName().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponseDto.error(400, "documentName is required"))
                    .build();
        }

        Document document = new Document();
        document.setDocumentName(dto.getDocumentName().trim());
        document.setDocumentNumber(dto.getDocumentNumber());
        document.setDocumentCode(dto.getDocumentCode());
        document.setComments(dto.getComments());
        document.setCreatedAt(new Date());
        document.setRetired(false);

        documentFacade.create(document);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponseDto.success(toMap(document)))
                .build();
    }

    private Map<String, Object> toMap(Document d) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", d.getId());
        m.put("documentName", d.getDocumentName());
        m.put("documentNumber", d.getDocumentNumber());
        m.put("documentCode", d.getDocumentCode());
        m.put("documentType", d.getDocumentType() != null ? d.getDocumentType().name() : null);
        m.put("comments", d.getComments());
        m.put("documentDate", d.getDocumentDate());
        m.put("createdAt", d.getCreatedAt());
        m.put("institution", d.getInstitution() != null ? d.getInstitution().getName() : null);
        return m;
    }

}
