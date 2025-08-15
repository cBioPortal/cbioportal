package org.cbioportal.domain.clinical_data.usecase;

import java.util.List;
import org.cbioportal.domain.clinical_attributes.ClinicalAttribute;
import org.cbioportal.domain.clinical_attributes.usecase.GetClinicalAttributesForStudiesUseCase;
import org.cbioportal.domain.clinical_attributes.util.ClinicalAttributeUtil;
import org.cbioportal.domain.clinical_attributes.util.ClinicalAttributeUtil.CategorizedClinicalAttributeIds;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.patient.usecase.GetFilteredPatientCountUseCase;
import org.cbioportal.domain.sample.usecase.GetFilteredSamplesCountUseCase;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving and processing clinical data counts. This class orchestrates the
 * retrieval of clinical data counts from the repository, normalizes the data, and ensures that
 * missing attributes are accounted for in the result.
 */
@Service
@Profile("clickhouse")
public class GetClinicalDataCountsUseCase {

  private final ClinicalDataRepository clinicalDataRepository;
  private final GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase;
  private final GetFilteredSamplesCountUseCase getFilteredSamplesCountUseCase;
  private final GetFilteredPatientCountUseCase getFilteredPatientCountUseCase;

  /**
   * Constructs a {@code GetClinicalDataCountsUseCase} with the provided use cases and repository.
   *
   * @param clinicalDataRepository the repository to be used for retrieving clinical data counts
   * @param getClinicalAttributesForStudiesUseCase the use case for retrieving clinical attributes
   *     for studies
   * @param getFilteredSamplesCountUseCase the use case for retrieving filtered sample counts
   * @param getFilteredPatientCountUseCase the use case for retrieving filtered patient counts
   */
  public GetClinicalDataCountsUseCase(
      ClinicalDataRepository clinicalDataRepository,
      GetClinicalAttributesForStudiesUseCase getClinicalAttributesForStudiesUseCase,
      GetFilteredSamplesCountUseCase getFilteredSamplesCountUseCase,
      GetFilteredPatientCountUseCase getFilteredPatientCountUseCase) {
    this.clinicalDataRepository = clinicalDataRepository;
    this.getClinicalAttributesForStudiesUseCase = getClinicalAttributesForStudiesUseCase;
    this.getFilteredSamplesCountUseCase = getFilteredSamplesCountUseCase;
    this.getFilteredPatientCountUseCase = getFilteredPatientCountUseCase;
  }

  /**
   * Executes the use case to retrieve and process clinical data counts with unified strategy. For
   * attributes that exist at both sample and patient levels across studies, this method applies a
   * sample-priority strategy to ensure consistent semantic interpretation. It normalizes the data
   * counts and ensures that missing attributes are restored.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @param filteredAttributes a list of filtered clinical attribute IDs
   * @return a list of {@link ClinicalDataCountItem} containing the normalized and complete clinical
   *     data counts with unified sample/patient level strategy
   */
  public List<ClinicalDataCountItem> execute(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {

    List<String> involvedCancerStudies = studyViewFilterContext.customDataFilterCancerStudies();

    // Get all clinical attributes for the involved studies to determine attribute levels
    List<ClinicalAttribute> clinicalAttributes =
        getClinicalAttributesForStudiesUseCase.execute(involvedCancerStudies);

    List<ClinicalAttribute> filteredClinicalAttributes =
        clinicalAttributes.stream()
            .filter(attr -> filteredAttributes.contains(attr.attrId()))
            .toList();

    CategorizedClinicalAttributeIds categorizedAttributeIds =
        ClinicalAttributeUtil.categorizeClinicalAttributes(filteredClinicalAttributes);
    List<String> sampleAttributeIds = categorizedAttributeIds.sampleAttributeIds();
    List<String> patientAttributeIds = categorizedAttributeIds.patientAttributeIds();
    List<String> conflictingAttributeIds = categorizedAttributeIds.conflictingAttributeIds();

    var result =
        clinicalDataRepository.getClinicalDataCounts(
            studyViewFilterContext,
            sampleAttributeIds,
            patientAttributeIds,
            conflictingAttributeIds);

    // Normalize data counts so that values like TRUE, True, and true are all merged in one count
    result.forEach(
        item -> item.setCounts(StudyViewColumnarServiceUtil.normalizeDataCounts(item.getCounts())));

    // attributes may be missing in result set because they have been filtered out
    // e.g. if the filtered samples happen to have no SEX data, they will not appear in the list
    // even though the inferred value of those attributes is NA
    // the following code restores these counts for missing attributes
    if (result.size() != filteredAttributes.size()) {
      var attributes =
          getClinicalAttributesForStudiesUseCase.execute(involvedCancerStudies).stream()
              .filter(attribute -> filteredAttributes.contains(attribute.attrId()))
              .toList();

      Integer filteredSampleCount = getFilteredSamplesCountUseCase.execute(studyViewFilterContext);
      Integer filteredPatientCount = getFilteredPatientCountUseCase.execute(studyViewFilterContext);

      result =
          StudyViewColumnarServiceUtil.addClinicalDataCountsForMissingAttributes(
              result,
              org.cbioportal.legacy.service.util.ClinicalAttributeUtil
                  .convertToLegacyClinicalAttributeList(attributes),
              filteredSampleCount,
              filteredPatientCount);
    }

    return StudyViewColumnarServiceUtil.mergeClinicalDataCounts(result);
  }
}
