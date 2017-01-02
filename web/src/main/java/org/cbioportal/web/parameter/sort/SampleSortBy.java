package org.cbioportal.web.parameter.sort;

public enum SampleSortBy {

    sampleId("stableId"),
    sampleType("sampleType"),
    cancerTypeId("typeOfCancerId");

    private String originalValue;

    SampleSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
