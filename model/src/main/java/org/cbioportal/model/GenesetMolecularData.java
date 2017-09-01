package org.cbioportal.model;

import java.io.Serializable;

public class GenesetMolecularData extends MolecularData implements Serializable {

    private String genesetId;
    
    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }
}
