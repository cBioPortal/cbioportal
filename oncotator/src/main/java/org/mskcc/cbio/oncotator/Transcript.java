package org.mskcc.cbio.oncotator;

/**
 *
 */
public class Transcript
{
	private String gene;
	private String proteinChange;
	private String variantClassification;
	private int exonAffected;

	public String getProteinChange() {
		return proteinChange;
	}

	public void setProteinChange(String proteinChange) {
		this.proteinChange = proteinChange;
	}

	public String getVariantClassification() {
		return variantClassification;
	}

	public void setVariantClassification(String variantClassification) {
		this.variantClassification = variantClassification;
	}

	public String getGene() {
		return gene;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public int getExonAffected() {
		return exonAffected;
	}

	public void setExonAffected(int exonAffected) {
		this.exonAffected = exonAffected;
	}
}
