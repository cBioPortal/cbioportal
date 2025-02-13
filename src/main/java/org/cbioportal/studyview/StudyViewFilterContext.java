package org.cbioportal.studyview;

import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.GeneFilter;
import org.cbioportal.legacy.model.StudyViewStructuralVariantFilter;
import org.cbioportal.legacy.web.parameter.CategorizedGenericAssayDataCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataFilter;
import org.cbioportal.legacy.web.parameter.CustomSampleIdentifier;
import org.cbioportal.legacy.web.parameter.DataFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.legacy.web.parameter.MutationDataFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.cbioportal.legacy.web.parameter.filter.AndedSampleTreatmentFilters;
import org.cbioportal.legacy.web.parameter.filter.AndedPatientTreatmentFilters;

import java.util.List;

public record StudyViewFilterContext(
        List<SampleIdentifier> sampleIdentifiers,
        List<String> studyIds,
        List<ClinicalDataFilter> clinicalDataFilters,
        List<GeneFilter> geneFilters,
        List<StudyViewStructuralVariantFilter> structuralVariantFilters,
        AndedSampleTreatmentFilters sampleTreatmentFilters,
        AndedSampleTreatmentFilters sampleTreatmentGroupFilters,
        AndedSampleTreatmentFilters sampleTreatmentTargetFilters,
        AndedPatientTreatmentFilters patientTreatmentFilters,
        AndedPatientTreatmentFilters patientTreatmentGroupFilters,
        AndedPatientTreatmentFilters patientTreatmentTargetFilters,
        List<List<String>> genomicProfiles,
        List<GenomicDataFilter> genomicDataFilters,
        List<GenericAssayDataFilter> genericAssayDataFilters,
        List<List<String>> caseLists,
        List<ClinicalDataFilter> customDataFilters,
        AlterationFilter alterationFilter,
        List<DataFilter> clinicalEventFilters,
        List<MutationDataFilter> mutationDataFilters,
        List<CustomSampleIdentifier> customSampleIdentifiers,
        List<String> customDataFilterCancerStudies,
        CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter
) {

    public String[] filteredSampleIdentifiers() {
        if (sampleIdentifiers != null) {
            return sampleIdentifiers.stream()
                    .map(sampleIdentifier -> sampleIdentifier.getStudyId() + "_" + sampleIdentifier.getSampleId())
                    .toArray(String[]::new);
        } else {
            return new String[0];
        }
    }

    public boolean isCategoricalClinicalDataFilter(ClinicalDataFilter clinicalDataFilter) {
        var filterValue = clinicalDataFilter.getValues().getFirst();
        return filterValue.getValue() != null;
    }
}
