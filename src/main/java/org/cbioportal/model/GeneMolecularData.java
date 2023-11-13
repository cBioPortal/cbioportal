package org.cbioportal.model;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class GeneMolecularData extends MolecularData implements Serializable {

    @NotNull
    private Integer entrezGeneId;
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
