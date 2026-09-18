---
name: cleanup-branches
description: >
  Safely clean up stale local and remote git branches in this repo: identify
  branches already merged into master, cross-check branches whose PRs were
  closed or merged via gh, prune stale remote-tracking refs, and delete local
  branches that are truly safe to remove. Never force-deletes unmerged work
  and always confirms with the user before deleting anything on origin. Use
  after PRs are merged, or periodically to tidy up local git state.
allowed-tools: Bash
---

# Cleanup Stale Branches

Find local (and, on request, remote) branches that are no longer needed and
remove them safely, without ever discarding unmerged work.

This repo uses `master` as its trunk. Feature/fix work happens on branches
named like `feature/issue-NNN-...` or `fix/...` and lands on `master` via PRs
on `lk-gov-health-hiu/dmis`.

## Step 1 — Fetch and Prune

```bash
git fetch origin --prune
```

`--prune` removes remote-tracking refs (`origin/<branch>`) for branches
already deleted on GitHub. This alone is always safe to run.

## Step 2 — Make Sure master Is Current

```bash
git checkout master
git merge --ff-only origin/master
```

Use `--ff-only` so this stops with an error instead of silently rewriting
history if local `master` has diverged. If it fails, report this to the user
and stop — do not force-reset.

## Step 3 — List Candidate Branches

```bash
git branch --format='%(refname:short)' | grep -vx 'master'
```

For each branch, classify it before doing anything:

### A. Already merged into master

```bash
git branch --merged master --format='%(refname:short)' | grep -vx 'master'
```

Any branch in this list is safe to delete locally with `git branch -d`.

### B. Not merged locally — check PR status via gh

For branches not caught by the `--merged` check (common after a squash or
rebase merge, where git can't see the ancestry), ask GitHub instead:

```bash
gh pr list --repo lk-gov-health-hiu/dmis --head <branch> --state all \
  --json number,title,state,baseRefName,mergedAt --jq '.[0]'
```

- `state == "MERGED"` → safe to delete (record the PR number/title for the
  report, and confirm `baseRefName` was `master`).
- `state == "CLOSED"` (not merged) → the work was abandoned. Still confirm
  with the user before deleting — closed-without-merge can mean "superseded"
  or "will resume later."
- `state == "OPEN"` or no PR found → **do not delete**. Flag it as active or
  unsubmitted work.

## Step 4 — Verify No Local-Only Commits Before Deleting

Even for a branch that looks merged, check for commits that never made it to
origin (e.g. local amends/rebases after the PR was opened):

```bash
git log origin/<branch>..<branch> --oneline 2>/dev/null
```

- Empty output → safe to delete.
- Non-empty output → **skip and warn the user**; these commits exist only
  locally and deleting the branch would lose them permanently.

## Step 5 — Delete Local Branches

For each branch confirmed safe in Steps 3–4:

```bash
git branch -d <branch>
```

If `-d` refuses because git can't trace the merge (typical for a squash-merged
PR that gh confirmed as `MERGED`, with no local-only commits per Step 4), it
is safe to force it:

```bash
git branch -D <branch>
```

Never use `git branch -D` on a branch that hasn't been confirmed merged or
explicitly approved for deletion by the user.

## Step 6 — Remote Branches Require Explicit Confirmation

**Never delete a remote branch without the user explicitly confirming it
first.** Present the list of remote branches whose PRs are merged/closed and
ask before running:

```bash
git push origin --delete <branch>
```

Do this one branch at a time, or as an explicit batch the user has reviewed —
never as a blanket "delete everything merged" pass on origin.

## Step 7 — Report

Summarize what happened:

```
Deleted local branches:
  - <branch>  (PR #NNN merged -> master)
  ...

Skipped (needs review):
  - <branch>  -- local-only commits, not on origin
  - <branch>  -- PR still open
  - <branch>  -- PR closed without merge, confirm before deleting
  ...

master is up to date at <short-sha> (<subject>)
```

If nothing was skipped, omit that section. If remote branches were deleted,
list them separately under "Deleted remote branches" with their PR numbers.

## Notes

- Never delete `master`.
- Never force-push or force-delete anything on `origin` — remote deletions
  always go through the plain `git push origin --delete` after explicit user
  confirmation.
- When in doubt about whether a branch is safe to remove, leave it and ask —
  losing someone's in-progress work is far worse than an extra stale branch.
