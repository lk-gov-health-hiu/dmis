---
name: dev-issue-unattended
description: >
  Take a GitHub issue in the DMIS repo (lk-gov-health-hiu/dmis) all the way
  from investigation to an open pull request with NO pauses for user
  confirmation — investigate, decide the approach, implement, build, verify,
  commit, push, and open the PR autonomously, logging every judgment call
  for after-the-fact review instead of asking as you go. Use ONLY when the
  user has said up front they will be unreachable (away, asleep, offline)
  and wants the issue shipped as a mergeable PR without them. For normal,
  attended work — where you'd naturally stop to confirm the approach or ask
  which record to test with — just work the issue directly instead; this
  skill exists specifically to remove those pauses, so only reach for it
  when nobody is present to answer them.
---

# Unattended Issue-to-PR (DMIS)

This skill runs the full issue lifecycle — investigate, implement, verify,
commit, push, open PR — without stopping to ask the user anything, by
replacing every point that would normally be a question with an
evidence-based decision that gets written down instead. Invoking this skill
is the user's authorization for every commit, push, PR, and (if needed)
issue-comment step below — do not re-confirm any of them individually.

It never authorizes the things listed under **Hard limits**. Those are not
judgment calls this skill makes more confidently than a human would — they
are steps a human has to be present for, full stop. Hit one and you stop
the run for that issue, leave a clear trail of why, and end there.

## Hard limits — never bypassed, no matter how confident

- **Never merge a PR.** This skill's job ends at "PR is open and passes its
  own checks" — merging is always a human decision.
- **Never push directly to `master`**, and never force-push, `reset --hard`,
  or skip hooks (`--no-verify`) on any shared branch. Everything goes
  through a feature branch and a PR, exactly as in attended work.
- **Never write test data, or any other mutation, against `jdbc/dmis`.**
  Per this repo's own `persistence.xml` and CLAUDE.md, this datasource is
  **production MySQL** — there is no separate staging or disposable dev
  database. A local Payara `domain1` you deploy to for verification still
  talks to real institutions', officers', and letters' data the moment it
  touches the database layer. So:
  - Verification is **read-only by default**: browse existing screens,
    run existing searches/reports, inspect real records that already
    exist, read server logs. This is almost always enough to confirm a
    fix — the app already holds years of real letter/document data to
    exercise a change against.
  - If, and only if, a fix genuinely cannot be verified without creating
    or changing a record (e.g. a brand-new entry form), that is a stop:
    post what you tried and what's blocking verification, and end the
    run for that issue rather than inventing production test data.
  - Never touch the commented-out `schema-generation` property in
    `persistence.xml` or run any DDL against this datasource — the
    in-file comment ("do not let EclipseLink ALTER prod schema") is
    already the project's own hard limit here; this skill just inherits
    it.
- **Never restart the Payara domain unattended** if there's any doubt it's
  a shared/production instance rather than an isolated local dev copy. A
  `resources/ezcomp/*.xhtml` change needs a full domain restart to take
  effect (see CLAUDE.md) — restarting a domain that government staff may
  currently be using is an availability decision, not a code decision.
  When unsure, ship the code change and PR, note in the PR that a domain
  restart is still needed to activate it, and let a human do the restart.
- **Never write a security/authentication/authorization change
  autonomously** (login, session, role/permission checks, `WebUser`
  credential handling) — flag it and stop instead.
