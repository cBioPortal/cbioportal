package org.cbioportal.legacy.web.parameter.sort;

public enum ClinicalDataSortBy {
  clinicalAttributeId("attrId"),
  value("attrValue"),
  patientId("patientId");

  private String originalValue;

  ClinicalDataSortBy(String originalValue) {
    this.originalValue = originalValue;
  }

  public String getOriginalValue() {
    return originalValue;
  }
}
