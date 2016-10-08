package org.cbioportal.model;

import java.io.Serializable;

/*
	Class to represent a COSMIC count record.
*/
public class CosmicCount implements Serializable {
	private String proteinChange;
	private String keyword;
	private Integer count;
	private String cosmicMutationId;
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getProteinChange() {
		return proteinChange;
	}
	public void setProteinChange(String proteinChange) {
		this.proteinChange = proteinChange;
	}
	public String getCosmicMutationId() {
		return cosmicMutationId;
	}
	public void setCosmicMutationId(String cosmicMutationId) {
		this.cosmicMutationId = cosmicMutationId;
	}
}
