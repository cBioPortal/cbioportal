package org.cbioportal.web.parameter.sort;

public enum SampleSortBy {

    sampleId("stableId"),
    sampleType("sampleType");

    private String originalValue;

    SampleSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
