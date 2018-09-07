package org.cbioportal.web.parameter;

import java.util.List;

public class CopyNumberGeneFilter {

    private List<CopyNumberGeneFilterElement> alterations;

	public List<CopyNumberGeneFilterElement> getAlterations() {
		return alterations;
	}

	public void setAlterations(List<CopyNumberGeneFilterElement> alterations) {
		this.alterations = alterations;
	}
}
