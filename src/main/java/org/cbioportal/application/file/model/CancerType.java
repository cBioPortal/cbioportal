package org.cbioportal.application.file.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.function.Function;

public class CancerType implements TableRow {
  private String typeOfCancerId;
  private String name;
  private String dedicatedColor;
  private String shortName;
  private String parent;

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

  private static final LinkedHashMap<String, Function<CancerType, String>> ROW = new LinkedHashMap<>();

  static {
    ROW.put("TYPE_OF_CANCER_ID", CancerType::getTypeOfCancerId);
    ROW.put("NAME", CancerType::getName);
    ROW.put("DEDICATED_COLOR", CancerType::getDedicatedColor);
    ROW.put("PARENT", CancerType::getParent);
    ROW.put("SHORT_NAME", CancerType::getShortName);
  }

  public static SequencedSet<String> getHeader() {
    return new LinkedHashSet<>(ROW.sequencedKeySet());
  }

  @Override
  public SequencedMap<String, String> toRow() {
    LinkedHashMap<String, String> row = new LinkedHashMap<>();
    ROW.forEach((key, value) -> row.put(key, value.apply(this)));
    return row;
  }
}
