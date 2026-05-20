package org.cbioportal.infrastructure.repository.clickhouse.coexpression;

public class CoExpressionResult {

  private Integer entrezGeneId;
  private Double spearmansCorrelation;
  private Integer numSamples;

  public Integer getEntrezGeneId() {
    return entrezGeneId;
  }

  public void setEntrezGeneId(Integer entrezGeneId) {
    this.entrezGeneId = entrezGeneId;
  }

  public Double getSpearmansCorrelation() {
    return spearmansCorrelation;
  }

  public void setSpearmansCorrelation(Double spearmansCorrelation) {
    this.spearmansCorrelation = spearmansCorrelation;
  }

  public Integer getNumSamples() {
    return numSamples;
  }

  public void setNumSamples(Integer numSamples) {
    this.numSamples = numSamples;
  }
}
