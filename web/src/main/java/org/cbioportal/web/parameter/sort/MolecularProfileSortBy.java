package org.cbioportal.web.parameter.sort;

public enum MolecularProfileSortBy {

    molecularProfileId("stableId"),
    molecularAlterationType("geneticAlterationType"),
    datatype("datatype"),
    name("name"),
    description("description"),
    showProfileInAnalysisTab("showProfileInAnalysisTab");

    private String originalValue;

    MolecularProfileSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
