package org.cbioportal.legacy.service;

import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.model.SampleClinicalDataCollection;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.SampleNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

public interface ClinicalDataService {

  List<ClinicalData> getAllClinicalDataOfSampleInStudy(
      String studyId,
      String sampleId,
      String attributeId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws SampleNotFoundException, StudyNotFoundException;

  BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId)
      throws SampleNotFoundException, StudyNotFoundException;

  List<ClinicalData> getAllClinicalDataOfPatientInStudy(
      String studyId,
      String patientId,
      String attributeId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws PatientNotFoundException, StudyNotFoundException;

  BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId)
      throws PatientNotFoundException, StudyNotFoundException;

  List<ClinicalData> getAllClinicalDataInStudy(
      String studyId,
      String attributeId,
      String clinicalDataType,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException;

  BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType)
      throws StudyNotFoundException;

  List<ClinicalData> fetchAllClinicalDataInStudy(
      String studyId,
      List<String> ids,
      List<String> attributeIds,
      String clinicalDataType,
      String projection)
      throws StudyNotFoundException;

  BaseMeta fetchMetaClinicalDataInStudy(
      String studyId, List<String> ids, List<String> attributeIds, String clinicalDataType)
      throws StudyNotFoundException;

  List<ClinicalData> fetchClinicalData(
      List<String> studyIds,
      List<String> ids,
      List<String> attributeIds,
      String clinicalDataType,
      String projection);

  BaseMeta fetchMetaClinicalData(
      List<String> studyIds, List<String> ids, List<String> attributeIds, String clinicalDataType);

  List<ClinicalDataCountItem> fetchClinicalDataCounts(
      List<String> studyIds, List<String> sampleIds, List<String> attributeIds);

  List<ClinicalData> getPatientClinicalDataDetailedToSample(
      List<String> studyIds, List<String> patientIds, List<String> attributeIds);

  ImmutablePair<SampleClinicalDataCollection, Integer> fetchSampleClinicalTable(
      List<String> studyIds,
      List<String> sampleIds,
      Integer pageSize,
      Integer pageNumber,
      String searchTerm,
      String sortBy,
      String direction);
}
