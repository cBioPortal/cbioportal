-- version 1.0.5 of derived table schema and data definition
-- when making updates:
--     increment the version number here
--     update pom.xml with the new version number

DROP TABLE IF EXISTS sample_to_gene_panel_derived;
DROP TABLE IF EXISTS gene_panel_to_gene_derived;
DROP TABLE IF EXISTS sample_derived;
DROP TABLE IF EXISTS genomic_event_derived;
DROP TABLE IF EXISTS clinical_data_derived;
DROP TABLE IF EXISTS clinical_event_derived;
DROP TABLE IF EXISTS genetic_alteration_derived;
DROP TABLE IF EXISTS generic_assay_data_derived;
DROP TABLE IF EXISTS mutation_derived;

-- the following query "fixes" the sample_profile table by adding entries for "missing" samples -- those which appear in mutated case list but not in the MySQL sample_profile table
-- this problem was handled in java at run time in legacy codebase
-- this MUST BE RUN prior to creation of any derived table which relies on sample_profile table
INSERT INTO sample_profile (sample_id, genetic_profile_id, panel_id) 
WITH missing_samples AS (
    -- Select all members of lists of type '_sequenced' (mutation) which do NOT appear in sample_profile table for profiles of type mutation 
    SELECT
        sample_id,
        cs.cancer_study_identifier AS cancer_study_identifier,
        CONCAT(cancer_study_identifier, '_mutations') as stable_id
    FROM
        sample_list_list sll
            JOIN sample_list sl ON sl.list_id = sll.list_id
            JOIN cancer_study cs ON cs.cancer_study_id = sl.cancer_study_id
    WHERE
            sl.stable_id LIKE '%_sequenced'
      AND CONCAT(sll.sample_id,'-',cs.cancer_study_id) NOT IN (
        SELECT
            CONCAT(sp.sample_id,'-',cs.cancer_study_id)
        FROM
            sample_profile sp
                JOIN genetic_profile gp ON gp.genetic_profile_id = sp.genetic_profile_id
                JOIN cancer_study cs ON cs.cancer_study_id = gp.cancer_study_id
        WHERE
                gp.genetic_alteration_type = 'MUTATION_EXTENDED'
    )
)
-- These are the missing items for the sample_profile table. They are missing because they were not included in matrix file
-- perhaps because they have no associated mutations (even though they WERE profiled for mutation as indicated by presence in the case list file
SELECT
    ms.sample_id as sample_id,
    gp.genetic_profile_id AS genetic_profile_id,
    NULL AS panel_id    
FROM
    missing_samples ms
        JOIN genetic_profile gp ON ms.stable_id=gp.stable_id
        JOIN cancer_study cs ON cs.cancer_study_id=gp.cancer_study_id;

CREATE TABLE sample_to_gene_panel_derived
(
    sample_unique_id String,
    alteration_type LowCardinality(String),
    gene_panel_id LowCardinality(String),
    cancer_study_identifier LowCardinality(String),
    genetic_profile_id LowCardinality(String)
) ENGINE = MergeTree()
ORDER BY (gene_panel_id, alteration_type, genetic_profile_id, sample_unique_id);

INSERT INTO sample_to_gene_panel_derived
SELECT
    concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
    genetic_alteration_type AS alteration_type,
    -- If a mutation is found in a gene that is not in a gene panel we assume Whole Exome Sequencing WES
    ifnull(gene_panel.stable_id, 'WES') AS gene_panel_id,
    cs.cancer_study_identifier AS cancer_study_identifier,
    gp.stable_id AS genetic_profile_id
FROM sample_profile sp
         INNER JOIN genetic_profile gp ON sample_profile.genetic_profile_id = gp.genetic_profile_id
         LEFT JOIN gene_panel ON sp.panel_id = gene_panel.internal_id
         INNER JOIN sample ON sp.sample_id = sample.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id;

CREATE TABLE gene_panel_to_gene_derived
(
    gene_panel_id LowCardinality(String),
    gene String
) ENGINE = MergeTree()
ORDER BY (gene_panel_id);

INSERT INTO gene_panel_to_gene_derived
SELECT
    gp.stable_id AS gene_panel_id,
    g.hugo_gene_symbol AS gene
FROM gene_panel gp
         INNER JOIN gene_panel_list gpl ON gp.internal_id = gpl.internal_id
         INNER JOIN gene g ON g.entrez_gene_id = gpl.gene_id
UNION ALL
SELECT
    'WES' AS gene_panel_id,
    gene.hugo_gene_symbol AS gene
FROM gene
WHERE gene.entrez_gene_id > 0;

CREATE TABLE sample_derived
(
    sample_unique_id            String,
    sample_unique_id_base64     String,
    sample_stable_id            String,
    patient_unique_id           String,
    patient_unique_id_base64    String,
    patient_stable_id           String,
    cancer_study_identifier     LowCardinality(String),
    internal_id                 Int,
    -- fields below are needed for the SUMMARY projection
    patient_internal_id         Int,
    sample_type                 String,
    -- fields below are needed for the DETAILED projection
    sequenced                   Int,
    copy_number_segment_present Int
)
    ENGINE = MergeTree
        ORDER BY (cancer_study_identifier, sample_unique_id);

INSERT INTO sample_derived
WITH 
    sequenced_samples AS (
        SELECT
            sample.stable_id
        FROM sample_list_list
                 INNER JOIN sample_list ON sample_list_list.list_id = sample_list.list_id
                 INNER JOIN sample ON sample_list_list.sample_id = sample.internal_id
                 INNER JOIN patient ON sample.patient_id = patient.internal_id
                 INNER JOIN cancer_study ON patient.cancer_study_id = cancer_study.cancer_study_id
        WHERE sample_list.stable_id = concat(cancer_study.cancer_study_identifier, '_sequenced')
    ),
    cn_segment_samples AS (
        SELECT
            concat(cancer_study.cancer_study_identifier, '_', sample.stable_id) as segment_unique_id
        FROM copy_number_seg
                 INNER JOIN cancer_study ON copy_number_seg.cancer_study_id = cancer_study.cancer_study_id
                 INNER JOIN sample ON copy_number_seg.sample_id = sample.internal_id
                 INNER JOIN patient ON sample.patient_id = patient.internal_id
    )
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       base64Encode(sample.stable_id)                            AS sample_unique_id_base64,
       sample.stable_id                                          AS sample_stable_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id)      AS patient_unique_id,
       base64Encode(p.stable_id)                                 AS patient_unique_id_base64,
       p.stable_id                                               AS patient_stable_id,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       sample.internal_id                                        AS internal_id,
       -- fields below are needed for the SUMMARY projection
       sample.patient_id                                         AS patient_internal_id,
       sample.sample_type                                        AS sample_type,
       -- fields below are needed for the DETAILED projection
       if (sample.stable_id IN sequenced_samples, 1, 0)          AS sequenced,
       if (sample_unique_id IN cn_segment_samples, 1, 0)         AS copy_number_segment_present
