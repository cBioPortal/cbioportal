package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.constraints.NotNull;

public class ExpressionEnrichment implements Serializable {
    @NotNull
    private Integer entrezGeneId;

    @NotNull
    private String hugoGeneSymbol;

    private String cytoband;

    @NotNull
    private List<GroupStatistics> groupsStatistics;

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

    public List<GroupStatistics> getGroupsStatistics() {
        return groupsStatistics;
    }

    public void setGroupsStatistics(List<GroupStatistics> groupsStatistics) {
        this.groupsStatistics = groupsStatistics;
    }

    public BigDecimal getpValue() {
        return pValue;
    }

    public void setpValue(BigDecimal pValue) {
        this.pValue = pValue;
    }
}
