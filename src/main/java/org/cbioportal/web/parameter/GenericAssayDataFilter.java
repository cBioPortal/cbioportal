package org.cbioportal.web.parameter;

import java.io.Serializable;

public class GenericAssayDataFilter extends DataFilter implements Serializable {
    private String stableId;
    private String profileType;

    public GenericAssayDataFilter() {}
    
    public GenericAssayDataFilter(String stableId, String profileType) {
        this.stableId = stableId;
        this.profileType = profileType;
    }
    
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
