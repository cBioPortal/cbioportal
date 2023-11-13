package org.cbioportal.model;

import java.io.Serializable;

public class GenesetCorrelation implements Serializable {

	private Integer entrezGeneId;
    private String hugoGeneSymbol;
	private Double correlationValue;
    private String expressionMolecularProfileId;
    private String zScoreMolecularProfileId;

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

    public String getExpressionMolecularProfileId() {
        return expressionMolecularProfileId;
    }

    public void setExpressionMolecularProfileId(String expressionMolecularProfileId) {
        this.expressionMolecularProfileId = expressionMolecularProfileId;
    }

    public String getzScoreMolecularProfileId() {
        return zScoreMolecularProfileId;
    }

    public void setzScoreMolecularProfileId(String zScoreMolecularProfileId) {
        this.zScoreMolecularProfileId = zScoreMolecularProfileId;
    }
}
