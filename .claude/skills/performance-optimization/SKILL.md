---
name: performance-optimization
description: >
  Diagnose and fix performance issues in this JSF/PrimeFaces/JPA application —
  N+1 queries from EL expressions in data tables, session-scoped bean bloat,
  unnecessary full-page AJAX updates, and unindexed queries against the
  letter/document tables. Use when a page or report is slow, a register/report
  loads too much data, session memory looks bloated, or before/after tuning a
  query or bean.
---

# Performance Optimization

Measure first. Every fix below has a concrete "how to confirm it's actually
happening" step — do that before changing code, and re-measure after.

## Core Principles

1. **Query in bulk, not per row.** If an EL expression inside a `p:dataTable`
   column reaches through an association (`#{c.document.fromInstitution.name}`),
   assume it costs one query per row until proven otherwise.
2. **Load only what the page needs.** A JPQL `select h from DocumentHistory h`
   returning full entities to populate a report is fine for 50 rows; for a
   register scoped to an institution's whole history it can mean thousands of
   entities, each pulling in several `@ManyToOne` associations.
3. **Scope beans to the view, not the session.** A field that only matters
   while one page is open shouldn't outlive it.
4. **Prefer partial AJAX updates.** `update` only the panel that changed;
   don't re-render the whole form.
5. **Index what reports filter and sort by.** If a report's `WHERE`/`ORDER BY`
   touches a column with no index, it degrees to a table scan as data grows.

## How to Profile Before Optimizing

Don't guess — this app already has the hooks to see real SQL and real timing.

- **See the actual SQL EclipseLink issues.** `persistence.xml`
  (`src/main/resources/META-INF/persistence.xml`) has commented-out logging
  properties:
  ```xml
  <property name="eclipselink.logging.level.sql" value="FINE"/>
  <property name="eclipselink.logging.parameters" value="true"/>
  ```
  Uncomment these on a dev/test Payara domain (never on the production
  datasource) and reload the slow page. Count the queries in the server log —
  if a report that returns 200 rows issues 200+ extra `SELECT ... FROM
  institution WHERE id=?` lines, that's your N+1 confirmed, not suspected.
- **Time the controller method, not the whole request.** Wrap the JPQL call
  in the bean with a `System.currentTimeMillis()` before/after (this codebase
  already litters `System.out.println` debug lines through
  `LetterController` for exactly this kind of ad-hoc timing — follow the
  existing style, then remove the prints once you're done, don't leave new
  permanent logging behind).
- **Use MySQL's slow query log / `EXPLAIN`** against `jdbc/dmis` for anything
  filtering `document_history` or `document` by institution, date range, or
  `document_generation_type` — these are the columns every letter report
  filters on.
- **Check payload size, not just query time.** A page that's fast on the
  server but ships a 5,000-row `ui:repeat` to the browser (see the print
  panels below) will still feel slow — check the rendered HTML size, not only
  server logs.

Re-run the same measurement after each change. If a fix doesn't move the
number, revert it — don't stack unverified "optimizations."

## N+1 Queries From EL in Data Tables

`DocumentHistory` (`src/main/java/lk/gov/health/phsp/entity/DocumentHistory.java`)
carries close to a dozen `@ManyToOne` associations — `item`, `fromInstitution`,
`toInstitution`, `institution`, `fromUser`, `toUser`, `createdBy`,
`acceptedBy`, and `document` — most left at JPA's default (effectively eager)
fetch, with only a couple explicitly marked `LAZY`. The report-building
methods in `LetterController` (e.g. `fillLettersEnteredOutside`,
`fillLettersEnteredOur`, `fillLettersReceived`) run JPQL like:

```java
String j = "select h from DocumentHistory h "
        + " where h.retired<>:ret and h.historyType=:ht and h.institution=:ti "
        + " and (h.document.documentGenerationType=:dgt or h.document.documentGenerationType is null) ";
```

with **no `join fetch`**. The corresponding XHTML
(`institution/letters_entered_outside_registry.xhtml`) then walks associations
two and three levels deep per row: `#{c.document.fromInstitution.name}`,
`#{c.document.fromInsOrUser.name}`, `#{c.document.documentDate}` — and does it
**twice**, once in the on-screen `p:dataTable` and again in a hidden
`ui:repeat` print panel that iterates the *entire* unpaginated list. For a
report scoped to an institution's full letter history, that's every
association on every row, rendered twice.

Fixes, in order of effort:
- Add `join fetch h.document d` (and any other association actually rendered)
  to the JPQL so the associations come back in one query instead of N extra
  round trips.
- Where a column only needs an id or a name, consider a constructor/DTO
  projection (`select new ...(h.id, h.document.documentNumber, ...)`) instead
  of the full entity graph — this avoids loading associations the page never
  reads.
