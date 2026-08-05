# Project Preferences

## Build & Run
- The user prefers to compile and run the application themselves
- Do not attempt to run maven compile, build, or deploy commands
- If there are errors, the user will report them
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

## JSF / XHTML Rules
- NEVER use plain HTML elements (`div`, `span`) with `id` attributes as AJAX update targets — use `h:panelGroup`, `p:outputPanel`, etc.
- Always escape `&` as `&amp;` in XHTML attribute values
- Use `p:growl` or `p:messages` for user feedback (not Bootstrap alerts)

## Deployment Notes
- Changes to `resources/ezcomp/*.xhtml` (composite components like `menu.xhtml`) require a **full Payara domain restart** to take effect — hot-redeploy is not enough
- JDBC datasource: `jdbc/dmis` (production MySQL)
