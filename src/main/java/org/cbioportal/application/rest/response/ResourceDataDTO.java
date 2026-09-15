package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ResourceData", description = "Represents resource data")
public class ResourceDataDTO {
  private String sampleId;
  private String patientId;
  private String studyId;
  private String resourceId;
  private String url;
  private String uniqueSampleKey;
  private String uniquePatientKey;
  private ResourceDefinitionDTO resourceDefinition;

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

  public String getUniqueSampleKey() {
    return uniqueSampleKey;
  }

  public void setUniqueSampleKey(String uniqueSampleKey) {
    this.uniqueSampleKey = uniqueSampleKey;
  }

  public String getUniquePatientKey() {
    return uniquePatientKey;
  }

  public void setUniquePatientKey(String uniquePatientKey) {
    this.uniquePatientKey = uniquePatientKey;
  }

  public ResourceDefinitionDTO getResourceDefinition() {
    return resourceDefinition;
  }

  public void setResourceDefinition(ResourceDefinitionDTO resourceDefinition) {
    this.resourceDefinition = resourceDefinition;
  }
}
