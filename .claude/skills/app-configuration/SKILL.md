---
name: app-configuration
description: >
  Reference for DMIS's configuration surface — persistence.xml, the Payara
  jdbc/dmis datasource, web.xml/faces-config.xml context params, and the
  system-property/env-var pattern used for tunable settings. Use when working
  on database connectivity, deployment config, JVM/env settings, or adding a
  new tunable value to a controller or EJB.
user-invocable: true
---

# DMIS Application Configuration

DMIS has no database-backed configuration-option store (no `ConfigOption`
entity or admin UI for toggles). Configuration lives in three places:
container-managed XML descriptors, the Payara domain itself (not checked into
source control), and small per-feature Java classes that resolve system
properties / environment variables with a hardcoded fallback.

## Persistence (`persistence.xml`)

`src/main/resources/META-INF/persistence.xml` declares a single JTA
persistence unit:

```xml
<persistence-unit name="hmisPU" transaction-type="JTA">
    <jta-data-source>jdbc/dmis</jta-data-source>
    <exclude-unlisted-classes>false</exclude-unlisted-classes>
    <shared-cache-mode>DISABLE_SELECTIVE</shared-cache-mode>
</persistence-unit>
```

- Unit name is `hmisPU` (legacy name, predates the DMIS rename — do not
  "fix" it, every `@PersistenceContext(unitName = "hmisPU")` in the
  facade layer depends on it).
- `jdbc/dmis` is a JNDI name only — the actual connection pool (host, port,
  schema, credentials, pool sizing) is **not in this repo**. It is defined in
  the Payara `domain1` domain config on the deployment target, via
  `asadmin create-jdbc-connection-pool` / `create-jdbc-resource` (or the
  admin console). If you need to inspect or change it, that's a Payara admin
  operation on the server, not a file edit here.
- `exclude-unlisted-classes=false` means every `@Entity` on the classpath is
  auto-discovered — no explicit `<class>` list to maintain.
