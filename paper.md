---
title: 'DMIS: A National Digital Correspondence Registry for Sri Lanka's Government Health System'
tags:
  - Java
  - health informatics
  - document management
  - correspondence management
  - autocomplete
  - government health
  - Sri Lanka
authors:
  - name: M. H. B. Ariyaratne
    orcid: 0000-0002-1798-6203
    affiliation: 1
affiliations:
  - name: Health Information Unit, Ministry of Health, Sri Lanka
    index: 1
date: 30 May 2026
bibliography: paper.bib
---

# Summary

The Document Management Information System (DMIS) is an open-source, web-based national digital correspondence registry developed by the Health Information Unit (HIU) of the Ministry of Health, Sri Lanka. It digitises the recording, routing, assignment, and auditing of official letters across Sri Lanka's government health system — from the Ministry headquarters to provincial health offices, teaching hospitals, district hospitals, and primary care centres.

Sri Lanka provides universal free health care through a network of public institutions. Managing official correspondence across this network has historically relied on disconnected paper registers maintained at each institution, with no cross-institutional visibility or audit trail. DMIS replaces this with a shared digital registry where every letter can be recorded once, forwarded digitally, acknowledged, assigned to a named staff member, and audited end-to-end.

A key technical contribution of DMIS is its multi-word substring AND filtering algorithm for the institution and user autocomplete fields. Sri Lanka's health system registry contains approximately 1,962 distinct institutional entries with long, complex official names. Standard prefix or single-fragment search is inadequate at this scale. The algorithm splits the user's query into individual words and returns only candidates where all words appear as substrings in any of the candidate's name fields — a conjunctive (AND) filter across words with disjunctive (OR) matching across fields. This allows users to search using any memorable fragments of an institution's name in any order, progressively narrowing results with each word typed. The method has been in continuous production use since 18 February 2022 [@dmis_commit_4bea801] and has measurably reduced the creation of duplicate institution records.

# Statement of Need

Official correspondence in large public sector health systems must be traceable, auditable, and analytically useful. In Sri Lanka, before DMIS, each institution maintained its own paper register. There was no mechanism to verify whether a forwarded letter was received, whether assigned staff had acted, or to generate national-level correspondence reports. Letters could not be searched, cross-referenced across institutions, or linked to accountability records.

Free-text sender/recipient fields were not viable — they preclude systematic tracing and analysis by institution. Standard autocomplete approaches (prefix search, single `contains` filter) failed because official names are long and structurally varied: a user seeking *"Office of the Provincial Director of Health Services, Southern Province"* could not reliably find it by typing either the beginning of the name or a single fragment. This caused users to create duplicate institution records, degrading data quality and making correspondence analysis unreliable.

DMIS provides the infrastructure for digitised correspondence management, and the multi-word AND autocomplete algorithm provides the practical usability mechanism that makes controlled, structured institution selection feasible at scale — without requiring users to know exact names, abbreviations, or name-order conventions.

# Algorithm: Multi-Word Substring AND Filtering

Given query `q`, split into words `W(q)` by whitespace. A candidate institution `c` with name fields `F(c)` is included in results if and only if:

```
∀ w ∈ W(q) : ∃ f ∈ F(c) : f.toLowerCase().contains(w.toLowerCase())
```

Each institution is matched against three name fields: full name (`name`), short name (`sname`), and Tamil name (`tname`). Each user account is matched against display name, login code, and linked person name. Results are sorted alphabetically, with "Personal" and "Other" pinned to the top to handle correspondence from individuals or unlisted entities.

The algorithm operates entirely in-memory against a pre-loaded registry, with no database query per keystroke. It was first committed on 18 February 2022 [@dmis_commit_4bea801] and is used across all autocomplete fields in the letter management module.

# System Overview

DMIS is built on a Java EE stack: JavaServer Faces (JSF) with PrimeFaces for the UI, JPA/EclipseLink for persistence, MySQL as the database, and Payara 5 as the application server. It is hosted at the Ministry of Health Health Data Centre and accessible at `https://nchis.health.gov.lk/dmis` [@dmis_repo].

Core capabilities include letter recording with image attachment, digital forwarding with acknowledgement tracking, staff assignment with action recording, printable official registers, an institutional dashboard, and a national overview dashboard for Ministry administrators. All actions are attributed to individual named user accounts; shared accounts are not permitted.

Active deployments as of 2026 include the Ministry of Health headquarters, the Director General of Health Services office, the Health Information Unit, RDHS offices, and Teaching Hospital Kurunegala, with broader rollout underway.

# Acknowledgements

The author acknowledges Dr. Chaminda Weerabaddana and Dr. Poorna Fernando (Consultants in Health Informatics, HIU technical team); Dr. A. I. Jagoda, Director (Health Information), Ministry of Health, Sri Lanka; Dr. Palitha Karunapema, former Director (Health Information), Ministry of Health, Sri Lanka; and Dr. Prasad Ranathunga, Consultant in Health Informatics, North Western Province.

# References
