package org.cbioportal.application.rest.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.cbioportal.legacy.model.GenePanelToGene;

@Schema(name = "GenePanel", description = "Represents a gene panel")
public class GenePanelDTO {
  private String genePanelId;
  @JsonIgnore private Integer internalId;
  private String description;
  private List<GenePanelToGene> genes;

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

  public List<GenePanelToGene> getGenes() {
    return genes;
  }

  public void setGenes(List<GenePanelToGene> genes) {
    this.genes = genes;
  }
}
