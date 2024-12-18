package org.cbioportal.web.parameter;

import java.io.Serializable;

public class CustomSampleIdentifier extends SampleIdentifier implements Serializable {
    
    private boolean isFilteredOut = false;
    private String value;
    private String uniqueSampleId;

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

    // Generating unique SampleId by concatenating studyId and sampleId
    public String getUniqueSampleId() {
        // Assuming studyId and sampleId are available in SampleIdentifier
        // Concatenate with "_" in between if both values are not null
        if (getStudyId() != null && getSampleId() != null) {
            return getStudyId() + "_" + getSampleId();
        }
        return null;  // or return null if either studyId or sampleId is null
    }
}
