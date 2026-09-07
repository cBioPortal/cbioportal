package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@Schema(
    name = "SampleClinicalDataCollection",
    description = "Represents clinical data grouped by unique sample key")
public record SampleClinicalDataCollectionDTO(
    Map<String, List<ClinicalDataDTO>> byUniqueSampleKey) {}
