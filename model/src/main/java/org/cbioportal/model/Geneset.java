package org.cbioportal.model;

import java.io.Serializable;

public class Geneset implements Serializable {

    private Integer internalId;
    private String genesetId;
    private String name;
    private String description;
    private String refLink;
    //representative score (can be set when retrieved in study context):
    private Double representativeScore;
    private Double representativePvalue;

	public Integer getInternalId() {
		return internalId;
	}

	public void setInternalId(Integer internalId) {
		this.internalId = internalId;
	}

	public String getGenesetId() {
		return genesetId;
	}

	public void setGenesetId(String genesetId) {
		this.genesetId = genesetId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRefLink() {
		return refLink;
	}

	public void setRefLink(String refLink) {
		this.refLink = refLink;
	}

	public Double getRepresentativeScore() {
		return representativeScore;
	}

	public void setRepresentativeScore(Double representativeScore) {
		this.representativeScore = representativeScore;
	}

	public Double getRepresentativePvalue() {
		return representativePvalue;
	}

	public void setRepresentativePvalue(Double representativePvalue) {
		this.representativePvalue = representativePvalue;
	}
}