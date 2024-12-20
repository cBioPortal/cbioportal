package org.cbioportal.persistence.model;

public record SampleAcquisitionEventRecord(String sampleId, String patientUniqueId, String cancerStudyId, int timeTaken) {
}
