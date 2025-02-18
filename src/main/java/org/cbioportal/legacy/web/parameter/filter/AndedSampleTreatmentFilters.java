package org.cbioportal.legacy.web.parameter.filter;

import org.cbioportal.legacy.model.PatientTreatmentRow;
import org.cbioportal.legacy.model.SampleTreatmentRow;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AndedSampleTreatmentFilters implements Serializable {
    private List<OredSampleTreatmentFilters> filters;

    public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
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
