package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.GenericAssayDataCount;

@Schema(
    name = "GenericAssayDataCountItem",
    description = "Represents generic assay data counts for a single item")
public record GenericAssayDataCountItemDTO(String stableId, List<GenericAssayDataCount> counts) {}
