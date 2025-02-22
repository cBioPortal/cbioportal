package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TypeOfCancerMixin {

  @JsonProperty("cancerTypeId")
  private String typeOfCancerId;
}
