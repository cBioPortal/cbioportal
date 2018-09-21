package org.cbioportal.web.parameter;

import java.util.List;

public class ClinicalDataEqualityFilter extends ClinicalDataFilter {
    private List<String> values;

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}
}
