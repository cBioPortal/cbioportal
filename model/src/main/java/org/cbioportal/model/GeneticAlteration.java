package org.cbioportal.model;

import java.io.Serializable;

public abstract class GeneticAlteration implements Serializable {
    
    private String values;
    private String[] splitValues = null;

    /**
     * Set the values for all samples.
     * 
     * @param values: string with list of values, comma (,) separated
     */
    public void setValues(String values) {
        this.values = values;
    }
    
    /**
     * Returns the same as getValues(), but already split on (,). 
     * 
     * Remembers last .split to avoid repeating this costly operation.
     * 
     * @return list of values for all samples
     */
    public String[] getSplitValues() {
    	if (splitValues == null) {
    		splitValues = values.split(",");
    	}
    	return splitValues;
    }
}
