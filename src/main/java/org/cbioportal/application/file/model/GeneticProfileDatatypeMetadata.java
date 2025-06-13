package org.cbioportal.application.file.model;

import java.util.List;
import java.util.SequencedMap;

public class GeneticProfileDatatypeMetadata implements GeneticDatatypeMetadata {
  private String stableId;
  private String geneticAlterationType;
  private String datatype;
  private String genericAssayType;
  private String cancerStudyIdentifier;
  private String profileName;
  private String profileDescription;
  private String genePanel;
  private Boolean showProfileInAnalysisTab;
  private Float pivotThreshold;
  private String sortOrder;
  private Boolean patientLevel;
  private List<String> genericEntitiesMetaProperties;

  public GeneticProfileDatatypeMetadata() {}

  public String getStableId() {
    return stableId;
  }

  public void setStableId(String stableId) {
    this.stableId = stableId;
  }

  /**
   * Get the stable ID of the genetic datatype without the cancer study identifier.
   *
   * @return
   */
  public String getGeneticDatatypeStableId() {
    if (stableId == null) {
      return null;
    }
    if (cancerStudyIdentifier == null) {
      return stableId;
    }
    return stableId.replace(this.cancerStudyIdentifier + "_", "");
  }

  @Override
  public String getGeneticAlterationType() {
    return geneticAlterationType;
  }

  public void setGeneticAlterationType(String geneticAlterationType) {
    this.geneticAlterationType = geneticAlterationType;
  }

  @Override
  public String getDatatype() {
    return datatype;
  }

  public void setDatatype(String datatype) {
    this.datatype = datatype;
  }

  public String getCancerStudyIdentifier() {
    return cancerStudyIdentifier;
  }

  public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
    this.cancerStudyIdentifier = cancerStudyIdentifier;
  }

  public String getProfileName() {
    return profileName;
  }

  public void setProfileName(String profileName) {
    this.profileName = profileName;
  }

  public String getProfileDescription() {
    return profileDescription;
  }

  public void setProfileDescription(String profileDescription) {
    this.profileDescription = profileDescription;
  }

  public String getGenePanel() {
    return genePanel;
  }

  public void setGenePanel(String genePanel) {
    this.genePanel = genePanel;
  }

  public Boolean getShowProfileInAnalysisTab() {
    return showProfileInAnalysisTab;
  }

  public void setShowProfileInAnalysisTab(Boolean showProfileInAnalysisTab) {
    this.showProfileInAnalysisTab = showProfileInAnalysisTab;
  }

  public String getGenericAssayType() {
    return genericAssayType;
  }

  public void setGenericAssayType(String genericAssayType) {
    this.genericAssayType = genericAssayType;
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

  public List<String> getGenericEntitiesMetaProperties() {
    return genericEntitiesMetaProperties;
  }

  public void setGenericEntitiesMetaProperties(List<String> genericEntitiesMetaProperties) {
    this.genericEntitiesMetaProperties = genericEntitiesMetaProperties;
  }

  @Override
  public SequencedMap<String, String> toMetadataKeyValues() {
    var metadata = GeneticDatatypeMetadata.super.toMetadataKeyValues();
    metadata.put("stable_id", getGeneticDatatypeStableId());
    metadata.put(
        "show_profile_in_analysis_tab",
        getShowProfileInAnalysisTab() == null
            ? null
            : getShowProfileInAnalysisTab().toString().toLowerCase());
    metadata.put("profile_name", getProfileName());
    metadata.put("profile_description", getProfileDescription());
    metadata.put("gene_panel", getGenePanel());
    metadata.put(
        "pivot_threshold_value",
        getPivotThreshold() == null ? null : getPivotThreshold().toString());
    metadata.put("value_sort_order", getSortOrder());
    metadata.put(
        "patient_level",
        getPatientLevel() == null ? null : getPatientLevel().toString().toLowerCase());
    metadata.put("generic_assay_type", getGenericAssayType());
    metadata.put(
        "generic_entity_meta_properties",
        getGenericEntitiesMetaProperties() == null
            ? null
            : String.join(",", getGenericEntitiesMetaProperties()));
    return metadata;
  }
}
