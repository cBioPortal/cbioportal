package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;

public class MutSig implements Serializable {
    
    private Integer cancerStudyId;
    @NotNull
    private String cancerStudyIdentifier;
    @NotNull
    private Integer entrezGeneId;
    @NotNull
    private String hugoGeneSymbol;
    @NotNull
    private Integer rank;
    private Integer numbasescovered;
    @NotNull
    private Integer nummutations;
    @NotNull
    private BigDecimal pValue;
    @NotNull
    private BigDecimal qValue;

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

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

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getNumbasescovered() {
        return numbasescovered;
    }

    public void setNumbasescovered(Integer numbasescovered) {
        this.numbasescovered = numbasescovered;
    }

    public Integer getNummutations() {
        return nummutations;
    }

    public void setNummutations(Integer nummutations) {
        this.nummutations = nummutations;
    }

    public BigDecimal getPValue() {
        return pValue;
    }

    public void setPValue(BigDecimal pValue) {
        this.pValue = pValue;
    }

    public BigDecimal getQValue() {
        return qValue;
    }

    public void setQValue(BigDecimal qValue) {
        this.qValue = qValue;
    }
}