FROM sample
         INNER JOIN patient AS p ON sample.patient_id = p.internal_id
         INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE TABLE IF NOT EXISTS genomic_event_derived
(
    sample_unique_id          String,
    hugo_gene_symbol          String,
    entrez_gene_id            Int32,
    gene_panel_stable_id      LowCardinality(String),
    cancer_study_identifier   LowCardinality(String),
    genetic_profile_stable_id LowCardinality(String),
    variant_type              LowCardinality(String),
    mutation_variant          String,
    mutation_type             LowCardinality(String),
    mutation_status           LowCardinality(String),
    driver_filter             LowCardinality(String),
    driver_tiers_filter       LowCardinality(String),
    cna_alteration            Nullable(Int8),
    cna_cytoband              String,
    sv_event_info             String,
    patient_unique_id         String,
    off_panel                 Boolean DEFAULT FALSE
) ENGINE = MergeTree
      ORDER BY (genetic_profile_stable_id, cancer_study_identifier, variant_type, entrez_gene_id, hugo_gene_symbol, sample_unique_id);

INSERT INTO genomic_event_derived
-- Insert Mutations
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                     AS hugo_gene_symbol,
       gene.entrez_gene_id                                       AS entrez_gene_id,
       ifNull(gp.stable_id, 'WES')                               AS gene_panel_stable_id,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       g.stable_id                                               AS genetic_profile_stable_id,
       'mutation'                                                AS variant_type,
       me.protein_change                                         AS mutation_variant,
       me.mutation_type                                          AS mutation_type,
       mutation.mutation_status                                  AS mutation_status,
       'NA'                                                      AS driver_filter,
       'NA'                                                      AS drivet_tiers_filter,
       NULL                                                      AS cna_alteration,
       ''                                                        AS cna_cytoband,
       ''                                                        AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id,
       (gene_panel_stable_id, hugo_gene_symbol) NOT IN (
           SELECT gene_panel_id, gene
           FROM gene_panel_to_gene_derived
       ) AS off_panel
