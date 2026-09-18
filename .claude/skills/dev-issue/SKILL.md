---
name: dev-issue
description: >
  Take a GitHub issue from lk-gov-health-hiu/dmis through to an open pull
  request: read the issue, branch, implement the fix/feature in this
  JSF/PrimeFaces/JPA codebase, build and verify it, commit, push, and open a
  PR that closes the issue. Use when asked to "work on issue #N", "pick up
  issue #N", "fix issue #N end to end", or similar.
argument-hint: "<issue-number-or-description>"
---

# Dev Issue Workflow (DMIS)

End-to-end flow for turning a GitHub issue into a merged-ready PR in this
repo. Follow the steps in order; don't skip the verification step just
because a change "looks like" XHTML-only or Java-only.

## 1. Read the issue

```bash
gh issue view <n> --repo lk-gov-health-hiu/dmis --comments
```

If given a description instead of a number, search first:

```bash
gh issue list --repo lk-gov-health-hiu/dmis --search "<keywords>"
```

Note whether the issue is a bug (needs a root cause) or a feature/enhancement
(needs a design decision). Read any linked screenshots or reports carefully —
this system has a lot of domain-specific report/letter logic where the exact
wording of the request matters (e.g. "outside letter" vs "our letter",
`toInstitution` vs `institution` — see this repo's CLAUDE.md for the
letter-domain gotchas before touching anything under `document/` or
`institution/`).

## 2. Create the branch

Branch off `master`, never off a stale local branch:

```bash
git fetch origin
git checkout -b <type>/issue-<n>-<short-slug> origin/master
```

- `feature/issue-<n>-<slug>` for new functionality
- `fix/issue-<n>-<slug>` for bug fixes
- `chore/issue-<n>-<slug>` for dependency/build/config-only changes

Keep the slug short (3-5 words, hyphenated) and derived from the issue title.

## 3. Investigate the code

Use `Explore` (or plain `grep`/`find`) rather than guessing package layout.
Useful landmarks in this codebase:

- `src/main/java/lk/gov/health/phsp/entity` — JPA entities
- `src/main/java/lk/gov/health/phsp/bean` — session-scoped JSF managed beans
  (controllers)
- `src/main/java/lk/gov/health/phsp/facade` — JPA facades (CRUD access per
  entity)
- `src/main/java/lk/gov/health/phsp/ejb` — business/service logic
- `src/main/java/lk/gov/health/phsp/ws` — REST web services, grouped by
  domain (`letter`, `institution`, `user`, `area`, `auth`, `common`)
- `src/main/java/lk/gov/health/phsp/enums` — shared enums (e.g.
  `SearchFilterType` — reuse existing enums instead of inventing parallel
  ones; see CLAUDE.md)
- `src/main/webapp/<module>/*.xhtml` — views, grouped by module (`document`,
  `institution`, `webUser`, `reports`, `systemAdmin`, `national`, …)
- `src/main/webapp/resources/ezcomp` — composite components shared across
  pages (menu, etc.) — editing these needs a full domain restart, not just a
  redeploy (see step 6)

Identify which managed bean backs the page(s) involved, and which
entity/facade it talks to. Check for an existing similar screen/report to
copy conventions from (pagination, filters, selection, print layout) rather
than inventing a new pattern.

There is no automated test suite in this repository (no JUnit test sources
under `src/`) — verification is manual, against a running Payara instance.
Plan for that in step 5.

## 4. Implement

Follow this repo's CLAUDE.md conventions while writing the change:

- **PrimeFaces 14 selection API** — `selectionMode` goes on `p:dataTable`,
  `selectionBox="true"` goes on `p:column` (not the old
  `selectionMode="multiple"` on the column). See CLAUDE.md for the full
  table of selection attributes.
- **Buttons** — PrimeFaces classes only (`ui-button-success`,
  `ui-button-warning`, `ui-button-danger`, `ui-button-info`), never Bootstrap
  `btn btn-*`.
- **Headings** — no `h1`-`h6`; use `h:outputText` with a CSS class.
- **Bootstrap** — layout/spacing/flex utilities only (`row`, `col-*`, `mb-3`,
  `d-flex`, `gap-2`); never Bootstrap buttons/alerts/modals.
