package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class GenomicDataCountItem implements Serializable {

    private String hugoGeneSymbol;
    private String profileType;
    private List<GenomicDataCount> counts;

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

    public List<GenomicDataCount> getCounts() {
        return counts;
    }

    public void setCounts(List<GenomicDataCount> counts) {
        this.counts = counts;
    }
}
