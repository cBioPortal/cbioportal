package org.cbioportal.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class MolecularProfileCaseIdentifier implements Serializable, Comparable<MolecularProfileCaseIdentifier> {

    @NotNull
    private String molecularProfileId;
    @NotNull
    private String caseId;
    // This field isn't included in the equals / hash code logic. The study ID is contained in the
    // molecular profile ID, so that'd be redundant. This field exists just so we don't have to
    // parse our own data classes.
    private String studyStableId;

    public MolecularProfileCaseIdentifier() {}

    public MolecularProfileCaseIdentifier(String caseId, String molecularProfileId) {
        this(caseId, molecularProfileId, null);
    }

    public MolecularProfileCaseIdentifier(String caseId, String molecularProfileId, String studyStableId) {
        this.molecularProfileId = molecularProfileId;
        this.caseId = caseId;
        this.studyStableId = studyStableId;
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

    public String getStudyStableId() {
        return studyStableId;
    }

    public void setStudyStableId(String studyStableId) {
        this.studyStableId = studyStableId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((caseId == null) ? 0 : caseId.hashCode());
        result = prime * result + ((molecularProfileId == null) ? 0 : molecularProfileId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MolecularProfileCaseIdentifier other = (MolecularProfileCaseIdentifier) obj;
        if (caseId == null) {
            if (other.caseId != null)
                return false;
        } else if (!caseId.equals(other.caseId))
            return false;
        if (molecularProfileId == null) {
            if (other.molecularProfileId != null)
                return false;
        } else if (!molecularProfileId.equals(other.molecularProfileId))
            return false;
        return true;
    }

    @Override
    public int compareTo(MolecularProfileCaseIdentifier o) {
        if (molecularProfileId.compareTo(o.molecularProfileId) == 0) {
            return caseId.compareTo(o.caseId);
        } else {
            return molecularProfileId.compareTo(o.molecularProfileId);
        }
    }
}
