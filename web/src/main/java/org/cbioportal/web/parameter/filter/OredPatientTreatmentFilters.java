package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class OredPatientTreatmentFilters {
    private List<PatientTreatmentFilter> filters;

    public boolean filter(SampleIdentifier sampleId, Map<String, Map<String, Boolean>> treatments) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        
        return filters.stream()
            .anyMatch(f -> f.filter(sampleId, treatments));
    }
    
    public List<PatientTreatmentFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<PatientTreatmentFilter> filters) {
        this.filters = filters;
    }
}
