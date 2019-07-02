package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class ExpressionEnrichment implements Serializable {

    @NotNull
    private Integer entrezGeneId;
    @NotNull
    private String hugoGeneSymbol;
    private String cytoband;
    @NotNull
    private BigDecimal meanExpressionInAlteredGroup;
    @NotNull
    private BigDecimal meanExpressionInUnalteredGroup;
    @NotNull
    private BigDecimal standardDeviationInAlteredGroup;
    @NotNull
    private BigDecimal standardDeviationInUnalteredGroup;
    @NotNull
    private BigDecimal pValue;
    
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
}
