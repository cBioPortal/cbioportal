package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class ClinicalDataBinCountFilter extends DataBinCountFilter implements Serializable {

    private List<ClinicalDataBinFilter> attributes;

    public List<ClinicalDataBinFilter> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ClinicalDataBinFilter> attributes) {
        this.attributes = attributes;
    }

}
