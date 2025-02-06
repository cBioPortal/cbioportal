package org.cbioportal.legacy.web.parameter.filter;

import org.cbioportal.legacy.model.ClinicalEventSample;
import org.cbioportal.legacy.model.PatientTreatmentRow;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OredPatientTreatmentFilters implements Serializable {
    private List<PatientTreatmentFilter> filters;

    public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
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
