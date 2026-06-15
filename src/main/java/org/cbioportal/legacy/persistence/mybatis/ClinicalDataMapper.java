package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.apache.ibatis.session.ResultHandler;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.meta.BaseMeta;

public interface ClinicalDataMapper {

  List<ClinicalData> getSampleClinicalData(
      List<String> studyIds,
      List<String> sampleIds,
      List<String> attributeIds,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  /**
   * Streaming overload of {@link #getSampleClinicalData}: routes to the same SQL statement but
   * passes each mapped row to {@code handler} instead of building a list, so a large result set is
   * never materialized in memory.
   */
  @SuppressWarnings("java:S107") // streaming overload mirrors the many-arg non-streaming method
  void getSampleClinicalData(
      List<String> studyIds,
      List<String> sampleIds,
      List<String> attributeIds,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction,
      ResultHandler<ClinicalData> handler);

  BaseMeta getMetaSampleClinicalData(
      List<String> studyIds, List<String> sampleIds, List<String> attributeIds);

  List<ClinicalData> getPatientClinicalData(
      List<String> studyIds,
      List<String> patientIds,
      List<String> attributeIds,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  /** Streaming overload of {@link #getPatientClinicalData}; see {@link #getSampleClinicalData}. */
  @SuppressWarnings("java:S107") // streaming overload mirrors the many-arg non-streaming method
  void getPatientClinicalData(
      List<String> studyIds,
      List<String> patientIds,
      List<String> attributeIds,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction,
      ResultHandler<ClinicalData> handler);

  List<ClinicalData> getSampleClinicalTable(
      List<String> studyIds,
      List<String> sampleIds,
      String projection,
      Integer limit,
      Integer offset,
      String searchTerm,
      String sortByAttrId,
      Boolean sortAttrIsNumber,
      Boolean sortIsPatientAttr,
      String direction);

  Integer getSampleClinicalTableCount(
      List<String> studyIds,
      List<String> sampleIds,
      String projection,
      String searchTerm,
      String sortBy,
      String direction);

  BaseMeta getMetaPatientClinicalData(
      List<String> studyIds, List<String> patientIds, List<String> attributeIds);

  List<ClinicalDataCount> fetchSampleClinicalDataCounts(
      List<String> studyIds, List<String> sampleIds, List<String> attributeIds);

  List<ClinicalDataCount> fetchPatientClinicalDataCounts(
      List<String> studyIds, List<String> patientIds, List<String> attributeIds, String projection);

  List<ClinicalData> getPatientClinicalDataDetailedToSample(
      List<String> studyIds,
      List<String> patientIds,
      List<String> attributeIds,
      String projection,
      Integer limit,
      Integer offset,
      String sortBy,
      String direction);

  List<Integer> getVisibleSampleInternalIdsForClinicalTable(
      List<String> studyIds,
      List<String> sampleIds,
      String projection,
      Integer limit,
      Integer offset,
      String searchTerm,
      String sortAttrId,
      Boolean sortAttrIsNumber,
      Boolean sortIsPatientAttr,
      String direction);

  List<ClinicalData> getSampleClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds);

  List<ClinicalData> getPatientClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds);
}
