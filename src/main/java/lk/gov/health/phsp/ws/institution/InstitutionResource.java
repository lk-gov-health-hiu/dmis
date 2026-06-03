/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.institution;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import lk.gov.health.phsp.entity.Institution;
import lk.gov.health.phsp.enums.InstitutionType;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.ws.common.ApiResponseDto;

@Path("institutions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstitutionResource {

    @Inject
    private ApiKeyController apiKeyController;

    @EJB
    private InstitutionFacade institutionFacade;

    /**
     * GET /api/institutions
     *   ?type=Hospital
     *   ?parentId=42       (direct children of 42; alias for /{id}/children)
     *   ?rootOnly=true     (only institutions with no parent)
     *   ?search=foo        (case-insensitive name contains)
     *   ?size=100
     */
    @GET
    public Response listInstitutions(@HeaderParam("Api-Key") String apiKey,
                                     @QueryParam("type") String type,
                                     @QueryParam("parentId") Long parentId,
                                     @QueryParam("rootOnly") @DefaultValue("false") boolean rootOnly,
                                     @QueryParam("search") String search,
                                     @QueryParam("size") Integer size) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }

        int pageSize = (size != null && size > 0 && size <= 1000) ? size : 200;

        StringBuilder jpql = new StringBuilder("SELECT i FROM Institution i WHERE i.retired = false");
        Map<String, Object> params = new HashMap<>();

        if (type != null && !type.trim().isEmpty()) {
            InstitutionType t;
            try {
                t = InstitutionType.valueOf(type.trim());
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown institutionType: " + type);
            }
            jpql.append(" AND i.institutionType = :t");
            params.put("t", t);
        }
        if (rootOnly) {
            jpql.append(" AND i.parent IS NULL");
        } else if (parentId != null) {
            Institution parent = institutionFacade.find(parentId);
            if (parent == null) {
                return badRequest("Unknown parentId: " + parentId);
            }
            jpql.append(" AND i.parent = :p");
            params.put("p", parent);
        }
        if (search != null && !search.trim().isEmpty()) {
            jpql.append(" AND lower(i.name) LIKE :q");
            params.put("q", "%" + search.trim().toLowerCase() + "%");
        }
        jpql.append(" ORDER BY i.name");

        List<Institution> institutions = institutionFacade.findByJpql(jpql.toString(), params, pageSize);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Institution i : institutions) {
            result.add(toMap(i));
        }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * GET /api/institutions/{id}
     */
    @GET
    @Path("{id}")
    public Response getInstitution(@HeaderParam("Api-Key") String apiKey,
                                   @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution institution = institutionFacade.find(id);
        if (institution == null || institution.isRetired()) {
            return notFound("Institution not found");
        }
        return Response.ok(ApiResponseDto.success(toMap(institution))).build();
    }

    /**
     * POST /api/institutions
     * Body: {name, sname?, tname?, code?, institutionType?, parentId?, address?, phone?, mobile?, fax?, email?, web?}
     */
    @POST
    public Response createInstitution(@HeaderParam("Api-Key") String apiKey,
                                      InstitutionCreateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        if (dto == null) {
            return badRequest("Request body is required");
        }
        if (isBlank(dto.getName())) {
            return badRequest("name is required");
        }

        Institution institution = new Institution();
        institution.setName(dto.getName().trim());
        institution.setSname(!isBlank(dto.getSname()) ? dto.getSname().trim() : dto.getName().trim());
        institution.setTname(!isBlank(dto.getTname()) ? dto.getTname().trim() : dto.getName().trim());
        if (!isBlank(dto.getCode())) {
            institution.setCode(dto.getCode().trim());
        }
        if (!isBlank(dto.getInstitutionType())) {
            try {
                institution.setInstitutionType(InstitutionType.valueOf(dto.getInstitutionType().trim()));
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown institutionType: " + dto.getInstitutionType());
            }
        }
        if (dto.getParentId() != null) {
            Institution parent = institutionFacade.find(dto.getParentId());
            if (parent == null || parent.isRetired()) {
                return badRequest("Unknown parentId: " + dto.getParentId());
            }
            institution.setParent(parent);
        }
        institution.setAddress(dto.getAddress());
        institution.setPhone(dto.getPhone());
        institution.setMobile(dto.getMobile());
        institution.setFax(dto.getFax());
        institution.setEmail(dto.getEmail());
        institution.setWeb(dto.getWeb());
        institution.setCreatedAt(new Date());
        institution.setRetired(false);

        institutionFacade.create(institution);

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponseDto.success(toMap(institution)))
                .build();
    }

    /**
     * PUT /api/institutions/{id}
     * Body: any of {name, sname, tname, code, institutionType, address, phone, mobile, fax, email, web}
     * Parent re-linking is done via PUT /api/institutions/{id}/parent.
     */
    @PUT
    @Path("{id}")
    public Response updateInstitution(@HeaderParam("Api-Key") String apiKey,
                                      @PathParam("id") Long id,
                                      InstitutionUpdateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution institution = institutionFacade.find(id);
        if (institution == null || institution.isRetired()) {
            return notFound("Institution not found");
        }
        if (dto == null) {
            return badRequest("Request body is required");
        }

        if (!isBlank(dto.getName())) {
            institution.setName(dto.getName().trim());
        }
        if (!isBlank(dto.getSname())) {
            institution.setSname(dto.getSname().trim());
        }
        if (!isBlank(dto.getTname())) {
            institution.setTname(dto.getTname().trim());
        }
        if (!isBlank(dto.getCode())) {
            institution.setCode(dto.getCode().trim());
        }
        if (!isBlank(dto.getInstitutionType())) {
            try {
                institution.setInstitutionType(InstitutionType.valueOf(dto.getInstitutionType().trim()));
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown institutionType: " + dto.getInstitutionType());
            }
        }
        if (dto.getAddress() != null) {
            institution.setAddress(dto.getAddress());
        }
        if (dto.getPhone() != null) {
            institution.setPhone(dto.getPhone());
        }
        if (dto.getMobile() != null) {
            institution.setMobile(dto.getMobile());
        }
        if (dto.getFax() != null) {
            institution.setFax(dto.getFax());
        }
        if (dto.getEmail() != null) {
            institution.setEmail(dto.getEmail());
        }
        if (dto.getWeb() != null) {
            institution.setWeb(dto.getWeb());
        }
        institution.setEditedAt(new Date());
        institutionFacade.edit(institution);

        return Response.ok(ApiResponseDto.success(toMap(institution))).build();
    }

    /**
     * DELETE /api/institutions/{id} — soft-delete. Refuses if the institution
     * still has any non-retired children, matching InstitutionController.deleteInstitution.
     */
    @DELETE
    @Path("{id}")
    public Response deleteInstitution(@HeaderParam("Api-Key") String apiKey,
                                      @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution institution = institutionFacade.find(id);
        if (institution == null || institution.isRetired()) {
            return notFound("Institution not found");
        }
        if (!directChildren(institution).isEmpty()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponseDto.error(409, "Cannot delete: institution still has child institutions"))
                    .build();
        }
        institution.setRetired(true);
        institution.setRetiredAt(new Date());
        institutionFacade.edit(institution);
        return Response.ok(ApiResponseDto.success(toMap(institution))).build();
    }

    /**
     * GET /api/institutions/{id}/children?recursive=true
     */
    @GET
    @Path("{id}/children")
    public Response getChildren(@HeaderParam("Api-Key") String apiKey,
                                @PathParam("id") Long id,
                                @QueryParam("recursive") @DefaultValue("false") boolean recursive) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution institution = institutionFacade.find(id);
        if (institution == null || institution.isRetired()) {
            return notFound("Institution not found");
        }

        List<Institution> children = recursive
                ? descendants(institution)
                : directChildren(institution);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Institution child : children) {
            result.add(toMap(child));
        }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * GET /api/institutions/{id}/parent
     */
    @GET
    @Path("{id}/parent")
    public Response getParent(@HeaderParam("Api-Key") String apiKey,
                              @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution institution = institutionFacade.find(id);
        if (institution == null || institution.isRetired()) {
            return notFound("Institution not found");
        }
        Institution parent = institution.getParent();
        Object data = (parent != null && !parent.isRetired()) ? toMap(parent) : null;
        return Response.ok(ApiResponseDto.success(data)).build();
    }

    /**
     * PUT /api/institutions/{id}/parent
     * Body: {"parentId": 42} or {"parentId": null} to unlink.
     * Rejects self-reference and cycles.
     */
    @PUT
    @Path("{id}/parent")
    public Response updateParent(@HeaderParam("Api-Key") String apiKey,
                                 @PathParam("id") Long id,
                                 ParentUpdateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        Institution institution = institutionFacade.find(id);
        if (institution == null || institution.isRetired()) {
            return notFound("Institution not found");
        }
        if (dto == null) {
            return badRequest("Request body is required");
        }

        if (dto.getParentId() == null) {
            institution.setParent(null);
        } else {
            if (dto.getParentId().equals(id)) {
                return badRequest("An institution cannot be its own parent");
            }
            Institution parent = institutionFacade.find(dto.getParentId());
            if (parent == null || parent.isRetired()) {
                return badRequest("Unknown parentId: " + dto.getParentId());
            }
            if (isDescendant(parent, institution)) {
                return badRequest("Cycle rejected: requested parent is a descendant of this institution");
            }
            institution.setParent(parent);
        }
        institution.setEditedAt(new Date());
        institutionFacade.edit(institution);
        return Response.ok(ApiResponseDto.success(toMap(institution))).build();
    }

    private List<Institution> directChildren(Institution parent) {
        String jpql = "SELECT i FROM Institution i WHERE i.retired = false AND i.parent = :p ORDER BY i.name";
        Map<String, Object> params = new HashMap<>();
        params.put("p", parent);
        return institutionFacade.findByJpql(jpql, params);
    }

    private List<Institution> descendants(Institution root) {
        Set<Long> visited = new LinkedHashSet<>();
        List<Institution> out = new ArrayList<>();
        collectDescendants(root, out, visited);
        return out;
    }

    private void collectDescendants(Institution node, List<Institution> out, Set<Long> visited) {
        for (Institution child : directChildren(node)) {
            if (child.getId() != null && visited.add(child.getId())) {
                out.add(child);
                collectDescendants(child, out, visited);
            }
        }
    }

    /**
     * True if {@code candidate} appears anywhere in the descendant subtree of
     * {@code ancestor}. Used to reject parent assignments that would form a cycle.
     */
    private boolean isDescendant(Institution candidate, Institution ancestor) {
        if (candidate == null || ancestor == null) {
            return false;
        }
        if (candidate.equals(ancestor)) {
            return true;
        }
        Set<Long> visited = new LinkedHashSet<>();
        return isDescendantInternal(candidate, ancestor, visited);
    }

    private boolean isDescendantInternal(Institution candidate, Institution ancestor, Set<Long> visited) {
        for (Institution child : directChildren(ancestor)) {
            if (child.getId() == null || !visited.add(child.getId())) {
                continue;
            }
            if (child.equals(candidate)) {
                return true;
            }
            if (isDescendantInternal(candidate, child, visited)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Object> toMap(Institution i) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", i.getId());
        m.put("name", i.getName());
        m.put("sname", i.getSname());
        m.put("tname", i.getTname());
        m.put("code", i.getCode());
        m.put("institutionType", i.getInstitutionType() != null ? i.getInstitutionType().name() : null);
        m.put("institutionTypeLabel", i.getInstitutionType() != null ? i.getInstitutionType().getLabel() : null);
        m.put("address", i.getAddress());
        m.put("phone", i.getPhone());
        m.put("mobile", i.getMobile());
        m.put("fax", i.getFax());
        m.put("email", i.getEmail());
        m.put("web", i.getWeb());
        m.put("parentId", i.getParent() != null ? i.getParent().getId() : null);
        m.put("parentName", i.getParent() != null ? i.getParent().getName() : null);
        m.put("retired", i.isRetired());
        return m;
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
