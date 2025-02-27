package org.cbioportal.domain.clinical_data.usecase;

import java.util.List;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
import java.util.ArrayList;
import java.util.List;

/**
 * Use case for retrieving clinical data for a sample from the repository. This class encapsulates
 * the business logic for fetching clinical data based on the provided study view filter context and
 * filtered attributes.
 */
@Service
@Profile("clickhouse")
public class GetSampleClinicalDataUseCase {

  private final ClinicalDataRepository clinicalDataRepository;

  /**
   * Constructs a {@code GetSampleClinicalDataUseCase} with the provided repository.
   *
   * @param clinicalDataRepository the repository to be used for fetching sample clinical data
   */
  public GetSampleClinicalDataUseCase(ClinicalDataRepository clinicalDataRepository) {
    this.clinicalDataRepository = clinicalDataRepository;
  }

  /**
   * Executes the use case to retrieve clinical data for a sample.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @param filteredAttributes a list of attributes to filter the clinical data
   * @return a list of {@link ClinicalData} representing the sample's clinical data
   */
  public List<ClinicalData> execute(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return clinicalDataRepository.getSampleClinicalData(studyViewFilterContext, filteredAttributes);
  }

    public List<ClinicalData> execute(ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter, List<String> attributeIds, ProjectionType projectionType) {
        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        for (ClinicalDataIdentifier identifier : clinicalDataMultiStudyFilter.getIdentifiers()) {
            studyIds.add(identifier.getStudyId());
            sampleIds.add(identifier.getEntityId());
        }

        // DETAILED level
        if (projectionType == ProjectionType.DETAILED) {
            return clinicalDataRepository.getSampleClinicalDataDetailed(studyIds, sampleIds, attributeIds);
        }

        // ID or SUMMARY level
        List<ClinicalData> clinicalDataList = clinicalDataRepository.getSampleClinicalDataSummary(studyIds, sampleIds, attributeIds);
        // ID level doesn't have attrValue
        if (projectionType == ProjectionType.ID) {
            for (ClinicalData clinicalData : clinicalDataList) {
                clinicalData.setAttrValue(null);
            }
        }
        return clinicalDataList;
    }
}
