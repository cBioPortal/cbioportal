package org.cbioportal.model;

import java.io.Serializable;

public abstract class MolecularAlteration implements Serializable {
    
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
     * Returns the values attribute split on (,).
     * 
     * Remembers last .split to avoid repeating this costly operation.
     * 
     * @return list of values for all samples
     */
    public String[] getSplitValues() {
        if (splitValues == null) {
            // Use Integer.MIN_VALUE to return empty string for empty when
            // trailing ,,,,
            splitValues = values.split(",", Integer.MIN_VALUE);
        }
        return splitValues;
    }

    public abstract String getStableId();
}
