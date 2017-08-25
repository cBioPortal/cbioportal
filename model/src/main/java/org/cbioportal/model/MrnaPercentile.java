package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class MrnaPercentile implements Serializable {

    private String molecularProfileId;
    private String sampleId;
    private Integer entrezGeneId;
    private BigDecimal percentile;
    private BigDecimal zScore;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public BigDecimal getPercentile() {
        return percentile;
    }

    public void setPercentile(BigDecimal percentile) {
        this.percentile = percentile;
    }

    public BigDecimal getzScore() {
        return zScore;
    }

    public void setzScore(BigDecimal zScore) {
        this.zScore = zScore;
    }
}
