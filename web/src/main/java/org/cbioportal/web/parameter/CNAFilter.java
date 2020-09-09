package org.cbioportal.web.parameter;

import org.cbioportal.model.CNA;

import java.util.List;

public class CNAFilter {
    private List<Integer> genes;
    private CNA alteration;
    

    public List<Integer> getGenes() {
        return genes;
    }

    public void setGenes(List<Integer> genes) {
        this.genes = genes;
    }

    public CNA getAlteration() {
        return alteration;
    }

    public void setAlteration(CNA alteration) {
        this.alteration = alteration;
    }
}
