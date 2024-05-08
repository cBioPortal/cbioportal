package org.cbioportal.web.parameter;

import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class ClinicalEventAttributeRequest implements Serializable {

    @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
    private List<PatientIdentifier> patientIdentifiers;

    private Set<ClinicalEventRequest> clinicalEventRequests;

    public List<PatientIdentifier> getPatientIdentifiers() {
        return patientIdentifiers;
    }

    public void setPatientIdentifiers(List<PatientIdentifier> patientIdentifiers) {
        this.patientIdentifiers = patientIdentifiers;
    }

    public Set<ClinicalEventRequest> getClinicalEventRequests() {
        return clinicalEventRequests;
    }

    public void setClinicalEventRequests(Set<ClinicalEventRequest> clinicalEventRequests) {
        this.clinicalEventRequests = clinicalEventRequests;
    }
}