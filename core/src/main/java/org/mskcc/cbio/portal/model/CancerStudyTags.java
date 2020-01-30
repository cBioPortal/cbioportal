package org.mskcc.cbio.portal.model;

public class CancerStudyTags {
    private int cancerStudyId;
    private String tags;

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }
}
