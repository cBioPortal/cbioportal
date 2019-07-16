package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class OncoKBDataCountItem implements Serializable {

    public enum OncoKBDataType {
        SAMPLE,
        PATIENT
    }

    private String attributeId;
    private OncoKBDataType oncoKBDataType;
    private List<OncoKBDataCount> counts;

    public String getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

	public OncoKBDataType getOncoKBDataType() {
		return clinicalDataType;
	}

	public void setOncoKBDataType(OncoKBDataType oncoKBDataType) {
		this.oncoKBDataType = oncoKBDataType;
	}

	public List<OncoKBDataCount> getCounts() {
		return counts;
	}

	public void setCounts(List<OncoKBDataCount> counts) {
		this.counts = counts;
	}
}
