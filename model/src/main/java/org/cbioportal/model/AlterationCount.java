package org.cbioportal.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author ochoaa
 */
public class AlterationCount implements Serializable {

    @NotNull
    private Integer alteredCount;
    @NotNull
    private Integer profiledCount;

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
