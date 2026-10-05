---
name: record-issues
description: >
  Open DMIS in a single Playwright-driven Chrome window so the USER can log in
  and demonstrate bugs/UI problems themselves, recording every click, input,
  page load, JS/HTTP/JSF error and a screenshot + description per reported
  issue — then read the recording and fix each issue. Use when the user says
  "I'll show you the issues", "let me demonstrate", "record what I do",
  "open the app so I can show you bugs", or similar. Distinct from
  demonstrate-issues (Claude drives the app to PROVE a fix) and
  playwright-e2e (writing automated tests).
---

# Record Issues (user demonstrates, Claude fixes)

The user drives; Claude watches the recording afterwards. The recorder is
`e2e/record-issues.mjs`: one headed Chrome window (never one-per-step — the
user explicitly dislikes windows repeatedly opening and closing), with a
floating **🐞 Report issue** / **■ End session** bar injected into every page.

## 1. Get the latest before every session

The user must demonstrate against current `master`, otherwise they may
report issues that are already fixed. Before launching:

```bash
git fetch origin
git status -sb                      # branch, ahead/behind, local changes
before=$(git rev-parse HEAD)
git checkout master && git pull --ff-only origin master
git diff --name-only "$before" HEAD # what the pull changed
```

- On another branch, or uncommitted changes in tracked files, means stop and
  ask the user (stash? finish that branch first?). Never discard their work
  to get onto master. Untracked files such as `dmis_test.sql` are fine.
- If the pull brought new commits, **rebuild and redeploy** (§6 step 2).
  If any changed path is under `src/main/webapp/resources/ezcomp/`, also
  `restart-domain`. A pull alone does not change the running app.
- If nothing new was pulled but you're unsure the running deployment matches
  HEAD (e.g. a branch was deployed earlier in the session), redeploy anyway.
  It takes about a minute and avoids demonstrating stale code.
- Tell the user the commit they're testing (`git log --oneline -1`).

## 2. Preconditions

- **App running** at `http://localhost:8080/dmis/` (`Invoke-WebRequest`
  returns 200). If not, start/redeploy per CLAUDE.md and the
  `dmis_local_dev_environment` memory (`asadmin start-domain domain1`).
  After any `restart-domain`, wait until `/dmis/` returns 200 — it 404s for
  ~20s while the app reloads.
- **Node** is a portable install at `C:\Dev\tools\node` and is NOT on PATH —
  prefix every command: PowerShell `$env:PATH="C:\Dev\tools\node;$env:PATH"`,
  bash `PATH="/c/Dev/tools/node:$PATH"`.
- **Playwright** is installed in `e2e/` (`npm i` there if `node_modules` is
  missing). It uses the installed Google Chrome (`channel: 'chrome'`) —
  `npx playwright install chromium` times out on this network, don't retry it.
- Credentials: the user logs in themselves in the window. Don't type,
  store or log their password.

Optional self-check (≈10s, no user needed): `node record-issues.mjs --smoke`
does a failed login, reports a fake issue and ends the session. Delete the
resulting `e2e/sessions/<stamp>/` afterwards.

## 3. Launch — in the background

```powershell
$env:PATH="C:\Dev\tools\node;$env:PATH"; Set-Location C:\Dev\DMIS\e2e; node record-issues.mjs
```

Run it with `run_in_background: true` (timeout 7200000). It prints
`SESSION_DIR=...` immediately and exits only when the session ends, so the
background-task completion notification is the "user is done" signal. **Do
not poll or sleep-wait** — end the turn after telling the user:

> A Chrome window is open on DMIS. Log in and use the app as normal. When you
> hit a problem, click **🐞 Report issue** (bottom-right), describe what's
> wrong and what you expected, and click Save. Report as many as you like.
> Click **■ End session** when finished and I'll go through them.

If the user closes the window with ✕ instead of End session, the log and
screenshots are still saved but `trace.zip` is not.

## 4. Read the session

Everything is in `e2e/sessions/<stamp>/` (git-ignored):

| File | Use |
|---|---|
| `session.md` | Step log + an **Issues** section: description, URL, screenshot, on-page error messages, and the ~15 steps leading up to each issue |
| `shots/NNN-issue-N.png` | Full-page screenshot at report time — **Read every one**; the description alone is often ambiguous |
| `shots/NNN-page.png` | Viewport screenshot after each page load (before/after context) |
| `issue-N.html` | Rendered DOM at report time — grep it for JSF client ids, CSS classes, actual rendered markup |
| `trace.zip` | Only if needed: `npx playwright show-trace sessions/<stamp>/trace.zip` (opens a viewer for the user) |

`⚠` lines in the step log are machine-detected problems the user may not have
noticed (JS exceptions, HTTP ≥400 from the app, JSF AJAX `<partial-response><error>`
returned as HTTP 200, `p:messages`/growl errors). Mention them even if not
reported as an issue — but third-party failures (analytics etc.) are already
filtered out.

Privacy: values typed into fields are logged (passwords masked as `••••`),
and `trace.zip`/`issue-N.html` contain page content. Never commit, upload or
paste session folders anywhere.

## 5. Triage before fixing

Summarise back to the user as a numbered list — one line per issue: what you
see in the screenshot, the likely cause, the file(s) involved, and the
proposed fix. Ask about anything genuinely ambiguous (the user said they want
to **discuss before UI changes**). Group issues that share a root cause.

Locating code:
- URL `/dmis/<path>.xhtml` → `src/main/webapp/<path>.xhtml`. Pages rendered
  via `faces-redirect`/`menuController` keep the old URL for one request —
  trust the screenshot/title over the URL when they disagree.
- JSF client id `j_idt170:loginForm:username` → the component with
  `id="username"` inside `id="loginForm"`; `j_idtNN` segments are
  auto-generated and don't appear in source.
- Shared chrome (menu, header, login, footer) lives in
  `src/main/webapp/resources/ezcomp/*.xhtml`.
- Action methods: `#{xxxController.method()}` →
  `src/main/java/lk/gov/health/phsp/bean/XxxController.java`.

Follow CLAUDE.md conventions while fixing (PrimeFaces 14 selection API,
`ui-button-*` classes, no h1–h6, `&amp;` in attributes, no plain `div` AJAX
targets) and load `ui-guidelines` for XHTML work.

## 6. Fix, deploy, verify

1. Branch off up-to-date master first (`feature/<short-topic>`); never commit
   to master directly.
2. Fix. Then deploy: `mvn -q -DskipTests package` +
   `asadmin deploy --force=true --contextroot dmis --name dmis-0.1 target\dmis-0.1.war`
   (the user isn't using NetBeans hot-deploy here). **`resources/ezcomp/*`
   changes also need `asadmin restart-domain domain1`**; wait for 200.
3. Verify each fix with a short scripted Playwright check (single browser,
   screenshots, compare to the issue screenshot) — see `playwright-e2e` for
   selectors and AJAX waiting. Read the screenshots.
4. Report per issue: fixed / not fixed / needs decision, with the evidence.
   Offer to re-launch the recorder so the user can confirm in their own hands.
5. Commit/PR only when asked (`commit-code`). `gh` is not installed — after
   pushing, give the user a pre-filled
   `https://github.com/lk-gov-health-hiu/dmis/compare/master...<branch>?expand=1&title=...&body=...`
   link (URL-encode with node) and open it with `start`.

## 7. Housekeeping

Delete smoke-test session folders. Leave real session folders until the
issues from them are fixed and the user has confirmed, then offer to delete
them.
