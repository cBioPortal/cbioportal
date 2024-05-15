DROP VIEW IF EXISTS sample_clinical_attribute_numeric_view;
DROP VIEW IF EXISTS sample_clinical_attribute_categorical_view;
DROP VIEW IF EXISTS patient_clinical_attribute_numeric_view;
DROP VIEW IF EXISTS patient_clinical_attribute_categorical_view;
DROP VIEW IF EXISTS sample_view;
DROP VIEW IF EXISTS sample_list_view;

CREATE VIEW sample_clinical_attribute_numeric_view
    AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       clinical_sample.attr_id                              as attribute_name,
       cast(clinical_sample.attr_value as float)            as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN sample s on p.internal_id = s.patient_id
         INNER JOIN clinical_sample ON s.internal_id = clinical_sample.internal_id
WHERE match(clinical_sample.attr_value, '^[\d\.]+$');

CREATE VIEW sample_clinical_attribute_categorical_view
    AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cl.attr_id                                           as attribute_name,
       cl.attr_value                                        as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN sample s on p.internal_id = s.patient_id
         INNER JOIN clinical_sample cl on s.internal_id = cl.internal_id
WHERE NOT match(cl.attr_value, '^[\d\.]+$');

CREATE VIEW patient_clinical_attribute_numeric_view
    AS
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cp.attr_id                                           as attribute_name,
       cast(cp.attr_value as float)                         as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN clinical_patient cp on p.internal_id = cp.internal_id
WHERE match(cp.attr_value, '^[\d\.]+$');

CREATE VIEW patient_clinical_attribute_categorical_view
    AS
SELECT concat(cs.cancer_study_identifier, '_', p.stable_id) as patient_unique_id,
       cp.attr_id                                           as attribute_name,
       cp.attr_value                                        as attribute_value,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM cancer_study cs
         INNER JOIN patient p on cs.cancer_study_id = p.cancer_study_id
         INNER JOIN clinical_patient cp on p.internal_id = cp.internal_id
WHERE NOT match(cp.attr_value, '^[\d\.]+$');

CREATE VIEW sample_view
    AS
SELECT concat(cs.cancer_study_identifier, '_', sample.stable_id) as sample_unique_id,
       base64Encode(sample.stable_id)                            as sample_unique_id_base64,
       sample.stable_id                                          as sample_stable_id,
       concat(cs.cancer_study_identifier, '_', p.stable_id)      as patient_unique_id,
       p.stable_id                                               as patient_stable_id,
       base64Encode(p.stable_id)                                 as patient_unique_id_base64,
       cs.cancer_study_identifier                                as cancer_study_identifier
FROM sample
         INNER JOIN patient p on sample.patient_id = p.internal_id
         INNER JOIN cancer_study cs on p.cancer_study_id = cs.cancer_study_id;

CREATE VIEW sample_list_view
    AS
SELECT concat(cs.cancer_study_identifier, '_', s.stable_id) as sample_unique_id,
       sl.stable_id                                         as sample_list_stable_id,
       sl.name                                              as name,
       cs.cancer_study_identifier                           as cancer_study_identifier
FROM sample_list as sl
         INNER JOIN sample_list_list AS sll on sll.list_id = sl.list_id
         INNER JOIN sample AS s on s.internal_id = sll.sample_id
         INNER JOIN cancer_study cs on sl.cancer_study_id = cs.cancer_study_id;