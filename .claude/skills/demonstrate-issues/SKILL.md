---
name: demonstrate-issues
description: >
  After implementing a fix for a DMIS bug, drive the running app (via Claude
  in Chrome) to prove the fix actually works — capturing before/after
  screenshots, console/network evidence, and a short repro walkthrough to
  attach to the GitHub issue or PR. Use when asked to "demonstrate the fix",
  "show this is actually fixed", "prove the bug is resolved", or before
  closing out a bug-fix issue/PR. Does not replace code review or automated
  tests — it is the last-mile check that reading the diff cannot give you:
  seeing the actual PrimeFaces page behave correctly end to end.
allowed-tools: Read, Glob, Grep, Bash, mcp__claude-in-chrome__navigate,
  mcp__claude-in-chrome__computer, mcp__claude-in-chrome__find,
  mcp__claude-in-chrome__form_input, mcp__claude-in-chrome__get_page_text,
  mcp__claude-in-chrome__read_page, mcp__claude-in-chrome__read_console_messages,
  mcp__claude-in-chrome__read_network_requests, mcp__claude-in-chrome__javascript_tool,
  mcp__claude-in-chrome__tabs_create_mcp, mcp__claude-in-chrome__tabs_close_mcp,
  mcp__claude-in-chrome__tabs_context_mcp, mcp__claude-in-chrome__resize_window
---

# Demonstrate Issues (DMIS)

Claiming a fix works because the diff "looks right" is not evidence — for a
JSF/PrimeFaces app the only way to know a page actually renders and behaves
correctly is to load it in a real browser and watch it happen. This skill
closes that gap: after a fix is implemented (and, ideally, compiled and
deployed per this repo's build rules), it drives the running DMIS instance
through the exact steps that used to trigger the bug, captures **before**
evidence (the bug still reproducing, if not already captured earlier) and
**after** evidence (the fix working), and leaves behind screenshots/logs
that can be pasted straight into the GitHub issue or PR.

## When to use this

- Immediately after a bug fix is implemented and deployed, before marking
  the issue/PR ready for review.
- When a reviewer or the user asks "did you actually check this in the
  browser?" or "prove it's fixed."
- Not for brand-new features with no prior bug to compare against — for
  those, a plain "here's it working" capture is enough; skip the
  before/after framing in that case (see Non-goals).

## Non-goals

- **Does not write or edit application code.** By the time this skill
  starts, the fix is already implemented. If the demonstration reveals the
  bug is *not* actually fixed, stop and report that back — go fix the code
  (outside this skill), then re-run the demonstration.
- **Does not decide whether a fix is correct at the code level.** That is
  what `code-review` is for. This skill only demonstrates runtime behavior.
- Does not replace unit/integration tests. Screenshots are evidence for
  humans reviewing the PR, not a substitute for automated coverage.

## 1. Confirm the running app matches the fix

Before capturing anything, make sure the browser will actually hit the
fixed code:

- **JSF/XHTML-only change** (no Java touched): per `CLAUDE.md`, if the user
  is running the app themselves through NetBeans, the edit hot-deploys on
  save — no rebuild needed. If Claude is driving deployment directly against
  a packaged WAR, hot-redeploy is **not** enough for XHTML — run
  `mvn package` and `asadmin deploy` on the freshly built WAR first.
- **Java/managed-bean/JPA change**: always requires `mvn package` +
  redeploy, regardless of who is driving.
- **Composite component change** (anything under
  `src/main/webapp/resources/ezcomp/*.xhtml`, e.g. `menu.xhtml`): requires a
  full Payara **domain restart**, not just a redeploy.
- Sanity-check the deployed app is actually current: compare the WAR's
  build/deploy timestamp against `git log -1` and `git status --short` on
  the branch under test. If they disagree, rebuild/redeploy before
  proceeding rather than demonstrating against stale bytecode/markup.
- Confirm the target Payara 5 `domain1` instance and the `jdbc/dmis`
  datasource are up (e.g. `asadmin list-applications`) and note the base
  URL/context root you'll navigate to.

If deployment is ambiguous (uncommitted changes, unclear which WAR is
live), stop and ask rather than guessing — capturing evidence against the
wrong build is worse than no evidence.

## 2. Load the browser tools

If the Chrome MCP tools aren't already loaded, fetch the core set in one
batch (`navigate`, `computer`, `read_page`, `tabs_create_mcp`,
`tabs_context_mcp`), plus `read_console_messages` /
`read_network_requests` when the bug involves a JS error or an AJAX/JSF
partial-response failure, and `form_input` when the repro fills in a form.
Use the `claude-in-chrome` skill's own tooling conventions — don't bypass
it with raw `computer` clicking when `find`/`form_input` can target an
element more reliably (PrimeFaces widgets often need a specific selector,
not just visible text, because of generated `id`s like `j_idt42`).

## 3. Reproduce the "before" state (if not already on record)

If the original bug report (issue, PR description, or a bug-fix commit
message) already has a screenshot or console log showing the failure, that
is your "before" — don't re-break working code just to re-capture it.

Otherwise, and only if it's safe to do temporarily (e.g. checking out the
pre-fix commit in a scratch build, or toggling the fix off), reproduce the
original failure once and capture:

