package org.cbioportal.shared.enums;

public enum ClinicalDataType {
  SAMPLE,
  PATIENT;

  @Override
  public String toString() {
    return this.name().toLowerCase();
  }
}
