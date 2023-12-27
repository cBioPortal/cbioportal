CREATE DATABASE cbioportal,

CREATE TABLE IF NOT EXISTS cancer_study
(
	id UInt32,
	cancerStudyIdentifier String,
    typeOfCancerId String,
    name String,
    description String,
    publicStudy String,
    pmid String,
    citation String,
    groups String,
    status UInt8,
    importDate Date,
    allSampleCount UInt32,
    sequencedSampleCount UInt32,
     cnaSampleCount UInt32,
    mrnaRnaSeqSampleCount UInt32,
    mrnaRnaSeqV2SampleCount UInt32,
    mrnaMicroarraySampleCount UInt32,
    miRnaSampleCount UInt32,
    methylationHm27SampleCount UInt32,
    rppaSampleCount UInt32,
    massSpectrometrySampleCount UInt32,
    completeSampleCount UInt32,
    referenceGenome String,
    readPermission Boolean,
    treatmentCount UInt32
)
ENGINE = MergeTree
PRIMARY KEY(id, cancerStudyIdentifier);

CREATE TABLE IF NOT EXISTS cancer_type
(
	typeOfCancerId String,
    name String,
    dedicatedColor String,
    shortName String,
    parent String
)
ENGINE = MergeTree
PRIMARY KEY(typeOfCancerId);

CREATE TABLE IF NOT EXISTS clinical_patient
(
	cancerStudyId UInt32,
	cancerStudyIdentifier String,
	patientIdentifier String,
	attrId UInt32,
	displayName String,
	description String,
	dataType String,
	patientAttribute Boolean,
	priority String,
	value String
)
ENGINE = MergeTree
PRIMARY KEY(cancerStudyIdentifier, patientIdentifier, attrId);

CREATE TABLE IF NOT EXISTS sample
(
	internalId UInt32,
    stableId String,
    sampleType String,
    patientInternalId UInt32,
    patientStableId String,
    cancerStudyIdentifier String
)
ENGINE = MergeTree
PRIMARY KEY(cancerStudyIdentifier, patientStableId, stableId);

CREATE TABLE IF NOT EXISTS molecular_profile
(
	molecularProfileId UInt32,
    stableId String,
    cancerStudyId UInt32,
    cancerStudyIdentifier String,
    molecularAlterationType String,
    genericAssayType String,
    datatype String,
    name String,
    description String,
    showProfileInAnalysisTab Boolean,
    pivotThreshold Float,
    sortOrder String,
    patientLevel Boolean
)
ENGINE = MergeTree
PRIMARY KEY(cancerStudyId, molecularProfileId);

CREATE TABLE IF NOT EXISTS alteration_driver_annotation
(
	alterationEventId UInt32,
    geneticProfileId UInt32,
    sampleId UInt32,
    driverFilter String,
    driverFilterAnnotation String,
    driverTiersFilter String,
    driverTiersFilterAnnotation String,
    genomicProfile_stable_id String
)
ENGINE = MergeTree
PRIMARY KEY(alterationEventId, geneticProfileId, sampleId);

CREATE TABLE IF NOT EXISTS clinical_event
(
	study_id String,
	patient_id String,
	event_id UInt32,
	event_type String,
	event_key String,
	event_value String,
	event_start UInt32,
	event_stop UInt32
)
ENGINE = MergeTree
PRIMARY KEY(study_id, patient_id, event_type);

CREATE TABLE IF NOT EXISTS clinical_sample
(
	cancerStudyId UInt32,
	cancerStudyIdentifier String,
	patientIdentifier String,
	sampleIdentifier String,
	attrId String,
	displayName String,
	description String,
	dataType String,
	priority String,
	value String
)
ENGINE = MergeTree
PRIMARY KEY(cancerStudyIdentifier, patientIdentifier, sampleIdentifier, attrId);