/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.common;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * JAX-RS application entry point. Registers the Jackson JSON provider and all
 * REST resource classes under the /api/* path.
 */
@javax.ws.rs.ApplicationPath("api")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        try {
            Class<?> jacksonProvider = Class.forName("com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider");
            resources.add(jacksonProvider);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Jackson JSON provider not found", ex);
        }
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(lk.gov.health.phsp.ws.auth.AuthResource.class);
        resources.add(lk.gov.health.phsp.ws.institution.InstitutionResource.class);
        resources.add(lk.gov.health.phsp.ws.common.CorsResponseFilter.class);
    }

}
