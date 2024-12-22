DROP TABLE IF EXISTS info;
DROP TABLE IF EXISTS clinical_event_data;
DROP TABLE IF EXISTS clinical_event;
DROP TABLE IF EXISTS cosmic_mutation;
DROP TABLE IF EXISTS copy_number_seg_file;
DROP TABLE IF EXISTS copy_number_seg;
DROP TABLE IF EXISTS sample_cna_event;
DROP TABLE IF EXISTS cna_event;
DROP TABLE IF EXISTS gistic_to_gene;
DROP TABLE IF EXISTS gistic;
DROP TABLE IF EXISTS mut_sig;
DROP TABLE IF EXISTS clinical_attribute_meta;
DROP TABLE IF EXISTS clinical_sample;
DROP TABLE IF EXISTS clinical_patient;
DROP TABLE IF EXISTS resource_definition;
DROP TABLE IF EXISTS resource_sample;
DROP TABLE IF EXISTS resource_patient;
DROP TABLE IF EXISTS resource_study;
DROP TABLE IF EXISTS mutation_count_by_keyword;
DROP TABLE IF EXISTS allele_specific_copy_number;
DROP TABLE IF EXISTS mutation;
DROP TABLE IF EXISTS mutation_event;
DROP TABLE IF EXISTS structural_variant;
DROP TABLE IF EXISTS sample_profile;
DROP TABLE IF EXISTS gene_panel_list;
DROP TABLE IF EXISTS gene_panel;
DROP TABLE IF EXISTS genetic_profile_samples;
DROP TABLE IF EXISTS genetic_alteration;
DROP TABLE IF EXISTS genetic_profile_link;
DROP TABLE IF EXISTS alteration_driver_annotation;
DROP TABLE IF EXISTS genetic_profile;
DROP TABLE IF EXISTS gene_alias;
DROP TABLE IF EXISTS geneset_gene;
DROP TABLE IF EXISTS reference_genome_gene;
DROP TABLE IF EXISTS gene;
DROP TABLE IF EXISTS sample_list_list;
DROP TABLE IF EXISTS sample_list;
DROP TABLE IF EXISTS sample;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS data_access_tokens;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS cancer_study_tags;
DROP TABLE IF EXISTS cancer_study;
DROP TABLE IF EXISTS type_of_cancer;
DROP TABLE IF EXISTS geneset_hierarchy_leaf;
DROP TABLE IF EXISTS geneset_hierarchy_node;
DROP TABLE IF EXISTS geneset;
DROP TABLE IF EXISTS generic_entity_properties;
DROP TABLE IF EXISTS genetic_entity;
DROP TABLE IF EXISTS reference_genome;

-- --------------------------------------------------------
CREATE TABLE generic_entity_properties
(
    id      Int32,
    genetic_entity_id Int32,
    name  String,
    value String
) ENGINE = MergeTree() ORDER BY id;

-- --------------------------------------------------------
CREATE TABLE geneset
(
    id Int32,
    genetic_entity_id  Int32,
    external_id String,
    name       String,
    description Nullable(String),
    ref_link String
) ENGINE = MergeTree() ORDER BY id;

CREATE TABLE geneset_gene
(
    geneset_id Int32,
    entrez_gene_id  Int32
) ENGINE = MergeTree() ORDER BY geneset_id;
-- --------------------------------------------------------
CREATE TABLE geneset_hierarchy_node
(
    node_id    Int32,
    node_name String,
    parent_id Nullable(Int32)
) ENGINE = MergeTree() ORDER BY node_id;

-- --------------------------------------------------------
CREATE TABLE geneset_hierarchy_leaf
(
    node_id Int32,
    geneset_id Nullable(Int32)
) ENGINE = MergeTree() ORDER BY node_id;

-- --------------------------------------------------------
CREATE TABLE type_of_cancer
(
    type_of_cancer_id String,
    name              String,
    dedicated_color   String,
    short_name Nullable(String),
    parent Nullable(String)
) ENGINE = MergeTree() ORDER BY type_of_cancer_id;

-- --------------------------------------------------------
CREATE TABLE cancer_study
(
    cancer_study_id     Int32,
    cancer_study_identifier String,
    type_of_cancer_id   String,
    name                String,
    description         String,
    public              UInt8,
    pmid Nullable(String),
    citation Nullable(String),
    groups Nullable(String),
    status Nullable(Int8),
    import_date Nullable(DateTime),
    reference_genome_id Int32 DEFAULT 1
) ENGINE = MergeTree() ORDER BY cancer_study_id;

-- --------------------------------------------------------
CREATE TABLE cancer_study_tags
(
    cancer_study_id Int32,
    tags            String
) ENGINE = MergeTree() ORDER BY cancer_study_id;

