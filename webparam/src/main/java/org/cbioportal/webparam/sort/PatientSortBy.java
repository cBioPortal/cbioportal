package org.cbioportal.webparam.sort;

public enum PatientSortBy {

    patientId("stableId");

    private String originalValue;

    PatientSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
