package org.cbioportal.web.parameter.sort;

public enum GenePanelSortBy {

    genePanelId("stableId"),
    description("description");

    private String originalValue;

    GenePanelSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