-- --------------------------------------------------------
CREATE TABLE users
(
    email   String,
    name    String,
    enabled UInt8
) ENGINE = MergeTree() ORDER BY email;

-- --------------------------------------------------------
CREATE TABLE authorities
(
    email     String,
    authority String
) ENGINE = MergeTree() ORDER BY email;

-- --------------------------------------------------------
CREATE TABLE patient
(
    internal_id     Int32,
    stable_id       String,
    cancer_study_id Int32
) ENGINE = MergeTree() ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE sample
(
    internal_id Int32,
    stable_id   String,
    sample_type String,
    patient_id  Int32
) ENGINE = MergeTree() ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE sample_list
(
    list_id         Int32,
    stable_id       String,
    category        String,
    cancer_study_id Int32,
    name            String,
    description Nullable(String)
) ENGINE = MergeTree() ORDER BY list_id;

-- --------------------------------------------------------
CREATE TABLE sample_list_list
(
    list_id   Int32,
    sample_id Int32
) ENGINE = MergeTree() ORDER BY list_id;

-- --------------------------------------------------------
CREATE TABLE genetic_entity
(
    id          Int32,
    entity_type String,
    stable_id Nullable(String)
) ENGINE = MergeTree() ORDER BY id;

-- --------------------------------------------------------
CREATE TABLE alteration_driver_annotation (
                                              alteration_event_id Int32,
                                              genetic_profile_id Int32,
                                              sample_id Int32,
                                              driver_filter String,
                                              driver_filter_annotation String,
                                              driver_tiers_filter String,
                                              driver_tiers_filter_annotation String
) ENGINE = MergeTree() ORDER BY (alteration_event_id, genetic_profile_id, sample_id);

-- --------------------------------------------------------
CREATE TABLE genetic_profile
(
    genetic_profile_id            Int32,
    stable_id                     String,
    genetic_alteration_type       String,
    genomic_build                 String,
    genetic_alteration_identifier String,
    name                          String,
    datatype                      String,
    description                   String,
    show_profile_in_analysis_tab  Int32,
    generic_assay_type            Nullable(String),
    cancer_study_id               Int32,
    patient_level                 Nullable(Int32),
) ENGINE = MergeTree() ORDER BY genetic_profile_id;

-- --------------------------------------------------------
CREATE TABLE genetic_profile_link
(
    genetic_profile_id Int32,
    genetic_entity_id  Int32
) ENGINE = MergeTree() ORDER BY genetic_profile_id;

-- --------------------------------------------------------
CREATE TABLE genetic_alteration
(
    genetic_profile_id Int32,
    genetic_entity_id     Int32,
    values                 String
) ENGINE = MergeTree() ORDER BY genetic_profile_id;

-- --------------------------------------------------------
CREATE TABLE genetic_profile_samples
(
    genetic_profile_id Int32,
    ordered_sample_list String
) ENGINE = MergeTree() ORDER BY genetic_profile_id;

-- --------------------------------------------------------
CREATE TABLE gene_panel
(
    internal_id Int32,
    stable_id     Nullable(String),
    name          Nullable(String),
    description Nullable(String)
) ENGINE = MergeTree() ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE gene_panel_list
(
    internal_id Int32,
    gene_id Int32
) ENGINE = MergeTree() ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE sample_profile
(
    panel_id  Nullable(Int32),
    sample_id          Int32,
    genetic_profile_id Int32
) ENGINE = MergeTree() ORDER BY sample_id;

-- --------------------------------------------------------
CREATE TABLE structural_variant
(
    internal_id        Int32,
    genetic_profile_id Int32,
    sample_id          Int32,
    site1_entrez_gene_id Nullable(Int32),
    site1_ensembl_transcript_id Nullable(String),
    site1_chromosome Nullable(String),
    site1_region Nullable(String),
    site1_region_number Nullable(Int32),
    site1_contig Nullable(String),
    site1_position Nullable(Int32),
    site1_description Nullable(String),
    site2_entrez_gene_id Nullable(Int32),
    site2_ensembl_transcript_id Nullable(String),
    site2_chromosome Nullable(String),
    site2_region Nullable(String),
    site2_region_number Nullable(Int32),
    site2_contig Nullable(String),
    site2_position Nullable(Int32),
    site2_description Nullable(String),
    site2_effect_on_frame Nullable(String),
    ncbi_build Nullable(String),
    dna_support Nullable(String),
    rna_support Nullable(String),
    normal_read_count Nullable(Int32),
    tumor_read_count Nullable(Int32),
    normal_variant_count Nullable(Int32),
    tumor_variant_count Nullable(Int32),
    normal_paired_end_read_count Nullable(Int32),
    tumor_paired_end_read_count Nullable(Int32),
    normal_split_read_count Nullable(Int32),
    tumor_split_read_count Nullable(Int32),
    annotation Nullable(String),
    breakpoint_type Nullable(String),
    connection_type Nullable(String),
    event_info Nullable(String),
    class Nullable(String),
    length Nullable(Int32),
    comments Nullable(String),
    sv_status Enum8('GERMLINE' = 0, 'SOMATIC' = 1) DEFAULT 'SOMATIC',
    annotation_json Nullable(String)
) ENGINE = MergeTree()
      ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE mutation_event
