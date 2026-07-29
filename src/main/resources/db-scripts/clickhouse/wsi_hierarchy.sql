-- Materialized WSI hierarchy storage. The daily loader replaces rows by
-- snapshot version and flips is_active only after validation succeeds.
CREATE TABLE IF NOT EXISTS wsi_patient_hierarchy
(
    study_id         String,
    patient_id       String,
    snapshot_version UInt64,
    hierarchy_json   String,
    updated_at       DateTime
) ENGINE = MergeTree()
ORDER BY (study_id, patient_id, snapshot_version);

CREATE TABLE IF NOT EXISTS wsi_patient_hierarchy_manifest
(
    study_id       String,
    active_version UInt64,
    updated_at     DateTime
) ENGINE = ReplacingMergeTree(updated_at)
ORDER BY study_id;
