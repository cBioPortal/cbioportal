package org.cbioportal.web.parameter;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

public class ClinicalEventRequestIdentifier implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    Set<ClinicalEventRequest> clinicalEventRequests;
    @NotNull
    OccurrencePosition position;

    public Set<ClinicalEventRequest> getClinicalEventRequests() {
        return clinicalEventRequests;
    }

    public void setClinicalEventRequests(Set<ClinicalEventRequest> clinicalEventRequests) {
        this.clinicalEventRequests = clinicalEventRequests;
    }

    public OccurrencePosition getPosition() {
        return position;
    }

    public void setPosition(OccurrencePosition position) {
        this.position = position;
    }
}
