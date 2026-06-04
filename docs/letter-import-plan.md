# Plan: User-keyed Claude letter import (PDF → split → OCR → review → create)

## Goal
Let users upload a multi-page PDF in which individual letters are separated by
blank pages. The application temporarily holds the PDF, uses **the logged-in
user's own Claude API key** to OCR and split it into per-letter segments, renders
each letter to image(s), and proposes letter metadata (subject, letter date,
stamp/received date defaulting to today, sending institution/sender — grounded
against the in-built institution/staff API). The user reviews each proposed
letter and may **Accept & Create**, **Save edits**, **Discard**, or **Skip**.

## Design decisions (approved)
- **Split:** PDFBox detects blank separator pages locally and segments the PDF;
  each segment is sent to Claude (user's key) for OCR + metadata.
- **Key storage:** dedicated `UserClaudeApiKey` entity, AES-GCM encrypted at
  rest, one active key per user, masked in UI, never returned to the browser.
- **Staging:** `LetterImportBatch` + `LetterImportItem` persist the PDF and
  per-letter proposals; resumable, with a scheduled cleanup job.
- **Grounding:** Claude is given in-process `institution_search` / `staff_search`
  tools (the HMIS agentic pattern) so the proposed sender resolves to a real
  entity; UI autocomplete is the fallback.

## Key facts about the codebase
- A "letter" is the `Document` entity; managed by session-scoped
  `LetterController`. Attachments are the `Upload` entity (`baImage` LONGBLOB).
- An internal REST API already exists (`Api-Key` header): `InstitutionResource`,
  `UserResource`, `LetterResource`.
- Java 8 source target. PDF libs present: itext + flying-saucer (neither can
  rasterize) → add **PDFBox 2.0.x**.
- HMIS reference: `com.divudi.service.AnthropicApiService` (Stateless EJB, Java
  `HttpClient`, agentic tool loop, image+document base64 blocks). HMIS uses one
  app-wide key; DMIS must be per-user instead.

## Issues / PRs (sequential)
1. **PDF segmentation & rendering service** — add PDFBox; `PdfSplitService`
   (`detectSegments`, `extractSubPdf`, `renderPagesToPng`).
2. **Per-user encrypted Claude API key** — `UserClaudeApiKey` entity + facade,
   `CryptoService` (AES-GCM), `ClaudeApiKeyController`, `claude_api_key.xhtml`
   under "Manage My API Keys" menu, new privilege.
3. **AnthropicApiService port + DMIS tools** — port/trim HMIS service into
   `lk.gov.health.phsp.ejb`; add `institution_search`/`staff_search` tools;
   `LetterExtractionService.extractFromSegment` → strict-JSON metadata + usage.
   Open question: confirm runtime JRE (Java 11+ keeps JDK HttpClient; true Java 8
   → Apache HttpClient).
4. **Staging entities + async orchestration** — `LetterImportBatch` /
   `LetterImportItem` + facades; `LetterImportService` (`@Asynchronous`):
   detect → render + Claude extract → persist items; progress tracking.
5. **Review wizard UI + create-letter integration** — `LetterImportController`,
   `letter_import.xhtml` (upload → progress poll → wizard), Accept & Create /
   Save / Discard / Skip; reuse `LetterController`/`uploadFacade` patterns; menu
   + privilege.
6. **Config, cleanup, privileges, docs** — ConfigOptions (model, max pages,
   blank threshold, DPI, retention days), `@Schedule` cleanup, docs.

PRs land 1 → 2 → 3 → 4 → 5 → 6. Issues 1/2/3 have no cross-deps and may be
parallelized; 5 depends on 1–4; 3 depends on 2.

## Risks
- Runtime JRE drives the HTTP-client choice (Issue 3).
- Cost is on the user's own key; mitigated by per-request page caps + cheaper
  default model.
- Security: AES-GCM at rest, masked display, key excluded from DTOs/logs.
