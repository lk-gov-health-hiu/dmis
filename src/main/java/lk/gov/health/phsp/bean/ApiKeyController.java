/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import lk.gov.health.phsp.entity.ApiKey;
import lk.gov.health.phsp.facade.ApiKeyFacade;

@Named
@ApplicationScoped
public class ApiKeyController {

    @EJB
    private ApiKeyFacade apiKeyFacade;

    /**
     * Validates the given raw key string against active (non-retired) ApiKey records.
     * Returns the matching ApiKey or null if not found / retired.
     */
    public ApiKey validateKey(String rawKey) {
        if (rawKey == null || rawKey.trim().isEmpty()) {
            return null;
        }
        String jpql = "SELECT k FROM ApiKey k WHERE k.keyValue = :kv AND k.retired = false";
        Map<String, Object> params = new HashMap<>();
        params.put("kv", rawKey.trim());
        List<ApiKey> results = apiKeyFacade.findByJpql(jpql, params);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Creates and persists a new ApiKey with a random UUID value.
     */
    public ApiKey generateKey(String name, String description) {
        ApiKey key = new ApiKey();
        key.setKeyValue(UUID.randomUUID().toString());
        key.setName(name);
        key.setDescription(description);
        key.setCreatedAt(new Date());
        key.setRetired(false);
        apiKeyFacade.create(key);
        return key;
    }

}
