package org.cbioportal.model;

import java.io.Serializable;

public class GeneMolecularAlteration extends MolecularAlteration implements Serializable {
    
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
}
