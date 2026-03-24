-- StarRocks views replicating ClickHouse derived tables
-- Allows exact CH mapper SQL to run against StarRocks unchanged
--
-- Column names and semantics match clickhouse.sql exactly.
-- Function substitutions:
--   base64Encode(x)  →  to_base64(x)
--   ifNull(x, y)     →  IFNULL(x, y)        (same behaviour)
--   concat(...)      →  concat(...)          (same)
--
-- Notes:
--   • genomic_event_derived: loaded table dropped; replaced with view over
--     mutation + cna_event base tables. No structural_variant table in SR,
--     so sv rows are absent (those studies have 0 SVs in this dataset).
--   • clinical_event_derived: loaded table (wrong schema) dropped; replaced
--     with view matching CH column names.
--   • clinical_data_derived: new view; replicates CH FULL OUTER JOIN semantics
--     (all study-defined attrs appear, missing values default to '').
--   • gene_panel_to_gene_derived: loaded table dropped; replaced with view.
--     Column 'gene' = hugo_gene_symbol, matching CH schema (mapper joins on it).

USE cbioportal;

-- ─────────────────────────────────────────────────────────────
-- 1. gene_panel_to_gene_derived
--    CH cols: gene_panel_id (String), gene (String = hugo_gene_symbol)
--    Includes 'WES' pseudo-panel covering all protein-coding genes
-- ─────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS gene_panel_to_gene_derived;
CREATE VIEW gene_panel_to_gene_derived AS
SELECT gp.stable_id          AS gene_panel_id,
       g.hugo_gene_symbol    AS gene
FROM gene_panel gp
JOIN gene_panel_list gpl ON gp.internal_id = gpl.internal_id
JOIN gene g               ON g.entrez_gene_id = gpl.gene_id
UNION ALL
SELECT 'WES'               AS gene_panel_id,
       g.hugo_gene_symbol  AS gene
FROM gene g
WHERE g.entrez_gene_id > 0;

