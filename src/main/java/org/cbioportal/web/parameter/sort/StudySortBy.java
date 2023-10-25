package org.cbioportal.web.parameter.sort;

public enum StudySortBy {

    studyId("cancerStudyIdentifier"),
    cancerTypeId("typeOfCancerId"),
    name("name"),
    description("description"),
    publicStudy("public"),
    pmid("pmid"),
    citation("citation"),
    groups("groups"),
    status("status"),
    importDate("importDate");

    private String originalValue;

    StudySortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
