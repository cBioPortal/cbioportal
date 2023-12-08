package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class Gistic implements Serializable {
    
    private Long gisticRoiId;
    @NotNull
    private String cancerStudyId;
    @NotNull
    private Integer chromosome;
    @NotNull
    private String cytoband;
    @NotNull
    private Integer widePeakStart;
    @NotNull
    private Integer widePeakEnd;
    @NotNull
    private BigDecimal qValue;
    @NotNull
    private Boolean amp;
    private List<GisticToGene> genes;

    public Long getGisticRoiId() {
        return gisticRoiId;
    }

    public void setGisticRoiId(Long gisticRoiId) {
        this.gisticRoiId = gisticRoiId;
    }

    public String getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(String cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public Integer getChromosome() {
        return chromosome;
    }

    public void setChromosome(Integer chromosome) {
        this.chromosome = chromosome;
    }

    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    public Integer getWidePeakStart() {
        return widePeakStart;
    }

    public void setWidePeakStart(Integer widePeakStart) {
        this.widePeakStart = widePeakStart;
    }

    public Integer getWidePeakEnd() {
        return widePeakEnd;
    }

    public void setWidePeakEnd(Integer widePeakEnd) {
        this.widePeakEnd = widePeakEnd;
    }
    @JsonProperty("qValue")
    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

    public Boolean getAmp() {
        return amp;
    }

    public void setAmp(Boolean amp) {
        this.amp = amp;
    }

    public List<GisticToGene> getGenes() {
        return genes;
    }

    public void setGenes(List<GisticToGene> genes) {
        this.genes = genes;
    }
}