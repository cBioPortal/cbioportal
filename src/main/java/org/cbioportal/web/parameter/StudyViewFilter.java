package org.cbioportal.web.parameter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.GeneFilter;
import org.cbioportal.model.StudyViewStructuralVariantFilter;
import org.cbioportal.web.parameter.filter.*;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class StudyViewFilter implements Serializable {

    @Size(min = 1)
    private List<SampleIdentifier> sampleIdentifiers;
    @Size(min = 1)
    private List<String> studyIds;
    private List<ClinicalDataFilter> clinicalDataFilters;
    private List<GeneFilter> geneFilters;
    @Valid
    private List<StudyViewStructuralVariantFilter> structuralVariantFilters;
    private AndedSampleTreatmentFilters sampleTreatmentFilters;
    private AndedSampleTreatmentFilters sampleTreatmentGroupFilters;
    private AndedSampleTreatmentFilters sampleTreatmentTargetFilters;
    private AndedPatientTreatmentFilters patientTreatmentFilters;
    private AndedPatientTreatmentFilters patientTreatmentGroupFilters;
    private AndedPatientTreatmentFilters patientTreatmentTargetFilters;
	private List<List<String>> genomicProfiles;
    private List<GenomicDataFilter> genomicDataFilters;
    private List<GenericAssayDataFilter> genericAssayDataFilters;
    private List<List<String>> caseLists;
    private List<ClinicalDataFilter> customDataFilters;
    private AlterationFilter alterationFilter;
    private List<DataFilter> clinicalEventFilters;
    private List<MutationDataFilter> mutationDataFilters;
    private static boolean areBinsMerged = false;
    
    @AssertTrue
    private boolean isEitherSampleIdentifiersOrStudyIdsPresent() {
        return sampleIdentifiers != null ^ studyIds != null;
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInClinicalDataIntervalFilters() {
        return validateDataFilters(clinicalDataFilters);
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInGenomicDataIntervalFilters() {
        return validateDataFilters(genomicDataFilters);
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInGenericAssayDataIntervalFilters() {
        return validateDataFilters(genericAssayDataFilters);
    }

    @AssertTrue
    private boolean isEitherValueOrRangePresentInCustomDataFilters() {
        return validateDataFilters(customDataFilters);
    }

    private <T extends DataFilter> boolean validateDataFilters(List<T> dataFilters) {
        long invalidCount = 0;

        if (dataFilters != null) {
            invalidCount = dataFilters.stream()
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

    public List<GeneFilter> getGeneFilters() {
        return geneFilters;
    }

    public void setGeneFilters(List<GeneFilter> geneFilters) {
        this.geneFilters = geneFilters;
    }

    public List<StudyViewStructuralVariantFilter> getStructuralVariantFilters() {
        return structuralVariantFilters;
    }

    public void setStructuralVariantFilters(List<StudyViewStructuralVariantFilter> structuralVariantFilters) {
        this.structuralVariantFilters = structuralVariantFilters;
    }

    public List<List<String>> getGenomicProfiles() {
        return genomicProfiles;
    }

    public void setGenomicProfiles(List<List<String>> genomicProfiles) {
        this.genomicProfiles = genomicProfiles;
    }

    public List<GenomicDataFilter> getGenomicDataFilters() {
        return genomicDataFilters;
    }

    public void setGenomicDataFilters(List<GenomicDataFilter> genomicDataFilters) {
        this.genomicDataFilters = genomicDataFilters;
    }

    public AndedSampleTreatmentFilters getSampleTreatmentFilters() {
        return sampleTreatmentFilters;
    }

    public void setSampleTreatmentFilters(AndedSampleTreatmentFilters sampleTreatmentFilters) {
        this.sampleTreatmentFilters = sampleTreatmentFilters;
    }

    public AndedPatientTreatmentFilters getPatientTreatmentFilters() {
        return patientTreatmentFilters;
    }

    public void setPatientTreatmentFilters(AndedPatientTreatmentFilters patientTreatmentFilters) {
        this.patientTreatmentFilters = patientTreatmentFilters;
    }

    public List<List<String>> getCaseLists() {
        return caseLists;
    }

    public void setCaseLists(List<List<String>> caseLists) {
        this.caseLists = caseLists;
    }

	public List<GenericAssayDataFilter> getGenericAssayDataFilters() {
		return genericAssayDataFilters;
	}

	public void setGenericAssayDataFilters(List<GenericAssayDataFilter> genericAssayDataFilters) {
		this.genericAssayDataFilters = genericAssayDataFilters;
	}

    public List<ClinicalDataFilter> getCustomDataFilters() {
        return customDataFilters;
    }

    public void setCustomDataFilters(List<ClinicalDataFilter> customDataFilters) {
        this.customDataFilters = customDataFilters;
    }

    public AlterationFilter getAlterationFilter() {
        return alterationFilter;
    }

    public void setAlterationFilter(AlterationFilter alterationFilter) {
        this.alterationFilter = alterationFilter;
    }

    public AndedSampleTreatmentFilters getSampleTreatmentGroupFilters() {
        return sampleTreatmentGroupFilters;
    }

    public void setSampleTreatmentGroupFilters(AndedSampleTreatmentFilters sampleTreatmentGroupFilters) {
        this.sampleTreatmentGroupFilters = sampleTreatmentGroupFilters;
    }

    public AndedPatientTreatmentFilters getPatientTreatmentGroupFilters() {
        return patientTreatmentGroupFilters;
    }

    public void setPatientTreatmentGroupFilters(AndedPatientTreatmentFilters patientTreatmentGroupFilters) {
        this.patientTreatmentGroupFilters = patientTreatmentGroupFilters;
    }

    public AndedSampleTreatmentFilters getSampleTreatmentTargetFilters() {
        return sampleTreatmentTargetFilters;
    }

    public void setSampleTreatmentTargetFilters(AndedSampleTreatmentFilters sampleTreatmentTargetFilters) {
        this.sampleTreatmentTargetFilters = sampleTreatmentTargetFilters;
    }

    public AndedPatientTreatmentFilters getPatientTreatmentTargetFilters() {
        return patientTreatmentTargetFilters;
    }

    public void setPatientTreatmentTargetFilters(AndedPatientTreatmentFilters patientTreatmentTagetFilters) {
        this.patientTreatmentTargetFilters = patientTreatmentTagetFilters;
    }

    public List<DataFilter> getClinicalEventFilters() {
        return clinicalEventFilters;
    }

    public void setClinicalEventFilters(List<DataFilter> clinicalEventFilters) {
        this.clinicalEventFilters = clinicalEventFilters;
    }

    public List<MutationDataFilter> getMutationDataFilters() { return mutationDataFilters; }

    public void setMutationDataFilters(List<MutationDataFilter> mutationDataFilters) {
        this.mutationDataFilters = mutationDataFilters;
    }
    
    /**
     * Merge the range of numerical values in DataFilters to reduce the number of scans that runs on the database.
     * Variable 'areBinsMerged' is static so this method only gets run once.
     */
    public void mergeDataFilterNumericalValues() {
        if (areBinsMerged || this.genomicDataFilters == null || this.genomicDataFilters.isEmpty()) return;
        
        List<GenomicDataFilter> mergedGenomicDataFilters = new ArrayList<>();
        
        for (GenomicDataFilter genomicDataFilter : this.genomicDataFilters) {
            GenomicDataFilter mergedGenomicDataFilter = new GenomicDataFilter(genomicDataFilter.getHugoGeneSymbol(), genomicDataFilter.getProfileType());
            List<DataFilterValue> mergedValues = new ArrayList<>();
            
            boolean hasNullStart = false, hasNullEnd = false;
            BigDecimal mergedStart = null, mergedEnd = null;
            for (DataFilterValue dataFilterValue : genomicDataFilter.getValues()) {
                // filter non-numerical values and keep them intact
                if (dataFilterValue.getValue() != null) {
                    mergedValues.add(dataFilterValue);
                }
                // record if numerical values have null start or end, otherwise record their start-end range
                else {
                    if (dataFilterValue.getStart() == null) hasNullStart = true;
                    else if (mergedStart == null) mergedStart = dataFilterValue.getStart();
                    else if (dataFilterValue.getStart().compareTo(mergedStart) < 0) mergedStart = dataFilterValue.getStart();
                    if (dataFilterValue.getEnd() == null) hasNullEnd = true;
                    else if (mergedEnd == null) mergedEnd = dataFilterValue.getEnd();
                    else if (dataFilterValue.getEnd().compareTo(mergedEnd) > 0) mergedEnd = dataFilterValue.getEnd();
                }
            }
            if (hasNullStart) mergedStart = null;
            if (hasNullEnd) mergedEnd = null;
            
            mergedValues.add(new DataFilterValue(mergedStart, mergedEnd, null));
            mergedGenomicDataFilter.setValues(mergedValues);
            mergedGenomicDataFilters.add(mergedGenomicDataFilter);
        }
        
        this.genomicDataFilters = mergedGenomicDataFilters;
        areBinsMerged = true;
    }
}
