package org.cbioportal.webparam;

import java.io.Serializable;

public class ClinicalDataFilter extends DataFilter implements Serializable {

    private String attributeId;

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

}