(
    mutation_event_id Int32,
    entrez_gene_id    Int32,
    chr Nullable(String),
    start_position Nullable(Int64),
    end_position Nullable(Int64),
    reference_allele Nullable(String),
    tumor_seq_allele Nullable(String),
    protein_change    String,
    mutation_type     String,
    ncbi_build Nullable(String),
    strand Nullable(String),
    variant_type Nullable(String),
    db_snp_rs Nullable(String),
    db_snp_val_status Nullable(String),
    refseq_mrna_id Nullable(String),
    codon_change Nullable(String),
    uniprot_accession Nullable(String),
    protein_pos_start Nullable(Int32),
    protein_pos_end Nullable(Int32),
    canonical_transcript Nullable(UInt8),
    functional_impact_score String,
    fis_value String,
    link_xvar String,
    link_pdb String,
    link_msa String,
    keyword Nullable(String) DEFAULT NULL COMMENT 'e.g. truncating, V200 Missense, E338del'
) ENGINE = MergeTree() ORDER BY mutation_event_id;

-- --------------------------------------------------------
CREATE TABLE mutation (
                          mutation_event_id Int32,
                          genetic_profile_id Int32,
                          sample_id Int32,
                          entrez_gene_id Int32,
                          center String,
                          sequencer String,
                          mutation_status String COMMENT 'Germline, Somatic or LOH.',
                          validation_status String,
                          tumor_seq_allele1 String,
                          tumor_seq_allele2 String,
                          matched_norm_sample_barcode String,
                          match_norm_seq_allele1 String,
                          match_norm_seq_allele2 String,
                          tumor_validation_allele1 String,
                          tumor_validation_allele2 String,
                          match_norm_validation_allele1 String,
                          match_norm_validation_allele2 String,
                          verification_status String,
                          sequencing_phase String,
                          sequence_source String,
                          validation_method String,
                          score String,
                          bam_file String,
                          tumor_alt_count Int32,
                          tumor_ref_count Int32,
                          normal_alt_count Int32,
                          normal_ref_count Int32,
                          amino_acid_change String,
                          annotation_json String
) ENGINE = MergeTree() ORDER BY (mutation_event_id, genetic_profile_id, sample_id);

-- --------------------------------------------------------
CREATE TABLE allele_specific_copy_number (
                                             mutation_event_id Int32,
                                             genetic_profile_id Int32,
                                             sample_id Int32,
                                             ascn_integer_copy_number Nullable(Int32),
                                             ascn_method String,
                                             ccf_expected_copies_upper Nullable(Float32),
                                             ccf_expected_copies Nullable(Float32),
                                             clonal Nullable(String),
                                             minor_copy_number Nullable(Int32),
                                             expected_alt_copies Nullable(Int32),
                                             total_copy_number Nullable(Int32)
) ENGINE = MergeTree()
      ORDER BY (mutation_event_id, genetic_profile_id, sample_id);

-- --------------------------------------------------------
CREATE TABLE mutation_count_by_keyword
(
    genetic_profile_id Int32,
    entrez_gene_id Int32,
    mutation_id                  Int32,
    keyword                      String,
    keyword_count Int32,
    gene_count Int32
) ENGINE = MergeTree() ORDER BY genetic_profile_id;

-- --------------------------------------------------------
CREATE TABLE resource_study
(
    resource_id Int32,
    study_id    Int32
) ENGINE = MergeTree() ORDER BY resource_id;

-- --------------------------------------------------------
CREATE TABLE resource_patient
(
    resource_id Int32,
    patient_id  Int32
) ENGINE = MergeTree() ORDER BY resource_id;

-- --------------------------------------------------------
CREATE TABLE resource_sample
(
    resource_id Int32,
    sample_id   Int32
) ENGINE = MergeTree() ORDER BY resource_id;

-- --------------------------------------------------------
CREATE TABLE resource_definition
(
    resource_id Int32,
    definition  String
) ENGINE = MergeTree() ORDER BY resource_id;

-- --------------------------------------------------------
CREATE TABLE clinical_patient
(
    internal_id Int32,
    attr_id Nullable(String),
    attr_value Nullable(String)
) ENGINE = MergeTree() ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE clinical_sample
(
    internal_id Int32,
    attr_id Nullable(String),
    attr_value Nullable(String)
) ENGINE = MergeTree() ORDER BY internal_id;