-- ─────────────────────────────────────────────────────────────
-- 2. sample_to_gene_panel_derived
--    CH cols: sample_unique_id, alteration_type, gene_panel_id,
--             cancer_study_identifier, genetic_profile_id
--    WES samples get gene_panel_id = 'WES'
-- ─────────────────────────────────────────────────────────────
CREATE VIEW sample_to_gene_panel_derived AS
SELECT CONCAT(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gp.genetic_alteration_type                           AS alteration_type,
       IFNULL(panel.stable_id, 'WES')                       AS gene_panel_id,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       gp.stable_id                                         AS genetic_profile_id
FROM sample_profile sp
JOIN genetic_profile gp  ON sp.genetic_profile_id = gp.genetic_profile_id
LEFT JOIN gene_panel panel ON sp.panel_id = panel.internal_id
JOIN sample s             ON sp.sample_id = s.internal_id
JOIN patient p            ON s.patient_id = p.internal_id
JOIN cancer_study cs      ON gp.cancer_study_id = cs.cancer_study_id;

-- ─────────────────────────────────────────────────────────────
-- 3. sample_derived
--    CH cols: sample_unique_id, sample_unique_id_base64, sample_stable_id,
--             patient_unique_id, patient_unique_id_base64, patient_stable_id,
--             cancer_study_identifier, internal_id, patient_internal_id,
--             sample_type, sequenced, copy_number_segment_present
--    Note: CH base64Encode encodes just the stable_id, not the composite key
-- ─────────────────────────────────────────────────────────────
CREATE VIEW sample_derived AS
SELECT CONCAT(cs.cancer_study_identifier, '_', s.stable_id)   AS sample_unique_id,
       to_base64(s.stable_id)                                  AS sample_unique_id_base64,
       s.stable_id                                             AS sample_stable_id,
       CONCAT(cs.cancer_study_identifier, '_', p.stable_id)   AS patient_unique_id,
       to_base64(p.stable_id)                                  AS patient_unique_id_base64,
       p.stable_id                                             AS patient_stable_id,
       cs.cancer_study_identifier                              AS cancer_study_identifier,
       s.internal_id                                           AS internal_id,
       s.patient_id                                            AS patient_internal_id,
       s.sample_type                                           AS sample_type,
       CASE WHEN seq.sample_id IS NOT NULL THEN 1 ELSE 0 END   AS sequenced,
       CASE WHEN cns.sample_id IS NOT NULL THEN 1 ELSE 0 END   AS copy_number_segment_present
FROM sample s
JOIN patient p       ON s.patient_id = p.internal_id
JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
-- sequenced: sample appears in the '{study}_sequenced' sample list
LEFT JOIN (
    SELECT sll.sample_id
    FROM sample_list_list sll
    JOIN sample_list sl ON sll.list_id = sl.list_id
    JOIN sample s2      ON sll.sample_id = s2.internal_id
    JOIN patient p2     ON s2.patient_id = p2.internal_id
    JOIN cancer_study cs2 ON p2.cancer_study_id = cs2.cancer_study_id
    WHERE sl.stable_id = CONCAT(cs2.cancer_study_identifier, '_sequenced')
) seq ON seq.sample_id = s.internal_id
-- copy_number_segment_present: sample has at least one segment row
LEFT JOIN (
    SELECT DISTINCT sample_id FROM copy_number_seg
) cns ON cns.sample_id = s.internal_id;

-- ─────────────────────────────────────────────────────────────
-- 4. genomic_event_derived
--    Drop the loaded 50M-row materialized table; replace with view.
--    CH cols: sample_unique_id, hugo_gene_symbol, entrez_gene_id,
--             gene_panel_stable_id, cancer_study_identifier,
--             genetic_profile_stable_id, variant_type, mutation_variant,
--             mutation_type, mutation_status, driver_filter,
--             driver_filter_annotation, driver_tiers_filter,
--             driver_tiers_filter_annotation, cna_alteration, cna_cytoband,
--             sv_event_info, patient_unique_id, off_panel
--    off_panel: gene not found in the assigned panel's gene list
--    SVs omitted (no structural_variant table in this SR instance)
-- ─────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS genomic_event_derived;
CREATE VIEW genomic_event_derived AS
-- ── Mutations ──────────────────────────────────────────────
SELECT CONCAT(cs.cancer_study_identifier, '_', s.stable_id)   AS sample_unique_id,
       g.hugo_gene_symbol                                      AS hugo_gene_symbol,
       g.entrez_gene_id                                        AS entrez_gene_id,
       IFNULL(panel.stable_id, 'WES')                          AS gene_panel_stable_id,
       cs.cancer_study_identifier                              AS cancer_study_identifier,
       gp.stable_id                                            AS genetic_profile_stable_id,
       'mutation'                                              AS variant_type,
       me.protein_change                                       AS mutation_variant,
       me.mutation_type                                        AS mutation_type,
       m.mutation_status                                       AS mutation_status,
       ada.driver_filter                                       AS driver_filter,
       ada.driver_filter_annotation                            AS driver_filter_annotation,
       ada.driver_tiers_filter                                 AS driver_tiers_filter,
       ada.driver_tiers_filter_annotation                      AS driver_tiers_filter_annotation,
       CAST(NULL AS TINYINT)                                   AS cna_alteration,
       ''                                                      AS cna_cytoband,
       ''                                                      AS sv_event_info,
       CONCAT(cs.cancer_study_identifier, '_', p.stable_id)   AS patient_unique_id,
       -- off_panel: panel assigned AND gene not in that panel's list
       CASE WHEN panel.stable_id IS NOT NULL AND cov_m.gene_id IS NULL
            THEN true ELSE false END                           AS off_panel
FROM mutation m
JOIN mutation_event me         ON m.mutation_event_id = me.mutation_event_id
JOIN sample_profile sp         ON m.sample_id = sp.sample_id
                               AND m.genetic_profile_id = sp.genetic_profile_id
LEFT JOIN gene_panel panel     ON sp.panel_id = panel.internal_id
JOIN genetic_profile gp        ON sp.genetic_profile_id = gp.genetic_profile_id
JOIN cancer_study cs           ON gp.cancer_study_id = cs.cancer_study_id
JOIN sample s                  ON m.sample_id = s.internal_id
JOIN patient p                 ON s.patient_id = p.internal_id
JOIN gene g                    ON m.entrez_gene_id = g.entrez_gene_id
LEFT JOIN alteration_driver_annotation ada
                               ON m.genetic_profile_id = ada.genetic_profile_id
                               AND m.sample_id = ada.sample_id
                               AND m.mutation_event_id = ada.alteration_event_id
LEFT JOIN (
    SELECT DISTINCT gpl.gene_id, gp2.stable_id AS panel_stable_id
    FROM gene_panel_list gpl
    JOIN gene_panel gp2 ON gpl.internal_id = gp2.internal_id
) cov_m ON cov_m.panel_stable_id = panel.stable_id AND cov_m.gene_id = m.entrez_gene_id

UNION ALL

-- ── CNAs ───────────────────────────────────────────────────
SELECT CONCAT(cs.cancer_study_identifier, '_', s.stable_id)   AS sample_unique_id,
       g.hugo_gene_symbol                                      AS hugo_gene_symbol,
       g.entrez_gene_id                                        AS entrez_gene_id,
       IFNULL(panel.stable_id, 'WES')                          AS gene_panel_stable_id,
       cs.cancer_study_identifier                              AS cancer_study_identifier,
       gp.stable_id                                            AS genetic_profile_stable_id,
       'cna'                                                   AS variant_type,
       'NA'                                                    AS mutation_variant,
       'NA'                                                    AS mutation_type,
       'NA'                                                    AS mutation_status,
       ada.driver_filter                                       AS driver_filter,
       ada.driver_filter_annotation                            AS driver_filter_annotation,
       ada.driver_tiers_filter                                 AS driver_tiers_filter,
       ada.driver_tiers_filter_annotation                      AS driver_tiers_filter_annotation,
       ce.alteration                                           AS cna_alteration,
       rgg.cytoband                                            AS cna_cytoband,
       ''                                                      AS sv_event_info,
       CONCAT(cs.cancer_study_identifier, '_', p.stable_id)   AS patient_unique_id,
       CASE WHEN panel.stable_id IS NOT NULL AND cov_c.gene_id IS NULL
            THEN true ELSE false END                           AS off_panel
FROM cna_event ce
JOIN sample_cna_event sce      ON ce.cna_event_id = sce.cna_event_id
JOIN sample_profile sp         ON sce.sample_id = sp.sample_id
                               AND sce.genetic_profile_id = sp.genetic_profile_id
LEFT JOIN gene_panel panel     ON sp.panel_id = panel.internal_id
JOIN genetic_profile gp        ON sp.genetic_profile_id = gp.genetic_profile_id
JOIN cancer_study cs           ON gp.cancer_study_id = cs.cancer_study_id
JOIN sample s                  ON sce.sample_id = s.internal_id
JOIN patient p                 ON s.patient_id = p.internal_id
JOIN gene g                    ON ce.entrez_gene_id = g.entrez_gene_id
JOIN reference_genome_gene rgg ON rgg.entrez_gene_id = ce.entrez_gene_id
                               AND rgg.reference_genome_id = cs.reference_genome_id
LEFT JOIN alteration_driver_annotation ada
                               ON sce.genetic_profile_id = ada.genetic_profile_id
                               AND sce.sample_id = ada.sample_id
                               AND sce.cna_event_id = ada.alteration_event_id
LEFT JOIN (
    SELECT DISTINCT gpl.gene_id, gp2.stable_id AS panel_stable_id
    FROM gene_panel_list gpl
    JOIN gene_panel gp2 ON gpl.internal_id = gp2.internal_id
) cov_c ON cov_c.panel_stable_id = panel.stable_id AND cov_c.gene_id = ce.entrez_gene_id;

-- ─────────────────────────────────────────────────────────────
-- 5. clinical_data_derived
--    CH cols: internal_id, sample_unique_id, patient_unique_id,
--             attribute_name, attribute_value, cancer_study_identifier, type
--    Replicates CH FULL OUTER JOIN semantics: every study-defined attribute
--    appears for every sample/patient; missing values default to ''
-- ─────────────────────────────────────────────────────────────
CREATE VIEW clinical_data_derived AS
-- Sample attributes
SELECT s.internal_id                                           AS internal_id,
       CONCAT(cs.cancer_study_identifier, '_', s.stable_id)   AS sample_unique_id,
       CONCAT(cs.cancer_study_identifier, '_', p.stable_id)   AS patient_unique_id,
       cam.attr_id                                             AS attribute_name,
       IFNULL(csam.attr_value, '')                             AS attribute_value,
       cs.cancer_study_identifier                              AS cancer_study_identifier,
       'sample'                                                AS type
FROM sample s
JOIN patient p               ON s.patient_id = p.internal_id
JOIN cancer_study cs         ON p.cancer_study_id = cs.cancer_study_id
JOIN clinical_attribute_meta cam
                             ON cam.cancer_study_id = cs.cancer_study_id
                             AND cam.patient_attribute = 0
LEFT JOIN clinical_sample csam
                             ON csam.internal_id = s.internal_id
                             AND csam.attr_id = cam.attr_id

UNION ALL

-- Patient attributes (one row per patient per attr, not expanded per sample)
SELECT p.internal_id                                          AS internal_id,
       ''                                                     AS sample_unique_id,
       CONCAT(cs.cancer_study_identifier, '_', p.stable_id)  AS patient_unique_id,
       cam.attr_id                                            AS attribute_name,
       IFNULL(cpat.attr_value, '')                            AS attribute_value,
       cs.cancer_study_identifier                             AS cancer_study_identifier,
       'patient'                                              AS type
FROM patient p
JOIN cancer_study cs         ON p.cancer_study_id = cs.cancer_study_id
JOIN clinical_attribute_meta cam
                             ON cam.cancer_study_id = cs.cancer_study_id
                             AND cam.patient_attribute = 1
LEFT JOIN clinical_patient cpat
                             ON cpat.internal_id = p.internal_id
                             AND cpat.attr_id = cam.attr_id;

-- ─────────────────────────────────────────────────────────────
-- 6. clinical_event_derived
--    Drop loaded table (mismatched schema); replace with view.
--    CH cols: patient_unique_id, key, value, start_date, stop_date,
--             event_type, cancer_study_identifier
-- ─────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS clinical_event_derived;
CREATE VIEW clinical_event_derived AS
SELECT CONCAT(cs.cancer_study_identifier, '_', p.stable_id)  AS patient_unique_id,
       ced.`key`                                              AS `key`,
       ced.value                                              AS value,
       ce.start_date                                          AS start_date,
       IFNULL(ce.stop_date, 0)                                AS stop_date,
       ce.event_type                                          AS event_type,
       cs.cancer_study_identifier                             AS cancer_study_identifier
FROM clinical_event ce
LEFT JOIN clinical_event_data ced ON ced.clinical_event_id = ce.clinical_event_id
JOIN patient p                    ON ce.patient_id = p.internal_id
JOIN cancer_study cs              ON p.cancer_study_id = cs.cancer_study_id;
