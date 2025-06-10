package org.cbioportal.application.file.model;

public class GeneticEntity {

  private Integer geneticEntityId;
  private String stableId;
  private String entityType;

  public Integer getGeneticEntityId() {
    return geneticEntityId;
  }

  public void setGeneticEntityId(Integer geneticEntityId) {
    this.geneticEntityId = geneticEntityId;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public String getStableId() {
    return stableId;
  }

  public void setStableId(String stableId) {
    this.stableId = stableId;
  }
}
