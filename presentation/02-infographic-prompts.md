# DMIS Presentation — Infographic Prompts for Claude.ai

## How to use

1. Run the SQL in `01-database-queries.md` and fill in all `{{PLACEHOLDER}}` values below.
2. Open claude.ai in a new conversation.
3. Paste each prompt exactly as written (after filling placeholders).
4. Ask Claude to output as an SVG file or as a downloadable PNG/SVG.
5. Save each result as `infographic-1.svg`, `infographic-2.svg`, etc.

Replace every `{{PLACEHOLDER}}` with your real number before pasting.

---

## Infographic 1 — System Overview (Architecture)

**Purpose:** Show the single centralised system connecting all health institutions.
**Slide:** "What is DMIS?" and "Deployment Model"

```
Create a professional government infographic as an SVG (1200×700 px) with the following content:

TITLE (top, centred, bold, dark navy blue): "Document Management Information System"
SUBTITLE (below title, smaller): "One System — All Health Institutions — Ministry of Health Sri Lanka"

Central element: A server tower icon in the middle labelled "MoH Data Centre" with the text "nchis.health.gov.lk" in small grey text below it. Use a dark blue filled rectangle (60×90 px) as a simple server icon.

Radiating outward from the centre server in a circular layout, show 8 institution node boxes. Each box is a rounded rectangle (120×50 px) in light teal (#0097A7), white text. Label them:
1. "Teaching Hospital"
2. "Provincial Hospital"
3. "District Hospital"
4. "MOH Office"
5. "RDHS Office"
6. "Base Hospital"
7. "Primary Health Care"
8. "+ More Institutions"

Connect each institution to the central server with a two-headed arrow (dark blue, 2px, with a slight curve).

Below the institution boxes on the outer ring, add a small envelope icon near each box to suggest letters flowing in both directions.

Bottom banner (full width, navy blue background, white text, 50px height):
Left side: "Developed by Health Informatics Unit (HIU) · Open Source · Free to Use"
Right side: "{{ACTIVE_INSTITUTIONS}} Institutions Active · {{TOTAL_LETTERS}} Letters Recorded"

Colour palette: Navy blue #1A237E, Teal #0097A7, Light blue #E3F2FD, White #FFFFFF, Accent green #2E7D32.
Font: Arial or system sans-serif. Clean, minimal, professional government style. No gradients.
```

---

## Infographic 2 — Letter Lifecycle Workflow

**Purpose:** Explain the end-to-end process a letter goes through in DMIS.
**Slide:** "How It Works"

```
Create a professional government workflow infographic as an SVG (1400×500 px).

Show a horizontal step-by-step flow with 7 steps connected by right-pointing arrows (dark blue, thick). Each step is a rounded rectangle (150×90 px).

Step 1 — "Letter Received" — background #1565C0 (dark blue), white text
  Icon: simple envelope shape (rectangle with V fold at top)
  Sub-text: "Post / Email / Fax / Hand"

Step 2 — "Entered in DMIS" — background #1976D2, white text
  Icon: simple document with lines
  Sub-text: "Date · Reference No · Topic · Sender"

Step 3 — "Image Attached" — background #1E88E5, white text
  Icon: simple camera/image rectangle
  Sub-text: "Scanned copy uploaded"

Step 4 — "Minuted" — background #039BE5, white text
  Icon: pencil icon (diagonal line)
  Sub-text: "Action recorded by officer"

Then a fork symbol (Y-shape) splitting into two parallel paths:

Path A — Step 5A — "Forwarded to Institution" — background #00838F (teal), white text
  Icon: arrow pointing right to building
  Sub-text: "Another unit / institution"

Path B — Step 5B — "Assigned to Staff" — background #2E7D32 (green), white text
  Icon: person silhouette
  Sub-text: "Staff member within unit"

Then the two paths rejoin to:

Step 6 — "Acknowledged / Accepted" — background #F57F17 (amber), white text
  Icon: checkmark circle
  Sub-text: "Receiver confirms receipt"

Step 7 — "Closed" — background #4CAF50 (green), white text
  Icon: lock icon
  Sub-text: "Full audit trail preserved"

Below the entire flow, add a thin horizontal timeline bar in light grey. Mark it "Day 0" on the left and "Ongoing" on the right.

Title above the diagram: "Complete Letter Management Workflow" — bold, dark navy, 20pt.
Footer text: "Every step is time-stamped and attributed · Accessible from any enrolled institution"

Background: white. No gradients. Clean government style.
```

