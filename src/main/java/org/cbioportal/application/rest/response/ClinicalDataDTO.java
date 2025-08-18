package org.cbioportal.application.rest.response;

public record ClinicalDataDTO(
    String uniqueSampleKey,
    String uniquePatientKey,
    String sampleId,
    String patientId,
    String studyId,
    ClinicalAttributeDTO clinicalAttribute,
    Boolean patientAttribute,
    String clinicalAttributeId,
    String value) {}