- Schema-generation is deliberately commented out (see the comment "DISABLED
  while pointed at production: do not let EclipseLink ALTER prod schema").
  Never uncomment `javax.persistence.schema-generation.database.action`
  against the production datasource.
- EclipseLink is the JPA provider (Payara 5 default); the commented-out
  connection-pool and SQL-logging properties are the standard levers if you
  need to turn on query logging locally — do it in a local copy, don't commit
  it enabled.

Every EJB facade extends `AbstractFacade<T>`
(`src/main/java/lk/gov/health/phsp/facade/`) and injects the entity manager
with `@PersistenceContext(unitName = "hmisPU")`. New facades must use the same
unit name.

## Web / JSF Descriptors

`src/main/webapp/WEB-INF/web.xml`:
- `javax.faces.PROJECT_STAGE` is `Production` — JSF's dev-mode diagnostics
  and stack traces are off. Don't flip this to `Development` in a commit.
- `primefaces.THEME` is `saga`.
- Faces Servlet is mapped to `/app/*`; the welcome file is `app/index.xhtml`.
- A Jersey servlet filter mounts the REST API under `/data/*` in addition to
  `ApplicationConfig`'s JAX-RS `@ApplicationPath("api")` (`/api/*`) — two
  separate REST entry points, don't assume only one exists.
- `session-timeout` is 120 minutes.
- The PrimeFaces file-upload filter writes to `/tmp/` — this is a real
  filesystem path on the deployment host, not configurable per-environment
  today.
- `ViewExpiredException` redirects to `/app/timeout.xhtml`.

`src/main/webapp/WEB-INF/faces-config.xml` registers four resource bundles
(`Bundle`, `BundleClinical`, `BundleDemo`, `BundleQuery` — backed by the
`.properties` files under `src/main/resources/`) and a single global
navigation rule (`welcome` → `/index.xhtml`). There is no
`managed-bean` section here — all beans are CDI (`@Named` + a scope
annotation), not JSF-managed-bean XML.

## Session-Scoped Bean Convention

Controllers live under `src/main/java/lk/gov/health/phsp/bean/` and follow a
consistent shape — see `ClaudeApiKeyController.java` or
`WebUserController.java` as reference:

```java
@Named
@SessionScoped
public class SomeController implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB
    private SomeFacade someFacade;
    @Inject
    private WebUserController webUserController;
    ...
}
```

- `@SessionScoped` (CDI, `javax.enterprise.context`) + `@Named` is the norm
  for controllers that track per-user/per-login state (current user, active
  API key, in-progress form data). `@ApplicationScoped` is used sparingly for
  genuinely shared, stateless-ish state (e.g. `ApplicationController`,
  `CommonController`'s lookup lists).
- Business/data-access logic sits in `@Stateless` or `@EJB`-injected classes
  under `lk.gov.health.phsp.ejb` / `lk.gov.health.phsp.facade`, not directly
  in the controller.
- The logged-in user is obtained via `webUserController.getLoggedUser()`,
  injected with `@Inject`, not re-derived from the HTTP session directly.

## Tunable Settings Without a Config Store

Because there is no `ConfigOption` table, feature-specific tunables are
resolved by small `@Stateless` "config" EJBs that check, in order: a JVM
system property, then an environment variable, then a hardcoded default.
This is the pattern to follow for any new tunable — see
`src/main/java/lk/gov/health/phsp/ejb/LetterImportConfig.java` and
`src/main/java/lk/gov/health/phsp/ejb/CryptoService.java`:

```java
private String resolve(String systemProperty, String envVar, String fallback) {
    String value = System.getProperty(systemProperty);
    if (value == null || value.trim().isEmpty()) {
        value = System.getenv(envVar);
    }
    return value != null && !value.trim().isEmpty() ? value.trim() : fallback;
}
```

Existing examples of this pattern:

| Setting | System property | Env var | Default |
|---|---|---|---|
| Letter-import default model | `dmis.letterImport.model` | `DMIS_LETTER_IMPORT_MODEL` | `claude-sonnet-4-6` |
| Letter-import blank-page threshold | `dmis.letterImport.blankThreshold` | `DMIS_LETTER_IMPORT_BLANK_THRESHOLD` | `0.005` |
| Letter-import render DPI | `dmis.letterImport.renderDpi` | `DMIS_LETTER_IMPORT_RENDER_DPI` | `150` |
| Letter-import max pages | `dmis.letterImport.maxPages` | `DMIS_LETTER_IMPORT_MAX_PAGES` | `500` |
| Letter-import retention days | `dmis.letterImport.retentionDays` | `DMIS_LETTER_IMPORT_RETENTION_DAYS` | `7` |
| Reversible-encryption passphrase | `dmis.crypto.secret` | `DMIS_CRYPTO_SECRET` | built-in placeholder — **must** be overridden in production |

Operators set the system property (`-Ddmis.crypto.secret=...` in
`domain.xml`'s JVM options) or the environment variable on the Payara host;
neither belongs in a properties file checked into the repo. When adding a new
tunable, add a row like the above to the owning class's Javadoc rather than a
separate config doc, and prefer this resolve-chain over inventing a new
mechanism.

Per-user secrets that must be recovered later (e.g. each user's personal
Claude API key in `UserClaudeApiKeyController`/`CryptoService`) are stored
encrypted in the database via `CryptoService`, not as plaintext columns and
not as application-wide config.

## Environment-Specific vs. Hardcoded — Where to Look

| Concern | Lives in |
|---|---|
| DB host/credentials/pool size | Payara `domain1` JDBC connection pool (`jdbc/dmis`) — not in this repo |
| JTA datasource JNDI name | `persistence.xml` (`jdbc/dmis`, hardcoded — same name every environment) |
| JSF project stage, PrimeFaces theme, session timeout | `web.xml` (hardcoded, same for every deploy) |
| Feature tunables (models, thresholds, page limits) | system property → env var → default, per `LetterImportConfig`-style EJB |
| Secrets (crypto passphrase, per-user API keys) | env var / system property (passphrase); encrypted DB column (per-user keys) |
| UI text / locale strings | `src/main/resources/Bundle*.properties` |
| Menu/composite-component markup | `resources/ezcomp/*.xhtml` — requires a full domain restart to pick up, see project CLAUDE.md |

There is currently no `.env` file, no Maven profile-driven property
substitution, and no `glassfish-resources.xml` in the repo — every
environment-specific value not covered by the table above must be set
directly on the target Payara domain.
