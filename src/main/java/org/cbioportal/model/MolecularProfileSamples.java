package org.cbioportal.model;

import java.io.Serializable;

public class MolecularProfileSamples implements Serializable {

    private String molecularProfileId;
    private String commaSeparatedSampleIds;
    private String[] splitSampleIds = null;

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getCommaSeparatedSampleIds() {
        return commaSeparatedSampleIds;
    }

    /**
     * Set the values for all samples.
     * 
     * @param values: string with list of values, comma (,) separated
     */
    public void setCommaSeparatedSampleIds(String commaSeparatedSampleIds) {
        this.commaSeparatedSampleIds = commaSeparatedSampleIds;
    }

    /**
     * Returns the values attribute split on (,).
     * 
     * Remembers last .split to avoid repeating this costly operation.
     * 
     * @return list of values for all samples
     */
    public String[] getSplitSampleIds() {
        if (splitSampleIds == null) {
            splitSampleIds = commaSeparatedSampleIds.split(",");
        }
        return splitSampleIds;
    }

}
