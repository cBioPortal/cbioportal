package org.cbioportal.model;

import java.io.Serializable;

public class GenericAssayDataCount implements Serializable {

    private String value;
    private Integer count;
    private String profileType;
    private String stableId;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
    
    public void setProfileType(String profileType) { this.profileType = profileType; }
    public String getProfileType() { return profileType; }

    public void setStableId(String profileType) { this.stableId = profileType; }
    public String getStableId() { return stableId; }
    
}

