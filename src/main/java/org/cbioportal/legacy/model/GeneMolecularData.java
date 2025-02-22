package org.cbioportal.legacy.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

public class GeneMolecularData extends MolecularData implements Serializable {

  @NotNull private Integer entrezGeneId;
  private Gene gene;

  public Integer getEntrezGeneId() {
    return entrezGeneId;
  }

  public void setEntrezGeneId(Integer entrezGeneId) {
    this.entrezGeneId = entrezGeneId;
  }

  public Gene getGene() {
    return gene;
  }

  public void setGene(Gene gene) {
    this.gene = gene;
  }

  @Override
  public String getStableId() {
    return entrezGeneId.toString();
  }
}
