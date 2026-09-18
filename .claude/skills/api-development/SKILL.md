---
name: api-development
description: >
  Conventions for adding backend endpoints or actions to DMIS. Use when creating a new JAX-RS
  REST resource under lk.gov.health.phsp.ws.*, adding an endpoint to an existing resource, or
  adding an action method to a JSF managed bean that backs an XHTML view. Covers request/response
  shape, Api-Key auth, error handling, and how both layers talk to facades/JPA.
---

# API Development Guide

DMIS has two distinct backend surfaces. Pick the right one:

- **REST (JAX-RS)** under `lk.gov.health.phsp.ws.*` — for external/programmatic clients
  (mobile apps, integrations, scripts). Real, in-production code — not a toy.
- **Managed-bean action methods** under `lk.gov.health.phsp.bean.*` — for the JSF/PrimeFaces
  UI itself. This is how almost all in-app functionality is actually built.

Most feature work in this app is a managed bean, not a REST endpoint. Only reach for REST
when the caller is genuinely outside the JSF request lifecycle.

---

## REST Endpoints (JAX-RS)

Package: `javax.ws.rs.*` (not `jakarta.*` — this app is still on the old namespace).
Existing resources: `ApiResource`, `AreaResource`, `AuthResource`, `DocumentResource`,
`InstitutionResource`, `LetterResource`, `UserResource` — all under `src/main/java/lk/gov/health/phsp/ws/`.

### 1. File placement
- One resource class per aggregate: `lk/gov/health/phsp/ws/<module>/<Name>Resource.java`
- Request/response bodies for non-trivial payloads go in their own DTO class next to the
  resource (e.g. `LetterCreateDto`, `LetterForwardDto`, `InstitutionUpdateDto`) — don't reuse
  JPA entities as wire types.
- Class-level: `@Path("<plural-name>")`, `@Produces(MediaType.APPLICATION_JSON)`,
  `@Consumes(MediaType.APPLICATION_JSON)`.

### 2. Register the resource
Add one line to `addRestResourceClasses()` in
`src/main/java/lk/gov/health/phsp/ws/common/ApplicationConfig.java`:
```java
resources.add(lk.gov.health.phsp.ws.<module>.<Name>Resource.class);
```
Nothing works until this is added — JAX-RS won't discover the class on its own here.

### 3. Auth — every endpoint validates an `Api-Key` header first
```java
@HeaderParam("Api-Key") String apiKey
...
ApiKey key = apiKeyController.validateKey(apiKey);
if (key == null) {
    return unauthorized();
}
```
`ApiKeyController` (`lk.gov.health.phsp.bean.ApiKeyController`, `@ApplicationScoped`) is
injected with `@Inject` and does the lookup against non-retired `ApiKey` rows.

For endpoints that write data or perform an action attributed to a person, also resolve an
**acting user** — see `LetterResource.resolveActor()`. It checks, in order: the
`X-Acting-User-Id` header (must resolve to a non-retired `WebUser`), then
`ApiKey.getCreatedBy()`. State-changing endpoints (POST/PUT/DELETE) must return `400` if
neither resolves — never silently attribute the action to nobody.

### 4. Response envelope — always `ApiResponseDto`
```json
{"status":"success","code":200,"data":{...}}
{"status":"error","code":400,"message":"..."}
```
```java
return Response.status(Response.Status.CREATED)
        .entity(ApiResponseDto.success(toMap(entity)))
        .build();
```
Use the shared private helpers already at the bottom of `LetterResource` as the pattern for
new resources — `unauthorized()`, `notFound(String)`, `badRequest(String)` — each wraps
`ApiResponseDto.error(code, message)` at the right HTTP status. Copy the pattern, don't
invent a new shape per resource.

Entities are never serialized directly (avoids JPA lazy-loading / cyclic-relation blowups
through Jackson) — build a `Map<String,Object>` or a dedicated DTO (`toMap(...)` helper
methods in `LetterResource`/`UserResource`) with exactly the fields the client needs.

### 5. Injecting the persistence layer
```java
@EJB
private DocumentFacade documentFacade;
```
REST resources inject facades (`@EJB`) directly — there is no separate service-layer
convention here. Do the same simple CRUD/JPQL calls you'd do from a managed bean (see
"JPA Access" below). Only introduce an intermediate helper class when logic is reused by
both a REST endpoint and a bean.

