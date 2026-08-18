package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TypeOfCancer", description = "Represents a cancer type")
public class TypeOfCancerDTO {
  private String cancerTypeId;
  private String name;
  private String dedicatedColor;
  private String shortName;
  private String parent;

  public String getCancerTypeId() {
    return cancerTypeId;
  }

  /** Legacy alias — the embedded TypeOfCancer in master serialised as {@code typeOfCancerId}. */
  @JsonProperty("typeOfCancerId")
  public String getTypeOfCancerId() {
    return cancerTypeId;
  }

  /** Alias used by the domain-model (CancerStudyMetadataDTO) shape. */
  @JsonProperty("id")
  public String getId() {
    return cancerTypeId;
  }

  public void setCancerTypeId(String cancerTypeId) {
    this.cancerTypeId = cancerTypeId;
  }

  @JsonProperty("typeOfCancerId")
  public void setTypeOfCancerId(String typeOfCancerId) {
    this.cancerTypeId = typeOfCancerId;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.cancerTypeId = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDedicatedColor() {
    return dedicatedColor;
  }

  public void setDedicatedColor(String dedicatedColor) {
    this.dedicatedColor = dedicatedColor;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }
}
