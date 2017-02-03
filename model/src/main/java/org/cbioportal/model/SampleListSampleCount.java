package org.cbioportal.model;

import java.io.Serializable;

public class SampleListSampleCount implements Serializable {
    
    private Integer sampleListId;
    private Integer sampleCount;

    public Integer getSampleListId() {
        return sampleListId;
    }

    public void setSampleListId(Integer sampleListId) {
        this.sampleListId = sampleListId;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}
