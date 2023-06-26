create table cna_discrete
(
    sample_unique_id          varchar(255),
    alteration                int,
    hugo_gene_symbol          varchar(255),
    gene_panel_stable_id      varchar(255),
    cancer_study_identifier   varchar(255),
    genetic_profile_stable_id varchar(255),
    PRIMARY KEY (sample_unique_id, alteration, hugo_gene_symbol, cancer_study_identifier)
);

create table genetic_profile
(
    sample_unique_id        varchar(255),
    genetic_alteration_type varchar(255),
    datatype                varchar(255),
    value                   varchar(255),
    cancer_study_identifier varchar(255),
    PRIMARY KEY (sample_unique_id, genetic_alteration_type, datatype, value, cancer_study_identifier)
);

create table genetic_profile_counts
(
    sample_unique_id          varchar(255),
    profile_name              varchar(255),
    genetic_profile_stable_id varchar(255),
    cancer_study_identifier   varchar(255),
    count                     int
);

create table genomic_event
(
    sample_unique_id          varchar(255),
    variant                   varchar(255),
    hugo_gene_symbol          varchar(255),
    gene_panel_stable_id      varchar(255),
    cancer_study_identifier   varchar(255),
    genetic_profile_stable_id varchar(255),
    PRIMARY KEY (sample_unique_id, variant, hugo_gene_symbol, cancer_study_identifier, genetic_profile_stable_id)
);

create table mutation
(
    sample_unique_id          varchar(255),
    variant                   varchar(255),
    hugo_gene_symbol          varchar(255),
    gene_panel_stable_id      varchar(255),
    cancer_study_identifier   varchar(255),
    genetic_profile_stable_id varchar(255),
    PRIMARY KEY (sample_unique_id, variant, hugo_gene_symbol, cancer_study_identifier)
);

create table patient_clinical_attribute_categorical
(
    patient_unique_id       varchar(255),
    attribute_name          varchar(255),
    attribute_value         varchar(255),
    cancer_study_identifier varchar(255),
    PRIMARY KEY (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier)
);

create table patient_clinical_attribute_numeric
(
    patient_unique_id       varchar(255),
    attribute_name          varchar(255),
    attribute_value         float,
    cancer_study_identifier varchar(255),
    PRIMARY KEY (patient_unique_id, attribute_name, attribute_value, cancer_study_identifier)
);

create table sample
(
    sample_unique_id         varchar(255),
    sample_stable_id         varchar(255),
    patient_unique_id        varchar(255),
    patient_stable_id        varchar(255),
    cancer_study_identifier  varchar(255),
    sample_unique_id_base64  varchar(255),
    patient_unique_id_base64 varchar(255),
    PRIMARY KEY (sample_unique_id, patient_unique_id, cancer_study_identifier)
);

create table sample_clinical_attribute_categorical
(
    patient_unique_id       varchar(255),
    sample_unique_id        varchar(255),
    attribute_name          varchar(255),
    attribute_value         varchar(255),
    cancer_study_identifier varchar(255),
    PRIMARY KEY (patient_unique_id, sample_unique_id, attribute_name, attribute_value, cancer_study_identifier)
);

create table sample_clinical_attribute_numeric
(
    patient_unique_id       varchar(255),
    sample_unique_id        varchar(255),
    attribute_name          varchar(255),
    attribute_value         float,
    cancer_study_identifier varchar(255),
    PRIMARY KEY (patient_unique_id, sample_unique_id, attribute_name, attribute_value, cancer_study_identifier)
);

create table sample_list
(
    sample_unique_id        varchar(255),
    sample_list_stable_id   varchar(255),
    name                    varchar(255),
    cancer_study_identifier varchar(255),
    PRIMARY KEY (sample_unique_id, sample_list_stable_id, name, cancer_study_identifier)
);

create table structural_variant
(
    sample_unique_id          varchar(255),
    hugo_symbol_gene1         varchar(255),
    hugo_symbol_gene2         varchar(255),
    gene_panel_stable_id      varchar(255),
    cancer_study_identifier   varchar(255),
    genetic_profile_stable_id varchar(255),
    PRIMARY KEY (sample_unique_id, hugo_symbol_gene1, hugo_symbol_gene2, cancer_study_identifier)
);
