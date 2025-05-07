package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalDataRepository;

public class VSAwareClinicalDataRepository implements ClinicalDataRepository {
  @Override
  public List<ClinicalData> getAllClinicalDataOfSampleInStudy(
      String studyId,
      String sampleId,
      String attributeId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return List.of();
  }

  @Override
  public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
    return null;
  }

  @Override
  public List<ClinicalData> getAllClinicalDataOfPatientInStudy(
      String studyId,
      String patientId,
      String attributeId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return List.of();
  }

  @Override
  public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
    return null;
  }

  @Override
  public List<ClinicalData> getAllClinicalDataInStudy(
      String studyId,
      String attributeId,
      String clinicalDataType,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return List.of();
  }

  @Override
  public BaseMeta getMetaAllClinicalData(
      String studyId, String attributeId, String clinicalDataType) {
    return null;
  }

  @Override
  public List<ClinicalData> fetchAllClinicalDataInStudy(
      String studyId,
      List<String> ids,
      List<String> attributeIds,
      String clinicalDataType,
      String projection) {
    return List.of();
  }

  @Override
  public BaseMeta fetchMetaClinicalDataInStudy(
      String studyId, List<String> ids, List<String> attributeIds, String clinicalDataType) {
    return null;
  }

  @Override
  public List<ClinicalData> fetchClinicalData(
      List<String> studyIds,
      List<String> ids,
      List<String> attributeIds,
      String clinicalDataType,
      String projection) {
    return List.of();
  }

  @Override
  public BaseMeta fetchMetaClinicalData(
      List<String> studyIds, List<String> ids, List<String> attributeIds, String clinicalDataType) {
    return null;
  }

  @Override
  public List<ClinicalDataCount> fetchClinicalDataCounts(
      List<String> studyIds,
      List<String> sampleIds,
      List<String> attributeIds,
      String clinicalDataType,
      String projection) {
    return List.of();
  }

  @Override
  public List<ClinicalData> getPatientClinicalDataDetailedToSample(
      List<String> studyIds, List<String> patientIds, List<String> attributeIds) {
    return List.of();
  }

  @Override
  public List<Integer> getVisibleSampleInternalIdsForClinicalTable(
      List<String> studyIds,
      List<String> sampleIds,
      Integer pageSize,
      Integer pageNumber,
      String searchTerm,
      String sortBy,
      String direction) {
    return List.of();
  }

  @Override
  public List<ClinicalData> getSampleClinicalDataBySampleInternalIds(
      List<Integer> visibleSampleInternalIds) {
    return List.of();
  }

  @Override
  public List<ClinicalData> getPatientClinicalDataBySampleInternalIds(
      List<Integer> visibleSampleInternalIds) {
    return List.of();
  }
}
