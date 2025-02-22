package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class GisticToGene implements Serializable {

  private Long gisticRoiId;
  @NotNull private Integer entrezGeneId;
  @NotNull private String hugoGeneSymbol;

  public Long getGisticRoiId() {
    return gisticRoiId;
  }

  public void setGisticRoiId(Long gisticRoiId) {
    this.gisticRoiId = gisticRoiId;
  }

  public Integer getEntrezGeneId() {
    return entrezGeneId;
  }

  public void setEntrezGeneId(Integer entrezGeneId) {
    this.entrezGeneId = entrezGeneId;
  }

  public String getHugoGeneSymbol() {
    return hugoGeneSymbol;
  }

  public void setHugoGeneSymbol(String hugoGeneSymbol) {
    this.hugoGeneSymbol = hugoGeneSymbol;
  }
}
