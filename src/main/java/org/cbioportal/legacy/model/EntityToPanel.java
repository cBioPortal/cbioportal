package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class EntityToPanel implements Serializable {

  @NotNull private String entityUniqueId;
  @NotNull private String genePanelId;
  private String geneticProfileId;

  public String getEntityUniqueId() {
    return entityUniqueId;
  }

  public void setEntityUniqueId(String entityUniqueId) {
    this.entityUniqueId = entityUniqueId;
  }

  public String getGenePanelId() {
    return genePanelId;
  }

  public void setGenePanelId(String genePanelId) {
    this.genePanelId = genePanelId;
  }

  public String getGeneticProfileId() {
    return geneticProfileId;
  }

  public void setGeneticProfileId(String geneticProfileId) {
    this.geneticProfileId = geneticProfileId;
  }
}
