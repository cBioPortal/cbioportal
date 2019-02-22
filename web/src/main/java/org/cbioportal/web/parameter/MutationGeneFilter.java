package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;

public class MutationGeneFilter implements Serializable {

    private List<Integer> entrezGeneIds;

	public List<Integer> getEntrezGeneIds() {
		return entrezGeneIds;
	}

	public void setEntrezGeneIds(List<Integer> entrezGeneIds) {
		this.entrezGeneIds = entrezGeneIds;
	}
}
