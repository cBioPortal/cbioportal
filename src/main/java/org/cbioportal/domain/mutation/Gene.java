package org.cbioportal.domain.mutation;

public record Gene(Integer entrezGeneId, String hugoGeneSymbol, String type) {}
