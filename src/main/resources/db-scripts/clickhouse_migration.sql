INSERT INTO cbioportal.cancer_study
SELECT
	cs.CANCERS_STUDY_ID AS id,
	cs.CANCER_STUDY IDENTIFIER AS cancerStudyIdentifier,
    cs.TYPE_OF_CANCER_ID AS typeOfCancerId,
    cs.NAME AS name,
    cs.DESCRIPTION AS description,
    CS.PUBLIC AS publicStudy,
    CS.PMID AS pmid,
    CS.CITATION AS citation,
    CS.GROUPS AS groups,
    CS.STATUS AS status,
    CS.IMPORT_DATE AS importDate,
    cs.REFERENCE_GENOME_ID AS referenceGenome,
FROM mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'cancer_study', 'cbio_user', 'cbio_pass') cs;

INSERT INTO cbioportal.cancer_type
SELECT
	ct.TYPE_OF_CANCER_ID as typeOfCancerId,
    ct.NAME as name,
    ct.DEDICATED_COLOR as dedicatedColor,
    ct.SHORT_NAME as shortName,
    ct.PARENT as parent
FROM mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'type_of_cancer', 'cbio_user', 'cbio_pass') ct;

INSERT INTO cbioportal.clinical_patient
SELECT
	cs.CANCER_STUDY_ID as cancerStudyId,
	cs.CANCER_STUDY_IDENTIFIER as cancerStudyIdentifier,
	patient.STABLE_ID as patientIdentifier,
	cam.ATTR_ID  as attrId,
	cam.DISPLAY_NAME as displayName,
	cam.DESCRIPTION as description,
	cam.DATATYPE as dataType,
	cam.PATIENT_ATTRIBUTE as patientAttribute,
	cam.PRIORITY as priority,
	cp.ATTR_VALUE as value
FROM mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'cancer_study', 'cbio_user', 'cbio_pass') cs
INNER JOIN  mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'clinical_attribute_meta', 'cbio_user', 'cbio_pass') cam ON cs.CANCER_STUDY_ID = cam.CANCER_STUDY_ID
INNER JOIN  mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'patient', 'cbio_user', 'cbio_pass') patient ON patient.CANCER_STUDY_ID  = cam.CANCER_STUDY_ID 
INNER JOIN  mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'clinical_patient', 'cbio_user', 'cbio_pass') cp ON cp.INTERNAL_ID = patient.INTERNAL_ID and cp.ATTR_ID = cam.ATTR_ID;

INSERT INTO cbioportal.sample
SELECT 
	sm.INTERNAL_ID as internalId,
	sm.STABLE_ID as stableId,
	sm.SAMPLE_TYPE as sampleType,
	patient.INTERNAL_ID as patientInternalId,
	patient.STABLE_ID as patientStableId,
	patient.CANCER_STUDY_ID as cancerStudyIdentifier
FROM mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'sample', 'cbio_user', 'cbio_pass') sm
INNER JOIN mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'patient', 'cbio_user', 'cbio_pass') patient ON sm.PATIENT_ID = patient.INTERNAL_ID;

INSERT INTO cbioportal.molecular_profile
SELECT
	gp.GENETIC_PROFILE_ID as molecularProfileId,
	gp.STABLE_ID as stableId, 
	gp.CANCER_STUDY_ID as cancerStudyId,
	cs.CANCER_STUDY_IDENTIFIER as cancerStudyIdentifier,
	gp.GENETIC_ALTERATION_TYPE as molecularAlterationType,
	gp.DATATYPE AS datatype,
	gp.NAME AS name,
	gp.DESCRIPTION AS description,
	gp.SHOW_PROFILE_IN_ANALYSIS_TAB AS showProfileInAnalysisTab,
	gp.PIVOT_THRESHOLD AS pivotThreshold,
	gp.SORT_ORDER AS sortOrder,
	gp.GENERIC_ASSAY_TYPE AS genericAssayType,
	gp.PATIENT_LEVEL AS patientLevel
FROM mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'genetic_profile', 'cbio_user', 'cbio_pass') gp
INNER JOIN mysql('devdb.cbioportal.org:3306', 'cgds_public_release_5_0_0', 'cancer_study', 'cbio_user', 'cbio_pass') cs ON gp.CANCER_STUDY_ID = cs.CANCER_STUDY_ID;