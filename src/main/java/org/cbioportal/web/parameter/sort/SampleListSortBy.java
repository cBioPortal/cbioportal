package org.cbioportal.web.parameter.sort;

public enum SampleListSortBy {

    sampleListId("stableId"),
    category("category"),
    studyId("cancerStudyIdentifier"),
    name("name"),
    description("description");
    
    private String originalValue;

    SampleListSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
