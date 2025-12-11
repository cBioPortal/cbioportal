package org.cbioportal.legacy.model;

import java.io.Serializable;

public class MolecularDataCountItem implements Serializable {

  private String molecularProfileId;
  private Integer count;

  public String getMolecularProfileId() {
    return molecularProfileId;
  }

  public void setMolecularProfileId(String molecularProfileId) {
    this.molecularProfileId = molecularProfileId;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }
}
