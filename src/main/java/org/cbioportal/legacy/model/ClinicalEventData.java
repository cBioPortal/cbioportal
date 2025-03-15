package org.cbioportal.legacy.model;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class ClinicalEventData implements Serializable {
    
    private Long clinicalEventId;
    @NotNull
    private String key;
    @NotNull
    private String value;

    public Long getClinicalEventId() {
        return clinicalEventId;
    }

    public void setClinicalEventId(Long clinicalEventId) {
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
