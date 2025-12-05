package org.cbioportal.legacy.model;

import java.io.Serializable;

public class MolecularDataRowPerSample implements Serializable {

  private Integer entrezGeneId;
  private String hugoGeneSymbol;
  private String value;
  private String molecularProfileId;
  private String sampleUniqueId;

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

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getMolecularProfileId() {
    return molecularProfileId;
  }

  public void setMolecularProfileId(String molecularProfileId) {
    this.molecularProfileId = molecularProfileId;
  }

  public String getSampleUniqueId() {
    return sampleUniqueId;
  }

  public void setSampleUniqueId(String sampleUniqueId) {
    this.sampleUniqueId = sampleUniqueId;
  }
}
