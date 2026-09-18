---
name: ui-guidelines
description: >
  Practical workflow for building or editing a JSF/PrimeFaces XHTML page in DMIS —
  which existing page to copy as a template (data table, form, print register),
  how to wire p:ajax updates without breaking sibling components, PrimeFaces 14
  component gotchas not already in CLAUDE.md, and a pre-submit checklist. Use
  whenever creating a new .xhtml page or modifying an existing one under
  src/main/webapp.
---

# DMIS UI Development Workflow

This skill assumes you have already read `CLAUDE.md` at the repo root — it is the
source of truth for the styling rules (PrimeFaces button classes, no HTML heading
tags, Bootstrap for layout only, `p:autoComplete` with no dropdown, the
`h:selectOneRadio` table-spacing quirk, and the print-layout pattern). This file
does not repeat those rules; it covers the workflow around them — which files to
copy, how to wire ajax, and less-obvious component gotchas.

## 1. Pick a template, don't start from a blank file

Nearly every screen in this app is one of three shapes. Copy the closest match and
strip it down rather than composing a page from scratch — this keeps markup,
naming, and print behavior consistent across the app.

| Page shape | Copy this file |
|---|---|
| Filterable report/register with a `p:dataTable`, Process/Print/Download toolbar, and a hidden print panel | `institution/letters_entered_outside_registry.xhtml` |
| Same, but the table also drives row actions (accept/assign/view) tied to `DocumentHistory` state | `institution/letter_receive_register.xhtml` |
| Single-entity edit form with an ajax-toggled layout (fields appear/disappear based on a flag) | `document/letter.xhtml` |
| Simple list-only screen using the generic `institution/reports_index.xhtml` layout wrapper | anything under `institution/` with `<ui:composition template="/institution/reports_index.xhtml">` |

All pages use `<ui:composition template="/template1.xhtml">` (or a sub-template
like `institution/reports_index.xhtml` that itself wraps `template1.xhtml`) and
define `title` and `content` (or `reports`, for pages under the reports-index
template). Check which `ui:define` name the template you're wrapping actually
expects — `reports_index.xhtml` uses `reports`, not `content`.

Note that `institution/letter_receive_register.xhtml` also still contains a
leftover, unused second print attempt (a raw `p:dataTable` printed directly, plus
a dead `h:dataTable`/test `<table>` block below the real hidden-panel print
section). Copy its filter toolbar and the working `p:dataTable`, but model the
print section itself on `letters_entered_outside_registry.xhtml`'s cleaner
`gridPrint` panel (institution header block, then a plain `<table>` built with
`ui:repeat`) — do not replicate the dead code.

## 2. Wiring `p:ajax` correctly

Look at `document/letter.xhtml` for the canonical patterns:

- **Scope `process` to `@this`** for a field that only needs to save/validate
  itself on blur — don't let it default to processing the whole form:
  ```xhtml
  <p:inputText ... value="#{letterController.selected.documentNumber}">
      <p:ajax event="blur" process="@this" global="false" />
  </p:inputText>
  ```
- **`update` targets must be `id`s on `h:panelGroup`/`p:outputPanel`, never a bare
  `div`/`span`** — CLAUDE.md already calls this out project-wide, and it's the
  #1 cause of a `p:ajax update` silently doing nothing. `letterGrid` and `regNo`
  in `letter.xhtml` are both `h:panelGroup` ids for exactly this reason.
- **Toggling a section of the form** (e.g. Outside Letter vs Our Letter changing
  which fields render): fire the ajax off the control that changes state, and
  `update` the wrapping `h:panelGrid`/`h:panelGroup` that contains every
  conditionally-rendered field, not just the one field that looks different:
  ```xhtml
  <p:ajax event="change" listener="#{letterController.toggleLetterDirection}"
          process="@this" update="letterGrid" global="false" />
  ```
- **`global="false"`** on frequent, low-stakes ajax calls (blur validation on
  every field) avoids triggering the global ajax status / loading indicator for
  every keystroke-adjacent event — reserve the global indicator for actions that
  actually take noticeable time (Process, Save, Download).
- **Cascading selects**: `letter.xhtml`'s `method` → `regNo` cascade uses
  `f:ajax` instead of `p:ajax` — that's an inconsistency in the existing code,
  not a pattern to copy. Use `p:ajax` for new cascading selects so behavior
  (partial submit, `global`) is consistent with the rest of the page.
