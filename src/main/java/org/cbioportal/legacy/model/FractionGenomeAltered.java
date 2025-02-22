package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class FractionGenomeAltered extends UniqueKeyBase {

  @NotNull private String studyId;
  @NotNull private String sampleId;
  @NotNull private String patientId;
  @NotNull private BigDecimal value;

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }
}
