package org.cbioportal.application.rest.response;

public record GeneDTO(Integer entrezGeneId, String hugoGeneSymbol, String type) {}
