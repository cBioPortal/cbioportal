package org.cbioportal.web.parameter;

import java.util.List;

public class StudyViewFilter {

	private List<String> sampleIds;
    private List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters;
	private List<MutationGeneFilter> mutatedGenes;
	private List<CopyNumberGeneFilter> cnaGenes;
	
	public List<String> getSampleIds() {
		return sampleIds;
	}

	public void setSampleIds(List<String> sampleIds) {
		this.sampleIds = sampleIds;
	}

	public List<ClinicalDataEqualityFilter> getClinicalDataEqualityFilters() {
		return clinicalDataEqualityFilters;
	}

	public void setClinicalDataEqualityFilters(List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters) {
		this.clinicalDataEqualityFilters = clinicalDataEqualityFilters;
	}

	public List<MutationGeneFilter> getMutatedGenes() {
		return mutatedGenes;
	}

	public void setMutatedGenes(List<MutationGeneFilter> mutatedGenes) {
		this.mutatedGenes = mutatedGenes;
	}

	public List<CopyNumberGeneFilter> getCnaGenes() {
		return cnaGenes;
	}

	public void setCnaGenes(List<CopyNumberGeneFilter> cnaGenes) {
		this.cnaGenes = cnaGenes;
	}
}
