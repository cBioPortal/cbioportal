package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.legacy.model.CancerStudy;

public class SampleListMixin {

  @JsonIgnore private Integer listId;

  @JsonProperty("sampleListId")
  private String stableId;

  @JsonIgnore private Integer cancerStudyId;

  @JsonProperty("studyId")
  private String cancerStudyIdentifier;

  @JsonIgnore private CancerStudy cancerStudy;
}
