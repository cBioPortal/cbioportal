package org.cbioportal.web.parameter.sort;

public enum MutationSortBy {
    
    entrezGeneId("entrezGeneId"),
    center("center"),
    mutationStatus("mutationStatus"),
    validationStatus("validationStatus"),
    tumorAltCount("tumorAltCount"),
    tumorRefCount("tumorRefCount"),
    normalAltCount("normalAltCount"),
    normalRefCount("normalRefCount"),
    aminoAcidChange("aminoAcidChange"),
    startPosition("startPosition"),
    endPosition("endPosition"),
    referenceAllele("referenceAllele"),
    variantAllele("tumorSeqAllele"),
    proteinChange("proteinChange"),
    mutationType("mutationType"),
    ncbiBuild("ncbiBuild"),
    variantType("variantType"),
    refseqMrnaId("refseqMrnaId"),
    proteinPosStart("proteinPosStart"),
    proteinPosEnd("proteinPosEnd"),
    keyword("keyword");

    private String originalValue;

    MutationSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
