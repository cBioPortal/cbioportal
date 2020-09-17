package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenomicDataBinCountFilter extends DataBinCountFilter implements Serializable {

    private List<GenomicDataBinFilter> genomicDataBinFilters;

    public List<GenomicDataBinFilter> getGenomicDataBinFilters() {
        return genomicDataBinFilters;
    }

    public void setGenomicDataBinFilters(List<GenomicDataBinFilter> genomicDataBinFilters) {
        this.genomicDataBinFilters = genomicDataBinFilters;
    }

}
