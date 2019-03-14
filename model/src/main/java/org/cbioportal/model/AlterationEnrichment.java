package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class AlterationEnrichment implements Serializable {

    @NotNull
    private Integer entrezGeneId;
    @NotNull
    private String hugoGeneSymbol;
    private String cytoband;
    @NotNull
    private String logRatio;
    @NotNull
    private BigDecimal pValue;
    @NotNull
    private CountSummary set1CountSummary;
    @NotNull
    private CountSummary set2CountSummary;
    
    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getCytoband() {
        return cytoband;
    }

    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    public String getLogRatio() {
        return logRatio;
    }

    public void setLogRatio(String logRatio) {
        this.logRatio = logRatio;
    }

    public BigDecimal getpValue() {
        return pValue;
    }

    public void setpValue(BigDecimal pValue) {
        this.pValue = pValue;
    }

    public CountSummary getSet1CountSummary() {
        return set1CountSummary;
    }

    public void setSet1CountSummary(CountSummary set1CountSummary) {
        this.set1CountSummary = set1CountSummary;
    }

    public CountSummary getSet2CountSummary() {
        return set2CountSummary;
    }

    public void setSet2CountSummary(CountSummary set2CountSummary) {
        this.set2CountSummary = set2CountSummary;
    }
}
