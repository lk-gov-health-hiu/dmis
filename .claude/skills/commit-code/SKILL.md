---
name: commit-code
description: >
  How to prepare and create commits in the DMIS repo — reviewing diffs before staging,
  splitting unrelated changes into separate commits, matching this repo's commit
  message conventions, avoiding secrets/build artifacts, and when to ask before
  committing. Use whenever the user asks to commit changes, or before running
  `git commit` on their behalf.
---

# Committing Code in DMIS

DMIS is a JSF/PrimeFaces 14 (NetBeans project layout) app backed by JPA/MySQL,
deployed on Payara 5. This skill governs how commits get made in this repo,
independent of what generated the change (hand-written, or Claude-assisted).

## 1. Only commit when asked

Per this repo's CLAUDE.md: never create a commit unless the user has explicitly
asked for one in this turn. Finishing an edit is not implicit permission to
commit it. If it's ambiguous whether "do X" includes committing, ask first
rather than guessing.

## 2. Review before staging

Before running `git add`, always look at what's actually changed:

```
git status
git diff              # unstaged
git diff --staged     # already staged
```

Read the diff, don't just skim filenames. In a JSF codebase it's easy to stage
more than intended — e.g. NetBeans re-saving an unrelated `.xhtml` with
whitespace/encoding churn, or a `.form`/generated getter-setter block in a
managed bean that came along for the ride. Drop anything the user didn't ask
for with `git restore <file>` (unstaged) or `git restore --staged <file>`.

Stage specific files by name (`git add path/to/File.java`). Avoid `git add -A`
or `git add .` — this repo has local-only files that are easy to sweep in by
accident (see §4).

## 3. Split unrelated changes into separate commits

If a diff touches more than one concern, split it rather than bundling:

- A bean/controller fix and an unrelated XHTML tweak on the same page →
  two commits.
- A dependency bump (`pom.xml`) and an application code change → two commits,
  even if the code change was *caused by* the bump (see `417ed2c` /
  `459b044` in this repo's history — the fusionauth-jwt revert and the
  PrimeFaces 15 chart migration were separate commits even though both were
  fallout from the same dependency PR).
- Multiple reports/pages added in one sitting are fine as a single commit
  *if* they're genuinely one feature (see `3a24bf3`, which added two related
  report views under one commit); split them if they're independently
  reviewable/revertable features instead.

Use `git add <file>` per commit, or `git add -p` for hunk-level splits within
a single file, rather than committing everything at once and sorting it out
later.

## 4. Never commit secrets or generated artifacts

Already covered by `.gitignore`, but double-check before staging anything
matching these — a manual `git add` can override the ignore for a file that
was previously tracked:

- `src/main/webapp/WEB-INF/glassfish-resources.xml` / `glassfish-resources.xml`
  — local datasource credentials, never committed.
- `/build/`, `/dist/`, `/target/` — Maven/NetBeans build output.
- `.settings`, `.project`, `.classpath`, `.vscode`, `.idea`, `.metadata` — IDE
  project files.
- `.claude/settings.local.json` — local Claude Code permissions.
- `src/main/resources/META-INF/persistence.xml` — this file IS tracked and
  should stay pointed at the shared `jdbc/dmis` JNDI name. If it's staged,
  check it hasn't been repointed at a personal/local JNDI name or had the
  commented-out `schema-generation.database.action` property uncommented
  (that would let EclipseLink alter the production schema on deploy).
- Any `.env`, `*.p12`/`*.jks` keystore, or hardcoded DB password/API key
  pasted into Java or XHTML — warn the user and stop rather than staging it.

## 5. Commit message conventions

Match this repo's existing style — check `git log --oneline -20` if unsure.

**Subject line:** `<type>: <imperative summary>`, lowercase after the colon,
no trailing period. Types actually used in this history: `feat`, `fix`,
`chore`, `docs`, `chore(deps)`, `wip`. Pick the one that matches the change;
don't invent new ones.

**Body:** explain *why*, not a restatement of the diff. This repo's commits
lean toward a short paragraph of root-cause/context — what broke or was
missing, why it matters, what the fix does — rather than a bullet list of
files touched. For a JSF/PrimeFaces-specific fix, name the concrete mechanism
(e.g. which lifecycle phase, which managed bean scope, which PrimeFaces
component behavior) rather than a vague "fixed a bug."

**Issue references:** `Closes #N` or `Fixes #N` on its own line when the
commit closes a GitHub issue tracked in this repo. Omit if there's no issue.

**Trailers:** end with
```
Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
```
on any commit Claude authored or materially drafted, exactly as this repo's
existing Claude-assisted commits do. Don't add a `Claude-Session:` trailer
unless the user's own workflow already does so for this change.

Compose the message with a heredoc so formatting survives:

```bash
git commit -m "$(cat <<'EOF'
fix: <summary>

<body explaining why>

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
```

## 6. Safety rules

- Never `git add -A`/`git add .` — name files explicitly.
- Never `--amend` unless the user explicitly asks — if a pre-commit hook
  fails, fix the issue, re-stage, and make a **new** commit; amending after a
  failed commit would silently rewrite the *previous* commit instead.
- Never `--no-verify`, `--no-gpg-sign`, or otherwise skip hooks/signing unless
  explicitly asked.
- Never push unless explicitly asked, and never force-push `master` under any
  circumstance without the user directly requesting it (and even then, warn
  first).
- Don't run `mvn package`/redeploy as part of "just commit this" — per this
  repo's CLAUDE.md, JSF-only XHTML changes don't need a build, and building
  is a separate, explicitly-requested step from committing.

## 7. When to ask vs. proceed

Ask before proceeding when:
- The diff spans clearly unrelated concerns and it's not obvious how the
  user wants them split.
- A file matching §4's secret/artifact patterns is staged.
- The commit would close an issue number you're inferring rather than one
  the user stated.
- Multiple plausible commit-message summaries exist and the "why" isn't
  clear from the diff alone (ask rather than guess at intent).

Proceed without asking when the user has clearly asked for a commit, the diff
is a single coherent change, and the message can be written confidently from
the diff and surrounding context (e.g. the issue/PR being worked on in this
session).
