/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.gov.health.phsp.bean.ApiKeyController;
import lk.gov.health.phsp.bean.AreaApplicationController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.Area;
import lk.gov.health.phsp.enums.AreaType;
import lk.gov.health.phsp.facade.AreaFacade;
import lk.gov.health.phsp.ws.common.ApiResponseDto;

@Path("areas")
@Produces(MediaType.APPLICATION_JSON)
public class AreaResource {

    @Inject
    private ApiKeyController apiKeyController;

    @Inject
    private AreaApplicationController areaApplicationController;

    @EJB
    private AreaFacade areaFacade;

    /**
     * GET /api/areas?type=DISTRICT
     * Header: Api-Key: <key>
     * If type is omitted, returns all areas.
     */
    @GET
    public Response listAreas(@HeaderParam("Api-Key") String apiKey,
                              @QueryParam("type") String type) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        List<Area> areas;
        if (type != null && !type.trim().isEmpty()) {
            try {
                AreaType areaType = AreaType.valueOf(type.trim());
                areas = areaApplicationController.getAllAreas(areaType);
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponseDto.error(400, "Unknown area type: " + type))
                        .build();
            }
        } else {
            areas = areaApplicationController.getAllAreas();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Area a : areas) {
            result.add(toMap(a));
        }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * GET /api/areas/{id}
     * Header: Api-Key: <key>
     */
    @GET
    @Path("{id}")
    public Response getArea(@HeaderParam("Api-Key") String apiKey,
                            @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid or missing Api-Key"))
                    .build();
        }

        Area area = areaFacade.find(id);
        if (area == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponseDto.error(404, "Area not found"))
                    .build();
        }
        return Response.ok(ApiResponseDto.success(toMap(area))).build();
    }

    private Map<String, Object> toMap(Area a) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", a.getId());
        m.put("name", a.getName());
        m.put("code", a.getCode());
        m.put("type", a.getType() != null ? a.getType().name() : null);
        return m;
    }

}
