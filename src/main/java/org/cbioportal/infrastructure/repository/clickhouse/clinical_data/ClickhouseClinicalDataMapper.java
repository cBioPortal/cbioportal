package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.List;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCountItem;

/**
 * Mapper interface for retrieving clinical data from ClickHouse. This interface provides methods to
 * fetch clinical data counts and clinical data for samples and patients.
 */
public interface ClickhouseClinicalDataMapper {

  /**
   * Retrieves clinical data counts based on the study view filter context, attribute IDs, and
   * filtered attribute values.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param sampleAttributeIds the list of sample attribute IDs to filter by
   * @param patientAttributeIds the list of patient attribute IDs to filter by
   * @param conflictingAttributeIds the list of both sample and patient attribute IDs to filter by
   * @return a list of clinical data count items
   */
  List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds);

  /**
   * Retrieves sample clinical data based on the study view filter context and attribute IDs.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param attributeIds the list of attribute IDs to filter by
   * @return a list of sample clinical data
   */
  List<ClinicalData> getSampleClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);

  /**
   * Retrieves patient clinical data based on the study view filter context and attribute IDs.
   *
   * @param studyViewFilterContext the context of the study view filter
   * @param attributeIds the list of attribute IDs to filter by
   * @return a list of patient clinical data
   */
  List<ClinicalData> getPatientClinicalDataFromStudyViewFilter(
      StudyViewFilterContext studyViewFilterContext, List<String> attributeIds);

  /**
   * Retrieves clinical data with ID projection (minimal data set).
   *
   * <p>Returns only essential identifiers: internal ID, sample/patient ID, study ID, and attribute
   * ID. This projection is optimized for performance when only basic identification is needed.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in results
   * @param clinicalDataType type of clinical data to retrieve ("sample" or "patient")
   * @return list of clinical data records with minimal field set
   */
  List<ClinicalData> fetchClinicalDataId(
      List<String> uniqueIds, List<String> attributeIds, String clinicalDataType);

  /**
   * Retrieves clinical data with SUMMARY projection (basic data with values).
   *
   * <p>Returns basic clinical data including identifiers and attribute values, but without detailed
   * clinical attribute metadata. This projection balances data completeness with performance.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in results
   * @param clinicalDataType type of clinical data to retrieve ("sample" or "patient")
   * @return list of clinical data records with basic field set including values
   */
  List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, String clinicalDataType);

  /**
   * Retrieves clinical data with DETAILED projection (complete data set).
   *
   * <p>Returns complete clinical data including all identifiers, attribute values, and detailed
   * clinical attribute metadata (display names, descriptions, data types, etc.). This projection
   * provides the most comprehensive data but may have higher performance costs due to joins.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in results
   * @param clinicalDataType type of clinical data to retrieve ("sample" or "patient")
   * @return list of clinical data records with complete field set including attribute metadata
   */
  List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, String clinicalDataType);

  /**
   * Retrieves the count of clinical data records matching the specified criteria.
   *
   * <p>Returns only the total number of records that would be returned by a corresponding data
   * retrieval operation, without actually fetching the data. This is useful for pagination,
   * progress indicators, and API responses that only need metadata.
   *
   * @param uniqueIds list of unique identifiers in format "studyId_entityId"
   * @param attributeIds list of clinical attribute IDs to include in count
   * @param clinicalDataType type of clinical data to count ("sample" or "patient")
   * @return total number of clinical data records matching the criteria
   */
  Integer fetchClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, String clinicalDataType);
}
