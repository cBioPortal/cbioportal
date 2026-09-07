package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Gene", description = "Represents a gene")
public record GeneDTO(Integer entrezGeneId, String hugoGeneSymbol, String type) {}
