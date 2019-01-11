package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataCountItem implements Serializable {

    public enum ClinicalDataType {
        SAMPLE,
        PATIENT
    }    

    private String attributeId;
    private ClinicalDataType clinicalDataType;
    private List<ClinicalDataCount> counts;

    public String getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

	public ClinicalDataType getClinicalDataType() {
		return clinicalDataType;
	}

	public void setClinicalDataType(ClinicalDataType clinicalDataType) {
		this.clinicalDataType = clinicalDataType;
	}

	public List<ClinicalDataCount> getCounts() {
		return counts;
	}

	public void setCounts(List<ClinicalDataCount> counts) {
		this.counts = counts;
	}
}
