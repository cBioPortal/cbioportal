package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class ResourceData extends UniqueKeyBase implements Serializable {

  private Long id;
  private String sampleId;
  private String patientId;
  @NotNull private String studyId;
  @NotNull private String resourceId;
  private Long parentId;
  @NotNull private NodeType nodeType;
  @NotNull private ResourceType entityType;
  @NotNull private String displayName;
  private String url;
  private String type;
  private String metadata;
  private Integer priority;
  private ResourceDefinition resourceDefinition;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getStudyId() {
    return studyId;
  }

  public void setStudyId(String studyId) {
    this.studyId = studyId;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  public Long getParentId() {
    return parentId;
  }

  public void setParentId(Long parentId) {
    this.parentId = parentId;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  public ResourceType getEntityType() {
    return entityType;
  }

  public void setEntityType(ResourceType entityType) {
    this.entityType = entityType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public ResourceDefinition getResourceDefinition() {
    return resourceDefinition;
  }

  public void setResourceDefinition(ResourceDefinition resourceDefinition) {
    this.resourceDefinition = resourceDefinition;
  }
}
