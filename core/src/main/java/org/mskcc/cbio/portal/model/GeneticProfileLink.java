package org.mskcc.cbio.portal.model;

public class GeneticProfileLink {
	private int referringGeneticProfileId;
	private int referredGeneticProfileId;
	private String referenceType;
	
	public int getReferringGeneticProfileId() {
		return referringGeneticProfileId;
	}
	public void setReferringGeneticProfileId(int referringGeneticProfileId) {
		this.referringGeneticProfileId = referringGeneticProfileId;
	}
	public int getReferredGeneticProfileId() {
		return referredGeneticProfileId;
	}
	public void setReferredGeneticProfileId(int referredGeneticProfileId) {
		this.referredGeneticProfileId = referredGeneticProfileId;
	}
	public String getReferenceType() {
		return referenceType;
	}
	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}
}