- **Never put real institution names, correspondent/officer personal
  names, or letter subject/content excerpts into a public GitHub issue,
  PR, or commit.** Describe problems by page, field, entity, and method
  name instead (e.g. "the institution search on `letter.xhtml`'s
  `p:autoComplete`", not the actual institution that was searched for).
  This applies even when reproducing a bug required looking at a real
  record — describe what you saw structurally, don't quote it.
- **Never resolve a genuinely ambiguous requirement by silently picking
  one reading.** If the issue, the code, and git history all fail to make
  the right choice clear, that is a stop, not a coin flip — see step 3.

If a hard limit fires, post a short comment on the issue explaining exactly
what's blocking (referencing the specific limit above), leave the branch
and any WIP commit pushed for a human to pick up, and end the run for that
issue. Don't guess past any of these no matter how strong the evidence
looks — that's what makes the rest of the run safe to run unattended at
all.

## 1. Resolve the issue

Accept a bare issue number or a full
`https://github.com/lk-gov-health-hiu/dmis/issues/<n>` URL. Confirm it
exists and pull its details:

```
gh issue view <n> --repo lk-gov-health-hiu/dmis
```

If `gh` reports the issue doesn't exist, or the number/URL doesn't parse,
that's a "could not resolve" outcome — report it and stop; there's no issue
to work from.

Branch from current `master`, following this repo's naming convention —
`<type>/issue-<n>-<short-slug>` (`feature/`, `fix/`, or `chore/` matching
the issue's nature, e.g. `feature/issue-184-letters-entered-outside-our-reports`):

```
git fetch origin
git checkout -b <type>/issue-<n>-<slug> origin/master
```

## 2. Investigate

Read the issue in full. Use `Explore` for anything spanning more than a
couple of files. In this codebase that typically means locating:

- the relevant JSF page(s) under `src/main/webapp/**/*.xhtml`
- the backing managed bean (session-scoped controller) it binds to
- the JPA entities involved — for anything letter/document-related, this
  is almost always `Document` and `DocumentHistory`; re-read the Letter
  Domain Model gotchas in CLAUDE.md before touching either (the
  `documentGenerationType` Outside/Our-Letter split, `institution` vs
  `toInstitution` on `DocumentHistory`, and the ~12,900 legacy rows with
  `documentGenerationType = null` that must be treated as "outside
  letter," not excluded)

For bug reports, prefer establishing root cause by reading code and
history first — `git log -p -S<term>`, `git blame`, related closed
issues/PRs — before falling back to live reproduction against real data
(which, per the hard limits, must stay read-only).

If investigation can't identify which pages/entities are even involved,
that's an **insufficient issue description** — see below, don't guess.

### Insufficient issue description

When the issue itself doesn't give enough to proceed — no page/screen
named, no clear current-vs-expected behavior, a report request with no
stated filters/columns/date-type — don't stall silently:

1. Look up the reporter: `gh issue view <n> --repo lk-gov-health-hiu/dmis --json author --jq '.author.login'`.
2. Post a comment starting with `@<that-login>` asking specifically for
   what's missing (page/menu path, current vs. expected behavior, which
   `SearchFilterType` the date filter should use, etc.) — grounded in what
   you actually checked, not a generic template.
3. Leave the issue open, no code change beyond the comment, and end the
   run for that issue.

## 3. Decide the approach — document, don't ask

Where attended work would pause to confirm a direction, instead:

1. Gather the evidence a confirmation would have needed: similar existing
   pages/reports to model the fix on, related issues/PRs, any code comment
   explaining prior intent.
2. Pick the option the evidence best supports. If two readings are both
   plausible and nothing in the code or issue favors one — that's the
   **genuinely ambiguous** hard limit: stop, post the two readings and why
   neither wins, and end the run.
3. Write the reasoning down now, in a form that survives into the commit
   body and PR description (steps 9 and 10) — since no one is reviewing a
   plan before the fact, the trail has to carry that weight afterward.

## 4. Implement

Follow this repo's conventions exactly — they're not optional style
preferences, several are breaking-change fixes for PrimeFaces 14:

- **Row selection**: `selectionMode` goes on `p:dataTable`, not
  `p:column`; use `<p:column selectionBox="true"/>` instead of the old
  `selectionMode="multiple"` on the column.
- **Buttons**: `ui-button-success` / `-warning` / `-danger` / `-info` on
  `p:commandButton`, never Bootstrap `btn btn-*`.
- **Headings**: no `h1`–`h6`; use `h:outputText` with a CSS class.
- **Bootstrap**: fine for grid/spacing/flex utilities (`row`, `col-*`,
  `mb-3`, `d-flex`, `gap-2`); not for buttons/alerts/modals — use the
  PrimeFaces equivalents.
- **`p:autoComplete`**: no `dropdown="true"` — plain type-ahead only, per
  this project's convention.
- **`h:selectOneRadio`**: renders a bare `<table>`; don't fight it with
  flex classes — give it its own `styleClass` and override `margin`/`td`
  padding in a scoped `<style>` block instead.
- **Print views**: build a dedicated hidden plain-`<table>` panel
  (`d-none d-print-block`) and point `p:printer` at that, never at the
  on-screen `p:dataTable` — see `institution/letter_received_registry.xhtml`
  or `institution/letter_receive_register.xhtml` for the pattern.
- **AJAX update targets**: only `h:panelGroup` / `p:outputPanel` /
  similar, never a plain `div`/`span` with a bare `id`.
- **Date-type filters**: reuse `lk.gov.health.phsp.enums.SearchFilterType`
  (`SYSTEM_DATE`/`DOCUMENT_DATE`/`RECEIVED_DATE`) rather than inventing a
  new enum — see `document/letter_search_by_date.xhtml`. Remember the
  `h.document.` prefix is needed for the latter two when querying
  `DocumentHistory` instead of `Document` directly.
- Escape `&` as `&amp;` in every XHTML attribute value.
- Use `p:growl`/`p:messages` for feedback, never a Bootstrap alert.

Prefer the smallest change that fixes the issue's actual root cause over a
broader refactor — there's no one here to catch scope creep before it's
pushed.

## 5. Build and deploy for verification

Determine whether the change is JSF-only (XHTML only, no Java touched) or
touches Java:

- **JSF-only**: no compile step required per CLAUDE.md. Still worth a
  quick well-formedness pass (the XHTML must at least parse) before moving
  on, since there's no NetBeans session live-reloading this session's
  edits.
- **Java changed**: compile and deploy the packaged WAR yourself —
  `mvn -q package` then `asadmin deploy` to `domain1` — since there is no
  human running NetBeans to hot-deploy for you here. Tail the server log
  for deployment errors before treating the build as good.
- **`resources/ezcomp/*.xhtml` changed** (e.g. `menu.xhtml`): this needs a
  full domain restart to pick up, not just a redeploy — see the domain
  restart hard limit above before doing that unattended.

## 6. Verify

Exercise the change against real, already-existing data — read-only, per
the hard limits:

- Navigate the affected screen(s) and confirm the behavior described in
  the issue is actually fixed.
- For a report/query change, run it against a date range or institution
  that already has real rows and sanity-check the output (counts, which
  rows appear/don't) rather than trusting the query by inspection alone.
- For anything touching `DocumentHistory`, specifically confirm the fix
  respects the `institution` vs `toInstitution` distinction and the
  `documentGenerationType = null` legacy-row handling — these are the two
  gotchas most likely to silently pass a shallow check and fail on real
  data.
- If verification would require creating or mutating a record, stop per
  the hard limit above instead of improvising test data in production.

## 7. Iterate

Fix, rebuild, re-verify until the issue's behavior is actually resolved.
If three attempts don't converge on a working fix, that's a sign the
approach itself needs rethinking, not more guessing — stop, write up what
was tried and what's still wrong, and end the run.

## 8. Self-review before pushing

There's no human reviewing this diff before it ships, and this repo has no
CI to catch anything either — so run one yourself before the first push:

1. Run `/code-review` (or the equivalent review skill) against the working
   diff at `medium` effort, bumped to `high` if the change touches
   `Document`/`DocumentHistory`, institution data, or `WebUser`/session
   code.
2. Confirmed, in-scope bugs: fix them and fold the fix into the same
   change, then re-verify (step 6).
3. Valid findings outside this issue's scope: don't expand the diff to fix
   them — file a separate issue describing the pattern instead (filing an
   issue is covered by this skill's standing authorization).
4. Low-confidence/stylistic notes: leave them out of the diff, but list
   them under the PR's "Decisions made without approval" section (step
   10) so a human can glance at them later.

## 9. Commit

Match this repo's existing commit style — a short, present-tense
conventional-commit-style subject (`feat: …`, `fix: …`, `chore: …`,
`docs: …`), body explaining why, not just what. Fold step 3's documented
reasoning into the commit body so it isn't only visible in the PR. Stage
only the files this change actually touches — never `git add -A`/`.`.

```
git add <specific files>
git commit -m "$(cat <<'EOF'
fix: <short summary of the actual change>

<why — the reasoning from step 3, and anything non-obvious about the fix>

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
git push -u origin <type>/issue-<n>-<slug>
```

## 10. Open the PR

```
gh pr create --repo lk-gov-health-hiu/dmis --base master \
  --head <type>/issue-<n>-<slug> \
  --title "<short title>" \
  --body "$(cat <<'EOF'
Closes #<n>

## Summary
<what changed and why, 2-4 bullets>

## Decisions made without approval
<every step-3 judgment call and step-8 triage note, one line each — or
"None" if the fix was unambiguous>

## Test plan
<what was verified in step 6, and against what real data, without naming
institutions/officers/letter content specifically>

## Follow-ups
<any separate issues filed in step 8, or "None">
EOF
)"
```

Redact this body the same way as everything else posted publicly — no real
institution/officer names or letter content, per the hard limits.

## 11. Review loop

This repo has no CI workflows configured, so "review loop" here means:

1. Watch for human review comments on the PR (`gh pr view <PR#> --comments`
   periodically, or via `Monitor` if left running longer). Auto-apply fixes
   for concrete, unambiguous requested changes; a genuinely ambiguous
   review comment is a hard-limit stop — reply with your read of it and
   wait rather than guess.
2. If a fix needs another push, re-run steps 6–9 for the delta.
3. Once satisfied, leave the PR open for a human to merge. **Never merge
   it yourself, ever.**

## 12. Report

One line per issue worked in the session:

- `#N — shipped as PR #M` (with the PR link)
- `#N — needs info from reporter` (with the comment link)
- `#N — stopped: <hard limit that fired, short reason>`
- `#N — could not resolve issue number/URL`

Include, for anything shipped: what the root cause was, what was decided
in step 3 and why, what was verified in step 6 and against what (without
naming real records), and the PR link.