- **`p:autoComplete`** — never add `dropdown="true"`; this project uses
  plain type-ahead only.
- **`h:selectOneRadio`** — renders a bare `<table>`; don't fight it with
  flex classes, give it its own `styleClass` and override `margin`/`td`
  padding in a scoped `<style>` block.
- **Print views** — never `p:printer` the on-screen `p:dataTable` directly;
  build a separate hidden `d-none d-print-block` panel with a plain
  borderless `<table>` and point `p:printer` at that panel's id. See
  `institution/letter_received_registry.xhtml` /
  `institution/letter_receive_register.xhtml` for the pattern.
- **AJAX update targets** — never a plain `div`/`span` with an `id`; use
  `h:panelGroup` / `p:outputPanel`.
- **XHTML attributes** — escape `&` as `&amp;`.
- **User feedback** — `p:growl`/`p:messages`, never Bootstrap alerts.
- **Letter domain fields** — if the change touches `Document` /
  `DocumentHistory`, re-read the "Letter Domain Model" section of CLAUDE.md
  before deciding which field to filter/set (`institution` vs
  `toInstitution`, the `documentGenerationType` null-means-outside-letter
  legacy rule).

Keep the diff scoped to the issue. If you spot unrelated pre-existing bugs,
note them for a separate issue rather than folding them in.

## 5. Verify

Determine the redeploy path from what changed:

- **XHTML-only** (no Java changes): no compilation is required by this
  project's conventions. If you (Claude) are driving a live Payara instance
  yourself, still redeploy the packaged WAR to see it — see below. If the
  user is running the app themselves via NetBeans, tell them the change
  hot-deploys on save and no action is needed from you.
- **Changed `resources/ezcomp/*.xhtml`** (composite components like
  `menu.xhtml`): requires a **full Payara domain restart**, not just a
  redeploy — hot-redeploy will not pick it up.
- **Java changes** (entities, beans, facades, EJBs, web services): build and
  deploy:

  ```bash
  mvn clean package -DskipTests
  asadmin deploy --force <path-to-target>/dmis.war
  ```

  (Only run build/deploy commands yourself when asked to — per CLAUDE.md,
  Claude may compile/build/deploy when the user asks for it.)

Then exercise the actual change:
- Load the affected page(s) in a browser and walk through the flow the issue
  describes (or, for a bug, reproduce the original symptom first if
  possible, then confirm it's gone).
- For report/filter changes, check edge cases named in CLAUDE.md's letter
  gotchas — legacy rows with `documentGenerationType = null`, correct
  date-field mapping via `SearchFilterType` when querying `DocumentHistory`
  vs `Document`.
- Check the Payara server log for exceptions after deploy
  (`glassfish/domains/domain1/logs/server.log`) before declaring it working.

If verification surfaces a bug, fix it and re-verify — don't hand off a
change you haven't actually watched work.

## 6. Commit

```bash
git add <files>
git commit -m "<type>: <concise summary>

<optional body — why, not what>

Closes #<n>"
```

Use a conventional prefix consistent with this repo's history (`feat:`,
`fix:`, `chore:`, `docs:`). Keep the subject line under ~70 characters.
Include a `Closes #<n>` (or `Fixes #<n>`) trailer so the issue auto-closes
when the PR merges. Never bypass hooks (`--no-verify`) unless explicitly
told to.

## 7. Push and open the PR

```bash
git push -u origin <branch-name>

gh pr create --repo lk-gov-health-hiu/dmis \
  --base master \
  --head <branch-name> \
  --title "<concise title>" \
  --body "$(cat <<'EOF'
## Summary
- <what changed and why, 1-3 bullets>

## Test plan
- [ ] <how you verified it — page(s) exercised, scenarios checked>

Closes #<n>
EOF
)"
```

Target `master` (this repo does not use a separate long-lived development
branch). Report the PR URL back to the user; never merge it yourself — that
is the user's call.

## 8. If review comments come back

Address them as ordinary follow-up commits on the same branch and push again
— `gh pr create` doesn't need to be re-run, the existing PR picks up new
pushes automatically. Re-verify anything touched by the fix per step 5
before pushing.
