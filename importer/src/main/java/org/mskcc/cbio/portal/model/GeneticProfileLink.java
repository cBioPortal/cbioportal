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

public class GeneticProfileLink implements Serializable {
	
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
