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
    
    private String[] splitValues = null;
    
    /**
     * Split on ',' of getValues(). Remembers last .split to avoid 
     * repeating this costly operation.
     * 
     * @return
     */
    public String[] getSplitValues() {
    	if (splitValues == null) {
    		splitValues = values.split(",");
    	}
    	return splitValues;
    }
}
