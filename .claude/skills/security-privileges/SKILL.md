---
name: security-privileges
description: >
  Role, privilege, and institution-scoping conventions for this app. Use when
  adding a privilege-gated feature or menu item, checking whether a controller
  correctly restricts data to the logged-in institution, reviewing code that
  touches WebUser/WebUserRole/Privilege/UserPrivilege, or auditing for
  cross-institution data leaks (e.g. one institution seeing another's letters).
---

# Security & Privileges

This app has **no container-managed security** — there is no `@RolesAllowed`,
no `<security-constraint>` in `web.xml`, no JAAS role mapping. Every access
check is hand-rolled in session-scoped beans. That means access control is
only as strong as the checks a developer remembers to write in each
controller — there is no framework backstop. Treat every new feature as
needing its own explicit checks.

There are **two independent axes** of access control here, and a correct
feature usually needs both:

1. **Privilege** — can this user perform this action at all?
2. **Institution scope** — even if they can, whose data are they allowed to
   see/touch?

Mixing these up, or implementing only one, is the source of most real bugs in
this codebase (see Pitfalls below).

## Axis 1: Roles and Privileges

- `lk.gov.health.phsp.enums.WebUserRole` — a coarse role per user (e.g.
  `Institutional_Administrator`, `Institutional_Super_User`,
  `Institutional_User`, and equivalents under `Postal_Branch_*` and the
  national/system tier). Read off `WebUser.getWebUserRole()`.
- `lk.gov.health.phsp.enums.WebUserRoleLevel` — `National` vs `Institutional`.
  Used to branch entire admin sections, e.g.
  `WebUserController.toAdministration()` sends `National` users to
  `/regional/administration/index` and everyone else to
  `/provincial/administration/index`.
- `lk.gov.health.phsp.enums.Privilege` — the fine-grained capability enum
  (`Add_Letter`, `Import_Letters`, `Manage_Institution_Users`, …). This is
  what individual features should check, not the role directly.
- `UserPrivilege` — the join entity granting one `Privilege` to one
  `WebUser`. It is **soft-deleted** (`retired` boolean + `retiredBy`/
  `retiredAt`/`retireComments`), same convention as `Document`/
  `DocumentHistory` — never hard-delete a grant.
- `PrivilegeConverter` persists `Privilege` **by enum name (String)**, not
  ordinal. Consequence: it's safe to add new constants anywhere in the enum,
  but **never rename or remove an existing constant** — that silently orphans
  every `UserPrivilege` row already stored under that name. Mark unused ones
  `@Deprecated` instead (see the block of `@Deprecated` legacy privileges
  already in `Privilege.java`).
- New users get a starter set of privileges from
  `WebUserController.getInitialPrivileges(WebUserRole role)` — a switch on
  role that seeds sensible defaults; grants beyond that are managed by hand
  through the admin privilege tree (`institution/admin/user_privileges.xhtml`,
  `national/admin/index.xhtml`, `webUser/manage_users.xhtml`, all backed by
  `PrivilegeTreeNode`/`WebUserApplicationController.preparePrivileges(...)`).

### The actual check: `hasPrivilege`

```java
// WebUserController — session-scoped
public boolean hasPrivilege(String privilege) { ... }   // EL-friendly overload
public boolean hasPrivilege(Privilege p) {
    return hasPrivilege(loggedUserPrivileges, p);
}
```

`loggedUserPrivileges` is loaded once at login (and refreshed if the user
"assumes" a different role — see Pitfalls) from the non-retired
`UserPrivilege` rows for that user.

The one real example of this being used to gate UI in the whole app is in
`resources/ezcomp/menu.xhtml`:

```xhtml
<p:menuitem class="submenu-item" ajax="false" value="Import Letters from PDF"
            action="#{letterImportController.toLetterImport()}"
            rendered="#{webUserController.hasPrivilege('Import_Letters')}" />
```

Copy this pattern for any new privilege-gated menu item or panel.

## Axis 2: Institution Scope

Independent of privilege, almost every query in this domain must be scoped to
`webUserController.getLoggedInstitution()`. This is the pattern used
throughout `LetterController` (and equivalents):

```java
String jpql = "select d from Document d where d.retired=false "
            + " and d.institution=:ins ";
m.put("ins", webUserController.getLoggedInstitution());
```

A privilege check answers "can they run this report at all?" — it says
nothing about "which rows they see." A user can legitimately hold
`Search_Letter` and still only be entitled to their own institution's rows.
**A new report or search screen needs both checks**: privilege to reach the
page, institution parameter bound into every query on it.

For the letters domain specifically, the correct field to filter on is
`DocumentHistory.institution` (the entering unit), not `toInstitution` — see
"Outside Letter vs Our Letter" in this repo's `CLAUDE.md` for the full
gotcha, including the ~12,900 legacy rows with `documentGenerationType=null`
that must be treated as outside letters, not excluded. The commit that added
the "Outside Letters"/"Our Letters" registry reports
(`LetterController` + `institution/letters_entered_outside_registry.xhtml` /
`institution/letters_entered_our_registry.xhtml`) is the canonical worked
example of scoping a new report correctly on both axes.

## Adding a New Privilege-Gated Feature — Checklist

1. **Reuse before adding.** Check `Privilege.java` for an existing constant
   that already means what you need.
2. **If genuinely new**, append a constant to `Privilege.java` in the
   relevant section (grouped by menu area already: File Management,
   Institutional Mail Management, System Administration, …). Do not touch
   existing constants.
3. **Wire it into the admin privilege tree** so it can actually be granted —
   `PrivilegeTreeNode` construction in `WebUserApplicationController`
   (`getAllPrivilegeRoot()`/`preparePrivileges`) and the corresponding
   `user_privileges.xhtml` pages.
4. **Gate the UI** with `rendered="#{webUserController.hasPrivilege('Your_New_Privilege')}"`
   on the menu item / button / panel — `h:panelGroup` or `p:outputPanel`
   wrapper if more than one element needs to disappear together (never a bare
   `div`/`span` with an `id` per this repo's AJAX-target rule).
5. **Also check it in the backing bean's action method**, not just in
   `rendered`. `rendered` only hides a button in the browser — it does not
   stop the action method from running if invoked another way (bookmarked
   action, resubmitted form, etc.). Right now `hasPrivilege` is called from
   exactly one `.xhtml` and from nowhere inside an action method — don't
   assume "hidden in the menu" already means "protected."
6. **Scope any query the feature runs** by
   `webUserController.getLoggedInstitution()` (or the appropriate
   `DocumentHistory` field per the letters gotcha above) — a privilege check
   is not a substitute for a `WHERE institution = :ins` clause.
7. **Test logged in as a user without the privilege** and confirm both the UI
   is hidden *and* the action method rejects the call if hit directly.

## Pitfalls Specific to This App

- **UI-only gating.** Hiding a menu item with `rendered` does not secure the
  underlying action. If the feature is sensitive, check `hasPrivilege(...)`
  at the top of the action method too and bail out with
  `JsfUtil.addErrorMessage(...)` if it fails.
- **`toInstitution` vs `institution` on `DocumentHistory`.** Filtering "our
  unit's letters" by `toInstitution` leaks other institutions' outgoing
  "Our Letter" records addressed to you into your own report. Always filter
  on `institution` for "entered by us" semantics.
- **Legacy `documentGenerationType = null`.** An equality filter on this
  field (e.g. `= Received_by_institution`) silently excludes the historical
  majority of letters. Treat `null` as the outside-letter case explicitly.
- **"Assume Role."** `WebUserController` supports a national/admin user
  temporarily assuming another role/institution/area
  (`assumedRole`/`assumedInstitution`/`assumedArea`,
  `generateAssumedPrivileges(...)`). Any new institution- or
  privilege-sensitive code must be re-verified under an assumed identity, not
  just the real logged-in user — it's easy to read `loggedUser` directly
  somewhere and bypass the assumed context.
- **Renaming a `Privilege` enum constant.** Breaks every already-granted
  `UserPrivilege` row silently (`PrivilegeConverter` looks it up by name and
  returns `null` on a miss, treated as "no privilege" — a fail-closed but
  hard-to-diagnose regression). Deprecate, don't rename.
- **Copy-pasting a query without the institution parameter.** The fastest way
  to introduce a cross-institution leak in this codebase is duplicating an
  existing `LetterController`/`FileController` query for a new report and
  forgetting to carry the `d.institution=:ins` (or `h.institution=:ins`)
  clause and its `m.put("ins", webUserController.getLoggedInstitution())`
  binding along with it.
