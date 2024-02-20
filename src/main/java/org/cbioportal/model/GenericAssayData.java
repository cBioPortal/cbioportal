package org.cbioportal.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class GenericAssayData extends MolecularData implements Serializable {

  @NotNull private String genericAssayStableId;

  /**
   * @return the genericAssayStableId
   */
  public String getGenericAssayStableId() {
    return genericAssayStableId;
  }

  /**
   * @param genericAssayStableId the genericAssayStableId to set
   */
  public void setGenericAssayStableId(String genericAssayStableId) {
    this.genericAssayStableId = genericAssayStableId;
  }

  @Override
  public String getStableId() {
    return genericAssayStableId;
  }
}
