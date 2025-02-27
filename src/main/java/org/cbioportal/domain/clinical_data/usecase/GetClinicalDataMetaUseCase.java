package org.cbioportal.domain.clinical_data.usecase;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving clinical data metadata and count information.
 *
 * <p>This use case provides functionality to get the total count of clinical data records that
 * match the specified filter criteria without actually retrieving the data itself. This is useful
 * for pagination, progress indicators, and API responses that only need metadata information.
 *
 * <p>The use case transforms the multi-study filter into the appropriate format for the repository
 * layer and delegates the count operation to the clinical data repository.
 *
 * @see ClinicalDataRepository#getClinicalDataMeta(List, List, ClinicalDataType)
 * @see GetClinicalDataUseCase
 */
@Service
@Profile("clickhouse")
public class GetClinicalDataMetaUseCase {

  private final ClinicalDataRepository clinicalDataRepository;

  /**
   * Constructs a new GetClinicalDataMetaUseCase with the specified repository.
   *
   * @param clinicalDataRepository the repository used for clinical data count operations
   */
  public GetClinicalDataMetaUseCase(ClinicalDataRepository clinicalDataRepository) {
    this.clinicalDataRepository = clinicalDataRepository;
  }

  /**
   * Executes the use case to retrieve the count of clinical data records matching the filter
   * criteria.
   *
   * <p>This method transforms the provided filter into a format suitable for the repository layer
   * by combining study IDs and entity IDs into unique identifiers, then delegates to the repository
   * to perform the actual count operation.
   *
   * @param clinicalDataMultiStudyFilter filter containing patient/sample identifiers and attribute
   *     IDs
   * @param clinicalDataType type of clinical data to count (SAMPLE or PATIENT)
   * @return the total number of clinical data records matching the filter criteria
   * @throws IllegalArgumentException if filter parameters are invalid
   * @see ClinicalDataMultiStudyFilter
   * @see ClinicalDataType
   */
  public Integer execute(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
      ClinicalDataType clinicalDataType) {
    // Transform filter identifiers into unique IDs for repository layer
    List<String> uniqueIds = new ArrayList<>();
    for (ClinicalDataIdentifier identifier : clinicalDataMultiStudyFilter.getIdentifiers()) {
      uniqueIds.add(identifier.getStudyId() + '_' + identifier.getEntityId());
    }
    List<String> attributeIds = clinicalDataMultiStudyFilter.getAttributeIds();

    return clinicalDataRepository.getClinicalDataMeta(uniqueIds, attributeIds, clinicalDataType);
  }
}
