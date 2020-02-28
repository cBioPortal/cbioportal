package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AndedPatientTreatmentFilters {
    private List<OredPatientTreatmentFilters> filters;

    public boolean filter(SampleIdentifier sampleId, Map<String, Map<String, Boolean>> treatments) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        
        return filters.stream()
            .allMatch(f -> f.filter(sampleId, treatments));
    }
    
    public List<OredPatientTreatmentFilters> getFilters() {
        return filters;
    }

    public void setFilters(List<OredPatientTreatmentFilters> filters) {
        this.filters = filters;
    }
}
