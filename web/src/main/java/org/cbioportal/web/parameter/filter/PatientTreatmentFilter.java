package org.cbioportal.web.parameter.filter;

import org.cbioportal.model.PatientTreatmentRow;
import org.cbioportal.web.parameter.SampleIdentifier;

import java.util.Map;

public class PatientTreatmentFilter {
    private String treatment;
    private boolean received;

    /**
     * A sampleId should be included if the treatment row that corresponds
     * to the treatment and time in this filter contains that sampleId.
     * @param sampleId sample.STABLE_ID
     * @param treatments key is PatientTreatmentRow::calculateKey
     */
    public boolean filter(SampleIdentifier sampleId, Map<String, PatientTreatmentRow> treatments) {
        PatientTreatmentRow row = treatments.get(treatment + received);
        return row != null && row.getSamples().contains(sampleId.getSampleId());
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public boolean getReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
