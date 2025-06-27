package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.StudyScopedId;
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

  List<StudyScopedId> getVisibleSampleIdsForClinicalTable(
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

  List<ClinicalData> getSampleClinicalDataBySampleIds(List<StudyScopedId> studyScopedIds);

  List<ClinicalData> getPatientClinicalDataBySampleIds(List<StudyScopedId> studyScopedIds);
}