---

## Infographic 3 — National Statistics Dashboard

**Purpose:** Show key numbers from the live system to demonstrate adoption and activity.
**Slide:** "Live Statistics" and "National Dashboard"

```
Create a bold statistics infographic as an SVG (1200×650 px) for the Ministry of Health Sri Lanka.

HEADER: Full-width bar (height 70px), background #1A237E (dark navy). White centred text:
  Main: "DMIS — National Statistics at a Glance"
  Sub: "Document Management Information System · As of {{QUERY_DATE}}"

BODY: 3 rows × 3 columns grid of stat cards. Each card is a rounded rectangle (320×130 px) with 20px gaps.

Row 1 — LETTERS:
Card 1: background #1565C0, white text
  Large number: "{{TOTAL_LETTERS}}"
  Label: "Total Letters Recorded"
  Sub: "All time · All institutions"

Card 2: background #1976D2, white text
  Large number: "{{LETTERS_THIS_YEAR}}"
  Label: "Letters This Year"
  Sub: "Calendar year {{CURRENT_YEAR}}"

Card 3: background #1E88E5, white text
  Large number: "{{LETTERS_30D}}"
  Label: "Letters Last 30 Days"
  Sub: "Recent activity"

Row 2 — INSTITUTIONS:
Card 4: background #00838F, white text
  Large number: "{{ACTIVE_INSTITUTIONS}}"
  Label: "Active Institutions"
  Sub: "With at least one letter"

Card 5: background #00ACC1, white text
  Large number: "{{TOTAL_INSTITUTIONS}}"
  Label: "Registered Institutions"
  Sub: "In the national system"

Card 6: background #26C6DA, white text
  Large number: "{{RECEIVED_PCT}}%"
  Label: "Forwarded Letters Received"
  Sub: "Acknowledgement compliance"

Row 3 — WORKFLOW:
Card 7: background #2E7D32, white text
  Large number: "{{TOTAL_FORWARDS}}"
  Label: "Cross-Institution Forwards"
  Sub: "All time"

Card 8: background #388E3C, white text
  Large number: "{{TOTAL_ASSIGNMENTS}}"
  Label: "Staff Assignments"
  Sub: "Letters assigned to officers"

Card 9: background #43A047, white text
  Large number: "{{ACCEPTED_ASSIGNMENTS}}"
  Label: "Actions Recorded"
  Sub: "Officers who actioned letters"

FOOTER: Light grey bar (height 40px), dark text centred:
  "Developed by Health Informatics Unit (HIU) · Open Source · Hosted at MoH Data Centre · Free for all health institutions"

Large numbers: bold, 42pt. Labels: bold, 13pt. Sub-text: 11pt, 80% opacity.
```

---

## Infographic 4 — Benefits Grid

**Purpose:** Summarise the 6 key benefits for administrators and officers.
**Slide:** "Why Use DMIS?"

```
Create a 2×3 benefits grid infographic as an SVG (900×620 px) for a Ministry of Health presentation.

TITLE (centred, top, 60px height, background #1A237E, white bold text 22pt):
"Why Your Institution Should Use DMIS"

BODY: 2 columns × 3 rows of benefit cards. Each card 400×130 px, rounded corners (12px), 15px gaps.
All cards have background #F0F4FF (very light blue), border 2px #1565C0.

Card layout per card: left 20%: coloured circle icon area. Right 80%: title + description.

Card 1 — icon circle colour #1565C0
  Title: "No Lost Letters"
  Description: "Every letter is digitally recorded with date, reference number, topic, and scanned image. Nothing is ever lost."

Card 2 — icon circle colour #2E7D32
  Title: "Real-Time Status"
  Description: "See instantly whether a forwarded letter has been received or an assignment has been acted upon."

Card 3 — icon circle colour #00838F
  Title: "Full Audit Trail"
  Description: "Every action — minute, forward, assign, accept — is time-stamped and linked to the responsible officer."

Card 4 — icon circle colour #F57F17
  Title: "Cross-Institution Visibility"
  Description: "Forward letters to any other enrolled institution and track receipt. National dashboard shows system-wide activity."

Card 5 — icon circle colour #6A1B9A
  Title: "Printable Registers"
  Description: "Generate the official Letters Received Register, Copy-Forward Register, and Accept Register — printable and downloadable as Excel."

Card 6 — icon circle colour #C62828
  Title: "Free & Open Source"
  Description: "Built from scratch by the Health Informatics Unit. No licensing cost. Hosted free at MoH Data Centre. Full HIU support."

For each icon circle, draw a simple white SVG shape:
  Card 1: envelope shape
  Card 2: clock shape
  Card 3: shield with checkmark
  Card 4: two buildings connected by line
  Card 5: document with lines
  Card 6: open padlock shape

FOOTER: white background, dark grey centred text, 12pt:
"Request an institution administrator account from HIU to get started · Training and support provided"
```

