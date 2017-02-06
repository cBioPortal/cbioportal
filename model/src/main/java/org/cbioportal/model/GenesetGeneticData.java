package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class GenesetGeneticData extends GeneticData implements Serializable {

    private String genesetId;
    
    public String getGenesetId() {
        return genesetId;
    }

    public void setGenesetId(String genesetId) {
        this.genesetId = genesetId;
    }
}
