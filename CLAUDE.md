# Project Preferences

## Build & Run
- Claude may compile, build, and deploy the application when asked to
- JSF-only changes (XHTML only, no Java changes) do not require compilation

## Project Info
- This is a JSF/PrimeFaces Java web application (DMIS - Document Management Information System)
- Uses JPA for persistence
- Session-scoped managed beans
- PrimeFaces version: **14.0.0**
- Deployed on Payara 5 (domain1)

## PrimeFaces 14 API — Breaking Changes

### DataTable Row Selection (CHANGED in v14)
The old `selectionMode="multiple"` on `p:column` no longer works. Use the new API:

```xhtml
<!-- ✅ PrimeFaces 14 — correct -->
<p:dataTable selectionMode="multiple"
             selection="#{bean.selectedItems}"
             rowKey="#{item.id}">
    <p:column selectionBox="true" style="width:2.5em; text-align:center;"/>
    ...
</p:dataTable>

<!-- ❌ Old API — broken in v14 -->
<p:dataTable selection="#{bean.selectedItems}" rowKey="#{item.id}">
    <p:column selectionMode="multiple"/>
</p:dataTable>
```

Key points:
- `selectionMode="multiple"` moves to `p:dataTable` (not `p:column`)
- `selectionBox="true"` on `p:column` replaces `selectionMode="multiple"` on `p:column`
- `selectionRowMode` replaces the old `rowSelectMode` attribute (values: `new`, `add`, `none`)
- When using `p:columnGroup` headers, also put `selectionBox="true"` on the header column inside the group

### Other Selection Attributes (v14)
| Attribute | On | Description |
|---|---|---|
| `selectionMode` | `p:dataTable` | "single" or "multiple" |
| `selectionBox` | `p:column` | Boolean — renders checkbox/radio in that column |
| `selectionRowMode` | `p:dataTable` | How row clicks select: "new", "add", "none" |
| `selectionPageOnly` | `p:dataTable` | Select-all affects current page only (default: true) |
| `selectionDisabled` | `p:dataTable` | Disables selection conditionally |
| `showSelectAll` | `p:dataTable` | Show/hide the select-all header checkbox |

## UI Styling Rules

### Button Classes — Use PrimeFaces, NOT Bootstrap btn-*
```xhtml
<!-- ✅ Correct -->
<p:commandButton class="ui-button-success" value="Save"/>
<p:commandButton class="ui-button-warning" value="Process"/>
<p:commandButton class="ui-button-danger" value="Delete"/>
<p:commandButton class="ui-button-info" value="View"/>

<!-- ❌ Wrong — Bootstrap classes don't integrate with PrimeFaces themes -->
<h:commandButton class="btn btn-success" value="Save"/>
```

### Typography
- Do NOT use HTML heading tags (h1–h6) — this is an ERP system, not a website
- Use `h:outputText` with CSS classes for any heading-like text

### Bootstrap is OK for Layout Only
- Grid: `row`, `col-*` — fine
- Spacing: `mb-3`, `mt-2`, `p-2` — fine
- Flexbox utilities: `d-flex`, `gap-2` — fine
- Button/Alert/Modal classes — use PrimeFaces equivalents instead

### p:autoComplete — No Dropdown Button
- Do NOT set `dropdown="true"` on `p:autoComplete`. This project's convention is a plain type-ahead field only, no dropdown trigger button.

### h:selectOneRadio Renders a Bare `<table>`
- Applying Bootstrap flex classes (`d-flex`, etc.) to `h:selectOneRadio` does nothing useful — it renders as an HTML `<table>`, and this site's global CSS pads bare tables/cells heavily (`table { margin: 25px 0 }`, `td { padding: 12px 15px }`), which throws off alignment next to other inline controls. Give it its own `styleClass` and override `margin`/`td padding` directly in a scoped `&lt;style&gt;` block instead of trying to flex it.

### Print Layout — Dedicated Plain Table, Not the On-Screen DataTable
For a "Print" button, don't `<p:printer target="...">` the on-screen `p:dataTable` directly — it carries pagination controls, column filter inputs, and cell borders/shading that print poorly (especially on dot-matrix printers) and pull in UI chrome that doesn't belong on a printed register. Instead build a second, hidden panel with a plain HTML `<table>` (no borders, tight padding, black text, small font) and point `p:printer` at that panel's id:
```xhtml
<h:panelGroup layout="block" id="gridPrint" class="d-none d-print-block m-0 p-0 w-100">
    <!-- institution header, report title, date range, then a bare <table> via ui:repeat -->
</h:panelGroup>
...
<p:printer target="gridPrint" />
```
See `institution/letter_received_registry.xhtml` and `institution/letter_receive_register.xhtml` for worked examples.

## JSF / XHTML Rules
- NEVER use plain HTML elements (`div`, `span`) with `id` attributes as AJAX update targets — use `h:panelGroup`, `p:outputPanel`, etc.
- Always escape `&` as `&amp;` in XHTML attribute values
- Use `p:growl` or `p:messages` for user feedback (not Bootstrap alerts)

## Letter Domain Model — Key Gotchas

### Outside Letter vs Our Letter (`letter.xhtml`)
The entry form's "Outside Letter"/"Our Letter" toggle (`letterController.outsideLetter`, boolean, default `true`) sets `Document.documentGenerationType` and drives different `DocumentHistory` field wiring — this is easy to get backwards:
- **Outside Letter** → `documentGenerationType = Received_by_institution`; the created `DocumentHistory.toInstitution` = the entering institution (the letter is *received by* them).
- **Our Letter** → `documentGenerationType = Created_by_institution`; `DocumentHistory.toInstitution` = whatever institution the user picks as recipient — which can legitimately be *any* institution, including one that isn't the entering institution.
- `DocumentHistory.institution` is always set to the entering institution's, regardless of the toggle — **this is the field to filter on when you mean "letters entered by our unit"**, not `toInstitution`. Filtering by `toInstitution` instead means another institution's own outgoing "Our Letter" addressed to you leaks into your own "entered by us" report.
- ~12,900 legacy letters predate this toggle entirely and have `documentGenerationType = null`. Treat `null` as "outside letter" (the only kind that existed before the toggle shipped) rather than excluding them — an equality-only filter silently empties out virtually the entire historical report.

### Date-Type Filtering — Reuse `SearchFilterType`
For "filter by which date field" UI (letter entered/system date vs. document date vs. stamp/received date), reuse the existing `lk.gov.health.phsp.enums.SearchFilterType` enum (`SYSTEM_DATE`/`DOCUMENT_DATE`/`RECEIVED_DATE`, exposed via `commonController.searchFilterTypes`) rather than inventing a new one — see `document/letter_search_by_date.xhtml` for the canonical usage. `getCode()` returns the `Document` field name (`createdAt`/`documentDate`/`receivedDate`); when querying `DocumentHistory` instead of `Document` directly, `SYSTEM_DATE` maps to `h.createdAt` but the other two need the `h.document.` prefix.

## Deployment Notes
- Changes to `resources/ezcomp/*.xhtml` (composite components like `menu.xhtml`) require a **full Payara domain restart** to take effect — hot-redeploy is not enough
- When the user runs the app themselves via NetBeans, XHTML-only edits hot-deploy on save — no rebuild needed.
- When deploying via `asadmin deploy` of a packaged WAR (e.g. Claude driving deployment directly), this does **not** apply: XHTML changes require a full `mvn package` + `asadmin deploy` to take effect. Verified by inspecting the live DOM after a browser refresh with no redeploy — stale markup was still served.
- JDBC datasource: `jdbc/dmis` (production MySQL)