---

## Infographic 5 — Top Institutions Bar Chart

**Purpose:** Show which institutions are most actively using the system.
**Slide:** "Institutional Adoption"

Replace the `{{TOP10_ROWS}}` section with the actual rows from query B2, formatted as:
`institution_name | count` (one per line).

```
Create a clean horizontal bar chart infographic as an SVG (1000×600 px).

TITLE: "Top Institutions by Letters Recorded" — bold, dark navy, 20pt, top-left aligned.
SUB-TITLE: "All time · DMIS Production System · Ministry of Health Sri Lanka" — grey, 12pt.

Draw 10 horizontal bars from left to right. Each bar is 28px tall with 14px vertical gap.
Bar container starts at x=260 (to fit institution name labels on the left).
Maximum bar width = 650px (scale all bars proportionally to the highest count).

Bar colours: alternate between #1565C0 and #1976D2.

Institution names (right-aligned, dark text, 11pt, truncated to 35 chars):
{{TOP10_ROWS}}

At the end of each bar, show the count number in white bold text (inside the bar if wide enough, outside in dark text if bar is too narrow).

Add a light grey vertical grid line every 25% of max width.

X-axis label at bottom: "Number of Letters"
Y-axis: institution names

FOOTER: "Source: DMIS Production Database · {{QUERY_DATE}}" — light grey, 10pt, bottom-right.
Background: white. No gradients. Clean and legible.
```

---

## Infographic 6 — Weekly Trend Line Chart

**Purpose:** Show growth and consistency of use over the last 12 weeks.
**Slide:** "Growing Adoption"

Replace `{{WEEKLY_ROWS}}` with rows from query E1, formatted as:
`Week of DD-MMM-YYYY | count` (one per line).

```
Create a clean line chart infographic as an SVG (1100×480 px).

TITLE: "Letters Added Per Week — Last 12 Weeks" — bold, dark navy, 20pt, top-left.
SUB-TITLE: "Across all enrolled institutions · DMIS · Ministry of Health Sri Lanka" — grey, 12pt.

Chart area: x=80 to x=1020, y=60 to y=380 (width 940px, height 320px).

Data points (12 weeks):
{{WEEKLY_ROWS}}

Scale Y-axis from 0 to max_count + 10% margin. Draw 5 horizontal light grey grid lines evenly spaced.
Draw the line in dark blue #1565C0, 2.5px stroke, with filled circles (radius 5px, same colour) at each data point.
Fill the area under the line with a very light blue (#E3F2FD), 40% opacity.

X-axis: week start date labels, rotated 30 degrees, 10pt, dark grey.
Y-axis: numeric labels, 10pt, dark grey.
Y-axis title (rotated 90°, 11pt): "Letters Added"

On each data point circle, show a tooltip-style label above the point: the count number in a small navy rounded rectangle (white text, 9pt).

FOOTER: "Source: DMIS Production Database · {{QUERY_DATE}}" — light grey, 10pt, bottom-right.
Background: white. No gradients.
```

---

## After Generating

Save each SVG output as:
- `infographic-1-architecture.svg`
- `infographic-2-workflow.svg`
- `infographic-3-statistics.svg`
- `infographic-4-benefits.svg`
- `infographic-5-top-institutions.svg`
- `infographic-6-weekly-trend.svg`

Place all six SVG files in the same folder as your PowerPoint file so you can insert them as images.
In PowerPoint: Insert → Pictures → This Device → select the SVG file.
SVG files scale perfectly to any size without pixelation.