### 6. Listing endpoints — pagination convention
```java
@QueryParam("size") Integer size,
@QueryParam("page") @DefaultValue("0") int page
...
int pageSize = (size != null && size > 0 && size <= MAX_PAGE_SIZE) ? size : DEFAULT_PAGE_SIZE;
int firstResult = Math.max(page, 0) * pageSize;
```
`MAX_PAGE_SIZE` / `DEFAULT_PAGE_SIZE` are constants at the top of the resource class
(see `LetterResource`). Echo `page`, `size`, and `returned` in the response envelope's
`data` so callers can tell if they've reached the end.

### 7. CORS
`CorsResponseFilter` / `CorsFilter` already handle CORS headers app-wide — don't add
per-endpoint CORS logic.

---

## Managed-Bean Action Methods (JSF)

This is the primary way features get built. A bean like `LetterController`
(`@Named @SessionScoped`) backs one or more XHTML views and exposes action methods bound
from `p:commandButton actionListener="#{bean.method}"` or `action="#{bean.method}"`.

### Conventions
- **Facades via `@EJB`**, same as REST: `@EJB private DocumentFacade documentFacade;`.
- **Feedback via `JsfUtil`**, never inline `FacesMessage` construction and never a Bootstrap
  alert: `JsfUtil.addErrorMessage("...")` / `JsfUtil.addSuccessMessage("...")`. Render with
  `p:growl`/`p:messages` in the view, per this project's UI rules.
- **Validate early, return, don't proceed**:
  ```java
  if (selected == null) {
      JsfUtil.addErrorMessage("Select a letter");
      return "";
  }
  ```
  Action methods that navigate return a `String` outcome (or `""`/`null` to stay put);
  pure AJAX actions return `void`.
- **Create vs update is decided by ID, not by a separate flag**:
  ```java
  if (e.getId() == null) {
      e.setCreatedAt(new Date());
      e.setCreatedBy(webUserController.getLoggedUser());
      getFacade().create(e);
  } else {
      e.setLastEditBy(webUserController.getLoggedUser());
      e.setLastEditeAt(new Date());
      getFacade().edit(e);
  }
  ```
  (see `LetterController.save(Document)`). Always stamp actor + timestamp from the
  session-scoped `webUserController.getLoggedUser()`/`getLoggedInstitution()` — never trust
  a client-supplied "who did this".
- **Wrap facade writes that can violate a DB constraint** in `try { ... } catch (EJBException
  ex) { ... }`, unwrap `ex.getCause().getLocalizedMessage()` for the user-facing message
  (see `LetterController.persist(...)`), and surface it with `JsfUtil.addErrorMessage(...)`.
  Don't let a raw `EJBException` reach the JSF error page.
- Side effects that must be recorded (assignment, forwarding, receipt) get a
  `DocumentHistory` row written alongside the main save — same as the REST layer's
  `writeHistory(...)`. Keep both layers' history-writing behavior identical if a feature is
  exposed through both.

---

## JPA Access (shared by both layers)

Both REST resources and managed beans go through the same `@EJB`-injected `*Facade` classes,
all extending `AbstractFacade<T>` (`src/main/java/lk/gov/health/phsp/facade/AbstractFacade.java`).
Useful methods: `find(id)`, `create(e)`, `edit(e)`, `remove(e)`, `findAll(withoutRetired)`,
`findByJpql(jpql, params)`, `findByJpql(jpql, params, maxRecords)`, `findFirstByJpql(...)`,
`countByJpql(...)`. Prefer `findByJpql` with a `Map<String,Object>` of named parameters over
building JPQL by string concatenation of values (SQL-injection-safe and consistent with the
rest of the codebase).

Soft delete is the norm: entities have a `retired` boolean — set it and `edit()`, don't call
`remove()`, unless the endpoint/action is explicitly a hard delete.

---

## Checklist for a New Endpoint or Action

1. REST: DTO(s) if the body isn't trivial → resource class → registered in
   `ApplicationConfig` → Api-Key (+ acting-user, if it writes) check → `ApiResponseDto`
   envelope on every return path.
   Bean: action method → validates and returns early on bad input → uses
   `webUserController` for actor/institution → `JsfUtil` for feedback.
2. Facade calls use named-parameter JPQL, never raw string concatenation of user input.
3. Writes stamp `createdBy`/`createdAt` or `lastEditBy`/`lastEditeAt` from the session,
   not from client input.
4. Any state change that matters for audit gets a `DocumentHistory` entry.
5. JSF-only changes (no `.java` touched) don't need a rebuild; anything under
   `src/main/java` does — see this repo's `CLAUDE.md` for build/deploy rules.
