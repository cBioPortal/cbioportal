package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.legacy.model.Patient;

public class SampleMixin {

  @JsonIgnore private Integer internalId;

  @JsonProperty("sampleId")
  private String stableId;

  @JsonIgnore private Integer patientId;

  @JsonProperty("patientId")
  private String patientStableId;

  @JsonIgnore private Patient patient;

  @JsonProperty("studyId")
  private String cancerStudyIdentifier;
}
