package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataBinCountFilter implements Serializable {

    private List<ClinicalDataBinFilter> attributes;
    private StudyViewFilter studyViewFilter;

    public List<ClinicalDataBinFilter> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ClinicalDataBinFilter> attributes) {
        this.attributes = attributes;
    }

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }
}
