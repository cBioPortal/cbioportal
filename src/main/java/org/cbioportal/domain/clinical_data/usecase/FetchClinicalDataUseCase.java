package org.cbioportal.domain.clinical_data.usecase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.legacy.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving clinical data for samples or patients from the repository.
 *
 * <p>This use case encapsulates the business logic for fetching clinical data based on the provided
 * multi-study filter and projection type. It handles the transformation of filter parameters and
 * routes requests to the appropriate repository method based on the desired level of detail.
 *
 * <p>Supported projection types:
 *
 * <ul>
 *   <li><strong>ID</strong> - Returns minimal data with only identifiers
 *   <li><strong>SUMMARY</strong> - Returns basic data including attribute values
 *   <li><strong>DETAILED</strong> - Returns complete data with clinical attribute metadata
 * </ul>
 *
 * @see ClinicalDataRepository
 * @see FetchClinicalDataMetaUseCase
 */
@Service
public class FetchClinicalDataUseCase {

  private final ClinicalDataRepository clinicalDataRepository;

  /**
   * Constructs a new GetClinicalDataUseCase with the specified repository.
   *
   * @param clinicalDataRepository the repository used for clinical data retrieval operations
   */
  public FetchClinicalDataUseCase(ClinicalDataRepository clinicalDataRepository) {
    this.clinicalDataRepository = clinicalDataRepository;
  }

  /**
   * Executes the use case to retrieve clinical data based on filter criteria and projection type.
   *
   * <p>This method transforms the provided filter into a format suitable for the repository layer
   * by combining study IDs and entity IDs into unique identifiers. It then routes the request to
   * the appropriate repository method based on the projection type to optimize data retrieval.
   *
   * <p>The projection type determines the amount of data returned:
   *
   * <ul>
   *   <li>ID projection returns only essential identifiers
   *   <li>SUMMARY projection includes attribute values
   *   <li>DETAILED projection includes complete clinical attribute metadata
   * </ul>
   *
   * @param clinicalDataMultiStudyFilter filter containing patient/sample identifiers and attribute
   *     IDs
   * @param clinicalDataType type of clinical data to retrieve (SAMPLE or PATIENT)
   * @param projectionType level of detail for the returned data
   * @return list of clinical data records matching the filter criteria, or empty list if no matches
   *     found
   * @see ProjectionType
   * @see ClinicalDataMultiStudyFilter
   * @see ClinicalDataType
   */
  public List<ClinicalData> execute(
      ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
      ClinicalDataType clinicalDataType,
      ProjectionType projectionType) {
    List<String> studyIds =
        clinicalDataMultiStudyFilter.getIdentifiers().stream()
            .map(ClinicalDataIdentifier::getStudyId)
            .distinct()
            .toList();

    // Transform filter identifiers into unique IDs for repository layer
    List<String> uniqueIds = new ArrayList<>();
    for (ClinicalDataIdentifier identifier : clinicalDataMultiStudyFilter.getIdentifiers()) {
      uniqueIds.add(identifier.getStudyId() + '_' + identifier.getEntityId());
    }
    List<String> attributeIds = clinicalDataMultiStudyFilter.getAttributeIds();

    // Route to appropriate repository method based on projection type
    return switch (projectionType) {
      case ID ->
          clinicalDataRepository.fetchClinicalDataId(
              uniqueIds, attributeIds, studyIds, clinicalDataType);
      case SUMMARY ->
          clinicalDataRepository.fetchClinicalDataSummary(
              uniqueIds, attributeIds, studyIds, clinicalDataType);
      case DETAILED ->
          clinicalDataRepository.fetchClinicalDataDetailed(
              uniqueIds, attributeIds, studyIds, clinicalDataType);
      default -> Collections.emptyList();
    };
  }
}
