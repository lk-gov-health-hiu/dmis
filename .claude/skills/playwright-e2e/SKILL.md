---
name: playwright-e2e
description: >
  Write and run Playwright end-to-end tests against DMIS, the JSF/PrimeFaces
  document management app. Use when asked to add, update, or run browser
  tests for a letter/document/institution workflow, or to verify a UI change
  actually works in a real browser rather than just compiling. Covers
  PrimeFaces AJAX waiting, p:dataTable/p:commandButton/growl selectors, and
  where test files live in this repo.
---

# Playwright E2E Testing (DMIS)

DMIS is a server-rendered JSF/PrimeFaces 14 app, not a single-page app. Almost
every meaningful interaction (`p:commandButton`, `p:selectOneMenu`,
`p:dataTable` paging/sorting, `f:ajax`/`p:ajax` listeners) fires a partial
AJAX postback that patches part of the DOM in place — there is no client-side
route change to wait for. Tests must wait on the *effect* of that postback
(a growl message, an updated panel, a new row) rather than on navigation or a
fixed timeout.

## Where tests live

```
e2e/
  tests/
    letters/
      letter-entry.spec.ts
      letter-search-by-date.spec.ts
    institutions/
      manage-institutions.spec.ts
  fixtures/
    auth.ts          # shared login helper / storageState setup
  playwright.config.ts
```

Group spec files by domain area (`letters/`, `institutions/`, `reports/`)
mirroring the `src/main/webapp/<area>/` layout, so a test is easy to find from
the page it covers. One spec file per page or workflow, not per bean method.

If `e2e/` doesn't exist yet, create it with `npm init playwright@latest` run
from the repo root (or `e2e/` if you want tests isolated from the Maven
build), targeting TypeScript. Do not put Playwright's `node_modules` or
config inside `src/main/webapp` — it is not part of the deployed WAR.

## Running tests

```bash
npx playwright test                       # headless, all specs
npx playwright test letters/              # one domain area
npx playwright test --headed --debug      # step through interactively
npx playwright show-report                # inspect the last HTML report
```

Set the base URL via `playwright.config.ts`'s `use.baseURL` (or a
`PLAYWRIGHT_BASE_URL` env var) pointing at the running Payara instance, e.g.
`http://localhost:8080/dmis`. Tests need a real deployment — start/redeploy
the app first per the deployment notes in `CLAUDE.md` before running the
suite; XHTML-only edits hot-deploy on save in NetBeans, but a Maven-packaged
deploy needs `mvn package` + `asadmin deploy` first.

## Login and session setup

Log in once in a `setup` project and reuse `storageState` across specs rather
than re-submitting the login form in every test — JSF session/view state
makes per-test login slow and easy to get wrong (a stale `javax.faces.ViewState`
submitted after the page moved on will 500).

```ts
// e2e/fixtures/auth.ts
import { test as base } from '@playwright/test';

export const test = base.extend({
  storageState: 'e2e/.auth/user.json',
});
```

```ts
// e2e/auth.setup.ts
import { test as setup } from '@playwright/test';

setup('authenticate', async ({ page }) => {
  await page.goto('/index.xhtml');
  await page.getByLabel('Username').fill(process.env.DMIS_TEST_USER!);
  await page.getByLabel('Password').fill(process.env.DMIS_TEST_PASS!);
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForSelector('text=Document Management Information System');
  await page.context().storageState({ path: 'e2e/.auth/user.json' });
});
```

Never hardcode real credentials in a spec — read them from environment
variables and keep `e2e/.auth/*.json` out of git.

## Waiting on PrimeFaces AJAX — do this, not `waitForTimeout`

PrimeFaces marks every AJAX request/response window with the `pfAjaxStart` /
`pfAjaxComplete` events, so wait on those instead of a network idle guess or a
sleep:

```ts
async function waitForPfAjax(page: Page) {
  await page.waitForFunction(() => (window as any).PrimeFaces?.ajax?.Queue?.isEmpty?.() ?? true);
}
```

More often it's simpler and more robust to wait on the concrete thing the
AJAX call is supposed to produce:

