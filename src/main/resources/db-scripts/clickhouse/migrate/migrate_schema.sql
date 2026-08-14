-- ClickHouse base-table migration file.
--
-- Format: one section per db_schema_version, applied strictly in ascending order by
-- migrate_db.py (see migrate_db.py in this directory for the runner).
--
--   ## db_schema_version: <version>
--   ## description: <one line>
--   <SQL statements for this section>
--
-- Rules for writing a section:
--   1. Sections should be idempotent wherever possible (IF EXISTS / IF NOT EXISTS) — ClickHouse
--      has no transactions, so a crash mid-section ideally leaves it safe to re-run from the top.
--      Not always achievable; use judgment, and note in the description when a section isn't.
--   2. Never DROP a column in the same section that reads from it.
--   3. For ORDER BY / primary-key changes: create a new table, INSERT ... SELECT, RENAME —
--      ClickHouse cannot ALTER these in place.
--   4. Don't write db_schema_version updates yourself — migrate_db.py advances
--      info.db_schema_version automatically after a section's SQL succeeds (and waits for that
--      and any other mutations the section triggered to finish before treating it as applied).
--   5. If a section changes a table that feeds a derived table (see
--      db-scripts/clickhouse/populate_derived_tables.sql), no extra bookkeeping is needed here —
--      run migrate_db.py with --populate-derived-tables and it repopulates derived tables
--      automatically after any migration that actually applied something.
--
-- Sections for versions already recorded in the target database's info.db_schema_version are
-- skipped automatically — do not remove or renumber old sections once released.

## db_schema_version: 3.0.0
## description: ClickHouse-native migration era begins; collapse derived_table_schema_version
##   into the single db_schema_version (fresh installs never had this column — see schema.sql)
ALTER TABLE info DROP COLUMN IF EXISTS derived_table_schema_version;