FROM mutation
         INNER JOIN mutation_event AS me ON mutation.mutation_event_id = me.mutation_event_id
         INNER JOIN sample_profile sp
                    ON mutation.sample_id = sp.sample_id AND mutation.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         LEFT JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON mutation.sample_id = sample.internal_id
         INNER JOIN patient on sample.patient_id = patient.internal_id
         LEFT JOIN gene ON mutation.entrez_gene_id = gene.entrez_gene_id;

INSERT INTO genomic_event_derived
-- Insert CNA Genes
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                     AS hugo_gene_symbol,
       gene.entrez_gene_id                                       AS entrez_gene_id,
       ifNull(gp.stable_id, 'WES')                               AS gene_panel_stable_id,
       cs.cancer_study_identifier                                AS cancer_study_identifier,
       g.stable_id                                               AS genetic_profile_stable_id,
       'cna'                                                     AS variant_type,
       'NA'                                                      AS mutation_variant,
       'NA'                                                      AS mutation_type,
       'NA'                                                      AS mutation_status,
       'NA'                                                      AS driver_filter,
       'NA'                                                      AS drivet_tiers_filter,
       ce.alteration                                             AS cna_alteration,
       rgg.cytoband                                              AS cna_cytoband,
       ''                                                        AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id,
       (gene_panel_stable_id, hugo_gene_symbol) NOT IN (
           SELECT gene_panel_id, gene
           FROM gene_panel_to_gene_derived
       ) AS off_panel
FROM cna_event ce
         INNER JOIN sample_cna_event sce ON ce.cna_event_id = sce.cna_event_id
         INNER JOIN sample_profile sp ON sce.sample_id = sp.sample_id AND sce.genetic_profile_id = sp.genetic_profile_id
         LEFT JOIN gene_panel gp ON sp.panel_id = gp.internal_id
         INNER JOIN genetic_profile g ON sp.genetic_profile_id = g.genetic_profile_id
         INNER JOIN cancer_study cs ON g.cancer_study_id = cs.cancer_study_id
         INNER JOIN sample ON sce.sample_id = sample.internal_id
         INNER JOIN patient on sample.patient_id = patient.internal_id
         INNER JOIN gene ON ce.entrez_gene_id = gene.entrez_gene_id
         INNER JOIN reference_genome_gene rgg ON rgg.entrez_gene_id = ce.entrez_gene_id AND rgg.reference_genome_id = cs.reference_genome_id;

INSERT INTO genomic_event_derived
-- Insert Structural Variants Site1
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                AS hugo_gene_symbol,
       gene.entrez_gene_id                                  AS entrez_gene_id,
       ifNull(gene_panel.stable_id, 'WES')                  AS gene_panel_stable_id,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       gp.stable_id                                         AS genetic_profile_stable_id,
       'structural_variant'                                 AS variant_type,
       'NA'                                                 AS mutation_variant,
       'NA'                                                 AS mutation_type,
       'NA'                                                 AS mutation_status,
       'NA'                                                 AS driver_filter,
       'NA'                                                 AS drivet_tiers_filter,
       NULL                                                 AS cna_alteration,
       ''                                                   AS cna_cytoband,
       event_info                                           AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id,
       (gene_panel_stable_id, hugo_gene_symbol) NOT IN (
           SELECT gene_panel_id, gene
           FROM gene_panel_to_gene_derived
       ) AS off_panel
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN patient on s.patient_id = patient.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene ON sv.site1_entrez_gene_id = gene.entrez_gene_id
         INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id AND sample_profile.genetic_profile_id = sv.genetic_profile_id
         LEFT JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id;

INSERT INTO genomic_event_derived
-- Insert Structural Variants Site2
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
       gene.hugo_gene_symbol                                AS hugo_gene_symbol,
       gene.entrez_gene_id                                  AS entrez_gene_id,
       ifNull(gene_panel.stable_id, 'WES')                  AS gene_panel_stable_id,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       gp.stable_id                                         AS genetic_profile_stable_id,
       'structural_variant'                                 AS variant_type,
       'NA'                                                 AS mutation_variant,
       'NA'                                                 AS mutation_type,
       'NA'                                                 AS mutation_status,
       'NA'                                                 AS driver_filter,
       'NA'                                                 AS drivet_tiers_filter,
       NULL                                                 AS cna_alteration,
       ''                                                   AS cna_cytoband,
       event_info                                           AS sv_event_info,
       concat(cs.cancer_study_identifier, '_', patient.stable_id) AS patient_unique_id,
       (gene_panel_stable_id, hugo_gene_symbol) NOT IN (
           SELECT gene_panel_id, gene
           FROM gene_panel_to_gene_derived
       ) AS off_panel