-- --------------------------------------------------------
CREATE TABLE clinical_attribute_meta
(
    attr_id String,
    display_name Nullable(String),
    description Nullable(String),
    datatype Nullable(String),
    patient_attribute Nullable(Int32),
    priority Nullable(String),
    cancer_study_id Nullable(Int32)
) ENGINE = MergeTree() ORDER BY attr_id;

-- --------------------------------------------------------
CREATE TABLE mut_sig
(
    cancer_study_id Int32,
    entrez_gene_id  Int32,
    rank            Int32,
    numbasescovered Int32,
    nummutations    Int32,
    p_value         Float32,
    q_value         Float32
) ENGINE = MergeTree()
      ORDER BY (cancer_study_id, entrez_gene_id);

-- --------------------------------------------------------
CREATE TABLE gistic
(
    gistic_roi_id Int32,
    cancer_study_id Nullable(Int32),
    chromosome Int32,
    cytoband Nullable(Float32),
    wide_peak_start Nullable(Float32),
    wide_peak_end Nullable(Float32),
    q_value Float32,
    amp tinyint
) ENGINE = MergeTree() ORDER BY gistic_roi_id;

-- --------------------------------------------------------
CREATE TABLE gistic_to_gene
(
    gistic_roi_id Int32,
    entrez_gene_id Nullable(Int32)
) ENGINE = MergeTree() ORDER BY gistic_roi_id;

-- --------------------------------------------------------
CREATE TABLE cna_event
(
    cna_event_id       Int32,
    entrez_gene_id Nullable(Int32),
    alteration Int32
    
) ENGINE = MergeTree() ORDER BY cna_event_id;

-- --------------------------------------------------------
CREATE TABLE sample_cna_event
(
    cna_event_id Int32,
    sample_id        Int32,
    genetic_profile_id           Int32,
    annotation_json    String
) ENGINE = MergeTree() ORDER BY cna_event_id;

-- --------------------------------------------------------
CREATE TABLE copy_number_seg
(
    seg_id             Int32,
    sample_id          Int32,
    genetic_profile_id Int32,
    cancer_study_id Int32,
    chr String,
    segment_mean Nullable(Float32),
    num_probes Int32,
    start Int64,
    end Int64
) ENGINE = MergeTree() ORDER BY seg_id;

-- --------------------------------------------------------
CREATE TABLE copy_number_seg_file
(
    seg_file_id            Int32,
    copy_number_profile_id Int32,
    file_path              String,
    file_md5sum            String,
    imported_datetime Nullable(DateTime)
) ENGINE = MergeTree() ORDER BY seg_file_id;

-- --------------------------------------------------------
CREATE TABLE cosmic_mutation
(
    cosmic_mutation_id String,
    chr Nullable(String),
    start_position Nullable(Int64),
    reference_allele Nullable(String),
    tumor_seq_allele Nullable(String),
    strand Nullable(String),
    codon_change Nullable(String),
    entrez_gene_id     Int32,
    protein_change     String,
    count              Int32,
    keyword Nullable(String)
) ENGINE = MergeTree() ORDER BY cosmic_mutation_id;

-- --------------------------------------------------------
CREATE TABLE clinical_event
(
    clinical_event_id Int32,
    patient_id Int,
    event_type        String,
    start_date Int32 default 0,
    stop_date Int32 default 0
) ENGINE = MergeTree() ORDER BY clinical_event_id;

-- --------------------------------------------------------
CREATE TABLE clinical_event_data
(
    clinical_event_id Int32,
    key      String,
    value        String
) ENGINE = MergeTree() ORDER BY clinical_event_id;

-- --------------------------------------------------------
CREATE TABLE info
(
    table_id    String,
    description String
) ENGINE = MergeTree() ORDER BY table_id;

-- --------------------------------------------------------
CREATE TABLE reference_genome
(
    reference_genome_id Int32,
    species             String,
    name                String,
    build_name          String,
    genome_size Nullable(Int64),
    url                 String,
    release_date Nullable(DateTime)
) ENGINE = MergeTree() ORDER BY reference_genome_id;

CREATE TABLE gene
(
    entrez_gene_id    Int32,
    hugo_gene_symbol  String,
    genetic_entity_id Int32,
    type Nullable(String)
) ENGINE = MergeTree() ORDER BY entrez_gene_id;

CREATE TABLE gene_alias
(
    entrez_gene_id Int32,
    gene_alias     String
) ENGINE MergeTree() ORDER BY entrez_gene_id;

CREATE TABLE reference_genome_gene
(
    entrez_gene_id      Int32,
    reference_genome_id Int32,
    chr                 varchar(5),
    cytoband            String,
    start               BIGINT,
    end                 BIGINT
) ENGINE MergeTree() ORDER BY entrez_gene_id;