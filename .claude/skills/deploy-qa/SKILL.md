---
name: deploy-qa
description: >
  Build and deploy DMIS to a QA/test Payara environment — separate from the
  production `domain1` instance and its `jdbc/dmis` production datasource.
  Covers the pre-deploy checklist, `mvn package`, targeting the QA domain
  with `asadmin deploy`, and post-deploy smoke checks. Use when asked to
  "deploy to QA", "push this to test", or "get this build onto the QA
  server" — never for production deploys, which this skill deliberately
  does not perform.
allowed-tools: Bash, Read, Grep, Glob
---

# Deploy DMIS to QA

This skill packages and deploys DMIS to a **QA/test Payara domain** — a
separate Payara domain from the production `domain1` instance described in
this repo's `CLAUDE.md`, with its own admin port and, critically, its own
`jdbc/dmis` connection pool pointed at a non-production database. Nothing
here should ever touch `domain1` or the production datasource. If there is
any doubt about which domain a command is about to hit, stop and ask.

## Non-goals

- **Not a production deploy.** This skill only targets QA. If the user asks
  to deploy to production, that's a different, higher-stakes operation —
  don't reuse these steps against `domain1` without the user explicitly
  asking for a production deploy.
- **Does not create or provision the QA domain.** It assumes a QA Payara
  domain already exists with its own admin port and its own `jdbc/dmis`
  pool/datasource configured against a QA database. If you don't know the
  QA domain's name, admin port, or base URL, ask the user rather than
  guessing — don't reuse production values by default.

## 1. Confirm before deploying

Even though QA is lower-stakes than production, it is typically a shared
environment other people (testers, reviewers) rely on. **Always confirm
with the user before actually running the deploy step** — building and
checking the pre-deploy checklist is fine to do proactively, but pause
before `asadmin deploy` and state plainly what you're about to overwrite
(which QA domain, which branch/commit). This matters most when:

- Someone else may currently have work-in-progress deployed to that QA
  instance that your deploy would replace.
- You're not certain which QA domain (name/port) the user means.

## 2. Pre-deploy checklist

Run through these before building:

```bash
# Correct branch and no surprise local changes
git status --short
git branch --show-current

# What's actually about to ship
git log --oneline origin/master..HEAD    # or the target branch, if different
git diff --stat origin/master..HEAD
```

- **No uncommitted changes** unless the user explicitly wants to deploy a
  dirty working tree (call this out if so — it means the deploy won't match
  any commit).
- **Correct branch** — confirm with the user which branch/commit should go
  to QA; don't assume `master`/`main` when a feature branch is what's
  actually under test.
- If there are uncommitted or unpushed changes the user didn't mention,
  surface them before proceeding rather than silently deploying whatever is
  on disk.

## 3. Build

```bash
mvn clean package -DskipTests
```

Per this repo's `CLAUDE.md`:

- **JSF/XHTML-only changes** (no Java touched) don't strictly require
  compilation when the user is running the app themselves via NetBeans
  (hot-deploy on save). But when *you* are driving deployment directly
  against a packaged WAR — which is what this skill does — build and
  redeploy the WAR regardless of change type; hot-redeploy doesn't apply to
  a WAR you deploy via `asadmin`.
- Confirm the build actually succeeded (`BUILD SUCCESS`, and the WAR
  present under `target/`) before moving on:

```bash
ls -la target/*.war
```

## 4. Deploy to the QA domain

Identify the QA domain's admin port (or host, if it's remote) before
deploying — this is what actually separates QA from production, since both
domains may use the same JNDI name `jdbc/dmis` internally while pointing
at completely different databases. Confirm it with the user if it isn't
already established in this conversation; don't default to `domain1`'s
admin port.

```bash
# Start the QA domain if it isn't already running
asadmin start-domain <qa-domain-name>

# Deploy, targeting the QA domain's admin port explicitly
asadmin --port <qa-admin-port> deploy --force target/dmis.war
```

If the QA domain is on a different host, use `--host` too:

```bash
asadmin --host <qa-host> --port <qa-admin-port> deploy --force target/dmis.war
```

**Guardrails:**

- Never omit `--port`/`--host` and rely on asadmin's default target — the
  default is whatever domain is locally running, and if that happens to be
  `domain1` you'd deploy a QA build to production.
- Double-check the deployed context root / app name matches the QA
  instance's existing app, not a fresh, differently-named deployment
  sitting alongside it.
- If the change touches a composite component under
  `resources/ezcomp/*.xhtml` (e.g. `menu.xhtml`), a redeploy is not
  enough — the QA domain needs a full restart for it to take effect:

  ```bash
  asadmin --port <qa-admin-port> restart-domain <qa-domain-name>
  ```

## 5. Post-deploy smoke checks

After deploy completes, verify against the QA instance before telling the
user it's ready:

```bash
# Confirm the app is actually listed and enabled
asadmin --port <qa-admin-port> list-applications

# Tail the QA domain's log for deploy-time exceptions
tail -100 <qa-domain-path>/logs/server.log
```

Then, functionally:

- Load the QA base URL and confirm the app comes up (login page or
  dashboard renders, no 500).
- Navigate to the specific page(s) affected by the change under test and
  confirm they render without JSF/PrimeFaces errors.
- If the change touches a form submission or AJAX update, exercise it once
  end-to-end rather than just loading the page.
- Check the browser console / server log for errors during that walkthrough
  — a page that "loads" with a swallowed JSF exception is not a pass.

If a smoke check fails, don't leave the user to discover it — report what
broke, and don't declare the deploy done.

## 6. Reporting back

When done, tell the user plainly:

- Which branch/commit was deployed and to which QA domain.
- Build result (success/warnings).
- Smoke-check results (what you checked, pass/fail).
- Anything skipped (e.g. "didn't restart the domain since nothing under
  `resources/ezcomp/` changed").

Keep production and QA clearly labeled in this summary — never phrase QA
results in a way that could be mistaken for a production deploy report.
