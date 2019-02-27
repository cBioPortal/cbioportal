package org.cbioportal.model;

import javax.validation.constraints.NotNull;

public class MolecularProfileCase {

    @NotNull
    private String molecularProfileId;
    @NotNull
    private String caseId;

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