FROM structural_variant sv
         INNER JOIN genetic_profile gp ON sv.genetic_profile_id = gp.genetic_profile_id
         INNER JOIN sample s ON sv.sample_id = s.internal_id
         INNER JOIN patient on s.patient_id = patient.internal_id
         INNER JOIN cancer_study cs ON gp.cancer_study_id = cs.cancer_study_id
         INNER JOIN gene ON sv.site2_entrez_gene_id = gene.entrez_gene_id
         INNER JOIN sample_profile ON s.internal_id = sample_profile.sample_id AND sample_profile.genetic_profile_id = sv.genetic_profile_id
         LEFT JOIN gene_panel ON sample_profile.panel_id = gene_panel.internal_id
WHERE
        sv.site2_entrez_gene_id != sv.site1_entrez_gene_id
   OR sv.site1_entrez_gene_id IS NULL;

CREATE TABLE IF NOT EXISTS clinical_data_derived
(
    internal_id Int,
    sample_unique_id String,
    patient_unique_id String,
    attribute_name LowCardinality(String),
    attribute_value String,
    cancer_study_identifier LowCardinality(String),
    type LowCardinality(String)
)
    ENGINE=MergeTree
        ORDER BY (cancer_study_identifier, type, attribute_name, sample_unique_id);

-- Insert sample attribute data
INSERT INTO TABLE clinical_data_derived
SELECT sm.internal_id             AS internal_id,
       sm.sample_unique_id        AS sample_unique_id,
       sm.patient_unique_id       AS patient_unique_id,
       cam.attr_id                AS attribute_name,
       ifNull(csamp.attr_value, '')          AS attribute_value,
       cs.cancer_study_identifier AS cancer_study_identifier,
       'sample'                   AS type
FROM sample_derived AS sm
         INNER JOIN cancer_study AS cs
                    ON sm.cancer_study_identifier = cs.cancer_study_identifier
         FULL OUTER JOIN clinical_attribute_meta AS cam
                         ON cs.cancer_study_id = cam.cancer_study_id
         FULL OUTER JOIN clinical_sample AS csamp
                         ON (sm.internal_id = csamp.internal_id) AND (csamp.attr_id = cam.attr_id)
WHERE cam.patient_attribute = 0;

-- INSERT patient attribute data
INSERT INTO TABLE clinical_data_derived
SELECT p.internal_id                                        AS internal_id,
       ''                                                   AS sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
       cam.attr_id                                          AS attribute_name,
       ifNull(clinpat.attr_value, '')                       AS attribute_value,
       cs.cancer_study_identifier                           AS cancer_study_identifier,
       'patient'                                            AS type
FROM patient AS p
         INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
         FULL OUTER JOIN clinical_attribute_meta AS cam
                         ON cs.cancer_study_id = cam.cancer_study_id
         FULL OUTER JOIN clinical_patient AS clinpat
                         ON (p.internal_id = clinpat.internal_id) AND (clinpat.attr_id = cam.attr_id)
WHERE cam.patient_attribute = 1;

CREATE TABLE clinical_event_derived
(
    patient_unique_id String,
    key String,
    value String,
    start_date Int32,
    stop_date Int32 DEFAULT 0,
    event_type LowCardinality(String),
    cancer_study_identifier LowCardinality(String)
)
ENGINE = MergeTree
    ORDER BY (cancer_study_identifier, event_type, patient_unique_id);

INSERT INTO clinical_event_derived
SELECT
    concat(cs.cancer_study_identifier, '_', p.stable_id)      AS patient_unique_id,
    ced.key AS key,
    ced.value AS value,
    ce.start_date AS start_date,
    ifNull(ce.stop_date, 0) AS stop_date,
    ce.event_type AS event_type,
    cs.cancer_study_identifier
FROM clinical_event_data ced
    RIGHT JOIN clinical_event ce ON ced.clinical_event_id = ce.clinical_event_id
    INNER JOIN patient p ON ce.patient_id = p.internal_id
    INNER JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE TABLE IF NOT EXISTS genetic_alteration_derived
(
    sample_unique_id String,
    cancer_study_identifier LowCardinality(String),
    hugo_gene_symbol String,
    profile_type LowCardinality(String),
    alteration_value Nullable(String)
    )
    ENGINE = MergeTree()
    ORDER BY (cancer_study_identifier, hugo_gene_symbol, profile_type, sample_unique_id);

