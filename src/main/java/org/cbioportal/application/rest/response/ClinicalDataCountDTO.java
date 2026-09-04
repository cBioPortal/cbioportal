package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClinicalDataCount", description = "Represents a clinical data count")
public record ClinicalDataCountDTO(String value, Integer count) {}
