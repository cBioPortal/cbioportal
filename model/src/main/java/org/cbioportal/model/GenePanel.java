package org.cbioportal.model;

import java.io.Serializable;
import java.util.List;
import jakarta.validation.constraints.NotNull;

public class GenePanel implements Serializable {
 
    private Integer internalId;
    @NotNull
    private String stableId;
    private String description;
    private List<GenePanelToGene> genes;

    public Integer getInternalId() {
        return internalId;
    }

    public void setInternalId(Integer internalId) {
        this.internalId = internalId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
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
