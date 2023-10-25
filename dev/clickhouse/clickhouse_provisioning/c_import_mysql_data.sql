insert into cbioportal.sample_in_genetic_profile
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_sample_in_genetic_profile',
    'cbio',
    'P@ssword1'
);

insert into cbioportal.sample_list
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_sample_list',
    'cbio',
    'P@ssword1'
    );

insert into cbioportal.structural_variant
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_structural_variant',
    'cbio',
    'P@ssword1'
    );

insert into cbioportal.sample
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_sample',
    'cbio',
    'P@ssword1'
    );


insert into cbioportal.sample_clinical_attribute_numeric
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_sample_clinical_attribute_numeric',
    'cbio',
    'P@ssword1'
    );


insert into cbioportal.sample_clinical_attribute_categorical
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_sample_clinical_attribute_categorical',
    'cbio',
    'P@ssword1'
    );


insert into cbioportal.patient_clinical_attribute_numeric
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_patient_clinical_attribute_numeric',
    'cbio',
    'P@ssword1'
    );



insert into cbioportal.patient_clinical_attribute_categorical
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_patient_clinical_attribute_categorical',
    'cbio',
    'P@ssword1'
    );


insert into cbioportal.genomic_event
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_genomic_event_mutation',
    'cbio',
    'P@ssword1'
    );


insert into cbioportal.genomic_event
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_genomic_event_cna',
    'cbio',
    'P@ssword1'
    );

insert into cbioportal.genomic_event
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_genomic_event_sv_gene1',
    'cbio',
    'P@ssword1'
    );


insert into cbioportal.genomic_event
select * from mysql(
    '127.0.0.1:3306',
    'cbioportal',
    'view_genomic_event_sv_gene2',
    'cbio',
    'P@ssword1'
    );