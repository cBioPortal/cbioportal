package org.cbioportal.model;

import java.io.Serializable;

public class GenesetAlteration implements Serializable {
    
    private String genesetId;
    private String values;

    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }
}
