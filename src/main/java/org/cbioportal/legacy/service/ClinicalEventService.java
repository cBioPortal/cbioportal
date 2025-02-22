package org.cbioportal.legacy.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventTypeCount;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.SurvivalRequest;

public interface ClinicalEventService {

  List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws PatientNotFoundException, StudyNotFoundException;

  BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId)
      throws PatientNotFoundException, StudyNotFoundException;

  List<ClinicalEvent> getAllClinicalEventsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction);

  BaseMeta getMetaClinicalEvents(String studyId) throws StudyNotFoundException;

  Map<String, Set<String>> getPatientsSamplesPerClinicalEventType(
      List<String> studyIds, List<String> sampleIds);

  List<ClinicalEventTypeCount> getClinicalEventTypeCounts(
      List<String> studyIds, List<String> sampleIds);

  List<ClinicalData> getSurvivalData(
      List<String> studyIds,
      List<String> patientIds,
      String attributeIdPrefix,
      SurvivalRequest survivalRequest);

  List<ClinicalEvent> getClinicalEventsMeta(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents);
}
