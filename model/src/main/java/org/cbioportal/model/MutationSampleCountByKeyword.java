package org.cbioportal.model;

import java.io.Serializable;

public class MutationSampleCountByKeyword implements Serializable {
    
    private String keyword;
    private Integer sampleCount;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(Integer sampleCount) {
        this.sampleCount = sampleCount;
    }
}
