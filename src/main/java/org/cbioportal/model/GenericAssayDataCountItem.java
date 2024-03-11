package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;

public class GenericAssayDataCountItem implements Serializable {
  private String stableId;
  private List<GenericAssayDataCount> counts;

  public String getStableId() {
    return stableId;
  }

  public void setStableId(String stableId) {
    this.stableId = stableId;
  }

  public List<GenericAssayDataCount> getCounts() {
    return counts;
  }

  public void setCounts(List<GenericAssayDataCount> counts) {
    this.counts = counts;
  }
}
