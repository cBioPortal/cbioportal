package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GenePanelToGene", description = "Represents a gene within a gene panel")
public class GenePanelToGeneDTO {
  private Integer entrezGeneId;
  private String hugoGeneSymbol;

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
