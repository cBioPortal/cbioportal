package org.cbioportal.web.parameter.sort;

public enum CancerTypeSortBy {

    cancerTypeId("typeOfCancerId"),
    name("name"),
    dedicatedColor("dedicatedColor"),
    shortName("shortName"),
    parent("parent");

    private String originalValue;

    CancerTypeSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
