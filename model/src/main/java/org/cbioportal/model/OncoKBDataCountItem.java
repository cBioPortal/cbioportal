package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class OncoKBDataCountItem implements Serializable {

    private String attributeId;
    private List<OncoKBDataCount> counts;

    public String getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

	public List<OncoKBDataCount> getCounts() {
		return counts;
	}

	public void setCounts(List<OncoKBDataCount> counts) {
		this.counts = counts;
	}
}
