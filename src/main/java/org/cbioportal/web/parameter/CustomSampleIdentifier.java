package org.cbioportal.web.parameter;

import java.io.Serializable;

public class CustomSampleIdentifier extends SampleIdentifier implements Serializable {
    
    private boolean isFilteredOut = false;
    private String attributeId;

    public boolean getIsFilteredOut() {
        return isFilteredOut;
    }

    public void setIsFilteredOut(boolean isFilteredOut) {
        this.isFilteredOut = isFilteredOut;
    }

    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }
}
