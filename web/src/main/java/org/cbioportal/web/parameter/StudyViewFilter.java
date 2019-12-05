package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class StudyViewFilter implements Serializable {

    @Size(min = 1)
    private List<SampleIdentifier> sampleIdentifiers;
    @Size(min = 1)
    private List<String> studyIds;
    private List<ClinicalDataFilter> clinicalDataFilters;
    private List<MutationGeneFilter> mutatedGenes;
    private List<FusionGeneFilter> fusionGenes;
	private List<CopyNumberGeneFilter> cnaGenes;
	private Boolean withMutationData;
	private Boolean withCNAData;

    @AssertTrue
    private boolean isEitherSampleIdentifiersOrStudyIdsPresent() {
        return sampleIdentifiers != null ^ studyIds != null;
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInClinicalDataIntervalFilters() {
        long invalidCount = 0;

        if (clinicalDataFilters != null) {
            invalidCount = clinicalDataFilters.stream()
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
    
    public List<ClinicalDataFilter> getClinicalDataFilters() {
        return clinicalDataFilters;
    }

    public void setClinicalDataFilters(List<ClinicalDataFilter> clinicalDataFilters) {
        this.clinicalDataFilters = clinicalDataFilters;
    }

    public List<MutationGeneFilter> getMutatedGenes() {
        return mutatedGenes;
    }

	public void setMutatedGenes(List<MutationGeneFilter> mutatedGenes) { this.mutatedGenes = mutatedGenes; }

    public List<FusionGeneFilter> getFusionGenes() {
        return fusionGenes;
    }

    public void setFusionGenes(List<FusionGeneFilter> fusionGenes) {
        this.fusionGenes = fusionGenes;
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
}
