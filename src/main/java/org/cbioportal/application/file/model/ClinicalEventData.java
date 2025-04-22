package org.cbioportal.application.file.model;

public class ClinicalEventData {
    private Integer clinicalEventId;
    private String key;
    private String value;

    public Integer getClinicalEventId() {
        return clinicalEventId;
    }

    public void setClinicalEventId(Integer clinicalEventId) {
        this.clinicalEventId = clinicalEventId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
