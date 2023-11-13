package org.cbioportal.web.parameter.sort;

public enum ClinicalAttributeSortBy {

    clinicalAttributeId("attrId"),
    displayName("displayName"),
    description("description"),
    datatype("datatype"),
    patientAttribute("patientAttribute"),
    priority("priority"),
    studyId("cancerStudyIdentifier");

    private String originalValue;

    ClinicalAttributeSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
