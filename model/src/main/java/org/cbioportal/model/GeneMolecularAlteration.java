package org.cbioportal.model;

import java.io.Serializable;

public class GeneMolecularAlteration extends MolecularAlteration implements Serializable {
    
    private String molecularProfileId;
    private Integer entrezGeneId;
    private Gene gene;

	public String getMolecularProfileId() {
		return molecularProfileId;
	}

	public void setMolecularProfileId(String molecularProfileId) {
		this.molecularProfileId = molecularProfileId;
	}

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
