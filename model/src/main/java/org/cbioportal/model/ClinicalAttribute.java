package org.cbioportal.model;

import org.cbioportal.model.summary.ClinicalAttributeSummary;

public class ClinicalAttribute extends ClinicalAttributeSummary {

    private CancerStudy cancerStudy;

    public CancerStudy getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(CancerStudy cancerStudy) {
        this.cancerStudy = cancerStudy;
    }
}

