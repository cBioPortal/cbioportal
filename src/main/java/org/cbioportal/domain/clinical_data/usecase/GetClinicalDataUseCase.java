package org.cbioportal.domain.clinical_data.usecase;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.shared.enums.ClinicalDataType;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving clinical data for samples or patients from the repository. This class
 * encapsulates the business logic for fetching clinical data based on the provided study view
 * filter context and filtered attributes.
 */
@Service
@Profile("clickhouse")
public class GetClinicalDataUseCase {

  private final GetPatientClinicalDataUseCase getPatientClinicalDataUseCase;
  private final GetSampleClinicalDataUseCase getSampleClinicalDataUseCase;

  /**
   * Constructs a {@code GetClinicalDataUseCase} with the provided use cases.
   *
   * @param getPatientClinicalDataUseCase the use case for retrieving patient clinical data
   * @param getSampleClinicalDataUseCase the use case for retrieving sample clinical data
   */
  public GetClinicalDataUseCase(
      GetPatientClinicalDataUseCase getPatientClinicalDataUseCase,
      GetSampleClinicalDataUseCase getSampleClinicalDataUseCase) {
    this.getPatientClinicalDataUseCase = getPatientClinicalDataUseCase;
    this.getSampleClinicalDataUseCase = getSampleClinicalDataUseCase;
  }

  /**
   * Executes the use case to retrieve clinical data for a sample.
   *
   * @param attributeIds a list of attribute IDs to filter the clinical data
   * @return a list of {@link ClinicalData} representing the sample's clinical data
   */
  public List<ClinicalData> execute(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
      List<String> attributeIds,
      ClinicalDataType clinicalDataType,
      ProjectionType projectionType) {
    return switch (clinicalDataType) {
      case SAMPLE ->
          getSampleClinicalDataUseCase.execute(
              clinicalDataMultiStudyFilter, attributeIds, projectionType);
      case PATIENT ->
          getPatientClinicalDataUseCase.execute(
              clinicalDataMultiStudyFilter, attributeIds, projectionType);
    };
  }
}
