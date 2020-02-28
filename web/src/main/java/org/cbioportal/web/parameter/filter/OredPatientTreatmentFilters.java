package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.List;
import java.util.Map;

public class OredPatientTreatmentFilters {
    private List<AndedPatientTreatmentFilters> filters; // anded

    public boolean filter(SampleIdentifier sampleId, Map<String, PatientTreatmentRow> treatments) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        
        return filters.stream()
            .allMatch(f -> f.filter(sampleId, treatments));
    }
    
    public List<AndedPatientTreatmentFilters> getFilters() {
        return filters;
    }

    public void setFilters(List<AndedPatientTreatmentFilters> filters) {
        this.filters = filters;
    }
}
