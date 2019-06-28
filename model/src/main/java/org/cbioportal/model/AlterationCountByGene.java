package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class AlterationCountByGene implements Serializable {

    private Integer entrezGeneId;
    private String hugoGeneSymbol;
    private Integer numberOfAlteredCases;
	private Integer totalCount;
	private Integer numberOfSamplesProfiled;
    private BigDecimal qValue;
    private List<String> matchingGenePanelIds;

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

	public Integer getNumberOfSamplesProfiled() {
		return numberOfSamplesProfiled;
	}

	public void setNumberOfSamplesProfiled(Integer numberOfSamplesProfiled) {
		this.numberOfSamplesProfiled = numberOfSamplesProfiled;
	}

	public BigDecimal getqValue() {
		return qValue;
	}

	public void setqValue(BigDecimal qValue) {
		this.qValue = qValue;
    }

    public List<String> getMatchingGenePanelIds() {
        return matchingGenePanelIds;
    }

    public void setMatchingGenePanelIds(List<String> matchingGenePanelIds) {
        this.matchingGenePanelIds = matchingGenePanelIds;
    }
}
