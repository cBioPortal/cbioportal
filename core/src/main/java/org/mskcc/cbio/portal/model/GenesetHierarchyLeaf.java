package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class GenesetHierarchyLeaf implements Serializable {

	private int nodeId;
	private int genesetId;

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public int getNodeId() {
		return nodeId;
	}

	public void setGenesetId(int genesetId) {
		this.genesetId = genesetId;
	}
	public int getGenesetId() {
		return genesetId;
	}
}


