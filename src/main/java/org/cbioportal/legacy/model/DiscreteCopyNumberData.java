package org.cbioportal.legacy.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

public class DiscreteCopyNumberData extends Alteration implements Serializable {
  @NotNull private Integer alteration;

  @JsonRawValue
  @Schema(hidden = true)
  private Object annotationJson;

  @Schema(type = "java.util.Map")
  private Map<String, Map<String, Object>> namespaceColumns;

  public Integer getAlteration() {
    return alteration;
  }

  public void setAlteration(Integer alteration) {
    this.alteration = alteration;
  }

  public Object getAnnotationJson() {
    return annotationJson;
  }

  public void setAnnotationJson(String annotationJson) {
    this.annotationJson = annotationJson;
  }

  public Map<String, Map<String, Object>> getNamespaceColumns() {
    return namespaceColumns;
  }

  public void setNamespaceColumns(Map<String, Map<String, Object>> namespaceColumns) {
    this.namespaceColumns = namespaceColumns;
  }
}
