package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.ClinicalDataCount;

@Schema(
    name = "ClinicalDataCountItem",
    description = "Represents clinical data counts for a single item")
public record ClinicalDataCountItemDTO(String attributeId, List<ClinicalDataCount> counts) {}
