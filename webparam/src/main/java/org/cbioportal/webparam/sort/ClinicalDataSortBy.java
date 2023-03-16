package org.cbioportal.webparam.sort;

public enum ClinicalDataSortBy {

    clinicalAttributeId("attrId"),
    value("attrValue");

    private String originalValue;

    ClinicalDataSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
