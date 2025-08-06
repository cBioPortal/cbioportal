package org.cbioportal.domain.studyview;

import java.util.List;
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
import org.cbioportal.legacy.web.parameter.filter.AndedPatientTreatmentFilters;
import org.cbioportal.legacy.web.parameter.filter.AndedSampleTreatmentFilters;

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
    CategorizedGenericAssayDataCountFilter categorizedGenericAssayDataCountFilter) {

  public String[] filteredSampleIdentifiers() {
    if (sampleIdentifiers != null) {
      return sampleIdentifiers.stream()
          .map(
              sampleIdentifier ->
                  sampleIdentifier.getStudyId() + "_" + sampleIdentifier.getSampleId())
          .toArray(String[]::new);
    } else {
      return new String[0];
    }
  }

  public boolean isCategoricalClinicalDataFilter(ClinicalDataFilter clinicalDataFilter) {
    var firstValue = clinicalDataFilter.getValues().getFirst();
    var lastValue = clinicalDataFilter.getValues().getLast();
    var filterValue =
        firstValue.getStart() == null
                && firstValue.getEnd() == null
                && firstValue.getValue() == null
            ? lastValue
            : firstValue;
    return filterValue.getValue() != null;
  }
}
