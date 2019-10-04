package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

public class AlterationCountByGene implements Serializable {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private Integer numberOfAlteredCases;
    private Integer totalCount;
    private Integer numberOfProfiledCases;
    private BigDecimal qValue;
    private Set<String> matchingGenePanelIds;

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

    public Integer getNumberOfAlteredCases() {
        return numberOfAlteredCases;
    }

    public void setNumberOfAlteredCases(Integer numberOfAlteredCases) {
        this.numberOfAlteredCases = numberOfAlteredCases;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
    
    public Integer getNumberOfProfiledCases() {
        return numberOfProfiledCases;
    }

    public void setNumberOfProfiledCases(Integer numberOfProfiledCases) {
        this.numberOfProfiledCases = numberOfProfiledCases;
    }

    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

    public Set<String> getMatchingGenePanelIds() {
        return matchingGenePanelIds;
    }

    public void setMatchingGenePanelIds(Set<String> matchingGenePanelIds) {
        this.matchingGenePanelIds = matchingGenePanelIds;
    }
}