- A `p:selectOneMenu`/`p:selectOneRadio` bound to an enum or entity needs either
  a real "unselected" `f:selectItem` (`<f:selectItem itemLabel="Select" />`) or a
  non-null default value — an enum-backed menu with no blank option and no
  default silently binds to the first enum constant, which then looks like a
  populated field to the user.

## 3. PrimeFaces 14 gotchas beyond CLAUDE.md's list

- **`p:autoComplete` binding entities** needs `var` + `itemLabel` + `itemValue`
  pointing at the full object (see `completeInsOrUsersByWords` usage in
  `letters_entered_outside_registry.xhtml` and `letter.xhtml`), and
  `forceSelection="true"` if a free-typed, unmatched string must not be allowed
  to bind — without it the property can end up holding a plain `String` instead
  of the entity, and downstream EL expressions like `#{insf.displayName}` throw.
- **`p:dataExporter`** targets the on-screen `p:dataTable`'s `id` directly
  (`target="tbl"`) — it exports what's currently rendered/filtered, including
  pagination-page-only data unless the table's `rowsPerPageTemplate` includes an
  `All` option the user has selected. Mark any action-only column
  `exportable="false"` (see the `Actions` column in
  `letters_entered_outside_registry.xhtml`) so button markup doesn't leak into
  the spreadsheet.
- **`f:convertDateTime` pattern mismatches**: this codebase is inconsistent
  about date patterns per screen (`dd MMMM yyyy`, `dd MM yyyy`, `dd/MM/yy`) —
  match whatever pattern the *other* dates on the same page/report already use
  rather than picking a new one, since registers are printed side by side.
- **Entity equality in `rendered`/`eq` checks**: comparisons like
  `#{c.document.fromInstitution.name eq 'Personal'}` compare a *name string*,
  not the entity — copy this exact pattern for the "Personal" sender special
  case rather than trying to compare institution objects, since `Personal` is
  effectively a sentinel institution name across the app.
- **`p:panelGrid` vs `h:panelGrid`**: `h:panelGrid` is the plain layout table
  used for filter/print rows; `p:panelGrid` adds PrimeFaces theming/borders and
  is used inside toolbars that need a bordered sub-grid (see the filter
  `p:panelGrid columns="2" class="border border-light"` nested inside an
  `h:panelGrid` in `letter_receive_register.xhtml`). Don't mix them up expecting
  identical visual output.
- **`p:growl` placement**: every form-bearing page has a single `<p:growl />`
  near the top, outside/before the `h:form` content it reports on — put one on
  every new page rather than relying on a global one from the template.

## 4. Pre-submit checklist

Before considering an XHTML change done, check:

- [ ] Buttons use `ui-button-*` (PrimeFaces) classes for actual submit/action
      buttons, or the plain Bootstrap `btn btn-*` classes only where the rest of
      that page's toolbar already uses `btn btn-*` for consistency (don't mix
      both conventions on the same toolbar — check what the page you copied
      from used and stay consistent within that page).
- [ ] No `h1`–`h6` tags anywhere; heading-style text is `h:outputText`/
      `p:outputLabel` with a CSS class.
- [ ] Every ajax `update` target is a `h:panelGroup`/`p:outputPanel`/component
      `id`, never a raw `div id="..."`.
- [ ] Every `&` in an attribute value (button labels like "Assign & Accept",
      query strings) is escaped as `&amp;`.
- [ ] If the page has a Print button, it targets a dedicated hidden
      `d-none d-print-block` panel with a plain `<table>`, not the on-screen
      `p:dataTable` — check that pagination/filter/Actions markup is genuinely
      absent from the print panel.
- [ ] Any `p:autoComplete` has no `dropdown="true"`.
- [ ] `h:selectOneRadio` (or any bare-`<table>`-rendering component) has its own
      `styleClass` with `margin`/`td padding` reset if it sits inline next to
      other controls — don't try to Bootstrap-flex it.
- [ ] Date-type filter UI reuses `SearchFilterType` (`commonController.searchFilterTypes`)
      rather than a bespoke enum, and the `Received_by_institution`/legacy-null
      handling from CLAUDE.md's Letter Domain Model section is respected if the
      page filters `DocumentHistory` by direction.
- [ ] If only XHTML changed (no Java), no rebuild is required for NetBeans
      hot-deploy; if deploying a packaged WAR via `asadmin deploy`, a full
      `mvn package` is required for XHTML changes to actually take effect.
- [ ] Load the page once and visually compare against the template it was
      copied from — card header color, toolbar alignment, column widths, and
      font sizes (some registers deliberately use compressed `0.7rem`/`0.75rem`
      styling for dense print-style tables; don't mix that into a normal-density
      screen).
