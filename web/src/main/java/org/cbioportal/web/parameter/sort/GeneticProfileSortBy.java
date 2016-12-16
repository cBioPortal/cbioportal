package org.cbioportal.web.parameter.sort;

public enum GeneticProfileSortBy {

    geneticProfileId("stableId"),
    geneticAlterationType("geneticAlterationType"),
    datatype("datatype"),
    name("name"),
    description("description"),
    showProfileInAnalysisTab("showProfileInAnalysisTab");

    private String originalValue;

    GeneticProfileSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
