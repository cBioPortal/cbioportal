package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class ResourceData extends UniqueKeyBase implements Serializable {

  private String sampleId;
  private String patientId;
  @NotNull private String studyId;
  @NotNull private String resourceId;
  @NotNull private String url;
  private ResourceDefinition resourceDefinition;

  /**
   * The definition's URL template, from which the URL is expanded when {@link #getUrl()} is empty.
   */
  private String urlTemplate;

  /**
   * The part of the row's metadata JSON its URL template reads, for a row without a URL of its own;
   * otherwise null. Trimmed as the rows are loaded so the cached rows do not hold the rest.
   */
  private String metadata;

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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public ResourceDefinition getResourceDefinition() {
    return resourceDefinition;
  }

  public void setResourceDefinition(ResourceDefinition resourceDefinition) {
    this.resourceDefinition = resourceDefinition;
  }

  public String getUrlTemplate() {
    return urlTemplate;
  }

  public void setUrlTemplate(String urlTemplate) {
    this.urlTemplate = urlTemplate;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }
}
