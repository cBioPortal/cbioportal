package org.cbioportal.legacy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class AlterationCountByGene extends AlterationCountBase {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private Integer numberOfAlteredCases;
    private BigDecimal qValue;
    private String studyId;
    private Set<String> alteredInStudyIds;

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

    @Override
    public Integer getNumberOfAlteredCases() {
        return numberOfAlteredCases;
    }

    @Override
    public void setNumberOfAlteredCases(Integer numberOfAlteredCases) {
        this.numberOfAlteredCases = numberOfAlteredCases;
    }

    @JsonProperty("qValue")
    public BigDecimal getqValue() {
        return qValue;
    }

    public void setqValue(BigDecimal qValue) {
        this.qValue = qValue;
    }

    @Override
    public String getUniqueEventKey() {
        return hugoGeneSymbol;
    }

    @Override
    public String[] getHugoGeneSymbols() {
        return new String[]{hugoGeneSymbol};
    }

    @Override
    public Integer[] getEntrezGeneIds() {
        return new Integer[]{entrezGeneId};
    }

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public Set<String> getAlteredInStudyIds() {
        return alteredInStudyIds;
    }

    public void setAlteredInStudyIds(Set<String> alteredInStudyIds) {
        this.alteredInStudyIds = alteredInStudyIds;
    }
    
    public void addAlteredInStudyIds(Set<String> alteredInStudyIds) {
        if (this.alteredInStudyIds == null) {
            this.alteredInStudyIds = new HashSet<>();
        }
        this.alteredInStudyIds.addAll(alteredInStudyIds);
    }
}
