package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cbioportal.legacy.model.MolecularProfile;

@Schema(name = "MolecularProfile", description = "Represents a molecular profile")
public class MolecularProfileDTO {
  private String molecularProfileId;
  private String studyId;
  private MolecularProfile.MolecularAlterationType molecularAlterationType;
  private String genericAssayType;
  private String datatype;
  private String name;
  private String description;
  private Boolean showProfileInAnalysisTab;
  private CancerStudyDTO study;
  private Float pivotThreshold;
  private String sortOrder;
  private Boolean patientLevel;

  public String getMolecularProfileId() {
    return molecularProfileId;
  }

  public void setMolecularProfileId(String molecularProfileId) {
    this.molecularProfileId = molecularProfileId;
  }

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public MolecularProfile.MolecularAlterationType getMolecularAlterationType() {
    return molecularAlterationType;
  }

  public void setMolecularAlterationType(
      MolecularProfile.MolecularAlterationType molecularAlterationType) {
    this.molecularAlterationType = molecularAlterationType;
  }

  public String getGenericAssayType() {
    return genericAssayType;
  }

  public void setGenericAssayType(String genericAssayType) {
    this.genericAssayType = genericAssayType;
  }

  public String getDatatype() {
    return datatype;
  }

  public void setDatatype(String datatype) {
    this.datatype = datatype;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getShowProfileInAnalysisTab() {
    return showProfileInAnalysisTab;
  }

  public void setShowProfileInAnalysisTab(Boolean showProfileInAnalysisTab) {
    this.showProfileInAnalysisTab = showProfileInAnalysisTab;
  }

  public CancerStudyDTO getStudy() {
    return study;
  }

  public void setStudy(CancerStudyDTO study) {
    this.study = study;
  }

  public Float getPivotThreshold() {
    return pivotThreshold;
  }

  public void setPivotThreshold(Float pivotThreshold) {
    this.pivotThreshold = pivotThreshold;
  }

  public String getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Boolean getPatientLevel() {
    return patientLevel;
  }

  public void setPatientLevel(Boolean patientLevel) {
    this.patientLevel = patientLevel;
  }
}
