package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

class OncoKBDataFilter implements Serializable {
	private String attributeId;
	private List<String> values;

	public String getAttributeId() {
			return attributeId;
	}

	public void setAttributeId(String attributeId) {
			this.attributeId = attributeId;
	}

	public List<String> getValues() {
			return values;
	}

	public void setValues(List<String> values) {
			this.values = values;
	}
}
