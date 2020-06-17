package org.cbioportal.web.parameter;

import java.io.Serializable;

public class ClinicalDataBinFilter extends DataBinFilter implements Serializable {

    private String attributeId;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

}
