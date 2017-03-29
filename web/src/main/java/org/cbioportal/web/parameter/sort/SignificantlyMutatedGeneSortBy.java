package org.cbioportal.web.parameter.sort;

public enum SignificantlyMutatedGeneSortBy {
    
    entrezGeneId("entrezGeneId"),
    hugoGeneSymbol("hugoGeneSymbol"),
    rank("rank"),
    numberOfMutations("nummutations"),
    pValue("pValue"),
    qValue("qValue");

    private String originalValue;

    SignificantlyMutatedGeneSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
