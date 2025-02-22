package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author ochoaa
 */
public class CountSummary implements Serializable {

  @NotNull private String name;
  @NotNull private Integer alteredCount;
  @NotNull private Integer profiledCount;

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
