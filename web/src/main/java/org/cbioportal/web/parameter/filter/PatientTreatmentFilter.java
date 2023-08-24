package org.cbioportal.web.parameter.filter;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.cbioportal.web.parameter.SampleIdentifier;

public class PatientTreatmentFilter implements Serializable {
    private String treatment;

    /**
     * A sampleId should be included if the treatment row that corresponds
     * to the treatment and time in this filter contains that sampleId.
     * @param sampleId sample.STABLE_ID
     * @param treatments key is PatientTreatmentRow::calculateKey
     */
    public boolean filter(SampleIdentifier sampleId, Map<String, Set<String>> treatments) {
        Set<String> row = treatments.get(treatment);
        return row != null && row.contains(sampleId.toString());
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
}
