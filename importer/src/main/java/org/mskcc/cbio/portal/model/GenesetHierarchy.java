/*
 * Copyright (c) 2017 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * @author Sander Tan
*/

package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class GenesetHierarchy implements Serializable {

	private int nodeId;
	private String nodeName;
	private int parentId;

	public GenesetHierarchy() {
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	
	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
}
