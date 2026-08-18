package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Patient", description = "Represents a patient")
public class PatientDTO {
  private String patientId;
  private String studyId;
  private String uniqueSampleKey;
  private String uniquePatientKey;

  @Schema(hidden = true)
  @JsonIgnore
  private Integer internalId;

  @JsonIgnore private CancerStudyDTO cancerStudy;

  @Schema(hidden = true)
  @JsonIgnore
  private CancerStudyDTO study;

  public String getPatientId() {
    return patientId;
  }

  public void setPatientId(String patientId) {
    this.patientId = patientId;
  }

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getUniquePatientKey() {
    return uniquePatientKey;
  }

  public void setUniquePatientKey(String uniquePatientKey) {
    this.uniquePatientKey = uniquePatientKey;
  }

  public String getUniqueSampleKey() {
    return uniqueSampleKey;
  }

  public void setUniqueSampleKey(String uniqueSampleKey) {
    this.uniqueSampleKey = uniqueSampleKey;
  }

  public Integer getInternalId() {
    return internalId;
  }

  public void setInternalId(Integer internalId) {
    this.internalId = internalId;
  }

  public CancerStudyDTO getCancerStudy() {
    return cancerStudy;
  }

  public void setCancerStudy(CancerStudyDTO cancerStudy) {
    this.cancerStudy = cancerStudy;
  }

  public CancerStudyDTO getStudy() {
    return study;
  }

  public void setStudy(CancerStudyDTO study) {
    this.study = study;
  }
}