- A screenshot of the broken state (e.g. wrong `DocumentHistory.toInstitution`
  showing up in an "Outside Letters" report, a `p:dataTable` selection
  checkbox not appearing, a print panel rendering pagination/filter chrome).
- `read_console_messages` output if the bug manifests as a JS error.
- `read_network_requests` output if the bug is a failed/malformed JSF ajax
  request (check the partial-response XML for a `<error>` or `<redirect>`
  element PrimeFaces surfaces as a growl/dialog).

Return to the fixed code afterward before continuing.

## 4. Demonstrate the "after" state

Drive the app through the same steps against the fixed, deployed build:

- Navigate to the relevant page (e.g. `letter.xhtml`, a letters-entered
  report, an institution search) and log in with a test account that has
  the necessary role/permissions.
- Perform the exact steps from the issue's repro: same filters, same
  Outside Letter/Our Letter toggle state, same date-type filter
  (`SearchFilterType`), same rows selected, same button clicked.
- Capture a full-page screenshot of the corrected result. Prefer capturing
  the actual data area over a full-viewport shot when the fix is about a
  specific control (e.g. crop to the `p:dataTable` header row to show the
  new `selectionBox="true"` checkbox column, or to the print preview panel
  to show it no longer carries filter/pagination chrome).
- If the fix touches an AJAX interaction, re-check
  `read_network_requests`/`read_console_messages` to confirm no error is
  present now where one was before.
- If the fix is data-shape related (e.g. the `documentGenerationType`
  null-handling for legacy letters, or filtering on `institution` instead
  of `toInstitution`), get the report/grid to actually show a row that
  proves the distinction — a legacy letter with `documentGenerationType =
  null` correctly appearing under "Outside Letters," or another
  institution's inbound "Our Letter" correctly absent from "entered by us."
  A blank-but-not-erroring screen is weak evidence; a populated result that
  matches expectations is strong evidence.

## 5. Sanitize before attaching anywhere

DMIS screenshots and network payloads can carry real institution names,
officer names, letter reference numbers, or NIC-like identifiers pulled
from production-shaped test data. Before attaching anything to a GitHub
issue or PR:

- Review every screenshot and any copied network/console text for
  identifiable data that isn't needed to prove the fix.
- Prefer test/seed data (a clearly fictitious institution or letter
  reference) over real production-looking records when setting up the
  repro, so there's nothing to redact later.
- If redaction is unavoidable, blank the field in the image rather than
  cropping it out silently — a reviewer should be able to tell the region
  was intentionally redacted, not that the page simply doesn't have that
  column.

## 6. Attach the evidence

`gh issue comment` / `gh pr comment` and `--body`/`--body-file` are
text-only — they cannot upload local image files directly. Use
`gh issue comment <n> --repo lk-gov-health-hiu/dmis --body-file <file>`
with the images referenced via `gh`'s drag-and-drop-equivalent upload flow
if available in your environment, or otherwise host the screenshots the
way this repository already does for other visual evidence and link them
in the body. Structure the write-up as:

- **Before** — screenshot/log + one-line description of the failure.
- **After** — screenshot/log + one-line description confirming correct
  behavior, referencing the specific steps taken (URL, filters, role used).
- **Build verified** — the commit SHA and how the build was deployed
  (NetBeans hot-deploy vs. `mvn package` + `asadmin deploy`), so a reviewer
  knows the evidence is against current code, not a stale WAR.

Do not close the issue or merge the PR yourself as part of this skill —
attach the evidence and let the normal review process take it from there.
