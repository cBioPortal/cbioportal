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
## description: ClickHouse-native migration era begins; collapse derived_table_schema_version into db_schema_version
ALTER TABLE info DROP COLUMN IF EXISTS derived_table_schema_version;

## db_schema_version: 3.1.0
## description: Add unified resource table and backfill from legacy resource_sample/patient/study tables
CREATE TABLE IF NOT EXISTS resource (
    `internal_id` Int64,
    `resource_id` String,
    `cancer_study_id` Int64,
    `resource_scope` String,
    `patient_id` Nullable(String),
    `sample_id` Nullable(String),
    `url` String,
    `display_name` Nullable(String),
    `metadata` Nullable(String)
) ENGINE = MergeTree ORDER BY (cancer_study_id, resource_id, patient_id, sample_id, internal_id)
  SETTINGS allow_nullable_key = 1;

-- create source tables in case this is a re-attempt at migration and they are missing
CREATE TABLE IF NOT EXISTS resource_sample (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);
CREATE TABLE IF NOT EXISTS resource_patient (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);
CREATE TABLE IF NOT EXISTS resource_study (`internal_id` Int64, `resource_id` String, `url` String) ENGINE = MergeTree ORDER BY (internal_id, resource_id, url);

-- Fail before anything is written if a legacy row references a sample, patient or study that is
-- not present. The backfills below join on those tables, so such a row is dropped rather than
-- migrated, and the DROPs at the end remove the only copy.
SELECT throwIf(
    (SELECT count() FROM resource_sample) != (
        SELECT count()
        FROM resource_sample rs
        INNER JOIN sample s ON rs.internal_id = s.internal_id
        INNER JOIN patient p ON s.patient_id = p.internal_id
        INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id),
    'resource_sample holds rows whose sample, patient or study is missing; load them before migrating'
);
SELECT throwIf(
    (SELECT count() FROM resource_patient) != (
        SELECT count()
        FROM resource_patient rp
        INNER JOIN patient pt ON rp.internal_id = pt.internal_id
        INNER JOIN cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id),
    'resource_patient holds rows whose patient or study is missing; load them before migrating'
);
SELECT throwIf(
    (SELECT count() FROM resource_study) != (
        SELECT count()
        FROM resource_study rst
        INNER JOIN cancer_study cs ON rst.internal_id = cs.cancer_study_id),
    'resource_study holds rows whose study is missing; load it before migrating'
);

INSERT INTO resource
    (internal_id, resource_id, cancer_study_id, resource_scope,
     patient_id, sample_id, url, display_name, metadata)
SELECT
    (SELECT ifNull(max(r.internal_id), 0) FROM resource r) + row_number() OVER (),
    rs.resource_id,
    cs.cancer_study_id,
    'SAMPLE',
    p.stable_id,
    s.stable_id,
    rs.url,
    NULL, NULL
FROM resource_sample rs
INNER JOIN sample       s  ON rs.internal_id    = s.internal_id
INNER JOIN patient      p  ON s.patient_id      = p.internal_id
INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
WHERE (cs.cancer_study_id, rs.resource_id, s.stable_id, rs.url) NOT IN (
    SELECT (cancer_study_id, resource_id, sample_id, url) FROM resource WHERE resource_scope = 'SAMPLE'
);

INSERT INTO resource
    (internal_id, resource_id, cancer_study_id, resource_scope,
     patient_id, sample_id, url, display_name, metadata)
SELECT
    (SELECT ifNull(max(r.internal_id), 0) FROM resource r) + row_number() OVER (),
    rp.resource_id,
    cs.cancer_study_id,
    'PATIENT',
    pt.stable_id,
    NULL,
    rp.url,
    NULL, NULL
FROM resource_patient rp
INNER JOIN patient      pt ON rp.internal_id     = pt.internal_id
INNER JOIN cancer_study cs ON pt.cancer_study_id = cs.cancer_study_id
WHERE (cs.cancer_study_id, rp.resource_id, pt.stable_id, rp.url) NOT IN (
    SELECT (cancer_study_id, resource_id, patient_id, url) FROM resource WHERE resource_scope = 'PATIENT'
);

