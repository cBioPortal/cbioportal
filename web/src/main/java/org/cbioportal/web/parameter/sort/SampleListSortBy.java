package org.cbioportal.web.parameter.sort;

public enum SampleListSortBy {

    caseListId("stableId"),
    category("category"),
    cancerStudyId("cancerStudyIdentifier"),
    name("name"),
    description("description");
    
    private String value;

    SampleListSortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
