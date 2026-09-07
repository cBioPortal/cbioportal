package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GenesetCorrelation", description = "Represents geneset correlation")
public record GenesetCorrelationDTO(
    Integer entrezGeneId,
    String hugoGeneSymbol,
    Double correlationValue,
    String expressionGeneticProfileId,
    String zScoreGeneticProfileId) {}
