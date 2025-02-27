package org.cbioportal.domain.clinical_data.repository;

import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCountItem;

/** Repository interface for retrieving clinical data related to patients and samples. */
public interface ClinicalDataRepository {

  /**
   * Retrieves clinical data for patients based on the given study view filter context and filtered
   * attributes.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param filteredAttributes A list of attributes to filter the clinical data.
   * @return A list of {@link ClinicalData} representing patient clinical data.
   */
  List<ClinicalData> getPatientClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

  /**
   * Retrieves clinical data for samples based on the given study view filter context and filtered
   * attributes.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param filteredAttributes A list of attributes to filter the clinical data.
   * @return A list of {@link ClinicalData} representing sample clinical data.
   */
  List<ClinicalData> getSampleClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes);

  /**
   * Retrieves counts of clinical data records based on the given study view filter context and
   * filtered attributes.
   *
   * @param studyViewFilterContext The filter criteria for the study view.
   * @param sampleAttributeIds A list of sample prioritized attributes to filter the clinical data.
   * @param patientAttributeIds A list of patient attributes to filter the clinical data.
   * @return A list of {@link ClinicalDataCountItem} representing clinical data counts.
   */
  List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds);

  /**
   * Retrieves counts of clinical data records for enrichment analysis. Takes both sample and
   * patient unique identifiers directly to avoid additional database queries for ID mapping.
   *
   * @param sampleUniqueIds list of sample unique identifiers in format "studyId_sampleId"
   * @param patientUniqueIds list of patient unique identifiers in format "studyId_patientId"
   * @param sampleAttributeIds list of sample-level clinical attribute IDs
   * @param patientAttributeIds list of patient-level clinical attribute IDs
   * @param conflictingAttributeIds list of conflicting attribute IDs (patient attributes mapped to
   *     sample level)
   * @return list of ClinicalDataCountItem representing clinical data counts
   */
  List<ClinicalDataCountItem> getClinicalDataCountsForEnrichments(
      List<String> sampleUniqueIds,
      List<String> patientUniqueIds,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds);

  /**
   * Retrieves clinical data with ID projection (minimal data set).
   *
   * <p>Returns only essential identifiers: internal ID, sample/patient ID, study ID, and attribute
   * ID. This projection is optimized for performance when only basic identification is needed.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in results
   * @param clinicalDataType type of clinical data to retrieve (SAMPLE or PATIENT)
   * @return list of clinical data records with minimal field set
   * @see ClinicalData
   * @see ClinicalDataType
   */
  List<ClinicalData> fetchClinicalDataId(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType);

  /**
   * Retrieves clinical data with SUMMARY projection (basic data with values).
   *
   * <p>Returns basic clinical data including identifiers and attribute values, but without detailed
   * clinical attribute metadata. This projection balances data completeness with performance.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in results
   * @param clinicalDataType type of clinical data to retrieve (SAMPLE or PATIENT)
   * @return list of clinical data records with basic field set including values
   * @see ClinicalData
   * @see ClinicalDataType
   */
  List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType);

  /**
   * Retrieves clinical data with SUMMARY projection for enrichment analysis. Accepts both sample
   * and patient unique identifiers along with categorized attributes to fetch all required clinical
   * data in a single optimized query.
   *
   * <p>This method handles three types of attributes:
   *
   * <ul>
   *   <li>Sample attributes: fetched using sampleUniqueIds
   *   <li>Patient attributes: fetched using patientUniqueIds
   *   <li>Conflicting attributes: patient-level attributes mapped to sample level
   * </ul>
   *
   * @param sampleUniqueIds list of sample unique identifiers in format "studyId_sampleId"
   * @param patientUniqueIds list of patient unique identifiers in format "studyId_patientId"
   * @param sampleAttributeIds list of sample-level clinical attribute IDs
   * @param patientAttributeIds list of patient-level clinical attribute IDs
   * @param conflictingAttributeIds list of conflicting attribute IDs (patient attributes mapped to
   *     sample level)
   * @return list of clinical data records with basic field set including values
   * @see ClinicalData
   */
  List<ClinicalData> fetchClinicalDataSummaryForEnrichments(
      List<String> sampleUniqueIds,
      List<String> patientUniqueIds,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds);

  /**
   * Retrieves clinical data with DETAILED projection (complete data set).
   *
   * <p>Returns complete clinical data including all identifiers, attribute values, and detailed
   * clinical attribute metadata (display names, descriptions, data types, etc.). This projection
   * provides the most comprehensive data but may have higher performance costs due to joins.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in results
   * @param clinicalDataType type of clinical data to retrieve (SAMPLE or PATIENT)
   * @return list of clinical data records with complete field set including attribute metadata
   * @see ClinicalData
   * @see ClinicalDataType
   */
  List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType);

  /**
   * Retrieves the count of clinical data records matching the specified criteria.
   *
   * <p>Returns only the total number of records that would be returned by a corresponding data
   * retrieval operation, without actually fetching the data. This is useful for pagination,
   * progress indicators, and API responses that only need metadata.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in count
   * @param clinicalDataType type of clinical data to count (SAMPLE or PATIENT)
   * @return total number of clinical data records matching the criteria
   * @see ClinicalDataType
   */
  Integer fetchClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType);
}