INSERT INTO TABLE genetic_alteration_derived
SELECT
    sample_unique_id,
    cancer_study_identifier,
    hugo_gene_symbol,
    replaceOne(stable_id, concat(sd.cancer_study_identifier, '_'), '') as profile_type,
    alteration_value
FROM
    (SELECT
         sample_id,
         hugo_gene_symbol,
         stable_id,
         alteration_value
    FROM
        (SELECT
            g.hugo_gene_symbol AS hugo_gene_symbol,
            gp.stable_id as stable_id,
            arrayMap(x -> (x = '' ? NULL : x), splitByString(',', assumeNotNull(substring(ga.values, 1, -1)))) AS alteration_value,
            arrayMap(x -> (x = '' ? NULL : toInt32(x)), splitByString(',', assumeNotNull(substring(gps.ordered_sample_list, 1, -1)))) AS sample_id
        FROM
            genetic_alteration ga
            JOIN genetic_profile gp ON ga.genetic_profile_id=gp.genetic_profile_id
            JOIN genetic_profile_samples gps ON gp.genetic_profile_id = gps.genetic_profile_id
            JOIN gene g ON ga.genetic_entity_id = g.genetic_entity_id
        WHERE
             gp.genetic_alteration_type NOT IN ('GENERIC_ASSAY', 'MUTATION_EXTENDED', 'MUTATION_UNCALLED', 'STRUCTURAL_VARIANT'))
            ARRAY JOIN alteration_value, sample_id
    WHERE alteration_value != 'NA') AS subquery
        JOIN sample_derived sd ON sd.internal_id = subquery.sample_id;

CREATE TABLE IF NOT EXISTS generic_assay_data_derived
(
    sample_unique_id String,
    patient_unique_id String,
    genetic_entity_id String,
    value String,
    generic_assay_type String,
    profile_stable_id String,
    entity_stable_id String,
    datatype String,
    patient_level NUMERIC,
    profile_type String
)
    ENGINE = MergeTree()
    ORDER BY (profile_type, entity_stable_id, patient_unique_id, sample_unique_id);

INSERT INTO TABLE generic_assay_data_derived
SELECT
    sd.sample_unique_id as sample_unique_id,
    sd.patient_unique_id as patient_unique_id,
    genetic_entity_id,
    value,
    generic_assay_type,
    profile_stable_id,
    entity_stable_id,
    datatype,
    patient_level,
    replaceOne(profile_stable_id, concat(cs.cancer_study_identifier, '_'), '') as profile_type
FROM
    (SELECT
         sample_id,
         genetic_entity_id,
         value,
         cancer_study_id,
         generic_assay_type,
         genetic_profile_id,
         profile_stable_id,
         entity_stable_id,
         patient_level,
         datatype
     FROM
         (SELECT
              sample_id as sample_unique_id,
              gp.cancer_study_id AS cancer_study_id,
              ga.genetic_entity_id as genetic_entity_id,
              gp.genetic_profile_id as genetic_profile_id,
              gp.generic_assay_type as generic_assay_type,
              gp.stable_id as profile_stable_id,
              ge.stable_id as entity_stable_id,
              gp.datatype as datatype,
              gp.patient_level as patient_level,
              arrayMap(x -> (x = '' ? NULL : x), splitByString(',', assumeNotNull(substring(ga.values, 1, -1)))) AS value,
              arrayMap(x -> (x = '' ? NULL : toInt64(x)), splitByString(',', assumeNotNull(substring(gps.ordered_sample_list, 1, -1)))) AS sample_id
          FROM

              genetic_alteration ga
              JOIN genetic_profile gp ON ga.genetic_profile_id=gp.genetic_profile_id
              JOIN genetic_profile_samples gps ON gp.genetic_profile_id = gps.genetic_profile_id
              JOIN genetic_entity ge on ga.genetic_entity_id = ge.id
          WHERE
              gp.generic_assay_type IS NOT NULL
         )
             ARRAY JOIN value, sample_id) AS subquery
        JOIN cancer_study cs ON cs.cancer_study_id = subquery.cancer_study_id
        JOIN sample_derived sd ON sd.internal_id = subquery.sample_id;



