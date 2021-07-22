package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenericAssayDataCountFilter implements Serializable {
    
    private List<GenericAssayDataFilter> genericAssayDataFilters;
    private StudyViewFilter studyViewFilter;

    public List<GenericAssayDataFilter> getGenericAssayDataFilters() {
        return genericAssayDataFilters;
    }

    public void setGenericAssayDataFilters(List<GenericAssayDataFilter> genericAssayDataFilters) {
        this.genericAssayDataFilters = genericAssayDataFilters;
    }

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }
}