- Don't build the print `ui:repeat` from the same full in-memory list you
  used for the paginated screen table without checking size first; a report
  spanning the ~12,900 legacy pre-toggle letters (see the `CLAUDE.md` note on
  `documentGenerationType = null`) can mean a very large hidden table shipped
  to every browser that opens the page.

## View-Scoped vs Session-Scoped Bean Bloat

`grep -rl "@SessionScoped" src/main/java/lk/gov/health/phsp/bean/` currently
returns 21 beans (including `LetterController`, 3,800+ lines) and **zero**
`@ViewScoped` beans in that package. Every field on a session-scoped bean —
including `documentHistories`, a `List<DocumentHistory>` that can hold
thousands of fully-loaded entities after running one of the report methods
above — lives for the user's entire HTTP session, across every page they
navigate to next, not just the report page. On a multi-user Payara domain
this multiplies: N logged-in users each holding their own last-run report's
full result set in session memory.

When adding a new report or form:
- Default to `@ViewScoped` for anything whose state should die when the user
  leaves the page (a report's result list, a form's edit buffer). Only use
  `@SessionScoped` for state that's genuinely cross-page (the logged-in user,
  the logged-in institution — patterns already centralized in
  `webUserController`).
- If you must add a field to an existing `@SessionScoped` controller like
  `LetterController` (large report lists, filter state), null it out
  explicitly once the report is downloaded/printed/navigated away from,
  rather than letting it sit until the next report overwrites it.
- Don't reflexively convert existing `@SessionScoped` beans wholesale —
  that's a larger refactor with its own risk (session-scoped state other
  code may depend on). Fix it at the point where you're already touching the
  bean for a real feature or bug.

## Full-Page vs Partial AJAX Updates

Check every `<p:commandButton>`/`<p:ajax>` you touch or add:
- `update="@form"` or `update="@all"` re-renders far more than necessary when
  only one panel changed. Scope `update` to the specific `h:panelGroup`/
  `p:outputPanel` id that actually needs to refresh.
- Remember the JSF/XHTML rule from this repo's `CLAUDE.md`: never put an
  `id` on a bare `div`/`span` and target it — use `h:panelGroup` or
  `p:outputPanel` so it's a real AJAX render target, and so you *can* scope
  updates narrowly instead of falling back to `@form`.
- For a `p:dataTable` filter/sort that only changes the table's rows, update
  the table's own id, not its parent panel grid — re-rendering the
  surrounding layout on every keystroke of a column filter is wasted work.
- Avoid `process="@all"` on frequent interactions (row filters, autocomplete)
  — it re-validates the entire form's inputs on every request.

## Unindexed Queries Against Letter/Document Tables

`Document` and `DocumentHistory` have plain `@Table` annotations with no
`indexes` attribute — indexing is whatever exists in the live MySQL schema,
not something declared alongside the entity. Every letter report filters on
some combination of:
- `document_history.institution_id` (always — see the `CLAUDE.md` gotcha:
  filter reports by `institution`, not `toInstitution`)
- `document_history.history_type`, `document_history.retired`
- `document_history.created_at` or `document.document_date` /
  `document.received_date` (`SearchFilterType`-driven date filtering)
- `document.document_generation_type`

A report `WHERE` clause combining several of these (as
`fillLettersEnteredOutside`/`fillLettersEnteredOur` do) needs a composite
index covering the equality filters plus the range filter last
(`institution_id, history_type, retired, created_at`, roughly), not separate
single-column indexes. Confirm with `EXPLAIN` against the real `jdbc/dmis`
schema before adding an index — don't guess at column order.

## This Repo's Actual Hot Paths

The Letters Entered by the Unit family of reports
(`institution/letter_receive_register.xhtml`,
`institution/letters_entered_outside_registry.xhtml`,
`institution/letters_entered_our_registry.xhtml`, and the older
`letter_received_registry.xhtml`) are the highest-traffic, highest-data-volume
screens in the app: they query the full `DocumentHistory` history for an
institution over an arbitrary date range with no query-level pagination
(`AbstractFacade.findByJpql(jpql, params, TemporalType)` fetches the whole
result set; `p:dataTable`'s paginator only paginates what's already in
memory). `AbstractFacade` already has an overload that takes a `maxRecords`
limit and a `findRange(int[] range)` method for `setFirstResult`/
`setMaxResults`-style paging — the report methods in `LetterController`
don't currently use either. When performance work touches these reports,
that's the first thing to check: is the query already unbounded, and would
database-level pagination (or at least a sane date-range cap) help before
reaching for indexes or fetch-join tuning.
