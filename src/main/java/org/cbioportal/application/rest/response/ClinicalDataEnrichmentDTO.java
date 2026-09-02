package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ClinicalDataEnrichment", description = "Represents clinical data enrichment")
public record ClinicalDataEnrichmentDTO(
    ClinicalAttributeDTO clinicalAttribute, BigDecimal score, String method, BigDecimal pValue) {}
