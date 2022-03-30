package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OredSampleTreatmentFilters implements Serializable {
    private List<SampleTreatmentFilter> filters; // ored

    public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }

        return filters.stream()
            .anyMatch(f -> f.filter(sampleId, treatments));
    }

    public List<SampleTreatmentFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SampleTreatmentFilter> filters) {
        this.filters = filters;
    }
}
