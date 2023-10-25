package org.cbioportal.model;

import java.io.Serializable;

public class GenericAssayDataBin extends DataBin implements Serializable {
    private String stableId;
    private String profileType;

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getProfileType() {
        return profileType;
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;
    }

}
