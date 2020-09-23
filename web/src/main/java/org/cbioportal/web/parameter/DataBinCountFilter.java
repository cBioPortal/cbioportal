package org.cbioportal.web.parameter;

import java.io.Serializable;

public class DataBinCountFilter implements Serializable {

    private StudyViewFilter studyViewFilter;

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }
}
