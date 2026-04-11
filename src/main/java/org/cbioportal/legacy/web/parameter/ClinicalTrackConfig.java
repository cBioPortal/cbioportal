package org.cbioportal.legacy.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.ALWAYS)
class ClinicalTrackConfig implements Serializable {
  private String stableId;
  private String sortOrder;
  private Boolean gapOn;
  private String gapMode;

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

  public String getGapMode() {
    return gapMode;
  }

  public void setGapMode(String gapMode) {
    this.gapMode = gapMode;
  }
}
