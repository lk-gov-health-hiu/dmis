---
name: start-issue
description: >
  Kickoff step for starting work on a GitHub issue in this DMIS repo: read the
  issue, clarify anything ambiguous with the user before writing code, create a
  correctly-named branch from an up-to-date master, and do initial
  investigation/planning (relevant XHTML/Java files, whether compilation is
  needed per this repo's CLAUDE.md rules). Distinct from a full issue-to-PR
  skill — this only covers kickoff, not implementation or the PR. Use when
  beginning work on a new issue: "start issue 190", "/start-issue 190", or
  "let's pick up issue 190".
disable-model-invocation: false
allowed-tools: Bash, Read, Grep, Glob
argument-hint: "<issue-number>"
---

# Start Issue

Kick off work on a GitHub issue against `lk-gov-health-hiu/dmis` — read it,
clarify it, branch it, and scope it — before any code is written.

## Step 1 — Read the Issue

```bash
gh issue view $0 --repo lk-gov-health-hiu/dmis --json number,title,body,labels,comments
```

Read the full body and any comments — requirements in this repo's issues are
often refined in follow-up comments, not just the original description.

## Step 2 — Clarify Ambiguity Before Coding

Do not start branching or investigating code until the request is
unambiguous. Common sources of ambiguity in this domain worth checking against
the issue text:

- **Outside Letter vs Our Letter** — if the issue touches letter entry or
  reports, confirm which `documentGenerationType` case(s) it covers, and
  whether legacy letters (`documentGenerationType = null`, treated as Outside
  Letter) are in scope.
- **Which institution field** — "letters entered by our unit" means filtering
  `DocumentHistory.institution`, not `toInstitution`. If the issue says
  "entered by" or "received by" ambiguously, ask.
- **Which date** — if the issue mentions filtering or reporting by date,
  confirm which date (system/entry date, document date, or received/stamp
  date — see `SearchFilterType`) is meant.
- **UI vs report vs data fix** — confirm whether the ask is a screen change,
  a print/report change, or a data correction, since these have different
  scopes in this codebase.

If the issue is clear on these points, skip this step and say so. If not,
ask the user directly rather than guessing — do not silently pick an
interpretation.

## Step 3 — Branch From Up-to-Date Master

```bash
git fetch origin
git checkout -b feature/issue-$0-<short-kebab-description> origin/master
```

Derive `<short-kebab-description>` from the issue title (lowercase, hyphens,
no punctuation). Confirm `git status` is clean before branching — if there
are uncommitted local changes on the current branch, stash them first so they
don't get carried onto the new branch's history.

## Step 4 — Initial Investigation & Planning

Before writing any code, scope the change:

1. **Find relevant files.** Grep for the domain terms in the issue (bean
   names, entity names, page names) across `src/main/webapp` (XHTML) and
   `src/main/java` (managed beans, entities, DAOs/facades).
2. **Classify the change**:
   - XHTML-only (markup, PrimeFaces attributes, layout, new report page
     using an existing bean's data) → no compilation needed; the user can
     hot-deploy by saving in NetBeans.
   - Any Java change (managed bean logic, new query, entity field, facade
     method) → compilation and deployment will be needed once implemented.
   - Composite components under `resources/ezcomp/*.xhtml` → flag that a
     full Payara domain restart is needed even after deployment, not just a
     redeploy.
3. **Check for PrimeFaces 14 pitfalls** if the change touches a
   `p:dataTable` with selection, `p:autoComplete`, `h:selectOneRadio`, or a
   print/printer button — this repo's CLAUDE.md documents breaking changes
   and conventions for each; re-read the relevant section before touching
   that markup.
4. **Look for a worked example.** This codebase tends to have a precedent
   for common patterns (e.g. print-layout panels in
   `institution/letter_received_registry.xhtml`, date-type filtering in
   `document/letter_search_by_date.xhtml`). Prefer following an existing
   pattern over inventing a new one.

## Step 5 — Summarize the Plan

Before coding, report back to the user with:

- Branch created (name) and confirmation it's based on current `origin/master`
- Any clarifications obtained in Step 2
- The list of files expected to change
- Whether the change is XHTML-only or requires a Java compile/deploy cycle
- Any relevant existing pattern being followed

Only proceed to implementation after this plan is presented (and, for
anything still uncertain, confirmed by the user).
