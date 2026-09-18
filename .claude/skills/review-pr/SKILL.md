---
name: review-pr
description: >
  Review a GitHub pull request on this repo. Fetches the PR diff with gh,
  checks it against this repo's own conventions (PrimeFaces 14 selection API,
  button classes, JSF/XHTML rules, letter domain gotchas), flags correctness
  issues, and posts a review (comment/approve/request-changes) via gh pr review.
allowed-tools: Read, Grep, Glob, Bash
argument-hint: "<pr-number-or-url>"
---

# Review PR

Review a pull request against the conventions documented in this repo's own
`CLAUDE.md`, then post the review with `gh`.

## Arguments

- `$0` — PR number or URL (required). Repo is `lk-gov-health-hiu/dmis` unless
  the URL says otherwise.

## Steps

### 1. Load the PR

```bash
gh pr view $0 --repo lk-gov-health-hiu/dmis --json number,title,body,baseRefName,headRefName,files
gh pr diff $0 --repo lk-gov-health-hiu/dmis
```

Note the base/head branches and the full list of changed files before reading
the diff — a file that isn't in the list shouldn't be assumed changed just
because the diff context mentions it.

### 2. Re-read this repo's CLAUDE.md

Re-read the project root's `CLAUDE.md` before judging the diff — don't rely on
memory of it. It is the source of truth for every rule below.

### 3. Check XHTML changes against the PrimeFaces 14 API

For every changed `.xhtml` file, grep the diff for these breaking-change
patterns:

- `selectionMode="multiple"` on a `p:column` — wrong in v14; it belongs on
  `p:dataTable`, paired with `selectionBox="true"` on the column(s).
- `rowSelectMode` — renamed to `selectionRowMode` in v14.
- A `p:dataTable` with `selection="#{...}"` and `rowKey="#{...}"` but no
  `selectionMode` attribute, or a `p:column` still carrying `selectionMode`
  instead of `selectionBox`.
- A `p:columnGroup` header row missing `selectionBox="true"` on its own
  selection column when the body column has it.

### 4. Check UI styling conventions

- `class="btn btn-*"` (Bootstrap) on any `h:commandButton`/`p:commandButton` —
  should be `ui-button-success` / `ui-button-warning` / `ui-button-danger` /
  `ui-button-info`.
- `<h1>`–`<h6>` tags anywhere in new markup — this is an ERP screen, not a
  website; heading-like text should be `h:outputText` with a CSS class.
- Bootstrap grid/spacing/flex utilities (`row`, `col-*`, `mb-*`, `d-flex`,
  `gap-*`) are fine to keep; Bootstrap button/alert/modal classes are not —
  those should route through PrimeFaces components (`p:commandButton`,
  `p:growl`/`p:messages`, `p:dialog`).
- `p:autoComplete` with `dropdown="true"` — this project's convention is a
  plain type-ahead with no dropdown trigger; flag it for removal.
- `h:selectOneRadio` wrapped in Bootstrap flex classes (`d-flex`, etc.) — it
  renders as a bare `<table>` and the flex classes do nothing; it needs its
  own `styleClass` with a scoped `<style>` override instead.
- A "Print" button pointed at `p:printer target="..."` where the target is
  the on-screen `p:dataTable` itself, rather than a dedicated hidden
  `d-print-block` panel with a plain `<table>`. Compare against
  `institution/letter_received_registry.xhtml` or
  `institution/letter_receive_register.xhtml` if unsure what the expected
  pattern looks like.

### 5. Check general JSF/XHTML rules

- A plain `<div id="...">` or `<span id="...">` used as an `update=`/
  `render=` target for `f:ajax`/`p:ajax` — must be a `h:panelGroup` or
  `p:outputPanel` (or another JSF component) instead.
- Unescaped `&` in an attribute value (should be `&amp;`).
- New user-facing errors/success messages not routed through
  `p:growl`/`p:messages`.

### 6. Check the letter domain model, if touched

If the diff touches `Document`, `DocumentHistory`, `letterController`, or any
letter report/query:

- `outsideLetter` (Outside Letter vs Our Letter) wiring: Outside Letter →
  `documentGenerationType = Received_by_institution` with
  `DocumentHistory.toInstitution` = the entering institution; Our Letter →
  `Created_by_institution` with `toInstitution` = the chosen recipient
  (can be any institution).
- Any "letters entered by our unit" query/report must filter on
  `DocumentHistory.institution`, never `toInstitution` — filtering on
  `toInstitution` leaks other institutions' outgoing "Our Letter" entries
  addressed to this unit into the report.
- Any `documentGenerationType` equality filter must also treat `null` as
  "outside letter" (pre-toggle legacy data), not exclude it — an
  equality-only filter empties out virtually the entire historical report.
- Date-filter UI should reuse `lk.gov.health.phsp.enums.SearchFilterType`
  (`SYSTEM_DATE`/`DOCUMENT_DATE`/`RECEIVED_DATE`) rather than a new enum, and
  when querying `DocumentHistory` remember `DOCUMENT_DATE`/`RECEIVED_DATE`
  need the `h.document.` prefix while `SYSTEM_DATE` maps to `h.createdAt`
  directly. See `document/letter_search_by_date.xhtml` for the canonical
  usage.

### 7. General correctness pass

Beyond the repo-specific checklist, read the actual diff (not just the file
names) for the usual things:

- Logic errors, off-by-one/null-handling bugs, and inverted conditionals.
- New JPA queries that could N+1, or that filter on the wrong entity field
  (see the letter-domain gotchas above — this bug class is easy to reintroduce
  even outside the letter feature).
- Java changes that will require a rebuild — cross-check against
  "JSF-only changes... do not require compilation" from CLAUDE.md when
  judging whether the PR's own testing notes make sense.
- Dead code, leftover debug output (`System.out.println`, commented-out
  blocks), and missing null checks on newly introduced fields.

### 8. Summarize findings before posting

List every issue found as `file:line — issue`, grouped by severity
(blocking vs. nit). Decide the overall verdict:

- **Approve** — no blocking issues.
- **Comment** — nits only, nothing that blocks merge.
- **Request changes** — any breaking-API misuse (PrimeFaces 14 selection,
  Bootstrap button classes) or a letter-domain correctness bug.

### 9. Post the review

```bash
gh pr review $0 --repo lk-gov-health-hiu/dmis --approve -b "..."
# or
gh pr review $0 --repo lk-gov-health-hiu/dmis --comment -b "..."
# or
gh pr review $0 --repo lk-gov-health-hiu/dmis --request-changes -b "..."
```

For issues tied to a specific line, prefer inline comments over stuffing
everything into the review body:

```bash
gh api repos/lk-gov-health-hiu/dmis/pulls/$0/comments \
  -f body="..." -f commit_id="$(gh pr view $0 --repo lk-gov-health-hiu/dmis --json headRefOid --jq .headRefOid)" \
  -f path="<file>" -F line=<line> -f side=RIGHT
```

Keep the review body itself short — a summary and the verdict — and let
inline comments carry the file:line detail found in step 8.
