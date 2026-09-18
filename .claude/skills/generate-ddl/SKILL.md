---
name: generate-ddl
description: >
  Derive MySQL DDL (CREATE TABLE / ALTER TABLE) from a new or changed JPA
  @Entity class in this repo. Use when asked to generate schema/migration
  SQL for an entity change, add a column, or figure out what SQL a JPA
  edit implies. Never runs anything against the production jdbc/dmis
  datasource.
---

# Generate DDL from a JPA Entity Change

This repo (DMIS) persists `@Entity` classes under
`src/main/java/lk/gov/health/phsp/entity/` to MySQL via EclipseLink/JPA,
using the `jdbc/dmis` JNDI datasource (production MySQL, Payara 5).
`persistence.xml` has schema generation **disabled** (see the comment
`DISABLED while pointed at production: do not let EclipseLink ALTER prod
schema`), so nothing auto-creates or auto-alters tables at deploy time.
When an entity gains a field, a table, or a relationship, the matching SQL
has to be produced and applied by hand. That is what this skill does.

## Step 1 — Read the entity diff

Look at the actual `@Entity` class (or its `git diff`) before writing any
SQL. Do not guess field-to-column mapping — confirm it from the class.

## Step 2 — Naming conventions (this schema is not consistent — check the neighbor)

There are **two coexisting conventions** in this codebase. Match whichever
one the entity you're touching already uses, don't mix them:

**Legacy convention (most existing entities — `Document`, `Institution`,
`DocumentHistory`, `WebUser`, `Item`, `Area`, …):**
- No `@Table(name=...)` / `@Column(name=...)` — EclipseLink defaults apply.
- Table name = the entity class name, **lowercased, no separators**
  (`Institution` → `institution`, `DocumentHistory` → `documenthistory`).
- Column name = the field name, **lowercased, no underscores**
  (`documentGenerationType` → `documentgenerationtype`).
- `@ManyToOne` FK column = the field name lowercased + `_id`
  (`poiInstitution` → `poiinstitution_id`, `mohArea` → `moharea_id` — see
  `src/main/webapp/resources/sql/adding_index_1.sql` for confirmed examples
  of this exact pattern in the live schema).

**Newer convention (recently added entities — e.g. `UserClaudeApiKey`):**
- Explicit `@Table(name = "snake_case_name")` and `@Column(name = "...",
  length = ..., columnDefinition = ...)` where it matters.
- Use ordinary `snake_case` for table/column names.

If you're adding a field to an *existing* legacy-style table, follow that
table's existing (no-annotation, lowercased) naming — do not introduce
`@Column(name=...)` partway through a table just for the new field, since
that fragments one table across two naming schemes. If you're adding a
brand-new entity/table, prefer the newer explicit `@Table`/`@Column` +
snake_case style — it's more self-documenting and matches recent entities.

## Step 3 — Type mapping

| Java field | SQL column type |
|---|---|
| `Long id` with `@Id @GeneratedValue(strategy = GenerationType.AUTO)` | `BIGINT NOT NULL` (see Step 4 — this is **not** `AUTO_INCREMENT` here) |
| `Long` (non-id) | `BIGINT` |
| `String` (no `@Lob`) | `VARCHAR(255)` (default JPA length), or the explicit `@Column(length=n)` if present |
| `String` with `@Lob @Column(columnDefinition="TEXT")` | `TEXT` |
| `byte[]`/`Serializable` blob with `@Column(columnDefinition="LONGBLOB")` | `LONGBLOB` |
| `boolean`/`Boolean` | `TINYINT(1)` (unless `@Column(name=...)` says otherwise) |
| `Date` with `@Temporal(TemporalType.DATE)` | `DATE` |
| `Date` with `@Temporal(TemporalType.TIMESTAMP)` | `DATETIME` |
| enum with `@Enumerated(EnumType.STRING)` | `VARCHAR(255)` (widen if enum constant names are long) |
| `@ManyToOne` | `BIGINT` FK column named per Step 2, referencing the target table's `id` |

## Step 4 — Primary keys use a shared `SEQUENCE` table, not `AUTO_INCREMENT`

Every entity here uses `@GeneratedValue(strategy = GenerationType.AUTO)`.
Under EclipseLink on MySQL this resolves to a **table-based generator**
backed by the existing `sequence` table (visible in
`src/main/webapp/resources/sql/lower_case_table_names.sql`), not a MySQL
`AUTO_INCREMENT` column. Concretely:
- New table's `id` column: `BIGINT NOT NULL PRIMARY KEY` — plain, no
  `AUTO_INCREMENT`.
- EclipseLink allocates ids from `sequence` itself at runtime; you do not
  need to seed a row for a new entity type by hand (it inserts one on
  first use). Do not add `AUTO_INCREMENT` — mixing the two id strategies
  on the same table is a real, previously-seen source of duplicate-key
  errors here.

## Step 5 — Write the migration as ALTER/CREATE statements

- New entity → one `CREATE TABLE ... (id BIGINT NOT NULL PRIMARY KEY, ...)`
  plus one `ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY (...) REFERENCES
  ...(id)` per `@ManyToOne`, plus an index on each FK column, matching the
  `<col>_INDEX` naming already used in `adding_index_1.sql`
  (`CREATE INDEX poiinstitution_id_INDEX ON institution(poiinstitution_id);`
  style — this repo's actual index names are inconsistently cased; match
  the table's existing casing rather than inventing a third style).
- New field on an existing entity → a single `ALTER TABLE <table> ADD
  COLUMN <column> <type> [NULL|NOT NULL] [DEFAULT ...];`. Prefer `NULL`
  (or an explicit default) for a new column on a table that already has
  rows — `NOT NULL` with no default fails against existing data.
- Renaming/dropping a column or changing its type is destructive — see
  Safety rules below before writing that kind of statement.

## Step 6 — Where the output goes

Write the generated SQL to a file (e.g. under a scratch/tmp location or
somewhere the user names) and show it in your response — do not run it
yourself. See Safety rules.

## Safety rules

1. **Never execute generated DDL against `jdbc/dmis`.** That datasource is
   production MySQL. This skill produces SQL text for the user to review;
   it does not run `asadmin`, a MySQL client, or any tool that reaches
   that datasource.
2. Hand the SQL to the user, or apply it only to a local/dev MySQL
   instance the user points you at explicitly (a different JDBC URL, not
   `jdbc/dmis`) — confirm the target before running anything.
3. For any destructive statement (`DROP COLUMN`, `DROP TABLE`, a type
   narrowing, removing a `NOT NULL` default, renaming a column/table),
   include:
   - the exact rollback statement (e.g. the `ADD COLUMN` that would undo a
     `DROP COLUMN`, including the original type — capture it from the
     entity's `git diff` or prior version before the column disappears),
   - a one-line note on data loss (a dropped/narrowed column loses data
     that a rollback cannot recover, since the rollback only restores the
     column shape, not its values).
4. Flag when a new `NOT NULL` column has no sensible default on a
   non-empty legacy table (see the ~12,900 pre-toggle `Document` rows with
   `documentGenerationType = null` noted in this repo's `CLAUDE.md` as a
   template for how "old data won't have this" problems show up here) —
   ask the user how they want existing rows backfilled rather than picking
   a default silently.
5. Never bundle unrelated schema changes into one script — one entity
   change in, one migration script out, so it's reviewable and revertible
   as a unit.
