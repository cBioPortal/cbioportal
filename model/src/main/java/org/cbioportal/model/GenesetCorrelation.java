package org.cbioportal.model;

import java.io.Serializable;

public class GenesetCorrelation implements Serializable {

	private Integer entrezGeneId;
    private String hugoGeneSymbol;
	private Double correlationValue;
    private String expressionGeneticProfileId;
    private String zScoreGeneticProfileId;

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
	
    public Double getCorrelationValue() {
        return correlationValue;
    }

    public void setCorrelationValue(Double correlationValue) {
        this.correlationValue = correlationValue;
    }

	public String getExpressionGeneticProfileId() {
		return expressionGeneticProfileId;
	}

	public void setExpressionGeneticProfileId(String expressionGeneticProfileId) {
		this.expressionGeneticProfileId = expressionGeneticProfileId;
	}

	public String getzScoreGeneticProfileId() {
		return zScoreGeneticProfileId;
	}

	public void setzScoreGeneticProfileId(String zScoreGeneticProfileId) {
		this.zScoreGeneticProfileId = zScoreGeneticProfileId;
	}


}
