package org.cbioportal.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

public class GenomicEnrichment extends ExpressionEnrichment implements Serializable {

	@NotNull
	private Integer entrezGeneId;
	@NotNull
	private String hugoGeneSymbol;
	private String cytoband;

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

	public String getCytoband() {
		return cytoband;
	}

	public void setCytoband(String cytoband) {
		this.cytoband = cytoband;
	}
}
