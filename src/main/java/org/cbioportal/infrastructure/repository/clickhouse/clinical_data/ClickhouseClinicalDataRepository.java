package org.cbioportal.infrastructure.repository.clickhouse.clinical_data;

import java.util.Collections;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.repository.ClinicalDataRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

/**
 * ClickHouse implementation of the ClinicalDataRepository interface.
 *
 * <p>This repository provides access to clinical data stored in ClickHouse column store, optimized
 * for analytical queries. It delegates to MyBatis mappers for SQL query execution and handles
 * empty-collection edge cases to prevent unnecessary database calls.
 *
 * <p>Only active when the "clickhouse" profile is enabled.
 *
 * @see ClinicalDataRepository
 * @see ClickhouseClinicalDataMapper
 */
@Repository
@Profile("clickhouse")
public class ClickhouseClinicalDataRepository implements ClinicalDataRepository {

  private final ClickhouseClinicalDataMapper mapper;

  /**
   * Constructor for dependency injection.
   *
   * @param mapper MyBatis mapper for executing ClickHouse queries
   */
  public ClickhouseClinicalDataRepository(ClickhouseClinicalDataMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to MyBatis mapper to execute optimized ClickHouse query with study view filters.
   */
  @Override
  public List<ClinicalData> getPatientClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getPatientClinicalDataByStudyViewFilter(
        studyViewFilterContext, filteredAttributes);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to MyBatis mapper to execute optimized ClickHouse query with study view filters.
   */
  @Override
  public List<ClinicalData> getSampleClinicalData(
      StudyViewFilterContext studyViewFilterContext, List<String> filteredAttributes) {
    return mapper.getSampleClinicalDataByStudyViewFilter(
        studyViewFilterContext, filteredAttributes);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Delegates to MyBatis mapper to execute count aggregation query with study view filters.
   */
  @Override
  public List<ClinicalDataCountItem> getClinicalDataCounts(
      StudyViewFilterContext studyViewFilterContext,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {
    return mapper.getClinicalDataCountsByStudyViewFilter(
        studyViewFilterContext, sampleAttributeIds, patientAttributeIds, conflictingAttributeIds);
  }

  /**
   * {@inheritDoc}
   *
   * <p>This overload is optimized for enrichment analysis by accepting pre-computed unique IDs to
   * avoid additional ID resolution queries. Delegates to MyBatis mapper for efficient batch
   * aggregation.
   */
  @Override
  public List<ClinicalDataCountItem> getClinicalDataCountsForEnrichments(
      List<String> sampleUniqueIds,
      List<String> patientUniqueIds,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {
    return mapper.getClinicalDataCountsForEnrichments(
        sampleUniqueIds,
        patientUniqueIds,
        sampleAttributeIds,
        patientAttributeIds,
        conflictingAttributeIds);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns early with empty list if no unique IDs provided to avoid unnecessary database call.
   */
  @Override
  public List<ClinicalData> fetchClinicalDataId(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataId(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns early with empty list if no unique IDs provided to avoid unnecessary database call.
   */
  @Override
  public List<ClinicalData> fetchClinicalDataSummary(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataSummary(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  /**
   * {@inheritDoc}
   *
   * <p>This overload is optimized for enrichment analysis by accepting pre-computed unique IDs and
   * categorized attributes. Executes a single optimized query that unions sample-level,
   * patient-level, and conflicting attribute data.
   */
  @Override
  public List<ClinicalData> fetchClinicalDataSummaryForEnrichments(
      List<String> sampleUniqueIds,
      List<String> patientUniqueIds,
      List<String> sampleAttributeIds,
      List<String> patientAttributeIds,
      List<String> conflictingAttributeIds) {
    return mapper.fetchClinicalDataSummaryForEnrichments(
        sampleUniqueIds,
        patientUniqueIds,
        sampleAttributeIds,
        patientAttributeIds,
        conflictingAttributeIds);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns early with empty list if no unique IDs provided to avoid unnecessary database call.
   */
  @Override
  public List<ClinicalData> fetchClinicalDataDetailed(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return Collections.emptyList();
    }
    return mapper.fetchClinicalDataDetailed(uniqueIds, attributeIds, clinicalDataType.toString());
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns 0 if no unique IDs provided or if count result is null from the database.
   */
  @Override
  public Integer fetchClinicalDataMeta(
      List<String> uniqueIds, List<String> attributeIds, ClinicalDataType clinicalDataType) {
    if (CollectionUtils.isEmpty(uniqueIds)) {
      return 0;
    }
    Integer cnt =
        mapper.fetchClinicalDataMeta(uniqueIds, attributeIds, clinicalDataType.toString());
    return cnt == null ? 0 : cnt;
  }
}
