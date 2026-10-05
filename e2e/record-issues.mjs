// Issue recorder for DMIS.
//
// Opens ONE headed Chrome window on the running app. The user logs in and
// demonstrates problems; everything is logged to e2e/sessions/<timestamp>/:
//   session.md      - step log (clicks, inputs, navigations, errors) + issues
//   shots/          - a screenshot per page load and per reported issue
//   issue-N.html    - page HTML at the moment each issue was reported
//   trace.zip       - full Playwright trace (npx playwright show-trace ...)
//
// The page gets a floating "Report issue" / "End session" bar. Report issue
// asks for a description and snapshots the page; End session stops the trace
// and closes the browser cleanly.
//
// Run:   node record-issues.mjs [--smoke]
// Env:   PLAYWRIGHT_BASE_URL (default http://localhost:8080/dmis/)
// --smoke reports one fake issue and ends the session automatically (self-test).

import { chromium } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

const BASE = process.env.PLAYWRIGHT_BASE_URL ?? 'http://localhost:8080/dmis/';
const SMOKE = process.argv.includes('--smoke');

const stamp = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19);
const dir = path.resolve('sessions', stamp);
fs.mkdirSync(path.join(dir, 'shots'), { recursive: true });
const md = path.join(dir, 'session.md');
fs.writeFileSync(md, `# DMIS issue session ${stamp}\n\nStart URL: ${BASE}\n\n## Steps\n\n`);

const t0 = Date.now();
const ts = () => `[${String(Math.round((Date.now() - t0) / 1000)).padStart(4, ' ')}s]`;
const recent = [];               // last steps, copied into each issue
const issues = [];
let shotNo = 0;
let finished = false;

const APP_HOST = new URL(BASE).host;
const isApp = url => { try { return new URL(url).host === APP_HOST; } catch { return false; } };
const short = url => url.length > 160 ? url.slice(0, 160) + '…' : url;

function step(line) {
  if (finished) return;
  const s = `${ts()} ${line}`;
  fs.appendFileSync(md, `- ${s}\n`);
  recent.push(s);
  if (recent.length > 15) recent.shift();
  console.log(s);
}

