# Working with assigned letters (REST API)

This guide explains how a user works through the letters assigned to them using
the DMIS REST API — authenticating, finding assigned letters, reading a letter
and its scanned pages, replying, and re-assigning. All examples use `curl`.

The same actions are available in the web UI (`LetterController`); this document
covers the programmatic path exposed by `LetterResource`.

## Base URL

The API is mounted at the `/api` application path under the web context root
(`/dmis`). On a local deployment:

```
http://localhost:8080/dmis/api
```

All endpoints produce/consume `application/json` unless noted.

## 1. Authenticate

Exchange a username/password for an API key:

```bash
curl -s -X POST http://localhost:8080/dmis/api/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"<user>","password":"<pass>"}'
```

```json
{ "status": "success", "code": 200,
  "data": { "apiKey": "1f72d650-...-6f2cf621dd1b", "username": "<user>" } }
```

Send the returned value as an **`Api-Key`** header on every other request.

### Who is the "acting user"?

State-changing endpoints and the `inbox/*` reads need an **actor** — the
`WebUser` recorded for audit fields, ownership transfers and history entries.
It is resolved in this order:

1. The **`X-Acting-User-Id`** header, if it resolves to a non-retired user.
2. The key's owner (`ApiKey.createdBy`) — set automatically for keys minted via
   `/auth/token`.

So a key obtained from `/auth/token` already acts as you; the
`X-Acting-User-Id` header is only needed to act on behalf of another user (or
with a key that has no owner).

```bash
API=http://localhost:8080/dmis/api
KEY=<your-api-key>
auth=(-H "Api-Key: $KEY")          # add -H "X-Acting-User-Id: <id>" to override the actor
```

## 2. Find your assigned letters

The inbox endpoints (most recent first; `?size=N`, default 20, max 100):

| Endpoint | Returns |
|---|---|
| `GET /letters/inbox/assigned-to-me` | Letters **assigned** to you, not yet accepted |
| `GET /letters/inbox/forwarded-to-me` | Letters **copy-forwarded** to you, not yet received |
| `GET /letters/inbox/to-receive` | Outstanding assignments **and** forwards addressed to you |
| `GET /letters/inbox/received-today` | Items you accepted since the start of today |
| `GET /letters/inbox/accepted` | All assignments/forwards you have accepted |
| `GET /letters/inbox/awaiting-institution` | Items forwarded to your institution, not yet received (`?institutionId=`) |

```bash
curl -s "${auth[@]}" "$API/letters/inbox/assigned-to-me?size=10"
```

Each entry is a `DocumentHistory` record; the letter itself is nested under
`letter` (with its `id`, `documentName`, `documentNumber`, `fromInstitution`,
`currentOwner`, `completed`, …). Use `letter.id` for the calls below.

### General search

`GET /letters` supports filtering across all letters (not just yours), e.g. by
owner, institution, status, date range, free text:

```bash
curl -s "${auth[@]}" "$API/letters?currentOwner=<myUserId>&completed=false&size=20"
curl -s "${auth[@]}" "$API/letters?q=HHIMS&fromDate=2026-06-01&dateField=receivedDate"
```

## 3. Read a letter and its pages

```bash
LID=<letterId>
curl -s "${auth[@]}" "$API/letters/$LID"                 # full metadata
curl -s "${auth[@]}" "$API/letters/$LID/attachments"     # attachment list (metadata)
```

Download a specific attachment binary (served with its original content type):

```bash
UID=<uploadId>
curl -s "${auth[@]}" "$API/letters/$LID/attachments/$UID" -o letter_$LID.pdf
```

Scanned letters are usually image-only PDFs (no text layer). To read the
content you must rasterize/OCR them — the in-app **Import Letters from PDF**
feature does this with Claude (see `letter-import.md`).

## 4. Reply to / act on a letter

There is no single "reply" endpoint. Use the workflow action that matches what
you actually want to do:

### Acknowledge / receive it

Accept the most recent outstanding assignment or forward addressed to you:

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters/$LID/receive" \
  -d '{"comments":"Received."}'
```

### Record an action taken (a minute / the closest thing to a reply)

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters/$LID/actions" \
  -d '{"comments":"Replied to RDHS that HIU cannot support until the new vendor is engaged.","itemId":null}'
```

### Mark it completed

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters/$LID/complete" -d '{"comments":"Closed."}'
```

### Issue a formal outgoing reply letter

Create a new letter that references the original:

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters" \
  -d '{"documentName":"Re: HHIMS installation","referenceDocumentId":'"$LID"',
       "toInstitutionId":53923,"documentDate":"2026-06-15"}'
```

Attach a signed scan / PDF (base64) to any letter:

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters/$LID/attachments" \
  -d '{"fileName":"reply.pdf","fileType":"application/pdf","base64":"<base64>"}'
```

## 5. Pass it on

### Assign to another user (transfers ownership)

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters/$LID/assign" \
  -d '{"toWebUserId":2017888,"comments":"Please correct Sinhala, print, get the Director'\''s signature.","minuteItemId":null}'
```

Bulk assign several letters at once: `POST /letters/assign` with
`{"toWebUserId":..., "letterIds":[...], "comments":"..."}`.

Undo the latest assignment (restores the previous owner): `POST /letters/$LID/unassign`.

### Forward or copy to a user or institution

Provide **exactly one** of `toWebUserId` / `toInstitutionId`:

```bash
curl -s -X POST "${auth[@]}" -H "Content-Type: application/json" \
  "$API/letters/$LID/forward" \
  -d '{"toInstitutionId":53923,"comments":"For necessary action."}'
```

## Typical flow

1. `POST /auth/token` → store `Api-Key`.
2. `GET /letters/inbox/assigned-to-me` → pick `letter.id`.
3. `GET /letters/{id}` + `GET /letters/{id}/attachments[/{uploadId}]` → read it.
4. Decide and act: `/receive`, `/actions`, `/complete`, `/forward`, `/assign`,
   or create a reply letter with `referenceDocumentId`.

## Responses & errors

All JSON responses are wrapped as
`{ "status": "success|error", "code": <http-code>, "data"|"message": ... }`.

| Code | Meaning |
|---|---|
| `401` | Invalid or missing `Api-Key` |
| `400` | Validation error, or the acting user could not be resolved |
| `404` | Letter / attachment not found (or retired, or not a letter) |
| `409` | No outstanding assignment/forward to receive or unassign |

## Related docs

- `letter-import.md` — importing & OCR-ing scanned letters with Claude.
- `LetterResource` (`src/main/java/lk/gov/health/phsp/ws/letter/`) — source of truth.
