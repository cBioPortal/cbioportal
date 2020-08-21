package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.TemporalRelation;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.Map;

public class SampleTreatmentFilter {
    private String treatment;
    private TemporalRelation time;

    /**
     * A sample should be included if the treatment row that corresponds
     * to the treatment and time in this filter contains that sample.
     * @param sampleId sample.STABLE_ID
     * @param treatments key is SampleTreatmentRow::calculateKey
     */
    public boolean filter(SampleIdentifier sampleId, Map<String, Map<String, Boolean>> treatments) {
        Map<String, Boolean> row = treatments.get(treatment + time.name());
        return row != null && row.containsKey(sampleId.toString());
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public TemporalRelation getTime() {
        return time;
    }

    public void setTime(TemporalRelation time) {
        this.time = time;
    }

}
