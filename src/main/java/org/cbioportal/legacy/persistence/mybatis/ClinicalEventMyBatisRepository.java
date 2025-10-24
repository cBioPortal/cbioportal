package org.cbioportal.legacy.persistence.mybatis;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalEventRepository;
import org.cbioportal.legacy.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ClinicalEventMyBatisRepository implements ClinicalEventRepository {

  @Autowired private ClinicalEventMapper clinicalEventMapper;

  @Override
  public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return clinicalEventMapper.getPatientClinicalEvent(
        studyId,
        patientId,
        projection,
        pageSize,
        PaginationCalculator.offset(pageSize, pageNumber),
        sortBy,
        direction);
  }

  @Override
  public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) {

    return clinicalEventMapper.getMetaPatientClinicalEvent(studyId, patientId);
  }

  @Override
  public List<ClinicalEventData> getDataOfClinicalEvents(List<Long> clinicalEventIds) {

    return clinicalEventMapper.getDataOfClinicalEvents(clinicalEventIds);
  }

  @Override
  public List<ClinicalEvent> getAllClinicalEventsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return clinicalEventMapper.getStudyClinicalEvent(
        studyId,
        projection,
        pageSize,
        PaginationCalculator.offset(pageSize, pageNumber),
        sortBy,
        direction);
  }

  @Override
  public BaseMeta getMetaClinicalEvents(String studyId) {

    return clinicalEventMapper.getMetaClinicalEvent(studyId);
  }

  @Override
  public Map<String, Set<String>> getSamplesOfPatientsPerEventTypeInStudy(
      List<String> studyIds, List<String> sampleIds) {
    return clinicalEventMapper.getSamplesOfPatientsPerEventType(studyIds, sampleIds).stream()
        .collect(
            groupingBy(
                ClinicalEvent::getEventType,
                Collectors.mapping(ClinicalEvent::getUniqueSampleKey, Collectors.toSet())));
  }

  @Override
  public List<ClinicalEvent> getPatientsDistinctClinicalEventInStudies(
      List<String> studyIds, List<String> patientIds) {
    return clinicalEventMapper.getPatientsDistinctClinicalEventInStudies(
        studyIds, patientIds, Collections.emptyList());
  }

  @Override
  public List<ClinicalEvent> getTimelineEvents(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
    return clinicalEventMapper.getTimelineEvents(studyIds, patientIds, clinicalEvents);
  }

  @Override
  public List<ClinicalEvent> getClinicalEventsMeta(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
    return clinicalEventMapper.getClinicalEventsMeta(studyIds, patientIds, clinicalEvents);
  }
}
