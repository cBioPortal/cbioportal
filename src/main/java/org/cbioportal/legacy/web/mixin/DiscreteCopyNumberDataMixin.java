package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DiscreteCopyNumberDataMixin {

  @JsonProperty("namespaceColumns")
  private Object annotationJson;
}
