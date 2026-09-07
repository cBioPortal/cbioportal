package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.cbioportal.legacy.model.ResourceType;

@Schema(name = "ResourceDefinition", description = "Represents a resource definition")
public class ResourceDefinitionDTO {
  private String resourceId;
  private String displayName;
  private String description;
  private ResourceType resourceType;
  private String priority;
  private Boolean openByDefault;
  private String studyId;
  private String customMetaData;

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

  public ResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(ResourceType resourceType) {
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

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getCustomMetaData() {
    return customMetaData;
  }

  public void setCustomMetaData(String customMetaData) {
    this.customMetaData = customMetaData;
  }
}
