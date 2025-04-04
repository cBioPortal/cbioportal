package org.cbioportal.legacy.web.parameter;

import java.io.Serializable;
import java.util.List;

public class NamespaceDataCountFilter implements Serializable {

    private List<NamespaceDataFilter> attributes;
    private StudyViewFilter studyViewFilter;

    public List<NamespaceDataFilter> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<NamespaceDataFilter> attributes) {
        this.attributes = attributes;
    }

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }
}
