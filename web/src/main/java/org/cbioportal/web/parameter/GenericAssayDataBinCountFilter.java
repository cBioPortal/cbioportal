package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenericAssayDataBinCountFilter extends DataBinCountFilter implements Serializable {

    private List<GenericAssayDataBinFilter> genericAssayDataBinFilters;

    public List<GenericAssayDataBinFilter> getGenericAssayDataBinFilters() {
        return genericAssayDataBinFilters;
    }

    public void setGenericAssayDataBinFilters(List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        this.genericAssayDataBinFilters = genericAssayDataBinFilters;
    }

}
