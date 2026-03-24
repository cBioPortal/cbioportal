-- StarRocks schema for cBioPortal
-- Migrated from cgds_public_staging (ClickHouse)
--
-- Conventions:
--   • Dimension tables use PRIMARY KEY engine (upsert support for patient/sample edits)
--   • Fact tables use DUPLICATE KEY engine (max ingest throughput, no dedup needed)
--   • genetic_alteration_derived → genetic_alteration  (exploded form is canonical; packed CH table skipped)
--   • generic_assay_data_derived → generic_assay_data
--   • All other _derived tables keep their names
--   • replication_num = 1 (single-node Docker)
--   • Bucket counts sized for local evaluation (can be raised for production)

CREATE DATABASE IF NOT EXISTS cbioportal;
USE cbioportal;

-- ============================================================
-- DIMENSION TABLES  (PRIMARY KEY — supports row-level upserts)
-- ============================================================

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
    ordered_sample_list STRING NULL
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
-- DERIVED TABLES  (kept as-is for query compatibility)
-- ============================================================

-- gene_panel_to_gene_derived: pre-joined panel→gene mapping
CREATE TABLE IF NOT EXISTS gene_panel_to_gene_derived (
    gene_panel_id  VARCHAR(255) NULL,
    entrez_gene_id BIGINT       NULL
) ENGINE=OLAP
DUPLICATE KEY (gene_panel_id)
DISTRIBUTED BY HASH(gene_panel_id) BUCKETS 4
PROPERTIES ("replication_num" = "1");

-- clinical_data_derived: denormalized clinical data (joined with study/sample/patient)
CREATE TABLE IF NOT EXISTS clinical_data_derived (
    cancer_study_identifier VARCHAR(255) NOT NULL,
    type                 VARCHAR(64)  NOT NULL,
    attribute_name       VARCHAR(255) NOT NULL,
    sample_unique_id     VARCHAR(255) NOT NULL,
    internal_id          INT          NULL,
    patient_unique_id    VARCHAR(255) NULL,
    attribute_value      STRING       NULL
) ENGINE=OLAP
DUPLICATE KEY (cancer_study_identifier, type, attribute_name, sample_unique_id)
DISTRIBUTED BY HASH(cancer_study_identifier) BUCKETS 16
PROPERTIES ("replication_num" = "1");

-- clinical_event_data_derived: denormalized timeline events
CREATE TABLE IF NOT EXISTS clinical_event_data_derived (
    cancer_study_identifier VARCHAR(255) NOT NULL,
    event_type              VARCHAR(255) NOT NULL,
    patient_unique_id       VARCHAR(255) NOT NULL,
    `key`                   VARCHAR(255) NOT NULL,
    value                   STRING       NULL,
    start_date              INT          NOT NULL DEFAULT "0",
    stop_date               INT          NOT NULL DEFAULT "0"
) ENGINE=OLAP
DUPLICATE KEY (cancer_study_identifier, event_type, patient_unique_id)
DISTRIBUTED BY HASH(cancer_study_identifier) BUCKETS 16
PROPERTIES ("replication_num" = "1");

-- clinical_event_derived: clinical events enriched with study + patient stable IDs
CREATE TABLE IF NOT EXISTS clinical_event_derived (
    cancer_study_identifier VARCHAR(255) NOT NULL,
    event_type              VARCHAR(255) NOT NULL,
    clinical_event_id       BIGINT       NOT NULL,
    patient_id              BIGINT       NULL,
    patient_stable_id       VARCHAR(255) NULL,
    start_date              BIGINT       NULL,
    stop_date               BIGINT       NULL
) ENGINE=OLAP
DUPLICATE KEY (cancer_study_identifier, event_type, clinical_event_id)
DISTRIBUTED BY HASH(cancer_study_identifier) BUCKETS 8
PROPERTIES ("replication_num" = "1");

-- generic_assay_meta_derived: entity metadata as JSON (ClickHouse Map exported via toJSONString)
CREATE TABLE IF NOT EXISTS generic_assay_meta_derived (
    entity_stable_id VARCHAR(255)  NOT NULL,
    entity_type      VARCHAR(255)  NOT NULL,
    properties       JSON          NULL
) ENGINE=OLAP
DUPLICATE KEY (entity_stable_id)
DISTRIBUTED BY HASH(entity_stable_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

-- generic_assay_profile_entity_derived: profile→entity membership
CREATE TABLE IF NOT EXISTS generic_assay_profile_entity_derived (
    profile_stable_id VARCHAR(255) NOT NULL,
    entity_stable_id  VARCHAR(255) NOT NULL
) ENGINE=OLAP
DUPLICATE KEY (profile_stable_id, entity_stable_id)
DISTRIBUTED BY HASH(profile_stable_id) BUCKETS 8
PROPERTIES ("replication_num" = "1");

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
-- LARGE FACT TABLES  (exploded form — DUPLICATE KEY)
-- These replace the compact ClickHouse tables:
--   genetic_alteration_derived  → genetic_alteration
--   generic_assay_data_derived  → generic_assay_data
-- ============================================================

-- genetic_alteration: one row per (study, gene, profile_type, sample)
-- Source: genetic_alteration_derived (10.3B rows, 28.5 GB compressed in CH)
CREATE TABLE IF NOT EXISTS genetic_alteration (
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

-- genomic_event: one row per genomic event per sample
-- Source: genomic_event_derived (50M rows, 369 MB compressed in CH)
CREATE TABLE IF NOT EXISTS genomic_event_derived (
    genetic_profile_stable_id       VARCHAR(255) NOT NULL,
    cancer_study_identifier         VARCHAR(255) NOT NULL,
    variant_type                    VARCHAR(64)  NOT NULL,
    entrez_gene_id                  INT          NOT NULL,
    hugo_gene_symbol                VARCHAR(255) NOT NULL,
    sample_unique_id                VARCHAR(255) NOT NULL,
    gene_panel_stable_id            VARCHAR(255) NULL,
    mutation_variant                VARCHAR(1024) NULL,
    mutation_type                   VARCHAR(255) NULL,
    mutation_status                 VARCHAR(64)  NULL,
    driver_filter                   VARCHAR(255) NULL,
    driver_filter_annotation        STRING       NULL,
    driver_tiers_filter             VARCHAR(255) NULL,
    driver_tiers_filter_annotation  STRING       NULL,
    cna_alteration                  TINYINT      NULL,
    cna_cytoband                    VARCHAR(64)  NULL,
    sv_event_info                   STRING       NULL,
    patient_unique_id               VARCHAR(255) NULL,
    off_panel                       BOOLEAN      NOT NULL DEFAULT "0"
) ENGINE=OLAP
DUPLICATE KEY (genetic_profile_stable_id, cancer_study_identifier, variant_type, entrez_gene_id, hugo_gene_symbol, sample_unique_id)
DISTRIBUTED BY HASH(cancer_study_identifier) BUCKETS 64
PROPERTIES ("replication_num" = "1");
