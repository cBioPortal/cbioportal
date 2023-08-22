package org.cbioportal.webparam.sort;

public enum ResourceDataSortBy {

    ResourceId("resourceId"),
    url("url");

    private String originalValue;

    ResourceDataSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
