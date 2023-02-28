package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class GenesetHierarchyInfo implements Serializable {

	private Integer nodeId;
    private String nodeName;
    private Integer parentId;
	private String parentNodeName;
	private List<Geneset> genesets;
	
	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public String getParentNodeName() {
		return parentNodeName;
	}
	
	public void setParentNodeName(String parentNodeName) {
		this.parentNodeName = parentNodeName;
	}
	
	public List<Geneset> getGenesets() {
		return genesets;
	}
	
	public void setGenesets(List<Geneset> genesets) {
		this.genesets = genesets;
	}
}
