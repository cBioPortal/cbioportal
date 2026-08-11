package org.cbioportal.legacy.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

public class ClinicalDataCount implements Serializable {

  @Schema(hidden = true)
  private String attributeId;

  private String value;
  private Integer count;

  public String getAttributeId() {
    return attributeId;
  }

  public void setAttributeId(String attributeId) {
    this.attributeId = attributeId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }
}
