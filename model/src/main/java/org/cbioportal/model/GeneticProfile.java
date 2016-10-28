package org.cbioportal.model;

import org.cbioportal.model.summary.GeneticProfileSummary;

public class GeneticProfile extends GeneticProfileSummary {

    private CancerStudy cancerStudy;

    public CancerStudy getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(CancerStudy cancerStudy) {
        this.cancerStudy = cancerStudy;
    }
}