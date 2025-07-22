package org.cbioportal.legacy.persistence.virtualstudy;

import static java.util.stream.Collectors.groupingBy;
import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.calculateUniqueKey;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalEventRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.ClinicalEventSortBy;

public class VSAwareClinicalEventRepository implements ClinicalEventRepository {

  private final VirtualizationService virtualizationService;
  private final ClinicalEventRepository clinicalEventRepository;
  // TODO does it have to be a VS aware repository?
  private final VSAwarePatientRepository vsAwarePatientRepository;

  public VSAwareClinicalEventRepository(
      VirtualizationService virtualStudyService,
      ClinicalEventRepository clinicalEventRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    this.virtualizationService = virtualStudyService;
    this.clinicalEventRepository = clinicalEventRepository;
    this.vsAwarePatientRepository = vsAwarePatientRepository;
  }

  @Override
  public List<ClinicalEvent> getAllClinicalEventsOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {

    var resultStream =
        virtualizationService
            .handleStudyPatientData(
                List.of(studyId),
                List.of(patientId),
                ClinicalEvent::getStudyId,
                ClinicalEvent::getPatientId,
                (studyIds, patientIds) ->
                    clinicalEventRepository.getAllClinicalEventsOfPatientInStudy(
                        studyIds.getFirst(),
                        patientIds.getFirst(),
                        projection,
                        null,
                        null,
                        null,
                        null),
                this::virtualizeClinicalEvent)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Comparator<ClinicalEvent> composeComparator(String sortBy, String direction) {
    ClinicalEventSortBy ca = ClinicalEventSortBy.valueOf(sortBy);
    Comparator<ClinicalEvent> result =
        switch (ca) {
          case eventType -> Comparator.comparing(ClinicalEvent::getEventType);
          case startNumberOfDaysSinceDiagnosis -> Comparator.comparing(ClinicalEvent::getStartDate);
          case endNumberOfDaysSinceDiagnosis -> Comparator.comparing(ClinicalEvent::getStopDate);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public BaseMeta getMetaPatientClinicalEvents(String studyId, String patientId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getPatientsDistinctClinicalEventInStudies(List.of(studyId), List.of(patientId)).size());
    return baseMeta;
  }

  @Override
  public List<ClinicalEventData> getDataOfClinicalEvents(List<Long> clinicalEventIds) {
    return clinicalEventRepository.getDataOfClinicalEvents(clinicalEventIds).stream()
        .map(this::virtualizeClinicalEventData)
        .toList();
  }

  private ClinicalEventData virtualizeClinicalEventData(ClinicalEventData clinicalEventData) {
    // TODO Do we need to virtualize this? We don't change anything
    ClinicalEventData virtualClinicalEventData = new ClinicalEventData();
    virtualClinicalEventData.setClinicalEventId(clinicalEventData.getClinicalEventId());
    virtualClinicalEventData.setKey(clinicalEventData.getKey());
    virtualClinicalEventData.setValue(clinicalEventData.getValue());
    return virtualClinicalEventData;
  }

  @Override
  public List<ClinicalEvent> getAllClinicalEventsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Stream<ClinicalEvent> resultStream =
        virtualizationService
            .handleStudyPatientData(
                studyId,
                ClinicalEvent::getPatientId,
                (stid) ->
                    clinicalEventRepository.getAllClinicalEventsInStudy(
                        stid, projection, null, null, null, null),
                this::virtualizeClinicalEvent)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  @Override
  public BaseMeta getMetaClinicalEvents(String studyId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getAllClinicalEventsInStudy(studyId, Projection.ID.name(), null, null, null, null).size());
    return baseMeta;
  }

  @Override
  public Map<String, Set<String>> getSamplesOfPatientsPerEventTypeInStudy(
      List<String> studyIds, List<String> sampleIds) {
    List<Patient> patients = vsAwarePatientRepository.getPatientsOfSamples(studyIds, sampleIds);
    return getPatientsDistinctClinicalEventInStudies(
            studyIds, patients.stream().map(Patient::getStableId).toList())
        .stream()
        .collect(
            groupingBy(
                ClinicalEvent::getEventType,
                Collectors.mapping(ClinicalEvent::getUniqueSampleKey, Collectors.toSet())));
  }

  @Override
  public List<ClinicalEvent> getPatientsDistinctClinicalEventInStudies(
      List<String> studyIds, List<String> patientIds) {
    return virtualizationService.handleStudyPatientData(
        studyIds,
        patientIds,
        ClinicalEvent::getStudyId,
        ClinicalEvent::getPatientId,
        (studies, patients) ->
            clinicalEventRepository.getPatientsDistinctClinicalEventInStudies(studies, patients),
        this::virtualizeClinicalEvent);
  }

  // TODO move it to the vs service
  private ClinicalEvent virtualizeClinicalEvent(String virtualStudyId, ClinicalEvent ce) {
    ClinicalEvent virtualClinicalEvent = new ClinicalEvent();
    // TODO It's sadly used to fetch data for the clinical event
    virtualClinicalEvent.setClinicalEventId(ce.getClinicalEventId());

    virtualClinicalEvent.setStudyId(virtualStudyId);
    virtualClinicalEvent.setEventType(ce.getEventType());
    virtualClinicalEvent.setPatientId(ce.getPatientId());
    virtualClinicalEvent.setUniquePatientKey(
        calculateUniqueKey(virtualStudyId, ce.getUniquePatientKey()));
    virtualClinicalEvent.setStartDate(ce.getStartDate());
    virtualClinicalEvent.setStopDate(ce.getStopDate());
    if (ce.getAttributes() != null) {
      virtualClinicalEvent.setAttributes(
          ce.getAttributes().stream().map(this::virtualizeClinicalEventData).toList());
    }
    return virtualClinicalEvent;
  }

  @Override
  public List<ClinicalEvent> getTimelineEvents(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
    return virtualizationService.handleStudyPatientData(
        studyIds,
        patientIds,
        ClinicalEvent::getStudyId,
        ClinicalEvent::getPatientId,
        (studies, patients) ->
            clinicalEventRepository.getTimelineEvents(studies, patients, clinicalEvents),
        this::virtualizeClinicalEvent);
  }

  @Override
  public List<ClinicalEvent> getClinicalEventsMeta(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
    return virtualizationService.handleStudyPatientData(
        studyIds,
        patientIds,
        ClinicalEvent::getStudyId,
        ClinicalEvent::getPatientId,
        (studies, patients) ->
            clinicalEventRepository.getTimelineEvents(studies, patients, clinicalEvents),
        this::virtualizeClinicalEvent);
  }
}
