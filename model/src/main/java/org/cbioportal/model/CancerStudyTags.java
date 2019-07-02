package org.cbioportal.model;

import java.io.Serializable;

public class CancerStudyTags implements Serializable {

    private Integer cancerStudyId;
    private String tags;

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}