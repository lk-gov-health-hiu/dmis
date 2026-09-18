---
name: api-usage
description: >
  Conventions for calling or adding external HTTP API integrations in DMIS — the Anthropic
  (Claude) API used for letter-import OCR/extraction, and the SMS gateway. Use when writing
  code that calls an external service, wiring in a new one, handling its credentials, adding
  timeout/retry/error handling around it, or writing tests that must not hit the real
  service. Covers where secrets live (CryptoService / Payara config, never hardcoded), the
  java.net.http HttpClient pattern already used in this codebase, and how to stub external
  calls in tests.
---

# External API Usage (DMIS)

## Existing integrations in this codebase

| Integration | Entry point | Credential |
|---|---|---|
| Anthropic (Claude) Messages API — letter OCR + institution/staff resolution during letter import | `lk.gov.health.phsp.ejb.AnthropicApiService` (called from `LetterExtractionService` / `LetterImportService`) | Per-user key, `UserClaudeApiKey` entity, encrypted at rest |
| SMS gateway (`https://hims.health.gov.lk/sms-mw`) | `lk.gov.health.phsp.ejb.SmsManagerEjb` / `bean.SmsController` | App-wide key — currently hardcoded (see below) |

When adding a **new** external API call, model it on `AnthropicApiService` — it's the current, correct
pattern. Don't copy `SmsManagerEjb`/`SmsController`: that code predates these conventions and has the
exact problems this skill exists to prevent (hardcoded key, no timeout, no retry, `System.out.println`
instead of logging).

## Credentials — never hardcode

- Real, present problem: `SmsController.java` hardcodes the SMS provider key twice as the literal
  `"XAOHBFRNKKODDCNOUYB4587GDDS63DHJ"`. Don't add more of these. If you touch that file, migrate it
  off the literal rather than propagating the pattern elsewhere.
- Correct pattern, already in the codebase: `CryptoService` (Jasypt `StrongTextEncryptor`) encrypts a
  secret before persistence. Its passphrase resolves from the system property `dmis.crypto.secret`,
  then the env var `DMIS_CRYPTO_SECRET`, then a built-in fallback — operators must set one of the
  first two in production.
- Per-user credentials (e.g. each user's own Claude key) go through `CryptoService` into a dedicated
  entity (`UserClaudeApiKey`) — never stored plaintext, never re-read back into a page, only a masked
  `last4` shown for display (see `ClaudeApiKeyController`).
- App-wide credentials (one shared key, not per-user) belong in a Payara-level env var / system
  property set on the domain (e.g. `asadmin create-jvm-options -Dmyapi.secret=...`) or a JNDI
  resource, not in Java source, not in an XHTML/web.xml literal.
- Never log a raw key. `AnthropicApiService` logs the HTTP status and response body on error but never
  the `x-api-key` header — keep that split when adding new logging.

## Making the call

- Use `java.net.http.HttpClient` (the JDK 11+ client), as `AnthropicApiService` does — not the legacy
  `HttpURLConnection` style in `SmsManagerEjb`. Note `pom.xml` pins `source/target=1.8`; the *runtime*
  JRE must still be 11+ for this client to work (this is called out in `AnthropicApiService`'s
  javadoc). If ever forced onto a genuine Java 8 runtime, use the already-present `unirest-java`
  dependency instead of hand-rolled `HttpURLConnection`.
- Always set both a connect timeout (`HttpClient.newBuilder().connectTimeout(...)`) and a per-request
  timeout (`HttpRequest.newBuilder().timeout(...)`). Nothing in `SmsManagerEjb` sets either — don't
  repeat that; an unreachable external host would otherwise hang the calling EJB thread indefinitely.
- Bound any multi-step interaction (a tool-use loop, pagination, polling) with both a max-iteration
  count and a wall-clock deadline, the way `AnthropicApiService.sendMessage` does
  (`MAX_TOOL_ITERATIONS`, `loopDeadlineMs`) — don't let a misbehaving remote or loop condition run
  forever.
- Wrap the call in try/catch and return a typed result carrying an `errorMessage`/success flag (see
  `AnthropicResponse`, `LetterExtractionResult`) rather than letting an exception escape into the JSF
  request. These callers run from `@Stateless` EJBs invoked off page actions or `@Schedule` batch
  jobs; an unhandled exception there produces a poor user-facing error page or, worse, silently kills
  a scheduled timer.

## Error handling and retries

- Check the HTTP status code explicitly — a non-2xx response is not an exception with
  `java.net.http`, so treating it as success by accident is easy. `AnthropicApiService` checks
  `response.statusCode() != 200` before touching the body; do the same for any new caller.
- Distinguish transient failures (429, 5xx, connect/read timeouts) from permanent ones (4xx other than
  429). Nothing here currently retries; if a new integration can hit rate limits or a flaky network,
  add a small bounded retry (2-3 attempts, short backoff) around transient cases only — never retry a
  4xx.
- Surface failures to the user via `p:growl`/`p:messages`, per project convention, with an actionable
  message — not a raw stack trace or the raw response body.
- A failed call inside an `@Schedule` method (e.g. `SmsManagerEjb.myTimer`) must be caught and logged,
  not thrown — an uncaught exception there can stop the timer from being rescheduled.

## Testing without hitting the real service

- There is currently no JUnit/Mockito test infrastructure in this project (no test dependency in
  `pom.xml`, no `src/test/java`). If you add tests around a new or existing API caller, add that
  scaffolding at the same time rather than reaching for a live call to "just check it works."
- No automated test may make a real network call to Anthropic, the SMS gateway, or any other external
  host — a test that needs a live key or network access to pass is not safe for CI.
- Keep the actual HTTP call isolated behind a small, narrow method (as `AnthropicApiService.sendMessage`
  already is), so a test can substitute a fake at one of two levels:
  - **Caller level** — mock the EJB itself (Mockito `@Mock` / `when(...)`) when testing
    `LetterExtractionService`/`LetterImportService`, so the test never constructs a real `HttpClient`.
  - **Transport level** — for lower-level HTTP code, run a local stub with
    `com.sun.net.httpserver.HttpServer` bound to `localhost` (no extra dependency needed) and point
    the client at it instead of the real host.
- Cover both the happy path and at least one failure path (non-200 status, malformed JSON body,
  timeout). `AnthropicApiService` and `LetterExtractionService` are written to never throw out of
  their public methods, so assert on the returned error result rather than expecting an exception.
- Never use a real key in a test, including one pulled from `UserClaudeApiKey` or env config. If a
  test needs some key value to exercise the credential-plumbing path, use an obviously-fake constant
  (e.g. `"test-key-not-real"`) with the transport stubbed out.
