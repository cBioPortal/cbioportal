package org.cbioportal.web.parameter;

import java.util.List;

public class CopyNumberGeneFilter {

    private String molecularProfileId;
    private List<CopyNumberGeneFilterElement> alterations;

	public String getMolecularProfileId() {
		return molecularProfileId;
	}

	public void setMolecularProfileId(String molecularProfileId) {
		this.molecularProfileId = molecularProfileId;
	}

	public List<CopyNumberGeneFilterElement> getAlterations() {
		return alterations;
	}

	public void setAlterations(List<CopyNumberGeneFilterElement> alterations) {
		this.alterations = alterations;
	}
}
