package org.cbioportal.web.parameter.filter;

import java.util.Map;

import org.cbioportal.web.parameter.SampleIdentifier;

public class PatientTreatmentFilter {
    private String treatment;

    /**
     * A sampleId should be included if the treatment row that corresponds
     * to the treatment and time in this filter contains that sampleId.
     * @param sampleId sample.STABLE_ID
     * @param treatments key is PatientTreatmentRow::calculateKey
     */
    public boolean filter(SampleIdentifier sampleId, Map<String, Map<String, Boolean>> treatments) {
        Map<String, Boolean> row = treatments.get(treatment);
        return row != null && row.containsKey(sampleId.toString());
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }
}
