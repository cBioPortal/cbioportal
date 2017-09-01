package org.cbioportal.service.exception;

public class MolecularProfileNotFoundException extends Exception {

    private String molecularProfileId;

    public MolecularProfileNotFoundException(String molecularProfileId) {
        super();
        this.molecularProfileId = molecularProfileId;
    }

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }
}
