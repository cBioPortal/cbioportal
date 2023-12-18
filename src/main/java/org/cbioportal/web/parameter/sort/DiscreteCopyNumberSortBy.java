package org.cbioportal.web.parameter.sort;

public enum DiscreteCopyNumberSortBy {
    
    entrezGeneId("entrezGeneId"),
    alteration("alteration");

    private String originalValue;

    DiscreteCopyNumberSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
