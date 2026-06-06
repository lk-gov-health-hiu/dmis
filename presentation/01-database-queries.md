# DMIS Presentation — Database Queries

## How to Connect

The DMIS production database is at the Ministry of Health data centre (nchis.health.gov.lk).
It is **not** in the Azure HMIS credentials file — those are for carecode.org hospital clients.

To get connection details, check the Payara Admin Console of the DMIS production server:
1. Log in to the MoH server (SSH or direct access)
2. Open Payara Admin Console → Resources → JDBC → Connection Pools → `dmisprod`
3. Note the host, port, database name, username, and password

Once connected, run the queries below in order. Copy every result value into `02-infographic-prompts.md`
replacing the matching `{{PLACEHOLDER}}` token.

---

## Section A — Overall Letter Counts

```sql
-- A1: Total letters ever added (all time)
SELECT COUNT(*) AS total_letters_all_time
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0;

-- A2: Letters added in the last 12 months
SELECT COUNT(*) AS letters_last_12_months
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0
  AND CREATEDAT >= DATE_SUB(NOW(), INTERVAL 12 MONTH);

-- A3: Letters added in the last 30 days
SELECT COUNT(*) AS letters_last_30_days
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0
  AND CREATEDAT >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- A4: Letters added this calendar year
SELECT COUNT(*) AS letters_this_year
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0
  AND YEAR(CREATEDAT) = YEAR(NOW());
```

---

## Section B — Institution Activity

```sql
-- B1: Number of institutions that have added at least one letter
SELECT COUNT(DISTINCT INSTITUTION_ID) AS active_institutions
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0;

-- B2: Top 10 institutions by letter count (all time)
SELECT i.NAME AS institution_name,
       COUNT(d.ID) AS letter_count
FROM DOCUMENT d
JOIN INSTITUTION i ON d.INSTITUTION_ID = i.ID
WHERE d.DOCUMENTTYPE = 'Letter'
  AND d.RETIRED = 0
GROUP BY i.NAME
ORDER BY letter_count DESC
LIMIT 10;

-- B3: Institutions with letters entered in last 30 days (active institutions)
SELECT COUNT(DISTINCT d.INSTITUTION_ID) AS active_last_30_days
FROM DOCUMENT d
WHERE d.DOCUMENTTYPE = 'Letter'
  AND d.RETIRED = 0
  AND d.CREATEDAT >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- B4: Total institutions registered in the system
SELECT COUNT(*) AS total_registered_institutions
FROM INSTITUTION
WHERE RETIRED = 0;
```

---

## Section C — Copy / Forward Workflow

```sql
-- C1: Total copy/forward actions ever (letters forwarded to another institution)
SELECT COUNT(*) AS total_copy_forwards_all_time
FROM DOCUMENTHISTORY
WHERE HISTORYTYPE = 'Letter_Copy_or_Forward'
  AND RETIRED = 0;

-- C2: Copy/forwards in last 30 days
SELECT COUNT(*) AS copy_forwards_last_30_days
FROM DOCUMENTHISTORY
WHERE HISTORYTYPE = 'Letter_Copy_or_Forward'
  AND RETIRED = 0
  AND CREATEDAT >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- C3: Copy/forwards that have been received (acknowledged) - all time
SELECT COUNT(*) AS copy_forwards_received_all_time
FROM DOCUMENTHISTORY
WHERE HISTORYTYPE = 'Letter_Received'
  AND RETIRED = 0;

-- C4: Pending copy/forwards (forwarded but not yet received) - all time
SELECT
  (SELECT COUNT(*) FROM DOCUMENTHISTORY
   WHERE HISTORYTYPE = 'Letter_Copy_or_Forward' AND RETIRED = 0)
  -
  (SELECT COUNT(*) FROM DOCUMENTHISTORY
   WHERE HISTORYTYPE = 'Letter_Received' AND RETIRED = 0)
  AS pending_copy_forwards;

-- C5: Percentage received (run after C1 and C3)
-- received_pct = (C3 result / C1 result) * 100
-- Calculate manually: e.g. 1820 / 2100 * 100 = 86.7%
```

---

## Section D — Staff Assignment Workflow

```sql
-- D1: Total letter assignments ever
SELECT COUNT(*) AS total_assignments_all_time
FROM DOCUMENTHISTORY
WHERE HISTORYTYPE = 'Letter_Assigned'
  AND RETIRED = 0;

-- D2: Assignments in last 30 days
SELECT COUNT(*) AS assignments_last_30_days
FROM DOCUMENTHISTORY
WHERE HISTORYTYPE = 'Letter_Assigned'
  AND RETIRED = 0
  AND CREATEDAT >= DATE_SUB(NOW(), INTERVAL 30 DAY);

-- D3: Assignments accepted (staff acknowledged) - all time
SELECT COUNT(*) AS assignments_accepted_all_time
FROM DOCUMENTHISTORY
WHERE HISTORYTYPE = 'Letter_Action_Taken'
  AND RETIRED = 0;

-- D4: Pending assignments (assigned but no action taken)
SELECT COUNT(*) AS pending_assignments
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0
  AND COMPLETED = 0
  AND CURRENTOWNER_ID IS NOT NULL;
```

---

## Section E — Weekly Trend (last 12 weeks)

```sql
-- E1: Letters added per week for the last 12 weeks
SELECT
  YEAR(CREATEDAT) AS yr,
  WEEK(CREATEDAT, 1) AS wk,
  MIN(DATE(CREATEDAT)) AS week_start,
  COUNT(*) AS letters_added
FROM DOCUMENT
WHERE DOCUMENTTYPE = 'Letter'
  AND RETIRED = 0
  AND CREATEDAT >= DATE_SUB(NOW(), INTERVAL 12 WEEK)
GROUP BY YEAR(CREATEDAT), WEEK(CREATEDAT, 1)
ORDER BY yr, wk;
```

---

## Section F — How Letters Were Received

```sql
-- F1: Breakdown by receive method (post, email, fax, hand-delivered, etc.)
SELECT i.NAME AS receive_method,
       COUNT(d.ID) AS count
FROM DOCUMENT d
JOIN ITEM i ON d.RECEIVEDAS_ID = i.ID
WHERE d.DOCUMENTTYPE = 'Letter'
  AND d.RETIRED = 0
GROUP BY i.NAME
ORDER BY count DESC;
```

---

## Recording Your Results

After running each query, fill in the placeholder table below and paste into `02-infographic-prompts.md`.

| Placeholder              | Query | Value |
|--------------------------|-------|-------|
| `{{TOTAL_LETTERS}}`      | A1    |       |
| `{{LETTERS_12M}}`        | A2    |       |
| `{{LETTERS_30D}}`        | A3    |       |
| `{{LETTERS_THIS_YEAR}}`  | A4    |       |
| `{{ACTIVE_INSTITUTIONS}}`| B1    |       |
| `{{TOTAL_INSTITUTIONS}}` | B4    |       |
| `{{TOTAL_FORWARDS}}`     | C1    |       |
| `{{FORWARDS_RECEIVED}}`  | C3    |       |
| `{{PENDING_FORWARDS}}`   | C4    |       |
| `{{RECEIVED_PCT}}`       | C5    |       |
| `{{TOTAL_ASSIGNMENTS}}`  | D1    |       |
| `{{ACCEPTED_ASSIGNMENTS}}`| D3   |       |
| `{{PENDING_ASSIGNMENTS}}`| D4    |       |
| Top 10 table             | B2    | (paste rows)|
| Weekly trend             | E1    | (paste rows)|
| Receive methods          | F1    | (paste rows)|
