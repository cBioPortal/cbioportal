package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClinicalAttributeCount", description = "Represents clinical attribute count")
public record ClinicalAttributeCountDTO(String clinicalAttributeId, Integer count) {}
