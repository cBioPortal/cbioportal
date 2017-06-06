package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class AlterationEnrichment implements Serializable {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private String cytoband;
    private Integer numberOfSamplesInAlteredGroup;
    private Integer numberOfSamplesInUnalteredGroup;
    private String logRatio;
    private BigDecimal pValue;
    private BigDecimal qValue;

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

    public Integer getNumberOfSamplesInAlteredGroup() {
        return numberOfSamplesInAlteredGroup;
    }

    public void setNumberOfSamplesInAlteredGroup(Integer numberOfSamplesInAlteredGroup) {
        this.numberOfSamplesInAlteredGroup = numberOfSamplesInAlteredGroup;
    }

    public Integer getNumberOfSamplesInUnalteredGroup() {
        return numberOfSamplesInUnalteredGroup;
    }

    public void setNumberOfSamplesInUnalteredGroup(Integer numberOfSamplesInUnalteredGroup) {
        this.numberOfSamplesInUnalteredGroup = numberOfSamplesInUnalteredGroup;
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

    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }
}
