package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class TypeOfCancer implements Serializable {

  @NotNull private String typeOfCancerId;
  private String name;
  private String dedicatedColor;
  private String shortName;
  private String parent;
  private String version;
  private String status;
  private String history;
  private String uri;

  public String getTypeOfCancerId() {
    return typeOfCancerId;
  }

  public void setTypeOfCancerId(String typeOfCancerId) {
    this.typeOfCancerId = typeOfCancerId;
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

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
