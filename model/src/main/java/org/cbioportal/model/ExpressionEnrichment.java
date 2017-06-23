package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ExpressionEnrichment implements Serializable {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private String cytoband;
    private BigDecimal meanExpressionInAlteredGroup;
    private BigDecimal meanExpressionInUnalteredGroup;
    private BigDecimal standardDeviationInAlteredGroup;
    private BigDecimal standardDeviationInUnalteredGroup;
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

    public BigDecimal getMeanExpressionInAlteredGroup() {
        return meanExpressionInAlteredGroup;
    }

    public void setMeanExpressionInAlteredGroup(BigDecimal meanExpressionInAlteredGroup) {
        this.meanExpressionInAlteredGroup = meanExpressionInAlteredGroup;
    }

    public BigDecimal getMeanExpressionInUnalteredGroup() {
        return meanExpressionInUnalteredGroup;
    }

    public void setMeanExpressionInUnalteredGroup(BigDecimal meanExpressionInUnalteredGroup) {
        this.meanExpressionInUnalteredGroup = meanExpressionInUnalteredGroup;
    }

    public BigDecimal getStandardDeviationInAlteredGroup() {
        return standardDeviationInAlteredGroup;
    }

    public void setStandardDeviationInAlteredGroup(BigDecimal standardDeviationInAlteredGroup) {
        this.standardDeviationInAlteredGroup = standardDeviationInAlteredGroup;
    }

    public BigDecimal getStandardDeviationInUnalteredGroup() {
        return standardDeviationInUnalteredGroup;
    }

    public void setStandardDeviationInUnalteredGroup(BigDecimal standardDeviationInUnalteredGroup) {
        this.standardDeviationInUnalteredGroup = standardDeviationInUnalteredGroup;
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
