package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TumorVolumeChangeDTO(
    String studyId,
    String patientId,
    @Schema(description = "100 * (measurement volume - baseline volume) / baseline volume")
        double percentageChange) {}
