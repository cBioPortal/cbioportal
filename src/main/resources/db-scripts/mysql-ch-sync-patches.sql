ALTER TABLE clinical_event ADD COLUMN patient_unique_id VARCHAR(400);


UPDATE clinical_event ce
INNER JOIN patient AS p ON ce.patient_id = p.internal_id
INNER JOIN cancer_study AS cs ON p.cancer_study_id = cs.cancer_study_id
SET ce.patient_unique_id = CONCAT(cs.cancer_study_identifier, '_', p.stable_id);