package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class OncoKBDataCountFilter implements Serializable {

    private List<String> attributes;
    private StudyViewFilter studyViewFilter;

    public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
    }

    public StudyViewFilter getStudyViewFilter() {
		return studyViewFilter;
	}

	public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
		this.studyViewFilter = studyViewFilter;
	}
}
