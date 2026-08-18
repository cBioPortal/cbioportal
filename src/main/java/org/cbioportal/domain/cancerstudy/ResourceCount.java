package org.cbioportal.domain.cancerstudy;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public record ResourceCount(
    String resourceId,
    String displayName,
    String description,
    String resourceType,
    String priority,
    Boolean openByDefault,
    String cancerStudyIdentifier,
    String customMetaData,
    Integer sampleCount,
    Integer patientCount)
    implements Serializable {

  @JsonProperty("studyId")
  public String studyId() {
    return cancerStudyIdentifier;
  }
}
