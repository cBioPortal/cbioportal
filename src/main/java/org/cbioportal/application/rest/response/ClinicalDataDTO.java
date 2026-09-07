package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClinicalData", description = "Represents clinical data")
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
