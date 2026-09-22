# Use PrimeFaces widgets, not Bootstrap components

This app has `primefaces.THEME = saga` configured in `WEB-INF/web.xml`, but on
most existing pages that theme is effectively invisible: pages use plain
`h:commandButton class="btn btn-*"` and Bootstrap `card`/`card-header` divs
instead of PrimeFaces widgets (`p:commandButton`, `p:panel`), so there's no
PrimeFaces markup for the theme to style in the first place.

`scss/src/imports.scss` already isolates Bootstrap into its own low-priority
CSS `@layer` specifically so PrimeFaces wins whenever both are present:

```scss
@layer bootstrap;
@import url('.../bootswatch/.../bootstrap.min.css') layer(bootstrap);
```

That only helps when a PrimeFaces widget is actually used. When a page reaches
for a bare Bootstrap component instead, the layering has nothing to compete
with. Apply this conversion on every JSF page you touch from now on, not just
when explicitly asked:

- `h:commandButton class="btn btn-success/danger/warning/info"` →
  `p:commandButton styleClass="ui-button-success/danger/warning/info"`
- Bootstrap `<div class="card"><div class="card-header">...<div class="card-body">`
  → `p:panel` with `<f:facet name="header">`
- `h:selectOneRadio` (renders a bare `<table>`, needs manual CSS spacing fixes)
  → `p:selectOneMenu` when the choice is small and doesn't need to always be
  visible — no table, no custom CSS needed
- Drop Bootstrap classes that only fake something a PrimeFaces widget already
  provides, e.g. `class="form-control"` on `p:autoComplete`, or
  `shadow bg-white rounded` utility classes wrapping a `p:dataTable`/`p:panel`
- **Keep** Bootstrap layout/spacing-only classes: `row`, `col-*`, `g-*`,
  `d-flex`, `gap-*`, `mb-*`/`mt-*`/`my-*`/`p-*` — those are fine, see
  CLAUDE.md's "Bootstrap is OK for Layout Only" rule

### Gotcha: `p:commandButton` defaults to AJAX

`h:commandButton` defaults to a full postback; `p:commandButton` defaults to
`ajax="true"`. Any button that needs a full page response — most importantly
anything using `p:dataExporter` (file download; an AJAX response can't stream
a file to the browser) — needs an explicit `ajax="false"` added when you swap
the component, or the behavior silently breaks.

## Custom SCSS can override PrimeFaces widgets too — check for dead/unscoped rules

Converting a page's markup isn't the whole story: this app's own `scss/src/`
files can leak into PrimeFaces' internal markup even when a page uses proper
PrimeFaces widgets throughout. Two patterns found and fixed while doing this
(on `institution/letters_entered_our_registry.xhtml`, discovered because the
`p:datePicker` calendar popup still looked unstyled):

1. **Unscoped element selectors bleed into PrimeFaces' own internal HTML.**
   `scss/src/tables.scss` used to have a bare `table { ... } th, td { padding:
   12px 15px !important }` block meant for plain Bootstrap-style tables. It
   applied to *every* `<table>` on the site, including ones PrimeFaces itself
   renders internally — e.g. the day grid inside `p:datePicker`'s calendar
   popup — giving them an unwanted "plain HTML table" look (drop shadow,
   heavy padding, `thead` background color) instead of the theme's own
   compact styling. Same root cause as the already-documented
   `h:selectOneRadio` bare-`<table>` gotcha in CLAUDE.md, just hitting
   different markup. Fix: don't write unscoped `table`/`td`/`th` rules —
   scope any custom table styling to a specific class, or use PrimeFaces'
   own table-styling hooks (`p:dataTable` `styleClass`, etc.).

2. **Dead overrides for a PrimeFaces version this app no longer runs.**
   `scss/src/overrides.scss` had a long list of rules targeting old
   `ui-*`-prefixed PrimeFaces class names (`ui-datepicker-header`,
   `ui-datepicker-calendar`, `ui-timepicker-div`, `ui-autocomplete`,
   `ui-autocomplete-panel`, `ui-paginator`, `ui-chkbox-box`,
   `ui-selection-column`, `ui-selectonemenu-items-wrapper`,
   `ui-columntoggler-items`, `ui-timepicker`, `ui-inputfield`, `ui-shadow`).
   PrimeFaces 14 renders `p-*`-prefixed classes instead, so none of those
   rules matched anything in the DOM — pure dead weight that also made the
   file misleading to read (looked like deliberate styling that was actually
   inert). Removed. When auditing `overrides.scss` going forward, check any
   `ui-*`-prefixed selector against the actual rendered DOM class (dev tools,
   or `read_page`/`javascript_tool` via Claude-in-Chrome) before trusting
   that it still does anything.

## Rebuilding the SCSS

`src/main/webapp/resources/css/styles.min.css` is a **generated** file, not
hand-edited. Source lives in `scss/src/*.scss`, built via gulp
(`scss/package.json`, `scss/gulpfile.js`) — this is a separate step from
`mvn package`, so any SCSS change needs:

```bash
cd scss
npm install   # first time only
npx gulp scss minify
```

`minify` writes to both `scss/dist/styles.min.css` and
`src/main/webapp/resources/css/styles.min.css`. Only after that will
`mvn package` pick up the change for the WAR.