DROP TABLE IF EXISTS mutation_derived;
CREATE TABLE mutation_derived
(
    molecularProfileId String COMMENT 'Stable ID of the genetic profile',
    sampleId String COMMENT 'Stable ID of the sample',
    sampleInternalId Int64,
    patientId String COMMENT 'Stable ID of the patient',
    entrezGeneId Int64 COMMENT 'Entrez Gene ID from mutation table (NOT NULL)',
    studyId String COMMENT 'Cancer study identifier',
    center Nullable(String) COMMENT 'Sequencing center',
    mutationStatus Nullable(String) COMMENT 'Mutation status (e.g., Somatic, Germline)',
    validationStatus Nullable(String) COMMENT 'Validation status',
    tumorAltCount Nullable(Int64) COMMENT 'Tumor alternate allele count',
    tumorRefCount Nullable(Int64) COMMENT 'Tumor reference allele count',
    normalAltCount Nullable(Int64) COMMENT 'Normal alternate allele count',
    normalRefCount Nullable(Int64) COMMENT 'Normal reference allele count',
    aminoAcidChange Nullable(String) COMMENT 'Amino acid change',
    chr Nullable(String) COMMENT 'Chromosome',
    startPosition Nullable(Int64) COMMENT 'Start position',
    endPosition Nullable(Int64) COMMENT 'End position',
    referenceAllele Nullable(String) COMMENT 'Reference allele',
    tumorSeqAllele Nullable(String) COMMENT 'Tumor sequence allele',
    proteinChange Nullable(String) COMMENT 'Protein change',
    mutationType Nullable(String) COMMENT 'Type of mutation',
    ncbiBuild Nullable(String) COMMENT 'NCBI build version',
    variantType Nullable(String) COMMENT 'Variant type',
    refseqMrnaId Nullable(String) COMMENT 'RefSeq mRNA ID',
    proteinPosStart Nullable(Int64) COMMENT 'Protein position start',
    proteinPosEnd Nullable(Int64) COMMENT 'Protein position end',
    keyword Nullable(String) COMMENT 'Keyword',
    annotationJSON Nullable(String) COMMENT 'Annotation JSON',
    driverFilter Nullable(String) COMMENT 'Driver filter',
    driverFilterAnnotation Nullable(String) COMMENT 'Driver filter annotation',
    driverTiersFilter Nullable(String) COMMENT 'Driver tiers filter',
    driverTiersFilterAnnotation Nullable(String) COMMENT 'Driver tiers filter annotation',
    `GENE.entrezGeneId` Nullable(Int64) COMMENT 'Gene entrez ID',
    `GENE.hugoGeneSymbol` Nullable(String) COMMENT 'HUGO gene symbol',
    `GENE.type` Nullable(String) COMMENT 'Gene type',
    `alleleSpecificCopyNumber.ascnIntegerCopyNumber` Nullable(Int64) COMMENT 'ASCN integer copy number',
    `alleleSpecificCopyNumber.ascnMethod` Nullable(String) COMMENT 'ASCN method',
    `alleleSpecificCopyNumber.ccfExpectedCopiesUpper` Nullable(Float64) COMMENT 'CCF expected copies upper bound',
    `alleleSpecificCopyNumber.ccfExpectedCopies` Nullable(Float64) COMMENT 'CCF expected copies',
    `alleleSpecificCopyNumber.clonal` Nullable(String) COMMENT 'Clonality annotation',
    `alleleSpecificCopyNumber.minorCopyNumber` Nullable(Int64) COMMENT 'Minor copy number',
    `alleleSpecificCopyNumber.expectedAltCopies` Nullable(Int64) COMMENT 'Expected alternate copies',
    `alleleSpecificCopyNumber.totalCopyNumber` Nullable(Int64) COMMENT 'Total copy number'
)
    ENGINE = MergeTree()
      ORDER BY (molecularProfileId, sampleId, entrezGeneId)
      COMMENT 'Mutation query results with detailed annotations including driver status and allele-specific copy numbers';