// ---------------------------------------------------------------- in-page UI
// Runs on every document load (this app does full-page postbacks constantly).
const inPage = () => {
  const send = ev => { try { window.__rec(ev); } catch (e) { /* binding not ready */ } };
  const inOverlay = el => el && el.closest && el.closest('[data-rec]');

  const describe = el => {
    el = el.closest('button,a,input,select,textarea,label,[role=button],[role=option],[role=menuitem],[role=tab],li,td,th,.ui-selectonemenu,.ui-chkbox,.ui-radiobutton') || el;
    const tag = el.tagName.toLowerCase();
    // Never use a field's value here (it would leak passwords); values are logged
    // separately, masked. Only buttons' value is their visible caption.
    const isButton = tag === 'button' || ['submit', 'button', 'reset'].includes(el.type);
    const text = (el.getAttribute('aria-label') || el.innerText || (isButton ? el.value : '') || el.placeholder || el.title || '')
      .replace(/\s+/g, ' ').trim().slice(0, 70);
    let label = '';
    if (el.id) {
      const l = document.querySelector(`label[for="${CSS.escape(el.id)}"]`);
      if (l) label = l.innerText.trim();
    }
    return `${tag}${el.id ? '#' + el.id : ''}${text ? ` "${text}"` : ''}${label ? ` (label: ${label})` : ''}`;
  };

  document.addEventListener('click', e => {
    if (inOverlay(e.target)) return;
    send({ type: 'click', what: describe(e.target) });
  }, true);

  document.addEventListener('change', e => {
    const el = e.target;
    if (inOverlay(el) || !('value' in el)) return;
    let v = el.type === 'password' ? '••••' : (el.type === 'checkbox' || el.type === 'radio') ? String(el.checked) : el.value;
    if (el.tagName === 'SELECT') v = el.options[el.selectedIndex]?.text ?? v;
    send({ type: 'input', what: describe(el), value: String(v).slice(0, 120) });
  }, true);

  const build = () => {
    if (document.querySelector('[data-rec]')) return;
    const btn = (txt, bg) => {
      const b = document.createElement('button');
      b.type = 'button';
      b.textContent = txt;
      b.style.cssText = `background:${bg};color:#fff;border:0;border-radius:6px;padding:10px 14px;font:600 14px sans-serif;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,.3)`;
      return b;
    };
    const bar = document.createElement('div');
    bar.setAttribute('data-rec', '');
    bar.style.cssText = 'position:fixed;right:16px;bottom:16px;z-index:2147483647;display:flex;gap:8px';
    const bug = btn('🐞 Report issue', '#c0392b'); bug.setAttribute('data-rec-bug', '');
    const end = btn('■ End session', '#2c3e50'); end.setAttribute('data-rec-end', '');
    bar.append(bug, end);

    const modal = document.createElement('div');
    modal.setAttribute('data-rec', '');
    modal.style.cssText = 'position:fixed;inset:0;z-index:2147483647;background:rgba(0,0,0,.4);display:none;align-items:center;justify-content:center';
    modal.innerHTML = `
      <div style="background:#fff;border-radius:8px;padding:20px;width:min(520px,90vw);font:14px sans-serif;box-shadow:0 4px 20px rgba(0,0,0,.3)">
        <div style="font-weight:700;margin-bottom:8px">Describe the issue</div>
        <div style="color:#666;margin-bottom:8px">What is wrong, and what did you expect instead?</div>
        <textarea data-rec-text rows="6" style="width:100%;box-sizing:border-box;font:14px sans-serif;padding:8px"></textarea>
        <div style="display:flex;gap:8px;justify-content:flex-end;margin-top:12px">
          <button type="button" data-rec-cancel style="padding:8px 14px">Cancel</button>
          <button type="button" data-rec-save style="padding:8px 14px;background:#c0392b;color:#fff;border:0;border-radius:4px;font-weight:600">Save issue</button>
        </div>
      </div>`;
    const text = modal.querySelector('[data-rec-text]');
    text.addEventListener('keydown', e => e.stopPropagation());
    bug.onclick = () => { modal.style.display = 'flex'; text.value = ''; text.focus(); };
    modal.querySelector('[data-rec-cancel]').onclick = () => { modal.style.display = 'none'; };
    modal.querySelector('[data-rec-save]').onclick = () => {
      const d = text.value.trim();
      modal.style.display = 'none';
      if (d) send({ type: 'issue', description: d });
    };
    end.onclick = () => {
      end.textContent = 'Saving…';
      end.disabled = true;
      send({ type: 'end' });
    };
    document.body.append(bar, modal);
  };
  if (document.body) build(); else document.addEventListener('DOMContentLoaded', build);
};

// ------------------------------------------------------------------ browser
const browser = await chromium.launch({ channel: 'chrome', headless: false, args: ['--start-maximized'] });
const context = await browser.newContext({ viewport: null });
await context.tracing.start({ screenshots: true, snapshots: true });

async function errorMessages(page) {
  return page.locator('.ui-messages-error, .ui-message-error, .ui-growl-item-container.ui-state-error, .ui-growl-error')
    .allInnerTexts().then(a => a.map(s => s.replace(/\s+/g, ' ').trim()).filter(Boolean)).catch(() => []);
}

async function screenshot(page, name, fullPage) {
  const file = `shots/${String(++shotNo).padStart(3, '0')}-${name}.png`;
  await page.evaluate(() => document.querySelectorAll('[data-rec]').forEach(e => e.style.visibility = 'hidden')).catch(() => {});
  await page.screenshot({ path: path.join(dir, file), fullPage }).catch(() => {});
  await page.evaluate(() => document.querySelectorAll('[data-rec]').forEach(e => e.style.visibility = '')).catch(() => {});
  return file;
}

async function reportIssue(page, description) {
  const n = issues.length + 1;
  const shot = await screenshot(page, `issue-${n}`, true);
  fs.writeFileSync(path.join(dir, `issue-${n}.html`), await page.content().catch(() => ''));
  const errs = await errorMessages(page);
  const issue = { n, description, url: page.url(), shot, html: `issue-${n}.html`, errs, steps: [...recent] };
  issues.push(issue);
  step(`🐞 ISSUE ${n} reported: ${description.split('\n')[0]}`);
}

