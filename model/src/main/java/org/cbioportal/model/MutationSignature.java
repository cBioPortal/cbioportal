package org.cbioportal.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutationSignature implements Serializable {
	private String[] mutationTypes;
	private Integer[] counts;
	private String id;
	
	public String[] getMutationTypes() {
		return mutationTypes;
	}
	
	public Integer[] getCounts() {
		return counts;
	}
	
	public String getId() {
		return id;
	}
	
	private void setMutationTypes(String[] mutationTypes) {
		this.mutationTypes = mutationTypes;
	}
	
	private void setCounts(Integer[] counts) {
		this.counts = counts;
	}
	
	private void setId(String id) {
		this.id = id;
	}
	
	public MutationSignature(String _id, String[] _mutationTypes, Integer[] _counts) {
		id = _id;
		mutationTypes = _mutationTypes;
		counts = _counts;
	}
	
}
