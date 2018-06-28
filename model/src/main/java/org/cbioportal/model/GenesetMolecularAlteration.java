package org.cbioportal.model;

import java.io.Serializable;

public class GenesetMolecularAlteration extends MolecularAlteration implements Serializable {
    
    private String genesetId;

    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }
}
