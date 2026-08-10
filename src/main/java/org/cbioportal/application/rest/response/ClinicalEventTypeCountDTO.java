package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClinicalEventTypeCount", description = "Represents counts by clinical event type")
public record ClinicalEventTypeCountDTO(String eventType, Integer count) {}
