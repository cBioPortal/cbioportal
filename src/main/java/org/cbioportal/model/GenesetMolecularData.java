package org.cbioportal.model;

import java.io.Serializable;

public class GenesetMolecularData extends MolecularData implements Serializable {

    private String genesetId;
    private Geneset geneset;
    
    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }
    
    public Geneset getGeneset() {
        return geneset;
    }

    public void setGeneset(Geneset geneset) {
        this.geneset = geneset;
    }

    @Override
    public String getStableId() {
        return genesetId;
    }
}
