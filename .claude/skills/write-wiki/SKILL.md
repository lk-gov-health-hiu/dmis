---
name: write-wiki
description: >
  Write or update a page on the lk-gov-health-hiu/dmis GitHub wiki: feature
  guides for records staff and institution users, admin/deployment runbooks,
  and domain glossaries (e.g. Outside Letter vs Our Letter). Use when asked
  to "write a wiki page for X", "document this feature on the wiki", or
  "update the wiki" after a behavior change.
argument-hint: "<page-title-or-topic>"
---

# Write DMIS Wiki Page

DMIS (Document Management Information System) is used by institution clerks,
records officers, and ministry administrators — not developers. The wiki is
their manual for the live system, plus the runbook the on-call developer
reaches for during a restart or a bad deploy. Every page must reflect what
the deployed app actually does today, not what a ticket asked for.

## Before writing

1. Verify current behavior against the code, not the issue/PR description
   that prompted the page. Read the relevant `.xhtml` and backing managed
   bean, and check this repo's `CLAUDE.md` for domain gotchas (the Outside
   Letter/Our Letter distinction, `SearchFilterType`, PrimeFaces 14 selection
   API) before describing any of them.
2. Check whether a page already exists (`gh api
   repos/lk-gov-health-hiu/dmis/wiki` or browse the wiki sidebar) before
   creating a duplicate under a slightly different title.
3. Note the DMIS version/date the page describes at the top of the page, so
   a later reader can tell if it might be stale.

## Pick the right page type

**Feature guide** — for institution/records-office users. Explains a screen
or workflow: what it's for, how to do the task, what the messages mean.

**Admin/deployment runbook** — for whoever operates DMIS: Payara restarts,
`asadmin deploy` steps, the `jdbc/dmis` datasource, composite-component
(`resources/ezcomp/*.xhtml`) redeploy quirks. State the actual commands and
the actual restart requirement — don't soften "needs a full domain restart"
into "may require a restart."

**Domain glossary** — for a term whose plain-English meaning differs from
its behavior, or that's easy to get backwards. The Outside Letter/Our Letter
split in `letter.xhtml`, and `DocumentHistory.institution` vs. `toInstitution`,
are the canonical examples: explain the field, the trap, and give a "means
X, not Y" line a reader can scan for.

## Structure

Every page opens with:

```markdown
# Page Title

_Last verified: YYYY-MM-DD against DMIS <version/commit or branch>_

One paragraph: what this page covers and who it's for.
```

Then, depending on type:

**Feature guide**
1. **Overview** — what the feature does, in plain terms
2. **Who uses this** — role/screen it applies to (e.g. institution clerk vs.
   ministry admin)
3. **How to use it** — numbered steps, one action per step, bold UI labels
   ("Click **Save**", not "the save button should be clicked")
4. **What the messages mean** — the `p:growl`/`p:messages` text a user will
   actually see, and what to do about each
5. **Common mistakes** — e.g. picking "Outside Letter" when the letter was
   actually authored by your own institution
6. **Related pages** — link glossary/runbook pages instead of re-explaining

**Runbook**
1. **When to use this** — the trigger (deploy, incident, scheduled task)
2. **Preconditions** — access needed, what to check first
3. **Steps** — exact commands, in order, with expected output noted
4. **Rollback / if it goes wrong**
5. **Related pages**

**Glossary entry**
1. **Short definition** — one sentence
2. **Why it's confusing** — the trap, stated directly
3. **Where it lives in the code** — entity/field/enum name, no line numbers
   (those go stale; the code doesn't move by name as often as by line)
4. **Correct usage example**

## Writing rules

- Active voice, imperative mood for steps: "Select the institution", not
  "the institution should be selected."
- Write for the audience of that page type. A feature guide should not
  mention `documentGenerationType` or JPA; a runbook and a glossary entry
  should, because their reader is a developer or admin.
- Prefer a short table over prose when comparing options (e.g. Outside
  Letter vs. Our Letter field wiring) — it's scannable and harder to state
  inconsistently than paragraphs.
- Never paste SQL, stack traces, or full class listings into a feature
  guide. A runbook may include exact commands; keep them copy-pasteable
  (no line numbers, no placeholders left unexplained).
- Screenshots belong in feature guides where a screen layout matters; skip
  them in runbooks and glossary entries where they'd only need re-taking
  on every UI tweak.

## Keeping pages honest, not stale

- A wiki page is a claim about live behavior. If you're documenting a
  feature you just changed, write the page from the changed code/XHTML you
  just wrote, not from memory of the old behavior.
- When a change lands that contradicts an existing page (a field rename, a
  PrimeFaces API change, a new default), update that page in the same PR or
  issue that makes the change — don't leave it to a future cleanup pass.
- If you can't verify a claim against current code (e.g. a legacy behavior
  you can't easily reproduce), say so explicitly on the page rather than
  stating it as fact — "as of the last check on YYYY-MM-DD" is more useful
  to the next reader than confident silence.
- Prefer linking to the glossary/runbook over duplicating its content in a
  feature guide; duplicated explanations are the ones that drift out of
  sync first.

## Publishing

Wiki pages live in the separate `dmis.wiki` git repository (GitHub wikis are
their own repo, cloned as `<repo>.wiki.git`), not in this codebase. Clone or
pull it, add/edit the Markdown file, and push:

```bash
git clone https://github.com/lk-gov-health-hiu/dmis.wiki.git /tmp/dmis.wiki
cd /tmp/dmis.wiki
# create or edit Page-Title.md
git add "Page-Title.md"
git commit -m "Document <feature/runbook/term>"
git push
```

Use the page title with spaces replaced by hyphens as the filename (GitHub
wiki convention), and add a link to it from `Home.md` or the relevant
existing page's "Related pages" section so it's actually discoverable.
