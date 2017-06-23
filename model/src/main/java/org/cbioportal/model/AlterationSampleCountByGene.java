package org.cbioportal.model;

import java.io.Serializable;

public class AlterationSampleCountByGene implements Serializable {

    private Integer entrezGeneId;
    private Integer sampleCount;

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}
