package org.cbioportal.model;

import java.io.Serializable;

public class Patient implements Serializable {

    private Integer internalId;
    private String stableId;
    private Integer cancerStudyId;
    private String cancerStudyIdentifier;
    private CancerStudy cancerStudy;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public CancerStudy getCancerStudy() {
        return cancerStudy;
    }

    public void setCancerStudy(CancerStudy cancerStudy) {
        this.cancerStudy = cancerStudy;
    }
}