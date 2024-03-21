package org.cbioportal.model;

import java.io.Serializable;

public class GenericAssayMolecularAlteration extends MolecularAlteration implements Serializable {
    
    private String molecularProfileId;
    private String genericAssayStableId;

    /**
     * @return the genericAssayStableId
     */
    public String getGenericAssayStableId() {
        return genericAssayStableId;
    }

    /**
     * @param genericAssayStableId the genericAssayStableId to set
     */
    public void setGenericAssayStableId(String genericAssayStableId) {
        this.genericAssayStableId = genericAssayStableId;
    }

    /**
     * @return the molecularProfileId
     */
    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    /**
     * @param molecularProfileId the molecularProfileId to set
     */
    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

	@Override
	public String getStableId() {
		return genericAssayStableId;
	}
}
