package org.cbioportal.domain.clinical_data.usecase;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
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

  private final ClinicalDataRepository clinicalDataRepository;

  /**
   * Constructs a {@code GetClinicalDataUseCase} with the provided use cases.
   *
   * @param clinicalDataRepository the repository to be used for fetching clinical data
   */
  public GetClinicalDataUseCase(ClinicalDataRepository clinicalDataRepository) {
    this.clinicalDataRepository = clinicalDataRepository;
  }

  /**
   * Executes the use case to retrieve clinical data for a sample.
   *
   * @return a list of {@link ClinicalData} representing the sample's clinical data
   */
  public List<ClinicalData> execute(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
      ClinicalDataType clinicalDataType,
      ProjectionType projectionType) {
    List<String> uniqueIds = new ArrayList<>();
    List<String> attributeIds = clinicalDataMultiStudyFilter.getAttributeIds();
    for (ClinicalDataIdentifier identifier : clinicalDataMultiStudyFilter.getIdentifiers()) {
      uniqueIds.add(identifier.getStudyId() + '_' + identifier.getEntityId());
    }

    return clinicalDataRepository.getClinicalData(
        uniqueIds, attributeIds, clinicalDataType, projectionType);
  }
}
