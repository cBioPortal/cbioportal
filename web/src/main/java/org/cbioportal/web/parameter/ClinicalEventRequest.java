package org.cbioportal.web.parameter;

import org.cbioportal.model.ClinicalEventData;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

public class ClinicalEventRequest implements Serializable {

    @NotNull
    private String eventType;

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<ClinicalEventData> attributes;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<ClinicalEventData> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ClinicalEventData> attributes) {
        this.attributes = attributes;
    }
}
