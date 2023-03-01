package org.cbioportal.model;

import java.io.Serializable;

public class ClinicalEventTypeCount implements Serializable {
    private String eventType;
    private Integer count;

    public ClinicalEventTypeCount(String eventType, Integer count) {
        this.eventType = eventType;
        this.count = count;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