INSERT INTO resource
    (internal_id, resource_id, cancer_study_id, resource_scope,
     patient_id, sample_id, url, display_name, metadata)
SELECT
    (SELECT ifNull(max(r.internal_id), 0) FROM resource r) + row_number() OVER (),
    rst.resource_id,
    rst.internal_id,
    'STUDY',
    NULL, NULL,
    rst.url,
    NULL, NULL
FROM resource_study rst
WHERE (rst.internal_id, rst.resource_id, rst.url) NOT IN (
    SELECT (cancer_study_id, resource_id, url) FROM resource WHERE resource_scope = 'STUDY'
);

-- Fail if rows are not each assigned a unique internal_id
SELECT throwIf(
    count() != uniqExact(internal_id),
    'resource backfill produced duplicate internal_id values'
) FROM resource;

DROP TABLE IF EXISTS resource_sample;
DROP TABLE IF EXISTS resource_patient;
DROP TABLE IF EXISTS resource_study;

## db_schema_version: 3.2.0
## description: Add resource.metadata_json, a JSON column materialized from metadata
-- Requires ClickHouse 25.3 or later, with the JSON type enabled in the server's default profile:
-- on 25.3, enable_json_type = 1 or a compatibility setting of 25.3 or later (newer releases allow
-- the type whatever these settings say). MATERIALIZE COLUMN runs as a background mutation under the
-- default profile, so the SET below is not enough on its own there; inserts into resource evaluate
-- the column under the inserting user's profile. See schema_update_notes.md for what the
-- column holds.
--
-- The section changes nothing in resource until the probe has passed: it first materializes the
-- column into a one-row table, metadata_json_probe. The probe is an ordinary table because ClickHouse
-- Cloud does not allow MergeTree temporary tables. A failed probe leaves only that table, with its
-- failed mutation, behind; the next run drops it before probing again. Until MATERIALIZE COLUMN has
-- finished, a part written before the ALTER has no metadata_json data and reads every key's
-- subcolumn as NULL, so the section waits for it and then checks the written values against
-- metadata before returning. It is idempotent.
--
-- Privileges: SELECT on system.mutations, and on the database CREATE TABLE, INSERT, DROP TABLE,
-- SELECT and ALTER ADD COLUMN / MATERIALIZE COLUMN; see docs/deployment/clickhouse/README.md.
SELECT throwIf(
    (toUInt32OrZero(splitByChar('.', version())[1]), toUInt32OrZero(splitByChar('.', version())[2])) < (25, 3),
    'db_schema_version 3.2.0 requires ClickHouse 25.3 or later'
) FORMAT Null;
SET enable_json_type = 1;
DROP TABLE IF EXISTS metadata_json_probe SYNC;
CREATE TABLE metadata_json_probe (`id` UInt8, `metadata` Nullable(String))
    ENGINE = MergeTree ORDER BY id;
INSERT INTO metadata_json_probe (id, metadata) VALUES (1, '{"k":"v"}');
ALTER TABLE metadata_json_probe ADD COLUMN `metadata_json` JSON MATERIALIZED CAST(CAST(
        arrayMap(kv -> (kv.1, concat(':', kv.2)),
            arrayFilter((kv, n) -> n = 1 AND kv.2 != '',
                arrayMap(kv -> (kv.1, if(startsWith(kv.2, '"'), JSONExtractString(concat('[', kv.2, ']'), 1), if(kv.2 = 'null', '', kv.2))),
                    JSONExtractKeysAndValuesRaw(ifNull(metadata, '{}'))),
                arrayEnumerateUniq(arrayMap(kv -> kv.1, JSONExtractKeysAndValuesRaw(ifNull(metadata, '{}')))))),
        'Map(String, String)'), 'JSON');
