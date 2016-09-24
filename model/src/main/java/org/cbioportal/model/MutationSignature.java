package org.cbioportal.model;

import java.util.Map;

public class MutationSignature {
	private String id;
	private Map<String, Integer> counts;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public Map<String, Integer> getCounts() {
		return counts;
	}
	
	public void setCounts(Map<String, Integer> counts) {
		this.counts = counts;
	}
	
	public MutationSignature(String id, Map<String, Integer> counts) {
		this.id = id;
		this.counts = counts;
	}
}
