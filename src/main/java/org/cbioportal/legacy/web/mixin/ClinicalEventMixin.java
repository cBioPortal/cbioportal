package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClinicalEventMixin {

  @JsonIgnore private Long clinicalEventId;

  @JsonProperty("startNumberOfDaysSinceDiagnosis")
  private Integer startDate;

  @JsonProperty("endNumberOfDaysSinceDiagnosis")
  private Integer stopDate;
}
