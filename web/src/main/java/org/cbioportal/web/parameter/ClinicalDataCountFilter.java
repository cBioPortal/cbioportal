package org.cbioportal.web.parameter;

import java.util.List;

public class ClinicalDataCountFilter {

    private List<String> attributeIds;
    private StudyViewFilter filter;

	public List<String> getAttributeIds() {
		return attributeIds;
	}

	public void setAttributeIds(List<String> attributeIds) {
		this.attributeIds = attributeIds;
	}

	public StudyViewFilter getFilter() {
		return filter;
	}

	public void setFilter(StudyViewFilter filter) {
		this.filter = filter;
	}
}
