---
name: restore-local-db
description: >
  Restore or reset a LOCAL MySQL database for DMIS from a dump/backup file.
  Use when asked to "restore my local database", "reset my local dmis db",
  "load this dump into my local MySQL", or "get my local dev database back
  to a clean state". Matches the schema/user to this app's persistence.xml
  (`hmisPU` -> `jdbc/dmis`) conventions and never touches the production
  datasource — it always confirms the target host and database name with
  the user before running any destructive command.
argument-hint: "<path-to-dump.sql[.gz]> [database-name]"
---

# Restore a Local DMIS Database

Bring a **local** MySQL database used for DMIS development up to date from a
dump file, without any risk of running against the production `jdbc/dmis`
datasource.

The destructive step is `DROP DATABASE` / overwriting existing tables. Every
rule below exists to guarantee that step only ever touches a local, disposable
database.

## Hard safety rules (read first)

1. **This skill only ever targets a local/dev MySQL instance.** Per this
   repo's `CLAUDE.md`, `jdbc/dmis` on the deployed Payara `domain1` is the
   **production** JDBC datasource. Never assume a local dev database is
   named `dmis`, hosted at a remote address, or reachable through a
   production connection string — always ask.
2. **Always confirm host + database name with the user before running any
   `DROP DATABASE`, `CREATE DATABASE`, or import command.** Do not infer the
   target from a JNDI name like `jdbc/dmis` or from what "looks local" —
   state the exact `--host`, `--port`, and database name you are about to
   use and get explicit confirmation.
3. **Refuse (stop and ask) if the resolved host is anything other than
   `localhost` / `127.0.0.1`**, or if credentials resemble production
   credentials (e.g. pulled from a shared ops vault, a `.env.production`
   file, or a hostname containing the production server's name). A local
   dev restore should only ever need a local MySQL root/dev user.
4. **Never edit or point the deployed app's `jdbc/dmis` connection pool at
   whatever you just restored.** This skill restores data into a database;
   it does not reconfigure Payara resources. If the user's local Payara
   domain's `jdbc/dmis` pool is meant to point at this local database, that
   is a separate, explicit ask — don't do it as a side effect.
5. If the user does not name a target database, or the dump's origin is
   unclear (unlabeled file, no idea which environment it came from), stop
   and ask rather than guessing.

## Where this fits in this app's config

- `src/main/resources/META-INF/persistence.xml` defines persistence-unit
  `hmisPU` with `<jta-data-source>jdbc/dmis</jta-data-source>` — a JTA
  datasource resolved by JNDI name on the Payara domain, not a JDBC URL in
  the app itself. There is no `glassfish-resources.xml` checked into this
  repo; the connection pool (host, port, schema name, user/password) is
  configured directly on each developer's local Payara `domain1` via
  `asadmin create-jdbc-connection-pool` / `create-jdbc-resource jdbc/dmis`.
- Because of that, the "database name" for local dev is whatever schema the
  developer's own local `jdbc/dmis` pool points at — it is **not**
  necessarily called `dmis`. Ask the user for their local schema name, or
  look it up with (on their machine, not this one):
  ```bash
  asadmin get resources.jdbc-connection-pool.*.property.databaseName
  ```
- The app uses `mysql-connector-j` (see `pom.xml`) against MySQL — dumps and
  restores use plain `mysql` / `mysqldump`, no other DB engine is involved.

## Inputs

| Input | Meaning | Default |
|---|---|---|
| `<dump>` | Path to a `.sql` or `.sql.gz` dump file | required |
| `<database>` | Local schema name to restore into | ask the user; do not assume `dmis` |
| `--host` / `--port` | Local MySQL connection | `127.0.0.1` / `3306` — confirm, don't assume |
| `--user` | Local MySQL user | ask; typically a local `root` or dev user, never a production credential |

## Procedure

### 1. Confirm the target, out loud

Before touching anything, state back to the user: "I'm about to restore
`<dump>` into `<database>` on `<host>:<port>` as `<user>` — confirm this is
your local dev database, not production." Do not proceed without an
explicit yes.

### 2. Validate the dump file

```bash
# integrity, if gzipped
gzip -t <dump>.sql.gz

# sanity-check it doesn't carry a CREATE DATABASE/USE that could redirect
# the import to a different schema than the one just confirmed
gzip -dc <dump>.sql.gz | grep -E "^(CREATE DATABASE|USE )" || true

# eyeball where it came from
gzip -dc <dump>.sql.gz | head -30 | grep -E "^-- Host:|Database:"
```

If it contains `CREATE DATABASE`/`USE` statements naming something other
than the confirmed local `<database>`, stop and ask before continuing —
those statements can silently redirect the import.

### 3. Optional pre-restore backup of the local database

Cheap insurance before overwriting local data that might still be useful:

```bash
mysqldump --host=127.0.0.1 --port=3306 -u<user> -p \
  --single-transaction --routines --triggers <database> \
  | gzip > <database>_local_before_restore_$(date +%Y%m%d%H%M%S).sql.gz
```

### 4. Drop and recreate the schema

```bash
mysql --host=127.0.0.1 --port=3306 -u<user> -p -e \
  "DROP DATABASE IF EXISTS <database>; \
   CREATE DATABASE <database> CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
```

If this hangs on a metadata lock, a running local Payara instance is
holding the schema open through the `jdbc/dmis` pool — stop the local
domain (`asadmin stop-domain`) first, then retry.

### 5. Import

```bash
gzip -dc <dump>.sql.gz | mysql --host=127.0.0.1 --port=3306 -u<user> -p <database>
```

For a large dump, run this in the background and check the exit status
rather than assuming it finished — an interrupted import leaves the schema
half-loaded, in which case redo steps 4-5 from the same dump (dumps are
full snapshots; there's no partial resume).

### 6. Verify

```bash
mysql --host=127.0.0.1 --port=3306 -u<user> -p <database> -e \
  "SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema='<database>';"
```

Compare against the `CREATE TABLE` count in the dump
(`gzip -dc <dump>.sql.gz | grep -c "^CREATE TABLE "`). If restoring for use
with this app, also spot-check a couple of tables that back core entities
(e.g. `document`, `document_history`, `institution`) exist and have rows.

### 7. Restart local Payara (if it was stopped, or the pool needs to pick up schema changes)

```bash
asadmin restart-domain domain1
```

## Report back

- Dump restored, and into which database/host (confirming it was local).
- Table count after restore vs. the dump's `CREATE TABLE` count.
- Whether a pre-restore backup was taken, and where.
- Any mismatch or anomaly found in step 6 that the user should look at
  before relying on the restored data.
