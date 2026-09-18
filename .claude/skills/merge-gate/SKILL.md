---
name: merge-gate
description: >
  Gate an open PR in lk-gov-health-hiu/dmis before merge: confirm it builds
  cleanly with Maven, review the diff for regressions and PrimeFaces 14
  API misuse, check XHTML-only changes against this repo's UI conventions,
  confirm deployment notes were followed, cross-check the PR description
  against the real diff, and scan for stray debug code or secrets. Use when
  asked to "gate PR #N for merge", "check this PR is safe to merge", "run
  merge-gate on #N", or before approving/merging any PR in this repo.
argument-hint: "<pr-number>"
---

# Merge Gate (DMIS)

Pre-merge checklist for this JSF/PrimeFaces 14 / JPA / MySQL app deployed on
Payara 5. **This skill never merges, approves, or requests changes on the
user's behalf** — it ends with a pass/block report so the user makes the
final call.

## 0. Load the PR

```bash
gh pr view <PR> --repo lk-gov-health-hiu/dmis --json title,body,files,commits
gh pr diff <PR> --repo lk-gov-health-hiu/dmis
gh pr checks <PR> --repo lk-gov-health-hiu/dmis
```

Note the changed files up front — the checks below branch on whether the
diff is XHTML-only, Java-only, or mixed (see CLAUDE.md's build-requirement
rule).

## 1. CI status

If `gh pr checks` shows a failing or still-pending check, say so and don't
proceed to a manual approval recommendation until it's resolved — a green
manual review doesn't substitute for a red CI run.

## 2. Build gate

Per this repo's CLAUDE.md: **JSF-only changes (XHTML only, no Java changes)
do not require compilation.** Everything else does.

- Diff touches only `src/main/webapp/**/*.xhtml` → compilation is optional;
  note this and move on.
- Diff touches any `.java` file (or `pom.xml`, `persistence.xml`, or other
  non-XHTML resource) → build it:

```bash
git fetch origin
gh pr checkout <PR> --repo lk-gov-health-hiu/dmis
mvn clean package
```

A failing `mvn package` is an automatic block — capture the relevant error
excerpt (redact any JDBC connection string, credential, or hostname before
quoting it anywhere) and stop here; don't attempt a fix as part of gating.

## 3. Regression / correctness review

Read the diff (or delegate to the `code-review` skill at medium/high effort
if it's large) looking specifically for:

- **PrimeFaces 14 selection API misuse** — `selectionMode` still on
  `p:column`, or `p:column selectionMode="multiple"` instead of
  `selectionBox="true"`. This is the most common regression class in this
  codebase's history; see CLAUDE.md's PrimeFaces 14 breaking-changes table.
- **`p:autoComplete dropdown="true"`** — against this project's plain
  type-ahead convention.
- Bootstrap button/alert/modal classes (`btn btn-*`) instead of the
  `ui-button-*` PrimeFaces equivalents.
- Raw `h1`–`h6` tags instead of `h:outputText` with a CSS class.
- Plain HTML `div`/`span` with an `id` used as an AJAX `update` target
  instead of `h:panelGroup` / `p:outputPanel`.
- Unescaped `&` in XHTML attribute values.
- Letter-domain regressions if `document/` or `institution/` XHTML/beans are
  touched: `toInstitution` vs `institution` confusion, an outside/our-letter
  toggle wired backwards, or a `documentGenerationType` equality filter that
  silently drops the ~12,900 legacy `null` rows (see CLAUDE.md's Letter
  Domain Model section).
- Date-type filtering reinventing a new enum instead of reusing
  `SearchFilterType`, or missing the `h.document.` prefix when querying
  `DocumentHistory` by `DOCUMENT_DATE`/`RECEIVED_DATE`.
- A print feature pointing `p:printer` straight at the on-screen
  `p:dataTable` instead of a dedicated hidden plain-table print panel.

Anything in this list found in the diff is a blocking finding — call it out
with file:line, don't wave it through as style.

## 4. Deployment-notes compliance

If the diff touches `resources/ezcomp/*.xhtml` (composite components like
`menu.xhtml`), confirm the PR description or commit message acknowledges
that a full Payara domain restart is required — a hot-redeploy claim for
this path is wrong and should be flagged.

If the PR describes itself as verified via `asadmin deploy` of a packaged
WAR, confirm a `mvn package` actually preceded it — hot-deploy-only
assumptions don't hold for that path per CLAUDE.md.

## 5. PR description matches the diff

Compare the PR body's stated scope against `gh pr diff --name-only`:

- Files touched that aren't mentioned or explained by the description
  (scope creep, or an accidental unrelated commit).
- Claims in the description (e.g. "no Java changes", "XHTML only") that the
  actual diff contradicts.
- A referenced issue number that doesn't match what the diff actually does.

## 6. Debug code / secrets scan

```bash
gh pr diff <PR> --repo lk-gov-health-hiu/dmis | grep -nE \
  "System\.out\.println|printStackTrace|console\.log|TODO.*remove|FIXME|password\s*=\s*[\"']|jdbc:mysql://.*@|BEGIN (RSA|PRIVATE) KEY"
```

Also check for hardcoded JDBC URLs/credentials that should instead be using
the `jdbc/dmis` datasource lookup, and for any left-in `debug="true"` or
similar PrimeFaces diagnostic attributes.

## 7. Report

Summarize as a short table or list:

| Check | Result |
|---|---|
| CI | pass / fail / pending |
| Build (`mvn package`) | pass / fail / skipped (XHTML-only) |
| Regression review | clean / findings (list them) |
| Deployment notes | followed / needs restart note / n/a |
| PR description vs diff | matches / discrepancy (describe) |
| Debug code / secrets | none found / found (list them) |

End with an explicit **ready to merge** or **blocked — reasons** verdict,
but leave the actual merge action to the user.
