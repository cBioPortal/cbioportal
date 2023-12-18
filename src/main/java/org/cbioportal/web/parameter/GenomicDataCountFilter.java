package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenomicDataCountFilter implements Serializable {

    private List<GenomicDataFilter> genomicDataFilters;
    private StudyViewFilter studyViewFilter;

    public List<GenomicDataFilter> getGenomicDataFilters() {
        return genomicDataFilters;
    }

    public void setGenomicDataFilters(List<GenomicDataFilter> genomicDataFilters) {
        this.genomicDataFilters = genomicDataFilters;
    }

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }
}
