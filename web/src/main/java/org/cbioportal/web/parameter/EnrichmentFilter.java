package org.cbioportal.web.parameter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public class EnrichmentFilter {
    
    @NotNull
    @Size(min = 1)
    private List<String> alteredIds;
    @NotNull
    @Size(min = 1)
    private List<String> unalteredIds;
    private List<Integer> queryGenes;

    public List<String> getAlteredIds() {
        return alteredIds;
    }

    public void setAlteredIds(List<String> alteredIds) {
        this.alteredIds = alteredIds;
    }

    public List<String> getUnalteredIds() {
        return unalteredIds;
    }

    public void setUnalteredIds(List<String> unalteredIds) {
        this.unalteredIds = unalteredIds;
    }

	public List<Integer> getQueryGenes() {
		return queryGenes;
	}

	public void setQueryGenes(List<Integer> queryGenes) {
		this.queryGenes = queryGenes;
	}
}
