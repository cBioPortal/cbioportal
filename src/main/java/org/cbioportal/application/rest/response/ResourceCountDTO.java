package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResourceCount", description = "Represents resource data counts for a study")
public class ResourceCountDTO {
  private String resourceId;
  private String displayName;
  private String description;
  private String resourceType;
  private String priority;
  private Boolean openByDefault;
  private String studyId;
  private String customMetaData;
  private Integer sampleCount;
  private Integer patientCount;

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public Boolean getOpenByDefault() {
    return openByDefault;
  }

  public void setOpenByDefault(Boolean openByDefault) {
    this.openByDefault = openByDefault;
  }

  public String getStudyId() {
    return studyId;
  }

  @JsonProperty("cancerStudyIdentifier")
  public String getCancerStudyIdentifier() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  @JsonProperty("cancerStudyIdentifier")
  public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
    this.studyId = cancerStudyIdentifier;
  }

  public String getCustomMetaData() {
    return customMetaData;
  }

  public void setCustomMetaData(String customMetaData) {
    this.customMetaData = customMetaData;
  }

  public Integer getSampleCount() {
    return sampleCount;
  }

  public void setSampleCount(Integer sampleCount) {
    this.sampleCount = sampleCount;
  }

  public Integer getPatientCount() {
    return patientCount;
  }

  public void setPatientCount(Integer patientCount) {
    this.patientCount = patientCount;
  }
}
