package org.cbioportal.model;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;

/**
 *
 * @author ochoaa
 */
public class CountSummary implements Serializable {

    @NotNull
    private String name;
    @NotNull
    private Integer alteredCount;
    @NotNull
    private Integer profiledCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAlteredCount() {
        return alteredCount;
    }

    public void setAlteredCount(Integer alteredCount) {
        this.alteredCount = alteredCount;
    }

    public Integer getProfiledCount() {
        return profiledCount;
    }

    public void setProfiledCount(Integer profiledCount) {
        this.profiledCount = profiledCount;
    }

}
