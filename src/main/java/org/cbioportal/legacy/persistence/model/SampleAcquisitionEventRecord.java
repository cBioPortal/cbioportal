package org.cbioportal.legacy.persistence.model;

public record SampleAcquisitionEventRecord(
    String sampleId, String patientUniqueId, String cancerStudyId, int timeTaken) {}
