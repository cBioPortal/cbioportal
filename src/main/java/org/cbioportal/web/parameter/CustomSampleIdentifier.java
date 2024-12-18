package org.cbioportal.web.parameter;

import java.io.Serializable;

public class CustomSampleIdentifier extends SampleIdentifier implements Serializable {
    
    private boolean isFilteredOut = false;
    private String value;

    public boolean getIsFilteredOut() {
        return isFilteredOut;
    }

    public void setIsFilteredOut(boolean isFilteredOut) {
        this.isFilteredOut = isFilteredOut;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
