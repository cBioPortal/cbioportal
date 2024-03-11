package org.cbioportal.service.impl;

import java.util.*;
import java.util.stream.Collectors;
import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClinicalEventServiceImpl implements ClinicalEventService {

  @Autowired private ClinicalEventRepository clinicalEventRepository;
  @Autowired private PatientService patientService;

  @Override
  public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws PatientNotFoundException, StudyNotFoundException {

    patientService.getPatientInStudy(studyId, patientId);

    List<ClinicalEvent> clinicalEvents =
        clinicalEventRepository.getAllClinicalEventsOfPatientInStudy(
            studyId, patientId, projection, pageSize, pageNumber, sortBy, direction);

    if (!projection.equals("ID") && !clinicalEvents.isEmpty()) {

      List<ClinicalEventData> clinicalEventDataList =
          clinicalEventRepository.getDataOfClinicalEvents(
              clinicalEvents.stream()
                  .map(ClinicalEvent::getClinicalEventId)
                  .collect(Collectors.toList()));

      clinicalEvents.forEach(
          c ->
              c.setAttributes(
                  clinicalEventDataList.stream()
                      .filter(a -> a.getClinicalEventId().equals(c.getClinicalEventId()))
                      .collect(Collectors.toList())));
    }

    return clinicalEvents;
  }

  @Override
  public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId)
      throws PatientNotFoundException, StudyNotFoundException {

    patientService.getPatientInStudy(studyId, patientId);

    return clinicalEventRepository.getMetaPatientClinicalEvents(studyId, patientId);
  }

  @Override
  public List<ClinicalEvent> getAllClinicalEventsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {

    List<ClinicalEvent> clinicalEvents =
        clinicalEventRepository.getAllClinicalEventsInStudy(
            studyId, projection, pageSize, pageNumber, sortBy, direction);

    if (!projection.equals("ID")) {

      List<ClinicalEventData> clinicalEventDataList =
          clinicalEventRepository.getDataOfClinicalEvents(
              clinicalEvents.stream()
                  .map(ClinicalEvent::getClinicalEventId)
                  .collect(Collectors.toList()));

      clinicalEvents.forEach(
          c ->
              c.setAttributes(
                  clinicalEventDataList.stream()
                      .filter(a -> a.getClinicalEventId().equals(c.getClinicalEventId()))
                      .collect(Collectors.toList())));
    }

    return clinicalEvents;
  }

  @Override
  public BaseMeta getMetaClinicalEvents(String studyId) throws StudyNotFoundException {
    return clinicalEventRepository.getMetaClinicalEvents(studyId);
  }

  @Override
  public Map<String, Set<String>> getPatientsSamplesPerClinicalEventType(
      List<String> studyIds, List<String> sampleIds) {

    return clinicalEventRepository.getSamplesOfPatientsPerEventTypeInStudy(studyIds, sampleIds);
  }

  @Override
  public List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      List<String> studyIds, List<String> sampleIds) {

    List<Patient> patients = patientService.getPatientsOfSamples(studyIds, sampleIds);

    List<String> studies =
        patients.stream().map(Patient::getCancerStudyIdentifier).collect(Collectors.toList());
    List<String> patientIds =
        patients.stream().map(Patient::getStableId).collect(Collectors.toList());

    List<ClinicalEvent> clinicalEvents =
        clinicalEventRepository.getPatientsDistinctClinicalEventInStudies(studies, patientIds);

    Map<String, Integer> clinicalEventTypeCountMap = new HashMap<>();
    for (ClinicalEvent e : clinicalEvents) {
      clinicalEventTypeCountMap.put(
          e.getEventType(), clinicalEventTypeCountMap.getOrDefault(e.getEventType(), 0) + 1);
    }

    return clinicalEventTypeCountMap.entrySet().stream()
        .map(e -> new ClinicalEventTypeCount(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }
}
