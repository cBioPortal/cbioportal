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
    private AlterationCount set1AlterationCount;
    @NotNull
    private AlterationCount set2AlterationCount;
    
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

    public AlterationCount getSet1AlterationCount() {
        return set1AlterationCount;
    }

    public void setSet1AlterationCount(AlterationCount set1AlterationCount) {
        this.set1AlterationCount = set1AlterationCount;
    }

    public AlterationCount getSet2AlterationCount() {
        return set2AlterationCount;
    }

    public void setSet2AlterationCount(AlterationCount set2AlterationCount) {
        this.set2AlterationCount = set2AlterationCount;
    }
}
