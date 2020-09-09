package org.cbioportal.web.parameter;

import org.cbioportal.model.CNA;

public class CNAFilter {
    private Integer entrezGeneId;
    private CNA alteration;
    

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public CNA getAlteration() {
        return alteration;
    }

    public void setAlteration(CNA alteration) {
        this.alteration = alteration;
    }
}
