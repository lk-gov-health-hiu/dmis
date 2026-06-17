/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import lk.gov.health.phsp.entity.Person;
import lk.gov.health.phsp.entity.UserPrivilege;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.enums.Privilege;
import lk.gov.health.phsp.enums.WebUserRole;
import lk.gov.health.phsp.facade.InstitutionFacade;
import lk.gov.health.phsp.facade.PersonFacade;
import lk.gov.health.phsp.facade.UserPrivilegeFacade;
import lk.gov.health.phsp.facade.WebUserFacade;
import lk.gov.health.phsp.ws.common.ApiResponseDto;
import org.jasypt.util.password.BasicPasswordEncryptor;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private ApiKeyController apiKeyController;

    @EJB
    private WebUserFacade webUserFacade;

    @EJB
    private PersonFacade personFacade;

    @EJB
    private InstitutionFacade institutionFacade;

    @EJB
    private UserPrivilegeFacade userPrivilegeFacade;

    /**
     * GET /api/users
     * Header: Api-Key: <key>
     *
     * Query parameters (all optional, combine as needed):
     *   ?q=             general search (matches username or display name, partial case-insensitive)
     *   ?search=        same as ?q= (provided for backward compatibility; ?q= takes precedence)
     *   ?username=      partial match on username only
     *   ?name=          partial match on display name only
     *   ?institutionId= filter by institution
     *   ?retired=       include retired users (default: false, i.e. exclude retired)
     *   ?size=          max results (1–500, default: 100)
     */
    @GET
    public Response listUsers(@HeaderParam("Api-Key") String apiKey,
                              @QueryParam("size") Integer size,
                              @QueryParam("search") String search,
                              @QueryParam("q") String q,
                              @QueryParam("name") String name,
                              @QueryParam("username") String username,
                              @QueryParam("institutionId") Long institutionId,
                              @QueryParam("retired") Boolean retired) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }

        int pageSize = (size != null && size > 0 && size <= 500) ? size : 100;
        boolean includeRetired = (retired != null) ? retired : false;

        String jpql = "SELECT u FROM WebUser u WHERE 1=1";
        Map<String, Object> params = new HashMap<>();

        if (!includeRetired) {
            jpql += " AND u.retired = false";
        }

        // q takes precedence over search; both do the same OR match
        String generalQuery = (q != null && !q.trim().isEmpty()) ? q.trim()
                : (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        if (generalQuery != null) {
            jpql += " AND (lower(u.name) LIKE :q OR lower(u.person.name) LIKE :q)";
            params.put("q", "%" + generalQuery.toLowerCase() + "%");
        }

        if (username != null && !username.trim().isEmpty()) {
            jpql += " AND lower(u.name) LIKE :username";
            params.put("username", "%" + username.trim().toLowerCase() + "%");
        }

        if (name != null && !name.trim().isEmpty()) {
            jpql += " AND lower(u.person.name) LIKE :name";
            params.put("name", "%" + name.trim().toLowerCase() + "%");
        }

        if (institutionId != null) {
            jpql += " AND u.institution.id = :institutionId";
            params.put("institutionId", institutionId);
        }

        jpql += " ORDER BY u.name";

        List<WebUser> users = webUserFacade.findByJpql(jpql, params, pageSize);
        List<Map<String, Object>> result = new ArrayList<>();
        for (WebUser u : users) {
            result.add(toMap(u));
        }
        return Response.ok(ApiResponseDto.success(result)).build();
    }

    /**
     * GET /api/users/{id}
     */
    @GET
    @Path("{id}")
    public Response getUser(@HeaderParam("Api-Key") String apiKey,
                            @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }

        WebUser user = webUserFacade.find(id);
        if (user == null || user.isRetired()) {
            return notFound("User not found");
        }
        return Response.ok(ApiResponseDto.success(toMap(user))).build();
    }

    /**
     * POST /api/users
     * Body: {username, password, name, code?, email?, telNo?, webUserRole, institutionId}
     *
     * Creates a new user and auto-grants the role's default privileges. For
     * Institutional_Administrator this includes the institution-admin
     * privileges PLUS the full institution-level operating privileges
     * (matches WebUserController.getInitialPrivileges fallthrough).
     */
    @POST
    public Response createUser(@HeaderParam("Api-Key") String apiKey,
                               UserCreateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }

        if (dto == null) {
            return badRequest("Request body is required");
        }
        if (isBlank(dto.getUsername())) {
            return badRequest("username is required");
        }
        if (isBlank(dto.getPassword())) {
            return badRequest("password is required");
        }
        if (isBlank(dto.getName())) {
            return badRequest("name is required");
        }
        if (isBlank(dto.getWebUserRole())) {
            return badRequest("webUserRole is required");
        }
        WebUserRole role;
        try {
            role = WebUserRole.valueOf(dto.getWebUserRole().trim());
        } catch (IllegalArgumentException e) {
            return badRequest("Unknown webUserRole: " + dto.getWebUserRole());
        }
        if (usernameExists(dto.getUsername().trim())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(ApiResponseDto.error(409, "Username already exists"))
                    .build();
        }

        Institution institution = null;
        if (dto.getInstitutionId() != null) {
            institution = institutionFacade.find(dto.getInstitutionId());
            if (institution == null) {
                return badRequest("Unknown institutionId: " + dto.getInstitutionId());
            }
        }

        Person person = new Person();
        person.setName(dto.getName().trim());
        if (!isBlank(dto.getCode())) {
            person.setCode(dto.getCode().trim());
        }
        personFacade.create(person);

        WebUser user = new WebUser();
        user.setName(dto.getUsername().trim().toLowerCase());
        user.setPerson(person);
        user.setEmail(dto.getEmail());
        user.setTelNo(dto.getTelNo());
        user.setWebUserRole(role);
        user.setInstitution(institution);
        user.setWebUserPassword(new BasicPasswordEncryptor().encryptPassword(dto.getPassword()));
        user.setCreatedAt(new Date());
        user.setRetired(false);
        webUserFacade.create(user);

        grantPrivileges(user, defaultPrivilegesForRole(role));

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponseDto.success(toMap(user)))
                .build();
    }

    /**
     * PUT /api/users/{id}
     * Body: any of {name, code, email, telNo, webUserRole, institutionId}
     */
    @PUT
    @Path("{id}")
    public Response updateUser(@HeaderParam("Api-Key") String apiKey,
                               @PathParam("id") Long id,
                               UserUpdateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser user = webUserFacade.find(id);
        if (user == null || user.isRetired()) {
            return notFound("User not found");
        }
        if (dto == null) {
            return badRequest("Request body is required");
        }

        if (!isBlank(dto.getName())) {
            user.getPerson().setName(dto.getName().trim());
        }
        if (!isBlank(dto.getCode())) {
            user.getPerson().setCode(dto.getCode().trim());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getTelNo() != null) {
            user.setTelNo(dto.getTelNo());
        }
        if (!isBlank(dto.getWebUserRole())) {
            try {
                user.setWebUserRole(WebUserRole.valueOf(dto.getWebUserRole().trim()));
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown webUserRole: " + dto.getWebUserRole());
            }
        }
        if (dto.getInstitutionId() != null) {
            Institution institution = institutionFacade.find(dto.getInstitutionId());
            if (institution == null) {
                return badRequest("Unknown institutionId: " + dto.getInstitutionId());
            }
            user.setInstitution(institution);
        }
        user.setLastEditeAt(new Date());

        if (user.getPerson() != null && user.getPerson().getId() != null) {
            personFacade.edit(user.getPerson());
        }
        webUserFacade.edit(user);

        return Response.ok(ApiResponseDto.success(toMap(user))).build();
    }

    /**
     * PUT /api/users/{id}/password
     * Body: {"password":"..."}
     */
    @PUT
    @Path("{id}/password")
    public Response updatePassword(@HeaderParam("Api-Key") String apiKey,
                                   @PathParam("id") Long id,
                                   PasswordUpdateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser user = webUserFacade.find(id);
        if (user == null || user.isRetired()) {
            return notFound("User not found");
        }
        if (dto == null || isBlank(dto.getPassword())) {
            return badRequest("password is required");
        }
        user.setWebUserPassword(new BasicPasswordEncryptor().encryptPassword(dto.getPassword()));
        user.setLastEditeAt(new Date());
        webUserFacade.edit(user);
        return Response.ok(ApiResponseDto.success(toMap(user))).build();
    }

    /**
     * GET /api/users/{id}/privileges
     */
    @GET
    @Path("{id}/privileges")
    public Response getPrivileges(@HeaderParam("Api-Key") String apiKey,
                                  @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser user = webUserFacade.find(id);
        if (user == null || user.isRetired()) {
            return notFound("User not found");
        }
        List<String> names = new ArrayList<>();
        for (UserPrivilege up : activePrivileges(user)) {
            if (up.getPrivilege() != null) {
                names.add(up.getPrivilege().name());
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getName());
        data.put("privileges", names);
        return Response.ok(ApiResponseDto.success(data)).build();
    }

    /**
     * PUT /api/users/{id}/privileges
     * Body: {"privileges":["Manage_Users","Add_File", ...]}
     *
     * Replaces the user's privilege set: missing items are retired, new
     * items are granted (or un-retired if a retired row already exists).
     */
    @PUT
    @Path("{id}/privileges")
    public Response replacePrivileges(@HeaderParam("Api-Key") String apiKey,
                                      @PathParam("id") Long id,
                                      PrivilegesUpdateDto dto) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser user = webUserFacade.find(id);
        if (user == null || user.isRetired()) {
            return notFound("User not found");
        }
        if (dto == null || dto.getPrivileges() == null) {
            return badRequest("privileges array is required");
        }

        Set<Privilege> requested = new HashSet<>();
        for (String name : dto.getPrivileges()) {
            if (name == null || name.trim().isEmpty()) {
                continue;
            }
            try {
                requested.add(Privilege.valueOf(name.trim()));
            } catch (IllegalArgumentException e) {
                return badRequest("Unknown privilege: " + name);
            }
        }

        List<UserPrivilege> existing = activePrivileges(user);
        Set<Privilege> existingSet = new HashSet<>();
        for (UserPrivilege up : existing) {
            if (up.getPrivilege() != null) {
                existingSet.add(up.getPrivilege());
            }
        }

        for (Privilege p : requested) {
            if (!existingSet.contains(p)) {
                grantPrivilege(user, p);
            }
        }
        for (UserPrivilege up : existing) {
            if (up.getPrivilege() != null && !requested.contains(up.getPrivilege())) {
                up.setRetired(true);
                up.setRetiredAt(new Date());
                userPrivilegeFacade.edit(up);
            }
        }

        return getPrivileges(apiKey, id);
    }

    /**
     * DELETE /api/users/{id} — soft delete (retired=true).
     */
    @DELETE
    @Path("{id}")
    public Response deleteUser(@HeaderParam("Api-Key") String apiKey,
                               @PathParam("id") Long id) {
        ApiKey key = apiKeyController.validateKey(apiKey);
        if (key == null) {
            return unauthorized();
        }
        WebUser user = webUserFacade.find(id);
        if (user == null || user.isRetired()) {
            return notFound("User not found");
        }
        user.setRetired(true);
        user.setRetiredAt(new Date());
        webUserFacade.edit(user);
        return Response.ok(ApiResponseDto.success(toMap(user))).build();
    }

    private boolean usernameExists(String username) {
        String jpql = "SELECT u FROM WebUser u WHERE lower(u.name) = :un";
        Map<String, Object> params = new HashMap<>();
        params.put("un", username.toLowerCase());
        return webUserFacade.findFirstByJpql(jpql, params) != null;
    }

    private List<UserPrivilege> activePrivileges(WebUser user) {
        String jpql = "SELECT p FROM UserPrivilege p WHERE p.retired = false AND p.webUser = :u";
        Map<String, Object> params = new HashMap<>();
        params.put("u", user);
        return userPrivilegeFacade.findByJpql(jpql, params);
    }

    private void grantPrivileges(WebUser user, List<Privilege> privileges) {
        for (Privilege p : privileges) {
            grantPrivilege(user, p);
        }
    }

    private void grantPrivilege(WebUser user, Privilege p) {
        String jpql = "SELECT up FROM UserPrivilege up WHERE up.webUser = :u AND up.privilege = :p ORDER BY up.id DESC";
        Map<String, Object> params = new HashMap<>();
        params.put("u", user);
        params.put("p", p);
        UserPrivilege up = userPrivilegeFacade.findFirstByJpql(jpql, params);
        if (up == null) {
            up = new UserPrivilege();
            up.setWebUser(user);
            up.setPrivilege(p);
            up.setCreatedAt(new Date());
            userPrivilegeFacade.create(up);
        } else {
            up.setRetired(false);
            up.setRetiredAt(null);
            up.setCreatedAt(new Date());
            userPrivilegeFacade.edit(up);
        }
    }

    /**
     * Mirrors WebUserController.getInitialPrivileges. Institutional_Administrator
     * deliberately falls through to also include all the institution-level
     * operating privileges granted to Institutional_Super_User / Institutional_User.
     */
    private List<Privilege> defaultPrivilegesForRole(WebUserRole role) {
        List<Privilege> ps = new ArrayList<>();
        if (role == null) {
            return ps;
        }
        switch (role) {
            case Institutional_Administrator:
                ps.add(Privilege.Institution_Administration);
                ps.add(Privilege.Manage_Institution_Users);
                ps.add(Privilege.Manage_Authorised_Institutions);
                // fallthrough to also grant the institution operating privileges
            case Institutional_Super_User:
            case Institutional_User:
                ps.addAll(Arrays.asList(
                        Privilege.File_Management,
                        Privilege.Institutional_Mail_Management,
                        Privilege.User,
                        Privilege.Search_File,
                        Privilege.Retire_File,
                        Privilege.Add_Actions_To_Letter,
                        Privilege.Add_Letter,
                        Privilege.Assign_Letter,
                        Privilege.Edit_Letter,
                        Privilege.Receive_File,
                        Privilege.Retire_Letter));
                break;
            case System_Administrator:
            case Super_User:
                ps.add(Privilege.Manage_Users);
                ps.add(Privilege.System_Administration);
                break;
            case User:
                ps.add(Privilege.Monitoring_and_evaluation);
                ps.add(Privilege.Monitoring_and_evaluation_reports);
                ps.add(Privilege.View_aggragate_date);
                ps.add(Privilege.View_individual_data);
                ps.add(Privilege.File_Management);
                ps.add(Privilege.Institutional_Mail_Management);
                ps.add(Privilege.User);
                ps.add(Privilege.Add_File);
                ps.add(Privilege.Search_File);
                ps.add(Privilege.Retire_File);
                break;
            default:
                break;
        }
        return ps;
    }

    private Map<String, Object> toMap(WebUser u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("username", u.getName());
        m.put("name", u.getPerson() != null ? u.getPerson().getName() : null);
        m.put("code", u.getPerson() != null ? u.getPerson().getCode() : null);
        m.put("email", u.getEmail());
        m.put("telNo", u.getTelNo());
        m.put("webUserRole", u.getWebUserRole() != null ? u.getWebUserRole().name() : null);
        m.put("webUserRoleLabel", u.getWebUserRole() != null ? u.getWebUserRole().getLabel() : null);
        m.put("institutionId", u.getInstitution() != null ? u.getInstitution().getId() : null);
        m.put("institutionName", u.getInstitution() != null ? u.getInstitution().getName() : null);
        m.put("retired", u.isRetired());
        m.put("createdAt", u.getCreatedAt());
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
