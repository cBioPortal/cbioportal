package org.cbioportal.legacy.web.parameter;

import java.io.Serializable;
import java.util.List;

public class GenericAssaySelectionFilter implements Serializable {
  private String profileType;
  private Boolean patientLevel;
  private List<List<GenericAssaySelectionValue>> values;

  public String getProfileType() {
    return profileType;
  }

  public void setProfileType(String profileType) {
    this.profileType = profileType;
  }

  public Boolean getPatientLevel() {
    return patientLevel;
  }

  public void setPatientLevel(Boolean patientLevel) {
    this.patientLevel = patientLevel;
  }

  public List<List<GenericAssaySelectionValue>> getValues() {
    return values;
  }

  public void setValues(List<List<GenericAssaySelectionValue>> values) {
    this.values = values;
  }
}
