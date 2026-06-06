# Letter import (PDF → split → OCR → review → create)

This feature lets a user upload a single PDF that contains many letters
separated by blank pages. The application splits it into individual letters,
OCRs each one with **the user's own Claude API key**, proposes registry
metadata, and lets the user review and create letters one by one.

## Setup

### 1. Per-user Claude API key
Each user provides their own Anthropic key (billing is on their own account):

- Account menu (top-right) → **My Claude API Key**
- Paste the key (`sk-ant-...`), optionally set a model override, **Save Key**.
- The key is stored **encrypted** (`CryptoService`, Jasypt `StrongTextEncryptor`)
  and never shown again — only `sk-ant-••••last4`.

Get a key at <https://console.anthropic.com/settings/keys>.

### 2. Privilege
Grant the **Import Letters from PDF** privilege (`Privilege.Import_Letters`) to
users who may use the feature. The menu item (Create ▸ *Import Letters from PDF*)
and page are gated by it.

### 3. Operator configuration
Set the encryption secret in production so keys are not recoverable from a DB
dump with the default passphrase:

```
-Ddmis.crypto.secret=<long-random-string>        # or env DMIS_CRYPTO_SECRET
```

Optional tuning (system property → env var → default), read by
`LetterImportConfig`:

| Setting | System property | Env var | Default |
|---|---|---|---|
| Default Claude model | `dmis.letterImport.model` | `DMIS_LETTER_IMPORT_MODEL` | `claude-sonnet-4-6` |
| Blank-page threshold (ink fraction) | `dmis.letterImport.blankThreshold` | `DMIS_LETTER_IMPORT_BLANK_THRESHOLD` | `0.005` |
| Render DPI | `dmis.letterImport.renderDpi` | `DMIS_LETTER_IMPORT_RENDER_DPI` | `150` |
| Max pages per PDF | `dmis.letterImport.maxPages` | `DMIS_LETTER_IMPORT_MAX_PAGES` | `500` |
| Batch retention days | `dmis.letterImport.retentionDays` | `DMIS_LETTER_IMPORT_RETENTION_DAYS` | `7` |

## Workflow

1. **Create ▸ Import Letters from PDF**.
2. **Upload** a PDF whose individual letters are separated by blank pages.
3. **Start Processing.** A background job (`LetterImportService`, `@Asynchronous`)
   splits the PDF at blank pages (`PdfSplitService`), renders a preview, and asks
   Claude to OCR each letter and resolve the sending institution / signatory to
   real DMIS records (`institution_search` / `staff_search` tools). The page
   shows a live progress bar.
4. **Review** each letter: the rendered page on the left, the proposed metadata
   on the right (subject, letter date, **stamp/received date defaulting to
   today**, from-institution, sender name, registration/reference no).
   - **Accept & Create** — creates a `DocumentType.Letter` letter and attaches
     the rendered page image(s).
   - **Save Edits** — keeps your edits on the staged item without creating a
     letter yet.
   - **Discard** — drops the letter.
   - **Skip / Next**, **Previous** — navigate without deciding.

## Data lifecycle & privacy

- The uploaded PDF and per-letter proposals are held in `LetterImportBatch` /
  `LetterImportItem` only until reviewed; a scheduled job
  (`LetterImportCleanupBean`, every 6 h) purges batches older than the retention
  window.
- Letter content is sent to Anthropic for OCR using the user's own key. No
  application-wide key is used and keys are never logged.

## Components

| Concern | Class |
|---|---|
| PDF split / render | `ejb/PdfSplitService` |
| Per-user key | `entity/UserClaudeApiKey`, `bean/ClaudeApiKeyController`, `ejb/CryptoService` |
| Claude client + tools | `ejb/AnthropicApiService` |
| Metadata extraction | `ejb/LetterExtractionService` |
| Staging | `entity/LetterImportBatch`, `entity/LetterImportItem` (+ facades) |
| Orchestration | `ejb/LetterImportService`, `ejb/LetterImportTxBean` |
| Review UI | `bean/LetterImportController`, `webapp/document/letter_import.xhtml` |
| Config / cleanup | `ejb/LetterImportConfig`, `ejb/LetterImportCleanupBean` |
