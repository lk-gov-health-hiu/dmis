---
name: database-guide
description: >
  Guide to DMIS's database and JPA layer — locating entity classes and persistence.xml,
  the Document/DocumentHistory/Institution relationships, the Outside Letter/Our Letter
  gotcha, safe JPQL/Criteria query patterns for this schema, and cautions around the
  production jdbc/dmis datasource. Use when writing or debugging queries, adding a report,
  investigating letter/document data, or touching any JPA entity or facade.
user-invocable: true
---

# DMIS Database & JPA Guide

DMIS (Document Management Information System) persists letters, documents and
institutions through JPA (EclipseLink) on Payara 5, backed by MySQL. There is
**one persistence unit**, historically named `hmisPU` (a name carried over
from an earlier project — don't be confused by it, it is still DMIS's unit).

## Where things live

- **Entities**: `src/main/java/lk/gov/health/phsp/entity/*.java` — one class
  per `@Entity`. Key ones: `Document`, `DocumentHistory`, `Institution`,
  `WebUser`, `Item`, `Area`, `LetterImportBatch`, `LetterImportItem`,
  `UserPrivilege`, `Upload`, `Sms`, `ApiKey`/`ApiRequest`, `UserClaudeApiKey`.
- **Enums**: `src/main/java/lk/gov/health/phsp/enums/*.java` — e.g.
  `DocumentGenerationType`, `DocumentType`, `HistoryType`, `SearchFilterType`,
  `InstitutionType`.
- **Facades** (one per entity, session beans wrapping `EntityManager`):
  `src/main/java/lk/gov/health/phsp/facade/*Facade.java`, all extending
  `AbstractFacade<T>` (`src/main/java/lk/gov/health/phsp/facade/AbstractFacade.java`).
- **persistence.xml**: `src/main/resources/META-INF/persistence.xml`. JTA
  data source: `jdbc/dmis`. Schema-generation is explicitly commented out
  with the note "DISABLED while pointed at production: do not let
  EclipseLink ALTER prod schema" — never re-enable it against `jdbc/dmis`.
- **Managed beans / controllers** that build queries and drive JSF pages:
  `src/main/java/lk/gov/health/phsp/bean/*.java` (e.g. `LetterController.java`
  is the main letters/documents controller and the best source of real
  query examples).
- Before writing a new query, `grep` the bean package for the entity name —
  there is almost always an existing, working query to copy the shape of.

## Core domain model

```
Institution ──< WebUser (many users belong to one institution)
Institution ──< Document (institution, institutionUnit, fromInstitution,
                           toInstitution, currentInstitution, createdInstitution)
Document ──< DocumentHistory (document)
DocumentHistory ── Institution (fromInstitution, toInstitution, institution)
DocumentHistory ── WebUser (fromUser, toUser, createdBy, acceptedBy)
```

- `Document` is the letter/file record itself: names, dates
  (`documentDate`, `receivedDate`, `createdAt`), sender/recipient
  (`fromInstitution`/`fromWebUser`, `toInstitution`/`toWebUser`), and
  `documentGenerationType` (see gotcha below).
- `DocumentHistory` is the append-only trail of what happened to a
  `Document` (created, transferred, received, completed...), typed by the
  `HistoryType` enum (e.g. `Letter_Created`, `File_Institution_Transfer`).
  Reports over "what came through this institution" almost always query
  `DocumentHistory`, not `Document` directly.
