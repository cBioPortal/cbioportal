package org.cbioportal.legacy.model;

import java.io.Serializable;

public class ClinicalDataBin extends DataBin implements Serializable {
  private String attributeId;

  public String getAttributeId() {
    return attributeId;
  }

  public void setAttributeId(String attributeId) {
    this.attributeId = attributeId;
  }
}
