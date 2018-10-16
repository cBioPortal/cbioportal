package org.cbioportal.web.parameter;

import java.util.List;

public class ClinicalDataCountFilter {

    private List<ClinicalDataFilter> attributes;
    private StudyViewFilter studyViewFilter;

    public List<ClinicalDataFilter> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ClinicalDataFilter> attributes) {
		this.attributes = attributes;
    }
    
    public StudyViewFilter getStudyViewFilter() {
		return studyViewFilter;
	}

	public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
		this.studyViewFilter = studyViewFilter;
	}
}
