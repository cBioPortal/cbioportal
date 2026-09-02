package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "GenePanel", description = "Represents a gene panel")
public class GenePanelDTO {
  private String genePanelId;
  @JsonIgnore private Integer internalId;
  private String description;
  private List<GenePanelToGeneDTO> genes;

  public String getGenePanelId() {
    return genePanelId;
  }

  public void setGenePanelId(String genePanelId) {
    this.genePanelId = genePanelId;
  }

  public Integer getInternalId() {
    return internalId;
  }

  public void setInternalId(Integer internalId) {
    this.internalId = internalId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<GenePanelToGeneDTO> getGenes() {
    return genes;
  }

  public void setGenes(List<GenePanelToGeneDTO> genes) {
    this.genes = genes;
  }
}
