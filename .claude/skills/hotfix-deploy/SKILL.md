---
name: hotfix-deploy
description: >
  Emergency hotfix workflow for lk-gov-health-hiu/dmis: branch a minimal fix
  directly off master, get explicit user confirmation before touching the
  Payara 5 production domain, deploy to production with a rollback plan in
  hand, and verify afterward. Use when asked to "hotfix production", "deploy
  an emergency fix", "patch prod now", or similar urgent-production-issue
  requests. This is a real ministry system on the jdbc/dmis production
  datasource — every deploy/restart step requires the user's explicit go-ahead,
  even under time pressure.
argument-hint: "<short description of the production problem>"
---

# Hotfix Deploy (DMIS)

DMIS is a production JSF/PrimeFaces 14 application used by a government
health institution, running on Payara 5 (domain1) against the `jdbc/dmis`
MySQL datasource. This skill is for when something is broken **right now**
in production and needs the smallest possible fix pushed out fast — without
skipping the safety checks that a real outage tempts you to skip.

**Non-negotiable rule: never run `asadmin deploy`, any `asadmin` domain
restart command, or any other action that touches the live production
Payara instance without the user explicitly confirming that exact step
first.** Preparing the fix, building it, and describing the deploy plan
never need that confirmation — executing against production does. If the
user says "just do it" for the whole workflow up front, that authorizes
branching and coding, not the deploy step itself — stop and ask again
immediately before the `asadmin deploy` (or restart) command runs. Time
pressure is a reason to be crisper in each step, never a reason to remove
a confirmation gate.

## 1. Confirm the scope of the emergency

Before writing any code, get from the user (or the issue/report they point
you to):
- What is actually broken, and for whom (which institution/users, which
  screen or flow)
- Since when, and how they know (error in the UI, stack trace, a report
  giving wrong numbers, users unable to log a letter, etc.)
- Whether this is a full outage, a data-correctness bug, or a degraded
  feature — this affects how much delay is acceptable and how much
  investigation you can afford before touching prod

Do not skip straight to a fix based on a guess. A wrong hotfix deployed
under pressure is worse than a slightly slower correct one.

## 2. Branch the hotfix directly off master

Production is deployed from `master`. Branch from the latest `master`, not
from whatever feature branch happens to be checked out:

```bash
git fetch origin
git checkout -b hotfix/<short-description> origin/master
```

Use a real, specific description (`hotfix/letter-search-null-pointer`, not
`hotfix/fix`).

## 3. Make the smallest possible fix

- Touch only the files required to resolve the reported problem. Do not
  fold in unrelated cleanup, refactors, or other pending work — that
  belongs in a normal PR later, not in a hotfix.
- Check whether the fix is XHTML-only (no Java changes). Per this repo's
  CLAUDE.md, XHTML-only changes hot-deploy without a rebuild **when the
  user is running the app themselves via NetBeans**; a Claude-driven
  `asadmin deploy` of a packaged WAR always needs `mvn package` first,
  regardless of whether the change is XHTML-only.
- If the fix touches `resources/ezcomp/*.xhtml` (composite components such
  as `menu.xhtml`), remember this needs a full Payara domain restart to
  take effect, not just a redeploy — factor that into the rollback plan
  and the timing you discuss with the user.
- Follow the existing PrimeFaces 14 / JSF conventions in CLAUDE.md (button
  classes, selection API, `SearchFilterType` reuse, etc.) even for a quick
  fix — a hotfix that violates them just creates the next incident.

## 4. Build and verify locally before touching production

```bash
mvn -q -DskipTests=false package
```

Fix any compile or test failures. Do not deploy something that hasn't
built cleanly. If the fix is genuinely XHTML-only and you are not the one
running `asadmin deploy` (the user will hot-deploy it themselves via
NetBeans), you can skip the packaged build — but say so explicitly rather
than silently assuming it.

## 5. Write the rollback plan before deploying

Before asking for deploy confirmation, have ready:
- The exact command(s) to revert: redeploy the previous WAR/artifact, or
  `git revert` the hotfix commit and rebuild+redeploy
- Whether a domain restart is needed to roll back (yes, if the fix touched
  `resources/ezcomp/*.xhtml`)
- Any data-side rollback needed — e.g., if the fix included a manual data
  correction against `jdbc/dmis`, note how to undo that separately from
  the code rollback
- How you and the user will know the rollback worked

State this plan to the user as part of the deploy confirmation ask, not
as an afterthought after something goes wrong.

## 6. Get explicit confirmation, then deploy

Ask the user directly to confirm the production deploy — name the target
(production Payara domain1, `jdbc/dmis`), summarize the fix and the
rollback plan, and wait for an explicit yes. Do not infer consent from
"looks fine" or silence. Only after that:

```bash
mvn -q package
asadmin deploy --force <path-to-war>
```

If the fix requires a domain restart (e.g. an `ezcomp` change), call that
out as its own separate confirmation — restarting the domain is more
disruptive than a hot redeploy and briefly takes the whole app down for
every institution using it.

## 7. Verify in production after deploy

- Reproduce the original broken scenario and confirm it now works
- Spot-check one or two adjacent flows that share the changed code path,
  to catch regressions the fix might have introduced
- Check application/server logs for new errors right after deploy
- Confirm the fix is visible from a real browser refresh, not just from
  re-reading the source — stale markup can still be served if a step in
  steps 3-6 was missed

If verification fails, execute the rollback plan from step 5 immediately
and tell the user what happened before trying a second fix attempt.

## 8. Commit, push, and open a PR back to master

Even for an emergency fix, land it through review after the fact:

```bash
git add <changed-files>
git commit -m "fix: <description of the hotfix>

<what was broken, what this changes, and why it was urgent>"
git push -u origin hotfix/<short-description>
```

```bash
gh pr create --repo lk-gov-health-hiu/dmis --base master \
  --head hotfix/<short-description> \
  --title "fix: <description>" \
  --body "Emergency production hotfix.

What was broken: <...>
Fix: <...>
Verified in production: <...>
Rollback plan (if this needs to be reverted): <...>"
```

Note in the PR description that this was already deployed to production
ahead of review, so reviewers know to check it retroactively rather than
gate the deploy on their approval.
