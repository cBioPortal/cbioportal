package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.TemporalRelation;
import org.cbioportal.model.SampleTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class SampleTreatmentFilter implements Serializable {
    private String treatment;
    private TemporalRelation time;

    /**
     * A sample should be included if the treatment row that corresponds
     * to the treatment and time in this filter contains that sample.
     * @param sampleId sample.STABLE_ID
     * @param treatments key is SampleTreatmentRow::calculateKey
     */
    public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
        Set<String> row = treatments.get(treatment + time.name());
        return row != null && row.contains(sampleId.toString());
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
