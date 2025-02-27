package org.cbioportal.domain.clinical_data;

public enum ClinicalDataType {
  SAMPLE,
  PATIENT;

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
