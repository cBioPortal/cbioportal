package org.cbioportal.web.parameter.sort;

public enum GeneSortBy {

    entrezGeneId("entrezGeneId"),
    hugoGeneSymbol("hugoGeneSymbol"),
    type("type"),
    cytoband("cytoband"),
    length("length");

    private String originalValue;

    GeneSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
