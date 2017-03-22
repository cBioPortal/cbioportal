package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class MrnaPercentile implements Serializable {

    private String geneticProfileId;
    private String sampleId;
    private Integer entrezGeneId;
    private BigDecimal percentile;
    private BigDecimal zScore;

    public String getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(String geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
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
