package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

public class AlterationCountByStructuralVariant implements Serializable {

    private Integer gene1EntrezGeneId;
    private String gene1HugoGeneSymbol; 
    private Integer gene2EntrezGeneId;
    private String gene2HugoGeneSymbol;
    private Integer numberOfAlteredCases;
    private Integer totalCount;
    private Integer numberOfProfiledCases;
    private BigDecimal qValue;
    private Set<String> matchingGenePanelIds;

    public Integer getGene1EntrezGeneId() {
        return gene1EntrezGeneId;
    }

    public void setGene1EntrezGeneId(Integer gene1EntrezGeneId) {
        this.gene1EntrezGeneId = gene1EntrezGeneId;
    }

    public String getGene1HugoGeneSymbol() {
        return gene1HugoGeneSymbol;
    }

    public void setGene1HugoGeneSymbol(String gene1HugoGeneSymbol) {
        this.gene1HugoGeneSymbol = gene1HugoGeneSymbol;
    }

    public Integer getGene2EntrezGeneId() {
        return gene2EntrezGeneId;
    }

    public void setGene2EntrezGeneId(Integer gene2EntrezGeneId) {
        this.gene2EntrezGeneId = gene2EntrezGeneId;
    }

    public String getGene2HugoGeneSymbol() {
        return gene2HugoGeneSymbol;
    }

    public void setGene2HugoGeneSymbol(String gene2HugoGeneSymbol) {
        this.gene2HugoGeneSymbol = gene2HugoGeneSymbol;
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
