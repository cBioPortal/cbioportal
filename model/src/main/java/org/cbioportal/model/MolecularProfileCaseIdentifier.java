package org.cbioportal.model;

import javax.validation.constraints.NotNull;

public class MolecularProfileCaseIdentifier {

    @NotNull
    private String molecularProfileId;
    @NotNull
    private String caseId;

    public MolecularProfileCaseIdentifier() {}

    public MolecularProfileCaseIdentifier(String caseId, String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
        this.caseId = caseId;
    }

    public String getMolecularProfileId() {
        return molecularProfileId;
    }

    public void setMolecularProfileId(String molecularProfileId) {
        this.molecularProfileId = molecularProfileId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

}
