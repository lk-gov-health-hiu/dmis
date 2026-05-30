/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.institution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.gov.health.phsp.bean.ApiKeyController;
import lk.gov.health.phsp.bean.InstitutionApplicationController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.ws.common.ApiResponseDto;

@Path("institutions")
@Produces(MediaType.APPLICATION_JSON)
public class InstitutionResource {

    @Inject
    private ApiKeyController apiKeyController;

    @Inject
    private InstitutionApplicationController institutionApplicationController;

    /**
     * GET /api/institutions
     * Header: Api-Key: <key>
     */
    @GET
    public Response listInstitutions(@HeaderParam("Api-Key") String apiKey) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        List<Institution> institutions = institutionApplicationController.getInstitutions();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Institution i : institutions) {
            result.add(toMap(i));
        }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * GET /api/institutions/{id}
     * Header: Api-Key: <key>
     */
    @GET
    @Path("{id}")
    public Response getInstitution(@HeaderParam("Api-Key") String apiKey,
                                   @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        Institution institution = institutionApplicationController.findInstitutionById(id);
        if (institution == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponseDto.error(404, "Institution not found"))
                    .build();
        }
        return Response.ok(ApiResponseDto.success(toMap(institution))).build();
    }

    private Map<String, Object> toMap(Institution i) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", i.getId());
        m.put("name", i.getName());
        m.put("code", i.getCode());
        m.put("type", i.getInstitutionType() != null ? i.getInstitutionType().name() : null);
        m.put("address", i.getAddress());
        m.put("phone", i.getPhone());
        m.put("email", i.getEmail());
        return m;
    }

}
