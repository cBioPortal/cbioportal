package org.cbioportal.application.rest.response;

public record ClinicalAttributeDTO(
    String displayName,
    String description,
    String datatype,
    Boolean patientAttribute,
    String priority,
    String clinicalAttributeId,
    String studyId) {}
