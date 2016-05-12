package org.cbioportal.persistence.dto;

import java.io.Serializable;

public class SampleMutationCount implements Serializable {

    private String sampleStableId;
    private Integer count;

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
