package org.cbioportal.web.parameter;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

public class StudyViewFilter {

	@Size(min = 1)
	private List<SampleIdentifier> sampleIdentifiers;
	@Size(min = 1)
	private List<String> studyIds;
    private List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters;
    private List<ClinicalDataIntervalFilter> clinicalDataIntervalFilters;
	private List<MutationGeneFilter> mutatedGenes;
	private List<CopyNumberGeneFilter> cnaGenes;
	private Boolean withMutationData;
	private Boolean withCNAData;
	private RectangleBounds mutationCountVsCNASelection;

	@AssertTrue
    private boolean isEitherSampleIdentifiersOrStudyIdsPresent() {
        return sampleIdentifiers != null ^ studyIds != null;
    }

	@AssertTrue
    private boolean isEitherValueOrRangePresentInClinicalDataIntervalFilters() {
        long invalidCount = 0;

        if (clinicalDataIntervalFilters != null) {
            invalidCount = clinicalDataIntervalFilters.stream()
                .flatMap(f -> f.getValues().stream())
                .filter(Objects::nonNull)
                .filter(v -> v.getValue() != null == (v.getStart() != null || v.getEnd() != null))
                .count();
        }

        return invalidCount == 0;
    }
	
	public List<SampleIdentifier> getSampleIdentifiers() {
		return sampleIdentifiers;
	}

	public void setSampleIdentifiers(List<SampleIdentifier> sampleIdentifiers) {
		this.sampleIdentifiers = sampleIdentifiers;
	}

	public List<String> getStudyIds() {
		return studyIds;
	}

	public void setStudyIds(List<String> studyIds) {
		this.studyIds = studyIds;
	}

	public List<ClinicalDataEqualityFilter> getClinicalDataEqualityFilters() {
		return clinicalDataEqualityFilters;
	}

	public void setClinicalDataEqualityFilters(List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters) {
		this.clinicalDataEqualityFilters = clinicalDataEqualityFilters;
	}

    public List<ClinicalDataIntervalFilter> getClinicalDataIntervalFilters() {
        return clinicalDataIntervalFilters;
    }

    public void setClinicalDataIntervalFilters(List<ClinicalDataIntervalFilter> clinicalDataIntervalFilters) {
        this.clinicalDataIntervalFilters = clinicalDataIntervalFilters;
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

	public Boolean getWithMutationData() {
		return withMutationData;
	}

	public void setWithMutationData(Boolean withMutationData) {
		this.withMutationData = withMutationData;
	}

	public Boolean getWithCNAData() {
		return withCNAData;
	}

	public void setWithCNAData(Boolean withCNAData) {
		this.withCNAData = withCNAData;
	}

	public RectangleBounds getMutationCountVsCNASelection() {
		return mutationCountVsCNASelection;
	}

	public void setMutationCountVsCNASelection(RectangleBounds mutationCountVsCNASelection) {
		this.mutationCountVsCNASelection = mutationCountVsCNASelection;
	}
}
