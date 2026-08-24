package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import org.cbioportal.legacy.model.CountSummary;

@Schema(name = "AlterationEnrichment", description = "Represents an alteration enrichment result")
public record AlterationEnrichmentDTO(
    Integer entrezGeneId,
    String hugoGeneSymbol,
    String cytoband,
    BigDecimal pValue,
    List<CountSummary> counts) {}
