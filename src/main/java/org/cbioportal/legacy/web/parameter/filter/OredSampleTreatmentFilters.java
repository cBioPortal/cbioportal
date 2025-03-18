package org.cbioportal.legacy.web.parameter.filter;

import org.cbioportal.legacy.model.SampleTreatmentRow;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

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