-- Issued without waiting, so that a failure can be reported below rather than as the mutation's
-- own error; the second MATERIALIZE then waits for it. A failing mutation records its reason after
-- its first attempt, which a slow or busy server may not schedule at once. ClickHouse SQL has no
-- loop, so the wait is written out: each statement below sleeps 3 s while the probe's mutation has
-- neither finished nor failed, and returns at once after that, so the wait ends as soon as it
-- has an answer and lasts at most 30 s. A mutation still pending after that is waited for by the
-- second MATERIALIZE, which reports a later failure as the mutation's own error.
ALTER TABLE metadata_json_probe MATERIALIZE COLUMN `metadata_json` SETTINGS mutations_sync = 0;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT sleepEachRow(3) FROM system.one WHERE (
    SELECT count() FROM system.mutations
    WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND NOT is_done AND latest_fail_reason = ''
) > 0 FORMAT Null;
SELECT throwIf(
    count() > 0,
    'ClickHouse could not write a JSON column in a background mutation: enable the JSON type in the server''s default profile (enable_json_type = 1, or compatibility of 25.3 or later), restart, and run the migration again'
) FROM system.mutations
WHERE database = currentDatabase() AND table = 'metadata_json_probe' AND latest_fail_reason != ''
FORMAT Null;
ALTER TABLE metadata_json_probe MATERIALIZE COLUMN `metadata_json` SETTINGS mutations_sync = 2;
SELECT throwIf(
    countIf(substring(ifNull(CAST(metadata_json.k, 'Nullable(String)'), ''), 2) = 'v') != 1,
    'ClickHouse did not read back the JSON column it wrote: enable the JSON type in the server''s default profile (enable_json_type = 1, or compatibility of 25.3 or later), restart, and run the migration again'
) FROM metadata_json_probe FORMAT Null;
DROP TABLE metadata_json_probe SYNC;

ALTER TABLE resource ADD COLUMN IF NOT EXISTS `metadata_json` JSON MATERIALIZED CAST(CAST(
        arrayMap(kv -> (kv.1, concat(':', kv.2)),
            arrayFilter((kv, n) -> n = 1 AND kv.2 != '',
                arrayMap(kv -> (kv.1, if(startsWith(kv.2, '"'), JSONExtractString(concat('[', kv.2, ']'), 1), if(kv.2 = 'null', '', kv.2))),
                    JSONExtractKeysAndValuesRaw(ifNull(metadata, '{}'))),
                arrayEnumerateUniq(arrayMap(kv -> kv.1, JSONExtractKeysAndValuesRaw(ifNull(metadata, '{}')))))),
        'Map(String, String)'), 'JSON');
ALTER TABLE resource MATERIALIZE COLUMN `metadata_json` SETTINGS mutations_sync = 2;
-- A finished MATERIALIZE COLUMN mutation is ClickHouse's own record that every part written before
-- it now holds the column; a part written after it computed the column on insert. So the check
-- reads system.mutations: no mutation on resource that writes metadata_json may be unfinished.
SELECT throwIf(
    count() > 0,
    'MATERIALIZE COLUMN metadata_json left an unfinished mutation on resource; see system.mutations'
) FROM system.mutations
WHERE database = currentDatabase() AND table = 'resource' AND NOT is_done
    AND position(command, 'metadata_json') > 0
FORMAT Null;
-- Parity on a sample: each key's value as metadata_json holds it against JSONExtractString on the
-- raw document. toJSONString nests a dotted key's path, so dotted keys, and keys that a dotted key
-- extends, are left out here.
SELECT throwIf(
    countIf(JSONExtractString(doc, key) != substring(JSONExtractString(stored, key), 2)) > 0,
    'metadata_json does not match metadata on a sample of resource rows'
) FROM (
    SELECT doc, stored, arrayJoin(arrayFilter(
        k -> position(k, '.') = 0 AND NOT arrayExists(o -> startsWith(o, concat(k, '.')), keys), keys)) AS key
    FROM (
        SELECT ifNull(metadata, '{}') AS doc, JSONExtractKeys(doc) AS keys, toJSONString(metadata_json) AS stored
        FROM resource
        WHERE metadata IS NOT NULL AND metadata != ''
        LIMIT 10000
    )
) FORMAT Null;
