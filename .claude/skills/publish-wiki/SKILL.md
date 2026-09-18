---
name: publish-wiki
description: >
  Publish or update pages on the DMIS GitHub wiki
  (lk-gov-health-hiu/dmis.wiki) — cloning the wiki repo, adding or editing
  Markdown pages, and pushing them live. Use when asked to write, update, or
  publish wiki documentation for this project, or to record something "on
  the wiki".
---

# Publishing to the DMIS Wiki

The GitHub wiki lives in its own git repository, separate from the main
`dmis` codebase: `git@github.com:lk-gov-health-hiu/dmis.wiki.git` (HTTPS:
`https://github.com/lk-gov-health-hiu/dmis.wiki.git`). It is **not** a
submodule of this repo and its files must never be committed inside the
main `dmis` working tree.

**Anything pushed to the wiki repo is immediately live** on
`https://github.com/lk-gov-health-hiu/dmis/wiki` — there is no PR review, no
staging step, and no CI gate. Treat every push as a publish action.

## Steps

1. **Clone or update a working copy**, outside the main `dmis` repo tree
   (e.g. as a sibling directory or in a scratch location):
   ```bash
   test -d ../dmis.wiki && (cd ../dmis.wiki && git pull) \
     || git clone https://github.com/lk-gov-health-hiu/dmis.wiki.git ../dmis.wiki
   ```

2. **Add or edit pages** as plain Markdown files in `../dmis.wiki`. Wiki page
   names are derived from file names (spaces become dashes, e.g.
   `Letter-Search-By-Date.md` → "Letter Search By Date"). Keep names
   consistent with existing pages — check `../dmis.wiki` for the current
   naming pattern before adding a new one, and prefer updating an existing
   page over forking a near-duplicate.

3. **Review the diff** before committing:
   ```bash
   cd ../dmis.wiki && git status && git diff
   ```

4. **Commit** with a message describing what changed on the wiki, not the
   mechanics of editing it (same spirit as this repo's commit conventions —
   explain the "why"):
   ```bash
   git add <changed-files>
   git commit -m "Document <topic>

   Co-Authored-By: Claude <noreply@anthropic.com>"
   ```

5. **Confirm with the user before pushing.** Show them the page(s) changed
   and a summary of the content (or the diff) and get an explicit go-ahead —
   do not push automatically after committing. This is the one hard gate in
   this workflow: wiki pushes are immediately public/visible to anyone with
   repo access, with no review step to catch mistakes first.

6. **Push** only after confirmation:
   ```bash
   git push origin master
   ```

7. **Report back** the pages published, with their live URLs:
   `https://github.com/lk-gov-health-hiu/dmis/wiki/<Page-Name>`.

## Notes

- Never create a `wiki/` or `wiki-docs/` folder inside the main `dmis`
  repository — wiki content belongs solely in the separate
  `dmis.wiki` repo clone.
- Pull the latest wiki content before editing to avoid clobbering another
  editor's concurrent changes; if a push is rejected, `git pull --rebase`
  and re-check the merged content before pushing again.
- Keep wiki writing in plain, readable Markdown — no JSF/XHTML, no
  PrimeFaces-specific markup. The wiki is documentation for people, not
  application code.
- If asked to document something that's still in-progress on a feature
  branch, say so on the page and prefer waiting until it lands on `master`
  before publishing user-facing wiki instructions about it.
