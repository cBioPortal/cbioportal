-- ClickHouse base-table migration file.
--
-- Format: one section per db_schema_version, applied strictly in ascending order by
-- migrate_db.py (see migrate_db.py in this directory for the runner).
--
--   ## db_schema_version: <version>
--   ## description: <one line>
--   ## custom: true            (optional — see below)
--   <SQL statements for this section>
--
-- Rules for writing a section:
--   1. Sections should be idempotent wherever possible (IF EXISTS / IF NOT EXISTS) — ClickHouse
--      has no transactions, so a crash mid-section ideally leaves it safe to re-run from the top.
--      Not always achievable; use judgment, and note in the description when a section isn't.
--   2. Never DROP a column in the same section that reads from it.
--   3. For ORDER BY / primary-key changes: create a new table, INSERT ... SELECT, RENAME —
--      ClickHouse cannot ALTER these in place.
--   4. Every section must end by advancing db_schema_version, e.g.:
--        ALTER TABLE info UPDATE db_schema_version = '<version>' WHERE 1;
--      migrate_db.py waits for this mutation (and any others the section triggered) to finish
--      before treating the section as applied.
--   5. Mark a section '## custom: true' when it needs logic beyond raw SQL (e.g. data reshaping
--      that must be computed outside ClickHouse). migrate_db.py runs this section's SQL (if any)
--      first, then calls the matching hardcoded Python function registered in
--      migrate_db.py's CUSTOM_MIGRATIONS dict, keyed by this section's version.
--   6. Any section that changes base tables must also bump ../generate_derived_tables.sql's
--      version (even with no semantic change to that file), since a base-table change may affect
--      derived tables in ways that aren't obvious to every author. derived_table_schema_version
--      may also bump on its own with no corresponding section here.
--
-- Sections for versions already recorded in the target database's info.db_schema_version are
-- skipped automatically — do not remove or renumber old sections once released.

## db_schema_version: 3.0.0
## description: ClickHouse-native migration era begins
ALTER TABLE info UPDATE db_schema_version = '3.0.0' WHERE 1;
