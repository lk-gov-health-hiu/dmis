# DMIS Presentation — PowerPoint Creation Prompt

## How to Use

1. Complete steps 1–3 first (get real data from DB and screenshots).
2. Fill all `{{PLACEHOLDER}}` values below with your actual numbers.
3. Place all infographic SVG files and screenshot PNG files in one folder.
4. Open a new conversation in Claude (Desktop app with PowerPoint tool, or paste into any Claude interface that can create files).
5. Attach the infographic SVG files and screenshot PNG files to the conversation.
6. Paste the entire prompt below.

---

## PowerPoint Creation Prompt

```
Create a professional PowerPoint presentation (.pptx) for the Ministry of Health Sri Lanka.

PRESENTER: Dr A I Jagoda, Director (Health Information), Ministry of Health
AUDIENCE: Top-level officers — Directors, Provincial Directors, Hospital Superintendents
PURPOSE: Increase awareness of DMIS and promote its adoption across all health institutions
EVENT DATE: [INSERT DATE]
TONE: Professional, authoritative, data-driven. Government ministry style.

---

DESIGN SPECIFICATIONS:
- Slide size: Widescreen 16:9 (33.87 × 19.05 cm)
- Primary colour: Dark navy blue #1A237E
- Secondary colour: Teal #00838F
- Accent colour: Amber #F57F17 (for calls to action and highlights)
- Background: White (#FFFFFF) for content slides
- Title slide background: Dark navy #1A237E
- Font (headings): Calibri Bold, dark navy or white depending on background
- Font (body): Calibri, dark grey #333333, size 18pt minimum
- All slides must have the MoH footer: "Ministry of Health Sri Lanka — Health Informatics Unit"
- Slide numbers: bottom right, small, grey
- No slide transitions (keep it professional and simple)
- Use provided SVG/PNG files as images where indicated

---

SLIDE 1 — TITLE SLIDE
Background: Dark navy blue #1A237E
Centre layout:

  [White bold text, 36pt]
  Document Management Information System

  [White text, 20pt, with line above and below in teal]
  DMIS — National Digital Mail Registry

  [White text, 16pt, left-aligned bottom section]
  Presented by: Dr A I Jagoda
  Director (Health Information)
  Ministry of Health Sri Lanka
  [DATE]

  [Place MoH logo top-right if available]
  [Place small "HIU" text bottom-left in grey]

---

SLIDE 2 — THE PROBLEM WE ARE SOLVING
Layout: Title top, 2-column body.
Slide title: "The Challenge With Physical Letter Management"

Left column header (dark navy, bold): "Current Reality"
Left column bullet points (dark red icons):
  • Letters received in physical registers — risk of loss, damage, fire
  • No visibility into whether a forwarded letter was received
  • No way to check if assigned staff have taken action
  • Each unit maintains its own disconnected register
  • No national picture of correspondence across health system
  • Searching past letters takes hours or days

Right column header (dark navy, bold): "The Consequences"
Right column bullet points (dark amber icons):
  • Accountability gaps when instructions are not followed
  • Delays in action due to lost or delayed letters
  • Duplication of effort across institutions
  • No audit trail for official correspondence
  • Impossible to monitor compliance nationally

Bottom of slide (teal background box, white text):
"A national digital solution already exists — and it is free."

---

SLIDE 3 — WHAT IS DMIS?
Layout: Title top, icon grid body (2×3).
Slide title: "Document Management Information System (DMIS)"

Opening line (dark navy, 18pt, bold, centred):
"DMIS is a web-based document management system developed specifically for Sri Lanka's health sector."

Six feature boxes in a 2×3 grid (rounded rectangles, navy border, light blue background):

Box 1 — icon: envelope
"Records letters received by post, email, fax, or hand delivery"

Box 2 — icon: image/attachment
"Attaches scanned images of physical letters"

Box 3 — icon: buildings/arrow
"Forwards letters digitally to any enrolled institution"

Box 4 — icon: person/checkmark
"Assigns letters to specific staff members for action"

Box 5 — icon: chart
"Institutional and national dashboards for monitoring"

Box 6 — icon: shield
"Complete audit trail — who did what, when"

---

SLIDE 4 — DEVELOPED BY HIU — OPEN SOURCE
Layout: Title top, two-section body.
Slide title: "Built for Sri Lanka's Health System — By Sri Lanka's Health System"

Left section (60% width, white background with teal left border):
  [Bold navy, 16pt] Developed from Scratch by HIU
  "The Health Informatics Unit of the Ministry of Health developed DMIS entirely from the ground up as a purpose-built solution for the Sri Lankan health administration context."

  Key facts (with teal bullet icons):
  • 100% open source — source code freely available
  • No commercial licensing fees — ever
  • Designed for Sri Lankan administrative workflows
  • Built on proven Java EE technology
  • Continuously updated and supported by HIU

Right section (40% width, dark navy background, white text):
  [Teal heading] Deployment
  
  [White text, 16pt]
  ★ Hosted at Ministry of Health Data Centre
  
  ★ Single application — all institutions use the same system
  
  ★ Request one institution administrator account to start
  
  ★ HIU provides full training and technical support
  
  ★ Standalone deployment option available for technically capable teams

---

SLIDE 5 — HOW IT WORKS — THE WORKFLOW
Layout: Title top, full-width infographic body.
Slide title: "How DMIS Manages Your Correspondence"

Insert infographic-2-workflow.svg as full-width image (stretch to fill body area).

Below the infographic, add two small caption boxes side by side:
  Left box (teal background, white text): "Forwarding Path: Letter → Entered → Minuted → Forwarded to Institution → Other unit acknowledges receipt"
  Right box (green background, white text): "Assignment Path: Letter → Entered → Minuted → Assigned to Staff → Staff accepts → Action recorded"

---

SLIDE 6 — KEY FEATURES IN DETAIL
Layout: Title top, 4-box feature grid.
Slide title: "What You Can Do With DMIS"

Four feature boxes (2×2 grid, each box 45% width, tall enough for 3 lines):

Box A (dark navy background, white text):
  [Bold] Letter Entry & Image Attachment
  Record every letter: date, reference number, sender, topic, receive method (post/email/fax/hand).
  Attach scanned images directly to the record.
  [Insert screenshot-05-letter-entry-form.png as small thumbnail, right-aligned inside box]

Box B (teal background, white text):
  [Bold] Minute & Copy/Forward
  Add official minutes to any letter. Forward a copy to any other enrolled institution.
  The receiving institution must acknowledge receipt — tracked in real time.
  [Insert screenshot-07-forward-letter-form.png as small thumbnail]

Box C (green background, white text):
  [Bold] Staff Assignment & Action Recording
  Assign any letter to a specific staff member. They must formally accept the assignment and record the action taken.
  Full accountability chain maintained automatically.
  [Insert screenshot-08-assign-letter-form.png as small thumbnail]

Box D (amber background, white text):
  [Bold] Printable Registers & Reports
  Generate the official Letters Received Register, Copy-Forward Register, and Accept Register.
  Download as Excel or print directly from any browser.
  [Insert screenshot-09-receive-register.png as small thumbnail]

---

SLIDE 7 — INSTITUTIONAL DASHBOARD
Layout: Title top, screenshot-dominant layout.
Slide title: "Your Institution's Dashboard — Real-Time Status"

Insert screenshot-01-institutional-dashboard.png as the dominant visual (70% of slide body).

Right sidebar annotations (navy text, pointing to dashboard areas with lines/arrows):
  ▸ "Letters entered today"
  ▸ "Letters pending forwarding"
  ▸ "Forwards waiting for your acknowledgement"
  ▸ "Assignments pending staff acceptance"
  ▸ "Activity over last 7 days"

Bottom strip (light blue background, dark navy text, bold):
"Every institution sees its own real-time dashboard the moment they log in."

---

SLIDE 8 — NATIONAL DASHBOARD
Layout: Title top, screenshot left, key stats right.
Slide title: "National Dashboard — Ministry-Wide Visibility"

Left (60%): Insert screenshot-02-national-dashboard.png

Right (40%, stacked stat boxes, navy gradient):
  Stat 1 (dark navy box, white text):
    [Number, bold 32pt] {{TOTAL_LETTERS}}
    [Label, 13pt] Total Letters in System

  Stat 2 (teal box, white text):
    [Number, bold 32pt] {{ACTIVE_INSTITUTIONS}}
    [Label, 13pt] Active Institutions

  Stat 3 (green box, white text):
    [Number, bold 32pt] {{RECEIVED_PCT}}%
    [Label, 13pt] Forwarded Letters Acknowledged

  Stat 4 (amber box, white text):
    [Number, bold 32pt] {{LETTERS_30D}}
    [Label, 13pt] Letters Added — Last 30 Days

Caption at bottom (italic, grey, 12pt):
"National administrators can see activity across every enrolled institution from a single screen."

---

SLIDE 9 — LIVE STATISTICS
Layout: Title top, full-width infographic.
Slide title: "DMIS by the Numbers — {{QUERY_DATE}}"

Insert infographic-3-statistics.svg as full-width body image.

If infographic is not available yet, use a 3×3 table with these cells:

| {{TOTAL_LETTERS}} Total Letters | {{LETTERS_THIS_YEAR}} This Year | {{LETTERS_30D}} Last 30 Days |
| {{ACTIVE_INSTITUTIONS}} Active Institutions | {{TOTAL_INSTITUTIONS}} Registered | {{RECEIVED_PCT}}% Forwards Received |
| {{TOTAL_FORWARDS}} Cross-Institution Forwards | {{TOTAL_ASSIGNMENTS}} Staff Assignments | {{ACCEPTED_ASSIGNMENTS}} Actions Recorded |

Style the table: navy header row, alternating light blue/white rows, bold numbers, 22pt font.

---

SLIDE 10 — TOP INSTITUTIONS
Layout: Title top, bar chart body.
Slide title: "Leading Institutions in DMIS Adoption"

Insert screenshot-03-letters-by-institution.png (or infographic-5-top-institutions.svg) as body image.

If neither is available, create a PowerPoint bar chart using these values from query B2:
[PASTE TOP 10 ROWS FROM QUERY B2 HERE: institution_name, count]

Add caption (italic, grey, 12pt):
"These institutions demonstrate that consistent digital correspondence management is achievable across the health system."

---

SLIDE 11 — GROWING ADOPTION
Layout: Title top, trend chart body.
Slide title: "Consistent and Growing Use"

Insert screenshot-04-weekly-trend.png (or infographic-6-weekly-trend.svg) as body image.

If neither is available, create a PowerPoint line chart using values from query E1:
[PASTE 12 WEEKLY ROWS FROM QUERY E1 HERE: week_start, count]

Key message box (teal background, white text, bottom of slide):
"Even without a mandate, health institutions are adopting DMIS because it solves real problems. With ministerial direction, adoption can reach 100%."

---

SLIDE 12 — WHO CAN USE DMIS
Layout: Title top, 3-column body.
Slide title: "DMIS Is for Every Health Institution"

Column 1 (teal header: "Ministerial & National"):
  • Ministry headquarters units
  • Director General's office
  • All national programme offices
  • Health Informatics Unit

Column 2 (navy header: "Provincial & District"):
  • Provincial Ministry of Health offices
  • Regional Director of Health Services (RDHS)
  • Medical Officer of Health (MOH) offices
  • District hospitals

Column 3 (green header: "Institutions & Units"):
  • Teaching hospitals
  • Base hospitals
  • Divisional hospitals
  • Primary health care centres
  • Any institution with letter correspondence

Bottom banner (dark navy, white text, bold):
"Any health institution in Sri Lanka can request an account. All institutions share one connected system."

---

SLIDE 13 — ADMINISTRATION & SUPPORT
Layout: Title top, two-column body.
Slide title: "Simple Administration — Full HIU Support"

Left column (60%):
  [Bold navy heading] Institutional Administration
  • Each institution has its own Institution Administrator
  • Admin creates and manages all user accounts within their institution
  • Admin controls user roles and privileges
  • No IT department required — any senior officer can manage users

  [Insert screenshot-12-user-management.png as thumbnail]

  [Bold navy heading, below] Security
  • Each user logs in with a personal username and password
  • Actions are attributed to the specific logged-in user
  • No shared accounts — full individual accountability

Right column (40%, navy background, white text):
  [Teal heading] HIU Support Package
  
  ✓ Account activation within [X] working days of request
  
  ✓ Online and on-site training for institution staff
  
  ✓ User manual and quick-reference guides
  
  ✓ Technical helpdesk via email and phone
  
  ✓ System updates and bug fixes at no cost
  
  ✓ Continuous improvement based on institution feedback
  
  Contact HIU: [INSERT HIU EMAIL / PHONE]

---

SLIDE 14 — HOW TO JOIN
Layout: Title top, 4-step horizontal flow.
Slide title: "Getting Your Institution onto DMIS — 4 Simple Steps"

Four step boxes in a horizontal row connected by right arrows:

Step 1 (navy box, white text):
  [Large number "1"]
  Request Account
  Contact HIU with your institution name, address, and administrator's details

Step 2 (teal box, white text):
  [Large number "2"]
  HIU Activates
  HIU creates your institution in DMIS and activates your administrator account within [X] days

Step 3 (green box, white text):
  [Large number "3"]
  Attend Training
  HIU conducts a training session for your administrator and key users

Step 4 (amber box, white text):
  [Large number "4"]
  Start Recording
  Begin entering letters from the first day. The system is ready to use immediately.

Contact details box (full width, light blue background, dark navy text, centred):
  "To request your institution account, contact the Health Informatics Unit"
  [INSERT HIU CONTACT EMAIL]
  [INSERT HIU CONTACT PHONE]
  System access: https://nchis.health.gov.lk/dmis/

---

SLIDE 15 — CALL TO ACTION
Layout: Full-screen, high-impact.
Background: Dark navy #1A237E
All text: White

Centre, large bold text (32pt):
"Direct Your Institutions to Register on DMIS Today"

Below (20pt, normal weight):
"DMIS is already running. It is free. It is supported. It works."

Three white stat boxes side by side (navy outline, translucent background):
  [{{TOTAL_LETTERS}}] Letters Already Recorded
  [{{ACTIVE_INSTITUTIONS}}] Institutions Already Active
  [Zero Cost] To Your Institution

Large teal button-style box (centred, bottom third):
  "One letter to HIU is all it takes to get started"
  Contact: [INSERT HIU EMAIL]

Footer (small, grey at very bottom):
"Ministry of Health Sri Lanka · Health Informatics Unit · DMIS v[VERSION] · Open Source"

---

END OF PRESENTATION

Additional notes for the PowerPoint builder:
- Add smooth slide transitions only if requested (fade is acceptable)
- All images should be high-resolution — use SVG where possible for charts/infographics
- Print-quality should be maintained (300 dpi equivalent)
- Ensure all text is readable when projected — minimum 18pt for body text
- Test contrast ratios — white on dark navy and dark navy on white are both fine
- The presentation should work without animation — keep it fully static
- Save as both .pptx and .pdf
```
