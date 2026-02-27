package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class CancerType implements TableRow {
  private String typeOfCancerId;
  private String name;
  private String dedicatedColor;
  private String shortName;
  private String parent;
  private String version;
  private String status;
  private String history;
  private String uri;

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getDedicatedColor() {
    return dedicatedColor;
  }

  public void setDedicatedColor(String dedicatedColor) {
    this.dedicatedColor = dedicatedColor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTypeOfCancerId() {
    return typeOfCancerId;
  }

  public void setTypeOfCancerId(String typeOfCancerId) {
    this.typeOfCancerId = typeOfCancerId;
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

  @Override
  public SequencedMap<String, String> toRow() {
    var row = new LinkedHashMap<String, String>();
    row.put("TYPE_OF_CANCER_ID", typeOfCancerId);
    row.put("NAME", name);
    row.put("DEDICATED_COLOR", dedicatedColor);
    row.put("PARENT", parent);
    row.put("VERSION", version);
    row.put("STATUS", status);
    row.put("HISTORY", history);
    row.put("URI", uri);
    return row;
  }
}
