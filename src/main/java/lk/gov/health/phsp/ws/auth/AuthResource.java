/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.auth;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.gov.health.phsp.bean.ApiKeyController;
import lk.gov.health.phsp.bean.WebUserApplicationController;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.entity.WebUser;
import lk.gov.health.phsp.ws.common.ApiResponseDto;
import lk.gov.health.phsp.ws.common.AuthRequestDto;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private WebUserApplicationController webUserApplicationController;

    @Inject
    private ApiKeyController apiKeyController;

    /**
     * POST /api/auth/token
     * Body: {"username":"...","password":"..."}
     * Returns: {"status":"success","code":200,"data":{"apiKey":"<uuid>","username":"..."}}
     */
    @POST
    @Path("token")
    public Response authenticate(AuthRequestDto request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponseDto.error(400, "username and password are required"))
                    .build();
        }

        WebUser user = webUserApplicationController.getWebUser(
                request.getUsername(), request.getPassword());

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponseDto.error(401, "Invalid credentials"))
                    .build();
        }

        ApiKey key = apiKeyController.generateKey(
                user.getName(), "API key for " + user.getName());

        Map<String, String> data = new HashMap<>();
        data.put("apiKey", key.getKeyValue());
        data.put("username", user.getName());

        return Response.ok(ApiResponseDto.success(data)).build();
    }

}
