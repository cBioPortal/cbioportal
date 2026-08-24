package org.cbioportal.legacy.web.parameter;

import java.io.Serializable;

public class GenericAssaySelectionValue implements Serializable {
  private String stableId;
  private String value;

  public String getStableId() {
    return stableId;
  }

  public void setStableId(String stableId) {
    this.stableId = stableId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