```ts
// Clicking Process re-renders the dataTable body — wait for a row, not a timeout
await page.getByRole('button', { name: 'Process' }).click();
await page.locator('.reg-table table tbody tr').first().waitFor();

// A commandButton wired to p:ajax growl feedback
await page.getByRole('button', { name: 'Save' }).click();
await page.locator('.ui-growl-message').waitFor();
```

Avoid `page.waitForNavigation()` for anything except a genuine `<h:link>`/
redirect — an ajaxified `h:commandButton`/`p:commandButton` postback does not
navigate, it patches the DOM, so waiting for navigation will hang until
timeout.

## Selecting PrimeFaces components

PrimeFaces renders each component as a `<span>`/`<div>` wrapper with generated
`id`s (`j_idt42:tbl` etc.) around the real form control, so prefer
role/text/label locators over raw CSS ids, which are unstable across JSF view
changes:

- **`p:commandButton` / `h:commandButton`** — `page.getByRole('button', { name: 'Save' })`.
  Both render an `<button>`/`<input type=submit>`; PrimeFaces buttons carry
  `ui-button-*` classes (see `CLAUDE.md` — this repo uses `ui-button-success`
  /`-warning`/`-danger`/`-info`, not Bootstrap `btn-*`), which is useful for a
  `locator('.ui-button-danger')` fallback when several buttons share a label
  (e.g. multiple row-level "View" buttons in a `p:dataTable`).
- **`p:selectOneMenu`** — not a native `<select>`. Click to open, then click
  the option:
  ```ts
  await page.locator('.ui-selectonemenu').filter({ hasText: 'Filter output' }).click();
  await page.getByRole('option', { name: 'Document Date' }).click();
  ```
- **`p:dataTable`** — scope row/cell locators under the table's client id or
  a wrapping `styleClass` (e.g. `.reg-table`) rather than the whole page, and
  index by column header text so column order changes don't silently break
  the test:
  ```ts
  const row = page.locator('.reg-table tbody tr').filter({ hasText: 'LTR-2024-0113' });
  await row.getByRole('button', { name: 'View' }).click();
  ```
- **`p:growl` / `p:messages`** — assert on `.ui-growl-message` /
  `.ui-messages-info`/`.ui-messages-error` text, since this app always
  surfaces success/validation feedback there rather than a JS `alert()` or a
  Bootstrap toast:
  ```ts
  await expect(page.locator('.ui-growl-message')).toContainText('Letter saved');
  ```
- **`p:datePicker`** — type into the input directly rather than driving the
  popup calendar; it's far less brittle:
  ```ts
  await page.locator('input[id$="fromDate_input"]').fill('01 September 2026 12:00 AM');
  ```
- **Confirm dialogs (`p:confirmDialog` on a delete/reverse action)** — these
  are in-page PrimeFaces widgets, not native `confirm()` — click the rendered
  "Yes" button, don't use Playwright's `page.on('dialog', ...)`.

## Writing tests against this app's domain

Prefer end-to-end flows that mirror what a real registry clerk does, not
button-by-button unit checks:

- **Letter entry** (`letter.xhtml`) — cover both the "Outside Letter" and
  "Our Letter" paths through the toggle described in `CLAUDE.md`; after
  saving, assert the growl success message and that the new row appears in
  the relevant register (`institution/letter_receive_register.xhtml` etc.)
  rather than just asserting the form cleared.
- **Registers/reports with a date-type filter** — when a test drives the
  `SearchFilterType` dropdown (system/document/received date), assert the
  filtered row set actually changes between date types, since a filter that
  silently no-ops is the class of bug this UI has hit before.
- **Print panels** — don't assert against the on-screen `p:dataTable`'s
  print output; if a page follows the hidden-panel print pattern in
  `CLAUDE.md`, assert against that panel's own rows (e.g. `#gridPrint table
  tr`), since it's a separate markup tree from the paginated on-screen table.
- Seed data through the running app's own forms where practical (create a
  test institution/letter via the UI) instead of relying on production data
  being present, so specs are reproducible against a fresh database.

## Flagging product gaps

If a control can't be found by role/label/text and only by a generated JSF
id, treat that as a real accessibility/testability gap in the page (missing
`<h:outputLabel for="...">`, no discernible button text, etc.) worth fixing
in the XHTML — not something to route around with a brittle id selector.
