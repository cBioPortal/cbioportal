package org.cbioportal.model;

import java.io.Serializable;

public class CopyNumberSampleCountByGene implements Serializable {
    
    private Integer entrezGeneId;
    private Integer alteration;
    private Integer sampleCount;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}
