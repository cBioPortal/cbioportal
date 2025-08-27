package org.cbioportal.domain.clinical_data.usecase;

import java.util.List;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalData;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving clinical data for a sample from the repository. This class encapsulates
 * the business logic for fetching clinical data based on the provided study view filter context and
 * filtered attributes.
 */
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
}