INSERT INTO mutation_derived
SELECT
    genetic_profile.stable_id AS molecularProfileId,
    sample.stable_id AS sampleId,
    sample.internal_id As sampleInternalId,
    patient.stable_id AS patientId,
    mutation.entrez_gene_id AS entrezGeneId,
    cancer_study.cancer_study_identifier AS studyId,
    mutation.center AS center,
    mutation.mutation_status AS mutationStatus,
    mutation.validation_status AS validationStatus,
    mutation.tumor_alt_count AS tumorAltCount,
    mutation.tumor_ref_count AS tumorRefCount,
    mutation.normal_alt_count AS normalAltCount,
    mutation.normal_ref_count AS normalRefCount,
    mutation.amino_acid_change AS aminoAcidChange,
    mutation_event.chr AS chr,
    mutation_event.start_position AS startPosition,
    mutation_event.end_position AS endPosition,
    mutation_event.reference_allele AS referenceAllele,
    mutation_event.tumor_seq_allele AS tumorSeqAllele,
    mutation_event.protein_change AS proteinChange,
    mutation_event.mutation_type AS mutationType,
    mutation_event.ncbi_build AS ncbiBuild,
    mutation_event.variant_type AS variantType,
    mutation_event.refseq_mrna_id AS refseqMrnaId,
    mutation_event.protein_pos_start AS proteinPosStart,
    mutation_event.protein_pos_end AS proteinPosEnd,
    mutation_event.keyword AS keyword,
    mutation.annotation_json AS annotationJSON,
    alteration_driver_annotation.driver_filter AS driverFilter,
    alteration_driver_annotation.driver_filter_annotation AS driverFilterAnnotation,
    alteration_driver_annotation.driver_tiers_filter AS driverTiersFilter,
    alteration_driver_annotation.driver_tiers_filter_annotation AS driverTiersFilterAnnotation,
    gene.entrez_gene_id AS `GENE.entrezGeneId`,
    gene.hugo_gene_symbol AS `GENE.hugoGeneSymbol`,
    gene.type AS `GENE.type`,
    allele_specific_copy_number.ascn_integer_copy_number AS `alleleSpecificCopyNumber.ascnIntegerCopyNumber`,
    allele_specific_copy_number.ascn_method AS `alleleSpecificCopyNumber.ascnMethod`,
    allele_specific_copy_number.ccf_expected_copies_upper AS `alleleSpecificCopyNumber.ccfExpectedCopiesUpper`,
    allele_specific_copy_number.ccf_expected_copies AS `alleleSpecificCopyNumber.ccfExpectedCopies`,
    allele_specific_copy_number.clonal AS `alleleSpecificCopyNumber.clonal`,
    allele_specific_copy_number.minor_copy_number AS `alleleSpecificCopyNumber.minorCopyNumber`,
    allele_specific_copy_number.expected_alt_copies AS `alleleSpecificCopyNumber.expectedAltCopies`,
    allele_specific_copy_number.total_copy_number AS `alleleSpecificCopyNumber.totalCopyNumber`
FROM mutation
         INNER JOIN genetic_profile ON mutation.genetic_profile_id = genetic_profile.genetic_profile_id
         INNER JOIN sample ON mutation.sample_id = sample.internal_id
         INNER JOIN patient ON sample.patient_id = patient.internal_id
         INNER JOIN cancer_study ON patient.cancer_study_id = cancer_study.cancer_study_id
         LEFT JOIN alteration_driver_annotation ON (mutation.genetic_profile_id = alteration_driver_annotation.genetic_profile_id) AND (mutation.sample_id = alteration_driver_annotation.sample_id) AND (mutation.mutation_event_id = alteration_driver_annotation.alteration_event_id)
         INNER JOIN mutation_event ON mutation.mutation_event_id = mutation_event.mutation_event_id
         INNER JOIN gene ON mutation.entrez_gene_id = gene.entrez_gene_id
         LEFT JOIN allele_specific_copy_number ON (mutation.mutation_event_id = allele_specific_copy_number.mutation_event_id) AND (mutation.genetic_profile_id = allele_specific_copy_number.genetic_profile_id) AND (mutation.sample_id = allele_specific_copy_number.sample_id);




-- START: PRIMARY KEY ADDITIONS
-- THE FOLLOWING SCRIPTS EXIST TO ADD PRIMARY KEYS TO LEGACY TABLES THAT ARE MISSING THEM.  YOU
-- CANNOT CHANGE THE PRIMARY KEY ON A TABLE IN CLICKHOUSE, SO WE NEED TO CREATE A NEW TABLE WITH THE
-- PRIMARY KEY AND THEN COPY THE DATA OVER.


--Adds primary key to the sample_cna_event table for Clickhouse-only
DROP TABLE IF EXISTS sample_cna_event_BACKUP;
CREATE TABLE sample_cna_event_BACKUP
(
    `cna_event_id` Int64 COMMENT 'References cna_event.cna_event_id.',
    `sample_id` Int64 COMMENT 'References sample.internal_id.',
    `genetic_profile_id` Int64 COMMENT 'References genetic_profile.genetic_profile_id.',
    `annotation_json` Nullable(String) COMMENT 'JSON-formatted annotation details.'
)
    ENGINE = MergeTree()
PRIMARY KEY (genetic_profile_id, cna_event_id, sample_id)
ORDER BY (genetic_profile_id, cna_event_id, sample_id)
SETTINGS index_granularity = 8192
COMMENT 'Observed CNA events per sample and profile. References cna_event, sample, and genetic_profile.';

-- Copy the data
INSERT INTO sample_cna_event_BACKUP
SELECT * FROM sample_cna_event;