- Both entities (like most in this codebase) carry a **soft-delete**
  `retired` boolean plus `retiredBy`/`retiredAt`/`retireComments`, and audit
  fields `createdBy`/`createdAt`. **Always filter `retired` explicitly** —
  there is no global `@Where` clause doing it for you. The idiomatic form in
  this codebase is `h.retired<>:ret` with `ret=true` bound (i.e. "not
  retired"), matched against a `boolean`, not `Boolean` — see
  `LetterController.fillLettersEnteredOutside()` for a worked example.

### Outside Letter vs Our Letter — do not get this backwards

This is the single easiest mistake to make in this schema (see also
`CLAUDE.md`). The `letter.xhtml` entry toggle
(`letterController.outsideLetter`, default `true`) sets
`Document.documentGenerationType`:

| Toggle | `documentGenerationType` | `DocumentHistory.toInstitution` |
|---|---|---|
| Outside Letter | `Received_by_institution` | the entering institution (received *by* them) |
| Our Letter | `Created_by_institution` | whatever institution the user names as recipient — can be **any** institution |

- `DocumentHistory.institution` is always the entering institution,
  regardless of the toggle. **Filter on `institution`** when a report means
  "letters entered by our unit" — filtering on `toInstitution` instead lets
  another institution's own "Our Letter" addressed to you leak into what
  should be your own "entered by us" report.
- ~12,900 legacy rows predate the toggle and have `documentGenerationType =
  null`. Treat `null` as "outside letter" — `(h.document.documentGenerationType
  = :dgt OR h.document.documentGenerationType IS NULL)` — never an
  equality-only filter, or the historical report comes back nearly empty.
- Working reference implementation:
  `LetterController.fillLettersEnteredOutside()` /
  `fillLettersEnteredOur()` in
  `src/main/java/lk/gov/health/phsp/bean/LetterController.java`.

### Date-type filtering — reuse `SearchFilterType`

Don't invent a new "which date field" enum for a UI filter. Reuse
`lk.gov.health.phsp.enums.SearchFilterType` (`SYSTEM_DATE` /
`DOCUMENT_DATE` / `RECEIVED_DATE`, exposed as
`commonController.searchFilterTypes`). `getCode()` returns the `Document`
field name (`createdAt` / `documentDate` / `receivedDate`). When the query
root is `DocumentHistory` rather than `Document`, `SYSTEM_DATE` maps to
`h.createdAt` (the history row's own timestamp) but the other two need the
`h.document.` prefix — see `document/letter_search_by_date.xhtml` and
`LetterController`'s `fillLettersEnteredOutside`/`fillLettersEnteredOur` for
the exact branch.

## Writing queries safely

Facades extend `AbstractFacade<T>`, which wraps `EntityManager` and exposes
JPQL helpers used throughout the bean layer:

- `facade.findByJpql(String jpql, Map<String,Object> params)` — typed to the
  facade's entity, binds `Date` params with `TemporalType.DATE` by default.
- `facade.findByJpql(String jpql, Map<String,Object> params, TemporalType tt)`
  — pass `TemporalType.TIMESTAMP` for any field that's a `@Temporal(TIMESTAMP)`
  (e.g. `createdAt`) or the `between` comparison silently truncates to the day.
- `facade.findObjects(...)` / `findLongList(...)` — for DTO/scalar projections
  and counts instead of full entities.
- Build JPQL as parameterized strings (`:paramName`) with a `Map`, appending
  optional clauses conditionally (see the `if (filter instanceof Institution)`
  pattern in `LetterController`) — **never string-concatenate a user-supplied
  value directly into JPQL**.
- `Institution`/`WebUser` are often interchangeable as a "from/to" party via
  the `Nameable` interface (`fromInsOrUser`/`toInsOrUser` transient
  properties on both `Document` and `DocumentHistory`) — when filtering,
  branch on which concrete type the UI bound and bind the matching entity
  column (`fromInstitution` vs `fromWebUser`), not a single generic column.
- Prefer adding a method to the relevant `*Facade`/bean over a raw
  `EntityManager` in a new place — keeps transaction/persistence-context
  handling consistent with the rest of the app.

## The production datasource — treat with care

`jdbc/dmis` is **production MySQL**, wired directly into `persistence.xml`.
There is no separate reporting replica configured in this repo.

- Never run destructive or schema-altering SQL (`DELETE`, `UPDATE` without a
  tight `WHERE`, `DROP`, `ALTER`, or re-enabling EclipseLink schema
  generation) against `jdbc/dmis` directly, including from a scratch JPQL
  query or a one-off `asadmin` DB console session.
  `src/main/webapp/resources/sql/delete_data.sql` exists in-repo for a
  reason — treat any similarly-shaped ad hoc statement as something to run
  only against a local/dev copy, never live. Retired-row corrections or
  backfills belong through the entity's `retired`/`retiredBy`/`retiredAt`
  fields via JPA, not a raw `UPDATE`, so the audit trail stays intact.
- For read-only investigation, prefer scoping with `retired<>true` and a
  narrow date range rather than unbounded scans of `Document`/
  `DocumentHistory`, which are the largest tables in the schema.
- For anything experimental — testing a new JPQL query's shape, checking a
  migration, load-testing a report — restore a copy of the schema to a local
  MySQL instance and point a local `jdbc/dmis` pool (or a temporary
  persistence unit) at that instead of the production JNDI resource. Never
  add write-capable credentials for production to a local tool, script, or
  test.
- If you must inspect production data read-only, prefer existing JSF reports
  and controller queries (already scoped, already filtered by `retired`)
  over freehand SQL against the live database.
