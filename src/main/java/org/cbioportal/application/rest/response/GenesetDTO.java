package org.cbioportal.application.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Geneset", description = "Represents a gene set")
public class GenesetDTO {
  private String genesetId;
  private String name;
  private String description;
  private String refLink;
  private Double representativeScore;
  private Double representativePvalue;

  public String getGenesetId() {
    return genesetId;
  }

  public void setGenesetId(String genesetId) {
    this.genesetId = genesetId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRefLink() {
    return refLink;
  }

  public void setRefLink(String refLink) {
    this.refLink = refLink;
  }

  public Double getRepresentativeScore() {
    return representativeScore;
  }

  public void setRepresentativeScore(Double representativeScore) {
    this.representativeScore = representativeScore;
  }

  public Double getRepresentativePvalue() {
    return representativePvalue;
  }

  public void setRepresentativePvalue(Double representativePvalue) {
    this.representativePvalue = representativePvalue;
  }
}
