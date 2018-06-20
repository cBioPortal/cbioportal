package org.cbioportal.web.parameter;

import java.util.List;

public class ClinicalDataEqualityFilter {

	private String attributeId;
	private ClinicalDataType clinicalDataType;
    private List<String> values;

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

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
}