async function finish(reason) {
  if (finished) return;
  finished = true;
  let traceNote = 'trace.zip';
  try { await context.tracing.stop({ path: path.join(dir, 'trace.zip') }); }
  catch { traceNote = '(trace not saved - browser was closed directly instead of via End session)'; }

  let out = `\n## Issues (${issues.length})\n\n`;
  for (const i of issues) {
    out += `### Issue ${i.n}\n\n${i.description}\n\n` +
      `- URL: ${i.url}\n- Screenshot: ${i.shot}\n- Page HTML: ${i.html}\n` +
      (i.errs.length ? `- Error messages on page: ${i.errs.join(' | ')}\n` : '') +
      `\nSteps leading up to it:\n\n${i.steps.map(s => '    ' + s).join('\n')}\n\n`;
  }
  out += `## Session end\n\nEnded by: ${reason}\nTrace: ${traceNote}\n`;
  fs.appendFileSync(md, out);
  console.log(`\nSESSION ENDED (${reason}) - ${issues.length} issue(s). Log: ${md}`);
  await browser.close().catch(() => {});
  process.exit(0);
}

await context.exposeBinding('__rec', async ({ page }, ev) => {
  if (ev.type === 'click') step(`click ${ev.what}`);
  else if (ev.type === 'input') step(`input ${ev.what} = "${ev.value}"`);
  else if (ev.type === 'issue') await reportIssue(page, ev.description);
  else if (ev.type === 'end') await finish('End session button');
});
await context.addInitScript(inPage);

function attach(page) {
  page.on('framenavigated', f => { if (f === page.mainFrame()) step(`page ${f.url()}`); });
  page.on('load', async () => {
    const shot = await screenshot(page, 'page', false);
    step(`  loaded "${await page.title().catch(() => '')}" -> ${shot}`);
    for (const m of await errorMessages(page)) step(`  ⚠ error message: ${m}`);
  });
  page.on('console', m => { if (m.type() === 'error') step(`  ⚠ console error: ${m.text().slice(0, 300)}`); });
  page.on('pageerror', e => step(`  ⚠ JS exception: ${e.message.slice(0, 300)}`));
  page.on('requestfailed', r => {
    if (isApp(r.url())) step(`  ⚠ request failed: ${r.method()} ${short(r.url())} (${r.failure()?.errorText})`);
  });
  page.on('response', async r => {
    const url = r.url();
    if (!isApp(url)) return;
    if (r.status() >= 400) step(`  ⚠ HTTP ${r.status()} ${r.request().method()} ${short(url)}`);
    // JSF/PrimeFaces AJAX: errors come back as HTTP 200 <partial-response><error>
    if (['xhr', 'fetch'].includes(r.request().resourceType()) && (r.headers()['content-type'] ?? '').includes('xml')) {
      const body = await r.text().catch(() => '');
      const m = body.match(/<error>[\s\S]*?<error-name>([\s\S]*?)<\/error-name>[\s\S]*?<error-message>([\s\S]*?)<\/error-message>/);
      if (m) step(`  ⚠ JSF AJAX error: ${m[1].replace(/<!\[CDATA\[|\]\]>/g, '')}: ${m[2].replace(/<!\[CDATA\[|\]\]>/g, '').slice(0, 300)}`);
    }
  });
  page.on('close', () => step(`window closed: ${page.url()}`));
}

let firstPage = true;
context.on('page', p => {
  if (!firstPage) step('new window/tab opened');
  firstPage = false;
  attach(p);
});
browser.on('disconnected', () => finish('browser window closed'));
process.on('SIGINT', () => finish('interrupted'));

const page = await context.newPage();
console.log(`SESSION_DIR=${dir}`);
await page.goto(BASE);

if (SMOKE) {
  // A failed login exercises input/click logging and error-message capture.
  await page.getByLabel('Username').fill('nobody');
  await page.locator('input[type=password]').fill('wrong');
  await page.getByRole('button', { name: 'Login' }).click();
  await page.waitForLoadState('load');
  await page.waitForTimeout(1500);
  await page.locator('[data-rec-bug]').click();
  await page.locator('[data-rec-text]').fill('Smoke test issue: recorder self-check.');
  await page.locator('[data-rec-save]').click();
  await page.waitForTimeout(1500);
  await page.locator('[data-rec-end]').click();
}
