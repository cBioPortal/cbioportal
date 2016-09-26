package org.cbioportal.model;

import org.cbioportal.model.summary.ClinicalDataSummary;

public class SampleClinicalData extends ClinicalDataSummary {

    private Sample sample;
    private ClinicalAttribute clinicalAttribute;

    public Sample getSample() {
        return sample;
    }

    public void setSample(Sample sample) {
        this.sample = sample;
    }

    public ClinicalAttribute getClinicalAttribute() {
        return clinicalAttribute;
    }

    public void setClinicalAttribute(ClinicalAttribute clinicalAttribute) {
        this.clinicalAttribute = clinicalAttribute;
    }
}
