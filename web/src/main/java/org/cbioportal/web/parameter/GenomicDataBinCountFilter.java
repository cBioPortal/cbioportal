package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenomicDataBinCountFilter implements Serializable {

    private List<GenomicDataBinFilter> genomicDataBinFilters;
    private StudyViewFilter studyViewFilter;

    public List<GenomicDataBinFilter> getGenomicDataBinFilters() {
        return genomicDataBinFilters;
    }

    public void setGenomicDataBinFilters(List<GenomicDataBinFilter> genomicDataBinFilters) {
        this.genomicDataBinFilters = genomicDataBinFilters;
    }

    public StudyViewFilter getStudyViewFilter() {
        return studyViewFilter;
    }

    public void setStudyViewFilter(StudyViewFilter studyViewFilter) {
        this.studyViewFilter = studyViewFilter;
    }
}
