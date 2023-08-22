package org.cbioportal.web.parameter.sort;

public enum ClinicalEventSortBy {

    eventType("eventType"),
    startNumberOfDaysSinceDiagnosis("startDate"),
    endNumberOfDaysSinceDiagnosis("stopDate");

    private String originalValue;

    ClinicalEventSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
