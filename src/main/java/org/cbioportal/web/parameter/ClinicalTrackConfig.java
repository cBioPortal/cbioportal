package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.ALWAYS)
class ClinicalTrackConfig implements Serializable {
    private String stableId;
    private String sortOrder;
    private Boolean gapOn;

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getGapOn() {
        return gapOn;
    }

    public void setGapOn(Boolean gapOn) {
        this.gapOn = gapOn;
    }
}
