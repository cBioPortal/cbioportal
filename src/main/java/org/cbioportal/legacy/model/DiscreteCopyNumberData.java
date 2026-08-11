package org.cbioportal.legacy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

public class DiscreteCopyNumberData extends Alteration implements Serializable {
  private static final ObjectMapper ANNOTATION_JSON_MAPPER = new ObjectMapper();

  @NotNull private Integer alteration;

  @JsonIgnore
  @Schema(hidden = true)
  private String annotationJson;

  @JsonProperty
  @Schema(type = "object", description = "Custom columns from annotation namespaces")
  private Map<String, Map<String, Object>> namespaceColumns;

  public Integer getAlteration() {
    return alteration;
  }

  public void setAlteration(Integer alteration) {
    this.alteration = alteration;
  }

  public String getAnnotationJson() {
    return annotationJson;
  }

  public void setAnnotationJson(String annotationJson) {
    this.annotationJson = annotationJson;
  }

  public Map<String, Map<String, Object>> getNamespaceColumns() {
    if (namespaceColumns == null && annotationJson != null) {
      try {
        namespaceColumns =
            ANNOTATION_JSON_MAPPER.readValue(
                annotationJson, new TypeReference<Map<String, Map<String, Object>>>() {});
      } catch (Exception ignored) {
        return null;
      }
    }
    return namespaceColumns;
  }

  public void setNamespaceColumns(Map<String, Map<String, Object>> namespaceColumns) {
    this.namespaceColumns = namespaceColumns;
  }
}
