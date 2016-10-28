package org.cbioportal.model;

import org.cbioportal.model.summary.PatientSummary;

public class Patient extends PatientSummary {

    private CancerStudy cancerStudy;

    public CancerStudy getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(CancerStudy cancerStudy) {
        this.cancerStudy = cancerStudy;
    }
}