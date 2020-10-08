package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.List;
import java.util.Map;

public class AndedSampleTreatmentFilters {
    private List<OredSampleTreatmentFilters> filters;

    public boolean filter(SampleIdentifier sampleId, Map<String, Map<String, Boolean>> treatments) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }

        return filters.stream()
            .allMatch(f -> f.filter(sampleId, treatments));
    }

    public List<OredSampleTreatmentFilters> getFilters() {
        return filters;
    }

    public void setFilters(List<OredSampleTreatmentFilters> filters) {
        this.filters = filters;
    }
}
