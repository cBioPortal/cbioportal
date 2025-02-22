package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class GenericAssayData extends MolecularData implements Serializable {

  @NotNull private String genericAssayStableId;

  private boolean patientLevel;

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

  public void setPatientLevel(boolean patientLevel) {
    this.patientLevel = patientLevel;
  }

  public boolean getPatientLevel() {
    return patientLevel;
  }
}
