package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DensityPlotData implements Serializable {
    @NotNull
    private List<DensityPlotBin> bins = new ArrayList<>();
    
    private Double pearsonCorr;
    
    private Double spearmanCorr;

    public List<DensityPlotBin> getBins() {
        return bins;
    }

    public void setBins(List<DensityPlotBin> bins) {
        this.bins = bins;
    }
    
    public Double getPearsonCorr() {
        return pearsonCorr;
    }

    public void setPearsonCorr(Double pearsonCorr) {
        this.pearsonCorr = pearsonCorr;
    }

    public Double getSpearmanCorr() {
        return spearmanCorr;
    }

    public void setSpearmanCorr(Double spearmanCorr) {
        this.spearmanCorr = spearmanCorr;
    }
}
