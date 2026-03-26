-- StarRocks schema for cBioPortal
-- Migrated from cgds_public_staging (ClickHouse)
--
-- Conventions:
--   • Dimension tables use PRIMARY KEY engine (upsert support for patient/sample edits)
--   • Fact tables use DUPLICATE KEY engine (max ingest throughput, no dedup needed)
--   • genetic_alteration_derived  (kept as-is; exploded form is canonical)
--   • generic_assay_data_derived  (kept as-is; exploded form is canonical)
--   • All other _derived tables keep their names
--   • replication_num = 1 (single-node Docker)
--   • Bucket counts sized for local evaluation (can be raised for production)

CREATE DATABASE IF NOT EXISTS cbioportal;
USE cbioportal;

-- ============================================================
-- DIMENSION TABLES  (PRIMARY KEY — supports row-level upserts)
-- ============================================================

CREATE TABLE IF NOT EXISTS type_of_cancer (
    type_of_cancer_id   VARCHAR(63)  NOT NULL,
    name                VARCHAR(255) NOT NULL,
    dedicated_color     CHAR(31)     NOT NULL,
    short_name          VARCHAR(127) NULL,
    parent              VARCHAR(63)  NULL
) ENGINE=OLAP
DUPLICATE KEY(type_of_cancer_id)
DISTRIBUTED BY HASH(type_of_cancer_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS reference_genome (
    reference_genome_id  BIGINT       NOT NULL,
    species              VARCHAR(255) NULL,
    name                 VARCHAR(255) NULL,
    build_name           VARCHAR(255) NULL,
    genome_size          BIGINT       NULL,
    url                  VARCHAR(1024) NULL,
    release_date         DATETIME     NULL
) ENGINE=OLAP
PRIMARY KEY (reference_genome_id)
DISTRIBUTED BY HASH(reference_genome_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS cancer_study (
    cancer_study_id         BIGINT        NOT NULL,
    cancer_study_identifier VARCHAR(255)  NULL,
    type_of_cancer_id       VARCHAR(255)  NULL,
    name                    VARCHAR(1024) NULL,
    description             STRING        NULL,
    public                  INT           NULL,
    pmid                    VARCHAR(255)  NULL,
    citation                VARCHAR(1024) NULL,
    `groups`                VARCHAR(1024) NULL,
    status                  BIGINT        NULL,
    import_date             DATETIME      NULL,
    reference_genome_id     BIGINT        NULL
) ENGINE=OLAP
PRIMARY KEY (cancer_study_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS cancer_study_tags (
    cancer_study_id BIGINT NULL,
    tags            STRING NULL
) ENGINE=OLAP
DUPLICATE KEY (cancer_study_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS patient (
    internal_id     BIGINT       NOT NULL,
    stable_id       VARCHAR(255) NULL,
    cancer_study_id BIGINT       NULL
) ENGINE=OLAP
PRIMARY KEY (internal_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS sample (
    internal_id BIGINT       NOT NULL,
    stable_id   VARCHAR(255) NULL,
    sample_type VARCHAR(255) NULL,
    patient_id  BIGINT       NULL
) ENGINE=OLAP
PRIMARY KEY (internal_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS gene (
    entrez_gene_id    BIGINT       NOT NULL,
    hugo_gene_symbol  VARCHAR(255) NULL,
    genetic_entity_id BIGINT       NULL,
    type              VARCHAR(64)  NULL
) ENGINE=OLAP
PRIMARY KEY (entrez_gene_id)
DISTRIBUTED BY HASH(entrez_gene_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS gene_alias (
    entrez_gene_id BIGINT       NULL,
    gene_alias     VARCHAR(255) NULL
) ENGINE=OLAP
DUPLICATE KEY (entrez_gene_id)
DISTRIBUTED BY HASH(entrez_gene_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS genetic_entity (
    id          BIGINT       NOT NULL,
    entity_type VARCHAR(64)  NULL,
    stable_id   VARCHAR(255) NULL
) ENGINE=OLAP
PRIMARY KEY (id)
DISTRIBUTED BY HASH(id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS reference_genome_gene (
    entrez_gene_id      BIGINT       NULL,
    reference_genome_id BIGINT       NULL,
    chr                 VARCHAR(16)  NULL,
    cytoband            VARCHAR(64)  NULL,
    `start`             BIGINT       NULL,
    `end`               BIGINT       NULL
) ENGINE=OLAP
DUPLICATE KEY (entrez_gene_id, reference_genome_id)
DISTRIBUTED BY HASH(entrez_gene_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS genetic_profile (
    genetic_profile_id            BIGINT        NOT NULL,
    stable_id                     VARCHAR(255)  NULL,
    cancer_study_id               BIGINT        NULL,
    genetic_alteration_type       VARCHAR(255)  NULL,
    generic_assay_type            VARCHAR(255)  NULL,
    datatype                      VARCHAR(64)   NULL,
    name                          VARCHAR(1024) NULL,
    description                   STRING        NULL,
    show_profile_in_analysis_tab  INT           NULL,
    pivot_threshold               DOUBLE        NULL,
    sort_order                    VARCHAR(64)   NULL,
    patient_level                 INT           NULL
) ENGINE=OLAP
PRIMARY KEY (genetic_profile_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS genetic_profile_link (
    referring_genetic_profile_id BIGINT       NULL,
    referred_genetic_profile_id  BIGINT       NULL,
    reference_type               VARCHAR(64)  NULL
) ENGINE=OLAP
DUPLICATE KEY (referring_genetic_profile_id)
DISTRIBUTED BY HASH(referring_genetic_profile_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS genetic_profile_samples (
    genetic_profile_id  BIGINT NULL,
    ordered_sample_list TEXT NULL
) ENGINE=OLAP
DUPLICATE KEY (genetic_profile_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS gene_panel (
    internal_id BIGINT        NOT NULL,
    stable_id   VARCHAR(255)  NULL,
    description STRING        NULL
) ENGINE=OLAP
PRIMARY KEY (internal_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS gene_panel_list (
    internal_id BIGINT NULL,
    gene_id     BIGINT NULL
) ENGINE=OLAP
DUPLICATE KEY (internal_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS sample_profile (
    sample_id          BIGINT NULL,
    genetic_profile_id BIGINT NULL,
    panel_id           BIGINT NULL
) ENGINE=OLAP
DUPLICATE KEY (sample_id, genetic_profile_id)
DISTRIBUTED BY HASH(sample_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS sample_list (
    list_id         BIGINT        NOT NULL,
    stable_id       VARCHAR(255)  NULL,
    category        VARCHAR(255)  NULL,
    cancer_study_id BIGINT        NULL,
    name            VARCHAR(1024) NULL,
    description     STRING        NULL
) ENGINE=OLAP
PRIMARY KEY (list_id)
DISTRIBUTED BY HASH(list_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS sample_list_list (
    list_id   BIGINT NULL,
    sample_id BIGINT NULL
) ENGINE=OLAP
DUPLICATE KEY (list_id)
DISTRIBUTED BY HASH(list_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS clinical_attribute_meta (
    attr_id           VARCHAR(255) NULL,
    cancer_study_id   BIGINT       NULL,
    display_name      VARCHAR(1024) NULL,
    description       STRING       NULL,
    datatype          VARCHAR(64)  NULL,
    patient_attribute INT          NULL,
    priority          VARCHAR(64)  NULL
) ENGINE=OLAP
DUPLICATE KEY (attr_id, cancer_study_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS geneset (
    id                BIGINT        NOT NULL,
    genetic_entity_id BIGINT        NULL,
    external_id       VARCHAR(255)  NULL,
    name              VARCHAR(1024) NULL,
    description       STRING        NULL,
    ref_link          VARCHAR(1024) NULL
) ENGINE=OLAP
PRIMARY KEY (id)
DISTRIBUTED BY HASH(id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS geneset_gene (
    geneset_id     BIGINT NULL,
    entrez_gene_id BIGINT NULL
) ENGINE=OLAP
DUPLICATE KEY (geneset_id)
DISTRIBUTED BY HASH(geneset_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS geneset_hierarchy_node (
    node_id   BIGINT        NULL,
    node_name VARCHAR(1024) NULL,
    parent_id BIGINT        NULL
) ENGINE=OLAP
DUPLICATE KEY (node_id)
DISTRIBUTED BY HASH(node_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS geneset_hierarchy_leaf (
    node_id    BIGINT NULL,
    geneset_id BIGINT NULL
) ENGINE=OLAP
DUPLICATE KEY (node_id)
DISTRIBUTED BY HASH(node_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS gistic (
    gistic_roi_id   BIGINT       NOT NULL,
    cancer_study_id BIGINT       NULL,
    chromosome      BIGINT       NULL,
    cytoband        VARCHAR(64)  NULL,
    wide_peak_start BIGINT       NULL,
    wide_peak_end   BIGINT       NULL,
    q_value         DOUBLE       NULL,
    amp             INT          NULL
) ENGINE=OLAP
PRIMARY KEY (gistic_roi_id)
DISTRIBUTED BY HASH(gistic_roi_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS gistic_to_gene (
    gistic_roi_id  BIGINT NULL,
    entrez_gene_id BIGINT NULL
) ENGINE=OLAP
DUPLICATE KEY (gistic_roi_id)
DISTRIBUTED BY HASH(gistic_roi_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS mut_sig (
    cancer_study_id BIGINT NULL,
    entrez_gene_id  BIGINT NULL,
    `rank`          BIGINT NULL,
    NumBasesCovered BIGINT NULL,
    NumMutations    BIGINT NULL,
    p_value         DOUBLE NULL,
    q_value         DOUBLE NULL
) ENGINE=OLAP
DUPLICATE KEY (cancer_study_id, entrez_gene_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS resource_definition (
    resource_id     VARCHAR(255)  NULL,
    cancer_study_id BIGINT        NULL,
    display_name    VARCHAR(1024) NULL,
    description     STRING        NULL,
    resource_type   VARCHAR(64)   NULL,
    open_by_default INT           NULL,
    priority        BIGINT        NULL,
    custom_metadata STRING        NULL
) ENGINE=OLAP
DUPLICATE KEY (resource_id, cancer_study_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS resource_patient (
    internal_id BIGINT       NULL,
    resource_id VARCHAR(255) NULL,
    url         VARCHAR(1024) NULL
) ENGINE=OLAP
DUPLICATE KEY (internal_id, resource_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS resource_sample (
    internal_id BIGINT       NULL,
    resource_id VARCHAR(255) NULL,
    url         VARCHAR(1024) NULL
) ENGINE=OLAP
DUPLICATE KEY (internal_id, resource_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS resource_study (
    internal_id BIGINT       NULL,
    resource_id VARCHAR(255) NULL,
    url         VARCHAR(1024) NULL
) ENGINE=OLAP
DUPLICATE KEY (internal_id, resource_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS info (
    db_schema_version          VARCHAR(64) NULL,
    geneset_version            VARCHAR(64) NULL,
    derived_table_schema_version VARCHAR(64) NULL,
    gene_table_version         VARCHAR(64) NULL
) ENGINE=OLAP
DUPLICATE KEY (db_schema_version)
DISTRIBUTED BY HASH(db_schema_version) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS authorities (
    email     VARCHAR(255) NULL,
    authority VARCHAR(255) NULL
) ENGINE=OLAP
DUPLICATE KEY (email)
DISTRIBUTED BY HASH(email) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS data_access_tokens (
    token      VARCHAR(1024) NULL,
    username   VARCHAR(255)  NULL,
    expiration DATETIME      NULL,
    creation   DATETIME      NULL
) ENGINE=OLAP
DUPLICATE KEY (token)
DISTRIBUTED BY HASH(token) BUCKETS 4
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- CLINICAL DATA  (large EAV tables — DUPLICATE KEY)
-- ============================================================

CREATE TABLE IF NOT EXISTS clinical_patient (
    internal_id BIGINT       NULL,
    attr_id     VARCHAR(255) NULL,
    attr_value  STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (internal_id, attr_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 16
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS clinical_sample (
    internal_id BIGINT       NULL,
    attr_id     VARCHAR(255) NULL,
    attr_value  STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (internal_id, attr_id)
DISTRIBUTED BY HASH(internal_id) BUCKETS 16
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS clinical_event (
    clinical_event_id BIGINT       NOT NULL,
    patient_id        BIGINT       NULL,
    start_date        BIGINT       NULL,
    stop_date         BIGINT       NULL,
    event_type        VARCHAR(255) NULL
) ENGINE=OLAP
PRIMARY KEY (clinical_event_id)
DISTRIBUTED BY HASH(clinical_event_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS clinical_event_data (
    clinical_event_id BIGINT       NULL,
    `key`             VARCHAR(255) NULL,
    value             STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (clinical_event_id)
DISTRIBUTED BY HASH(clinical_event_id) BUCKETS 16
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- CNA / COPY NUMBER
-- ============================================================

CREATE TABLE IF NOT EXISTS cna_event (
    cna_event_id   BIGINT   NOT NULL,
    entrez_gene_id BIGINT   NULL,
    alteration     INT      NULL
) ENGINE=OLAP
PRIMARY KEY (cna_event_id)
DISTRIBUTED BY HASH(cna_event_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS sample_cna_event (
    cna_event_id       BIGINT  NOT NULL,
    sample_id          BIGINT  NOT NULL,
    genetic_profile_id BIGINT  NOT NULL,
    annotation_json    STRING  NULL
) ENGINE=OLAP
DUPLICATE KEY (cna_event_id, sample_id, genetic_profile_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 32
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS copy_number_seg (
    seg_id          BIGINT  NULL,
    cancer_study_id BIGINT  NULL,
    sample_id       BIGINT  NULL,
    chr             VARCHAR(16) NULL,
    `start`         BIGINT  NULL,
    `end`           BIGINT  NULL,
    num_probes      BIGINT  NULL,
    segment_mean    DOUBLE  NULL
) ENGINE=OLAP
DUPLICATE KEY (seg_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 32
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS copy_number_seg_file (
    seg_file_id         BIGINT       NULL,
    cancer_study_id     BIGINT       NULL,
    reference_genome_id VARCHAR(64)  NULL,
    description         STRING       NULL,
    filename            VARCHAR(1024) NULL
) ENGINE=OLAP
DUPLICATE KEY (seg_file_id)
DISTRIBUTED BY HASH(cancer_study_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- MUTATION
-- ============================================================

CREATE TABLE IF NOT EXISTS mutation_event (
    mutation_event_id   BIGINT       NULL,
    entrez_gene_id      BIGINT       NULL,
    chr                 VARCHAR(16)  NULL,
    start_position      BIGINT       NULL,
    end_position        BIGINT       NULL,
    reference_allele    VARCHAR(1024) NULL,
    tumor_seq_allele    VARCHAR(1024) NULL,
    protein_change      VARCHAR(255) NULL,
    mutation_type       VARCHAR(255) NULL,
    ncbi_build          VARCHAR(16)  NULL,
    strand              VARCHAR(4)   NULL,
    variant_type        VARCHAR(64)  NULL,
    db_snp_rs           VARCHAR(64)  NULL,
    db_snp_val_status   VARCHAR(64)  NULL,
    refseq_mrna_id      VARCHAR(255) NULL,
    codon_change        VARCHAR(1024) NULL,
    uniprot_accession   VARCHAR(64)  NULL,
    protein_pos_start   BIGINT       NULL,
    protein_pos_end     BIGINT       NULL,
    canonical_transcript INT         NULL,
    keyword             VARCHAR(255) NULL
) ENGINE=OLAP
DUPLICATE KEY (mutation_event_id)
DISTRIBUTED BY HASH(mutation_event_id) BUCKETS 32
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS mutation (
    mutation_event_id              BIGINT       NOT NULL,
    genetic_profile_id             BIGINT       NOT NULL,
    sample_id                      BIGINT       NOT NULL,
    entrez_gene_id                 BIGINT       NOT NULL,
    center                         VARCHAR(255) NULL,
    sequencer                      VARCHAR(255) NULL,
    mutation_status                VARCHAR(64)  NULL,
    validation_status              VARCHAR(64)  NULL,
    tumor_seq_allele1              VARCHAR(1024) NULL,
    tumor_seq_allele2              VARCHAR(1024) NULL,
    matched_norm_sample_barcode    VARCHAR(255) NULL,
    match_norm_seq_allele1         VARCHAR(1024) NULL,
    match_norm_seq_allele2         VARCHAR(1024) NULL,
    tumor_validation_allele1       VARCHAR(1024) NULL,
    tumor_validation_allele2       VARCHAR(1024) NULL,
    match_norm_validation_allele1  VARCHAR(1024) NULL,
    match_norm_validation_allele2  VARCHAR(1024) NULL,
    verification_status            VARCHAR(64)  NULL,
    sequencing_phase               VARCHAR(64)  NULL,
    sequence_source                VARCHAR(255) NULL,
    validation_method              VARCHAR(255) NULL,
    score                          VARCHAR(64)  NULL,
    bam_file                       VARCHAR(1024) NULL,
    tumor_alt_count                BIGINT       NULL,
    tumor_ref_count                BIGINT       NULL,
    normal_alt_count               BIGINT       NULL,
    normal_ref_count               BIGINT       NULL,
    amino_acid_change              VARCHAR(255) NULL,
    annotation_json                STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (mutation_event_id, genetic_profile_id, sample_id, entrez_gene_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 32
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS mutation_count_by_keyword (
    genetic_profile_id BIGINT       NULL,
    keyword            VARCHAR(255) NULL,
    entrez_gene_id     BIGINT       NULL,
    keyword_count      BIGINT       NULL,
    gene_count         BIGINT       NULL
) ENGINE=OLAP
DUPLICATE KEY (genetic_profile_id, keyword, entrez_gene_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 16
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS allele_specific_copy_number (
    mutation_event_id        BIGINT  NULL,
    genetic_profile_id       BIGINT  NULL,
    sample_id                BIGINT  NULL,
    ascn_integer_copy_number BIGINT  NULL,
    ascn_method              VARCHAR(255) NULL,
    ccf_expected_copies_upper DOUBLE NULL,
    ccf_expected_copies      DOUBLE  NULL,
    clonal                   VARCHAR(64)  NULL,
    minor_copy_number        BIGINT  NULL,
    expected_alt_copies      BIGINT  NULL,
    total_copy_number        BIGINT  NULL
) ENGINE=OLAP
DUPLICATE KEY (mutation_event_id, genetic_profile_id, sample_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

CREATE TABLE IF NOT EXISTS alteration_driver_annotation (
    alteration_event_id      BIGINT       NULL,
    genetic_profile_id       BIGINT       NULL,
    sample_id                BIGINT       NULL,
    driver_filter            VARCHAR(255) NULL,
    driver_filter_annotation STRING       NULL,
    driver_tiers_filter      VARCHAR(255) NULL,
    driver_tiers_filter_annotation STRING NULL
) ENGINE=OLAP
DUPLICATE KEY (alteration_event_id, genetic_profile_id, sample_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- GENERIC ASSAY
-- ============================================================

CREATE TABLE IF NOT EXISTS generic_entity_properties (
    id                BIGINT       NULL,
    genetic_entity_id BIGINT       NULL,
    name              VARCHAR(255) NULL,
    value             STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (id, genetic_entity_id, name)
DISTRIBUTED BY HASH(genetic_entity_id) BUCKETS 16
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- DERIVED VIEWS are defined at the end of this file (after all base tables).
-- Replaced tables: gene_panel_to_gene_derived, clinical_data_derived,
--   clinical_event_data_derived (dropped), clinical_event_derived,
--   generic_assay_meta_derived, genomic_event_derived, sample_derived,
--   sample_to_gene_panel_derived
-- ============================================================

-- mutation_derived: fully denormalized mutation rows (used by current mapper queries)
-- Note: dotted CH column names (GENE.*, alleleSpecificCopyNumber.*) renamed with underscores
CREATE TABLE IF NOT EXISTS mutation_derived (
    molecularProfileId              VARCHAR(255) NOT NULL,
    sampleId                        VARCHAR(255) NOT NULL,
    entrezGeneId                    BIGINT       NOT NULL,
    sampleInternalId                BIGINT       NOT NULL,
    patientId                       VARCHAR(255) NOT NULL,
    studyId                         VARCHAR(255) NOT NULL,
    center                          VARCHAR(255) NULL,
    mutationStatus                  VARCHAR(64)  NULL,
    validationStatus                VARCHAR(64)  NULL,
    tumorAltCount                   BIGINT       NULL,
    tumorRefCount                   BIGINT       NULL,
    normalAltCount                  BIGINT       NULL,
    normalRefCount                  BIGINT       NULL,
    aminoAcidChange                 VARCHAR(255) NULL,
    chr                             VARCHAR(16)  NULL,
    startPosition                   BIGINT       NULL,
    endPosition                     BIGINT       NULL,
    referenceAllele                 VARCHAR(1024) NULL,
    tumorSeqAllele                  VARCHAR(1024) NULL,
    proteinChange                   VARCHAR(255) NULL,
    mutationType                    VARCHAR(255) NULL,
    ncbiBuild                       VARCHAR(16)  NULL,
    variantType                     VARCHAR(64)  NULL,
    refseqMrnaId                    VARCHAR(255) NULL,
    proteinPosStart                 BIGINT       NULL,
    proteinPosEnd                   BIGINT       NULL,
    keyword                         VARCHAR(255) NULL,
    annotationJSON                  STRING       NULL,
    driverFilter                    VARCHAR(255) NULL,
    driverFilterAnnotation          STRING       NULL,
    driverTiersFilter               VARCHAR(255) NULL,
    driverTiersFilterAnnotation     STRING       NULL,
    gene_entrezGeneId               BIGINT       NULL,
    gene_hugoGeneSymbol             VARCHAR(255) NULL,
    gene_type                       VARCHAR(64)  NULL,
    ascn_integerCopyNumber          BIGINT       NULL,
    ascn_method                     VARCHAR(255) NULL,
    ascn_ccfExpectedCopiesUpper     DOUBLE       NULL,
    ascn_ccfExpectedCopies          DOUBLE       NULL,
    ascn_clonal                     VARCHAR(64)  NULL,
    ascn_minorCopyNumber            BIGINT       NULL,
    ascn_expectedAltCopies          BIGINT       NULL,
    ascn_totalCopyNumber            BIGINT       NULL
) ENGINE=OLAP
DUPLICATE KEY (molecularProfileId, sampleId, entrezGeneId)
DISTRIBUTED BY HASH(studyId) BUCKETS 32
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- PACKED-VALUE FACT TABLE  (one row per profile+gene, values = comma-sep sample values)
-- Matches legacy MySQL/ClickHouse schema; used by MolecularDataMapper
-- ============================================================

CREATE TABLE IF NOT EXISTS genetic_alteration (
    genetic_profile_id  BIGINT  NOT NULL,
    genetic_entity_id   BIGINT  NOT NULL,
    `values`            TEXT    NULL
) ENGINE=OLAP
DUPLICATE KEY(genetic_profile_id, genetic_entity_id)
DISTRIBUTED BY HASH(genetic_profile_id) BUCKETS 16
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- LARGE FACT TABLES  (exploded form — DUPLICATE KEY)
-- These replace the compact ClickHouse tables:
--   generic_assay_data_derived  → generic_assay_data
-- ============================================================

-- genetic_alteration_derived: one row per (study, gene, profile_type, sample)
-- Matches CH table name exactly; exploded form is canonical in SR
CREATE TABLE IF NOT EXISTS genetic_alteration_derived (
    cancer_study_identifier VARCHAR(255) NOT NULL,
    hugo_gene_symbol        VARCHAR(255) NOT NULL,
    profile_type            VARCHAR(255) NOT NULL,
    sample_unique_id        VARCHAR(255) NOT NULL,
    alteration_value        STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (cancer_study_identifier, hugo_gene_symbol, profile_type, sample_unique_id)
DISTRIBUTED BY HASH(cancer_study_identifier) BUCKETS 128
PROPERTIES ("replication_num" = "1");

-- generic_assay_data: one row per (profile_type, entity, patient/sample)
-- Source: generic_assay_data_derived (410M rows, 3.4 GB compressed in CH)
-- generic_assay_data_derived is a view over generic_assay_data for mapper compatibility
CREATE TABLE IF NOT EXISTS generic_assay_data (
    profile_type      VARCHAR(255) NOT NULL,
    entity_stable_id  VARCHAR(255) NOT NULL,
    patient_unique_id VARCHAR(255) NOT NULL,
    sample_unique_id  VARCHAR(255) NOT NULL,
    genetic_entity_id VARCHAR(255) NULL,
    value             STRING       NULL,
    generic_assay_type VARCHAR(255) NULL,
    profile_stable_id VARCHAR(255) NULL,
    datatype          VARCHAR(64)  NULL,
    patient_level     TINYINT      NULL
) ENGINE=OLAP
DUPLICATE KEY (profile_type, entity_stable_id, patient_unique_id, sample_unique_id)
DISTRIBUTED BY HASH(profile_type) BUCKETS 64
PROPERTIES ("replication_num" = "1");

-- ============================================================
-- DERIVED VIEWS  (replace pre-computed derived tables with live joins)
-- StarRocks CBO handles these joins efficiently at query time.
-- ============================================================

CREATE VIEW IF NOT EXISTS generic_assay_data_derived AS
    SELECT * FROM generic_assay_data;

CREATE VIEW IF NOT EXISTS generic_assay_profile_entity_derived AS
    SELECT DISTINCT profile_stable_id, entity_stable_id FROM generic_assay_data;

CREATE VIEW IF NOT EXISTS sample_derived AS
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
    LEFT JOIN (
        SELECT sll.sample_id
        FROM sample_list_list sll
        JOIN sample_list sl ON sll.list_id = sl.list_id
        JOIN sample s2      ON sll.sample_id = s2.internal_id
        JOIN patient p2     ON s2.patient_id = p2.internal_id
        JOIN cancer_study cs2 ON p2.cancer_study_id = cs2.cancer_study_id
        WHERE sl.stable_id = CONCAT(cs2.cancer_study_identifier, '_sequenced')
    ) seq ON seq.sample_id = s.internal_id
    LEFT JOIN (
        SELECT DISTINCT sample_id FROM copy_number_seg
    ) cns ON cns.sample_id = s.internal_id;

CREATE VIEW IF NOT EXISTS sample_to_gene_panel_derived AS
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

CREATE VIEW IF NOT EXISTS gene_panel_to_gene_derived AS
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

CREATE VIEW IF NOT EXISTS clinical_data_derived AS
    SELECT s.internal_id AS internal_id,
           CONCAT(cs.cancer_study_identifier, '_', s.stable_id) AS sample_unique_id,
           CONCAT(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
           cam.attr_id AS attribute_name,
           IFNULL(csam.attr_value, '') AS attribute_value,
           cs.cancer_study_identifier AS cancer_study_identifier,
           'sample' AS type
    FROM sample s
    JOIN patient p ON s.patient_id = p.internal_id
    JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
    JOIN clinical_attribute_meta cam ON cam.cancer_study_id = cs.cancer_study_id AND cam.patient_attribute = 0
    LEFT JOIN clinical_sample csam ON csam.internal_id = s.internal_id AND csam.attr_id = cam.attr_id
    UNION ALL
    SELECT p.internal_id AS internal_id,
           '' AS sample_unique_id,
           CONCAT(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
           cam.attr_id AS attribute_name,
           IFNULL(cpat.attr_value, '') AS attribute_value,
           cs.cancer_study_identifier AS cancer_study_identifier,
           'patient' AS type
    FROM patient p
    JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id
    JOIN clinical_attribute_meta cam ON cam.cancer_study_id = cs.cancer_study_id AND cam.patient_attribute = 1
    LEFT JOIN clinical_patient cpat ON cpat.internal_id = p.internal_id AND cpat.attr_id = cam.attr_id;

CREATE VIEW IF NOT EXISTS clinical_event_derived AS
    SELECT CONCAT(cs.cancer_study_identifier, '_', p.stable_id) AS patient_unique_id,
           ced.`key`                                             AS `key`,
           ced.value                                             AS value,
           ce.start_date                                         AS start_date,
           IFNULL(ce.stop_date, 0)                               AS stop_date,
           ce.event_type                                         AS event_type,
           cs.cancer_study_identifier                            AS cancer_study_identifier
    FROM clinical_event ce
    LEFT JOIN clinical_event_data ced ON ced.clinical_event_id = ce.clinical_event_id
    JOIN patient p  ON ce.patient_id = p.internal_id
    JOIN cancer_study cs ON p.cancer_study_id = cs.cancer_study_id;

CREATE VIEW IF NOT EXISTS generic_assay_meta_derived AS
    SELECT ge.stable_id AS entity_stable_id,
           ge.entity_type AS entity_type,
           parse_json(CONCAT('{', GROUP_CONCAT(
               CONCAT('"', gep.name, '":"', REPLACE(gep.value, '"', '\\"'), '"')
           ), '}')) AS properties
    FROM genetic_entity ge
    LEFT JOIN generic_entity_properties gep ON ge.id = gep.genetic_entity_id
    WHERE ge.entity_type = 'GENERIC_ASSAY'
    GROUP BY ge.stable_id, ge.entity_type;

CREATE VIEW IF NOT EXISTS genomic_event_derived AS
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
