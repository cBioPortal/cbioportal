package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClinicalAttribute", description = "Represents a clinical attribute")
public record ClinicalAttributeDTO(
    String displayName,
    String description,
    String datatype,
    Boolean patientAttribute,
    String priority,
    String clinicalAttributeId,
    String studyId) {}