-- SWITCH THE TABLES
EXCHANGE TABLES sample_cna_event_BACKUP AND sample_cna_event;

DROP TABLE IF EXISTS mutation_BACKUP;
CREATE TABLE mutation_BACKUP
(
    `mutation_event_id` Int64 COMMENT 'References mutation_event.mutation_event_id.',
    `genetic_profile_id` Int64 COMMENT 'References genetic_profile.genetic_profile_id.',
    `sample_id` Int64 COMMENT 'References sample.internal_id.',
    `entrez_gene_id` Int64 COMMENT 'References gene.entrez_gene_id.',
    `center` Nullable(String) COMMENT 'Center where sequencing was performed.',
    `sequencer` Nullable(String) COMMENT 'Sequencing platform used.',
    `mutation_status` Nullable(String) COMMENT 'Mutation status: Germline, Somatic, or LOH.',
    `validation_status` Nullable(String) COMMENT 'Validation status.',
    `tumor_seq_allele1` Nullable(String) COMMENT 'Tumor allele 1 sequence.',
    `tumor_seq_allele2` Nullable(String) COMMENT 'Tumor allele 2 sequence.',
    `matched_norm_sample_barcode` Nullable(String) COMMENT 'Matched normal sample barcode.',
    `match_norm_seq_allele1` Nullable(String) COMMENT 'Matched normal allele 1 sequence.',
    `match_norm_seq_allele2` Nullable(String) COMMENT 'Matched normal allele 2 sequence.',
    `tumor_validation_allele1` Nullable(String) COMMENT 'Tumor validation allele 1 sequence.',
    `tumor_validation_allele2` Nullable(String) COMMENT 'Tumor validation allele 2 sequence.',
    `match_norm_validation_allele1` Nullable(String) COMMENT 'Matched normal validation allele 1.',
    `match_norm_validation_allele2` Nullable(String) COMMENT 'Matched normal validation allele 2.',
    `verification_status` Nullable(String) COMMENT 'Verification status.',
    `sequencing_phase` Nullable(String) COMMENT 'Sequencing phase.',
    `sequence_source` Nullable(String) COMMENT 'Source of sequencing data.',
    `validation_method` Nullable(String) COMMENT 'Validation method used.',
    `score` Nullable(String) COMMENT 'Score or quality metric.',
    `bam_file` Nullable(String) COMMENT 'Associated BAM file.',
    `tumor_alt_count` Nullable(Int64) COMMENT 'Tumor alternate allele count.',
    `tumor_ref_count` Nullable(Int64) COMMENT 'Tumor reference allele count.',
    `normal_alt_count` Nullable(Int64) COMMENT 'Normal alternate allele count.',
    `normal_ref_count` Nullable(Int64) COMMENT 'Normal reference allele count.',
    `amino_acid_change` Nullable(String) COMMENT 'Amino acid change from mutation.',
    `annotation_json` Nullable(String) COMMENT 'JSON-formatted annotations.'
)
    ENGINE = MergeTree()
ORDER BY (genetic_profile_id,entrez_gene_id)
COMMENT 'Mutation observations in specific samples and profiles. References mutation_event, gene, genetic_profile, and sample.';

-- copy data into new table
INSERT INTO mutation_BACKUP
SELECT * FROM mutation;

-- switch the tables
EXCHANGE TABLES mutation_BACKUP AND mutation;


-- Adds primary key genetic_alteration table for Clickhouse-only
DROP TABLE IF EXISTS genetic_alteration_BACKUP;
CREATE TABLE genetic_alteration_BACKUP
(
    `genetic_profile_id` Int64,
    `genetic_entity_id` Int64,
    `values` String
)
    ENGINE = MergeTree()
        ORDER BY (genetic_profile_id, genetic_entity_id);

-- Copy the data
INSERT INTO genetic_alteration_BACKUP
SELECT * FROM genetic_alteration;

-- SWITCH THE TABLES
EXCHANGE TABLES genetic_alteration_BACKUP AND genetic_alteration;

--END: PRIMARY KEY ADDITIONS


OPTIMIZE TABLE sample_to_gene_panel_derived;
OPTIMIZE TABLE gene_panel_to_gene_derived;
OPTIMIZE TABLE sample_derived;
OPTIMIZE TABLE genomic_event_derived;
OPTIMIZE TABLE clinical_data_derived;
OPTIMIZE TABLE clinical_event_derived;
OPTIMIZE TABLE genetic_alteration_derived;
OPTIMIZE TABLE generic_assay_data_derived;
