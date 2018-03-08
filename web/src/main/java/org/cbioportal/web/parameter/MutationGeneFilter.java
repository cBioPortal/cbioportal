package org.cbioportal.web.parameter;

import java.util.List;

public class MutationGeneFilter {

    private String molecularProfileId;
    private List<Integer> entrezGeneIds;

	public String getMolecularProfileId() {
		return molecularProfileId;
	}

	public void setMolecularProfileId(String molecularProfileId) {
		this.molecularProfileId = molecularProfileId;
	}

	public List<Integer> getEntrezGeneIds() {
		return entrezGeneIds;
	}

	public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
		this.entrezGeneIds = entrezGeneIds;
	}
}
