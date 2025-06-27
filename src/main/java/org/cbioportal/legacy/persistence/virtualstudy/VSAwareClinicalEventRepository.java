package org.cbioportal.legacy.persistence.virtualstudy;

import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.ClinicalEventRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.sort.ClinicalEventSortBy;

public class VSAwareClinicalEventRepository implements ClinicalEventRepository {

  private final VirtualStudyService virtualStudyService;
  private final ClinicalEventRepository clinicalEventRepository;
  // TODO does it have to be a VS aware repository?
  private final VSAwarePatientRepository vsAwarePatientRepository;

  public VSAwareClinicalEventRepository(
      VirtualStudyService virtualStudyService,
      ClinicalEventRepository clinicalEventRepository,
      VSAwarePatientRepository vsAwarePatientRepository) {
    this.virtualStudyService = virtualStudyService;
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

    List<ClinicalEvent> allClinicalEventsOfPatientInStudy = new ArrayList<>();
    Set<String> virtualStudyIds = virtualStudyService.getPublishedVirtualStudyIds();
    Map<StudyScopedId, Set<String>> materializedStudyPatientPairsMap =
        virtualStudyService.toMaterializedStudyPatientPairsMap(
            List.of(new StudyScopedId(studyId, patientId)));
    if (materializedStudyPatientPairsMap.isEmpty()) {
      return allClinicalEventsOfPatientInStudy;
    }
    StudyScopedId materializedStudyScopedId =
        materializedStudyPatientPairsMap.keySet().iterator().next();
    Stream<ClinicalEvent> resultStream =
        clinicalEventRepository
            .getAllClinicalEventsOfPatientInStudy(
                materializedStudyScopedId.getStudyStableId(),
                materializedStudyScopedId.getStableId(),
                projection,
                pageSize,
                pageNumber,
                sortBy,
                direction)
            .stream();
    if (virtualStudyIds.contains(studyId)) {
      resultStream = resultStream.map(ce -> virtualizeClinicalEvent(studyId, ce));
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
    Optional<VirtualStudy> virtualStudyOptional =
        virtualStudyService.getVirtualStudyByIdIfExists(studyId);
    if (virtualStudyOptional.isEmpty()) {
      return clinicalEventRepository.getAllClinicalEventsInStudy(
          studyId, projection, pageSize, pageNumber, sortBy, direction);
    }
    VirtualStudy virtualStudy = virtualStudyOptional.get();
    List<Patient> patients = getVsPatients(virtualStudy);
    Stream<ClinicalEvent> resultStream =
        // TODO I should not use this method here.
        getPatientsDistinctClinicalEventInStudies(
            List.of(studyId), patients.stream().map(Patient::getStableId).toList())
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

  // TODO review usage of this method as it returns distinct clinical events without start and stop
  // dates. So these events are not useful for some use cases
  @Override
  public List<ClinicalEvent> getPatientsDistinctClinicalEventInStudies(
      List<String> studyIds, List<String> patientIds) {
    Map<String, VirtualStudy> virtualStudyMap =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .collect(Collectors.toMap(VirtualStudy::getId, vs -> vs));
    List<String> materialisedStudyIds = new ArrayList<>();
    List<String> materialisedPatientIds = new ArrayList<>();
    Map<String, Set<String>> actualVirtualStudyToPatientId = new HashMap<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      String patientId = patientIds.get(i);
      if (virtualStudyMap.containsKey(studyId)) {
        VirtualStudy virtualStudy = virtualStudyMap.get(studyId);
        if (!actualVirtualStudyToPatientId.containsKey(virtualStudy.getId())) {
          actualVirtualStudyToPatientId.put(virtualStudy.getId(), new HashSet<>());
        }
        actualVirtualStudyToPatientId.get(virtualStudy.getId()).add(patientId);
      } else {
        materialisedStudyIds.add(studyId);
        materialisedPatientIds.add(patientId);
      }
    }

    Stream<ClinicalEvent> materialisedClinicalEvents =
        materialisedStudyIds.isEmpty()
            ? Stream.empty()
            : clinicalEventRepository
                .getPatientsDistinctClinicalEventInStudies(
                    materialisedStudyIds, materialisedPatientIds)
                .stream();
    Stream<ClinicalEvent> virtualClinicalEvents =
        actualVirtualStudyToPatientId.entrySet().stream()
            .flatMap(
                entry -> {
                  String virtualStudyId = entry.getKey();
                  Set<String> patientIdsSet = entry.getValue();
                  VirtualStudy virtualStudy = virtualStudyMap.get(virtualStudyId);
                  List<Patient> vsPatients = getVsPatients(virtualStudy);
                  Map<String, Patient> vsPatientMap =
                      vsPatients.stream()
                          // TODO reuse
                          .collect(
                              Collectors.toMap(
                                  p -> p.getCancerStudyIdentifier() + "_" + p.getStableId(),
                                  p -> p));
                  List<String> studyIds2 = new ArrayList<>();
                  List<String> patientIds2 = new ArrayList<>();
                  for (String patientId : patientIdsSet) {
                    if (vsPatientMap.containsKey(patientId)) {
                      Patient patient = vsPatientMap.get(patientId);
                      studyIds2.add(patient.getCancerStudyIdentifier());
                      patientIds2.add(patient.getStableId());
                    }
                  }
                  return clinicalEventRepository
                      .getPatientsDistinctClinicalEventInStudies(studyIds2, patientIds2)
                      .stream()
                      .map(ce -> virtualizeClinicalEvent(virtualStudyId, ce));
                });

    return Stream.concat(materialisedClinicalEvents, virtualClinicalEvents).toList();
  }

  private List<Patient> getVsPatients(VirtualStudy virtualStudy) {
    List<String> vsDefStudyIds =
        virtualStudy.getData().getStudies().stream()
            .flatMap(s -> s.getSamples().stream().map(s1 -> s.getId()))
            .collect(Collectors.toList());
    List<String> vsDefSampleIds =
        virtualStudy.getData().getStudies().stream()
            .flatMap(s -> s.getSamples().stream())
            .collect(Collectors.toList());
    return vsAwarePatientRepository.getPatientsOfSamples(vsDefStudyIds, vsDefSampleIds);
  }

  // TODO move it to the vs service
  private ClinicalEvent virtualizeClinicalEvent(String virtualStudyId, ClinicalEvent ce) {
    ClinicalEvent virtualClinicalEvent = new ClinicalEvent();
    // TODO It's sadly used to fetch data for the clinical event
    virtualClinicalEvent.setClinicalEventId(ce.getClinicalEventId());

    virtualClinicalEvent.setStudyId(virtualStudyId);
    virtualClinicalEvent.setEventType(ce.getEventType());
    if (ce.getPatientId() != null) {
      virtualClinicalEvent.setPatientId(ce.getStudyId() + "_" + ce.getPatientId());
    }
    if (ce.getUniquePatientKey() != null) {
      virtualClinicalEvent.setUniquePatientKey(virtualStudyId + "_" + ce.getUniquePatientKey());
    }
    virtualClinicalEvent.setStartDate(ce.getStartDate());
    virtualClinicalEvent.setStopDate(ce.getStopDate());
    if (ce.getAttributes() != null) {
      virtualClinicalEvent.setAttributes(
          ce.getAttributes().stream()
              .map(this::virtualizeClinicalEventData)
              .map(
                  ced -> {
                    if ("SAMPLE_ID".equals(ced.getKey())) {
                      // TODO move to the central place
                      ced.setValue(ce.getStudyId() + "_" + ced.getValue());
                    }
                    return ced;
                  })
              .collect(Collectors.toList()));
    }
    return virtualClinicalEvent;
  }

  // TODO simplify. This and the following method are almost identical
  @Override
  public List<ClinicalEvent> getTimelineEvents(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
    Map<String, VirtualStudy> virtualStudyById =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .collect(Collectors.toMap(VirtualStudy::getId, vs -> vs));
    List<String> materialisedStudyIds = new ArrayList<>();
    List<String> materialisedPatientIds = new ArrayList<>();
    Map<String, Set<String>> actualVirtualStudyToPatientId = new HashMap<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      String patientId = patientIds.get(i);
      if (virtualStudyById.containsKey(studyId)) {
        VirtualStudy virtualStudy = virtualStudyById.get(studyId);
        if (!actualVirtualStudyToPatientId.containsKey(virtualStudy.getId())) {
          actualVirtualStudyToPatientId.put(virtualStudy.getId(), new HashSet<>());
        }
        actualVirtualStudyToPatientId.get(virtualStudy.getId()).add(patientId);
      } else {
        materialisedStudyIds.add(studyId);
        materialisedPatientIds.add(patientId);
      }
    }
    List<ClinicalEvent> result = new ArrayList<>();
    if (!materialisedStudyIds.isEmpty() && !materialisedPatientIds.isEmpty()) {
      result.addAll(
          clinicalEventRepository.getTimelineEvents(
              materialisedStudyIds, materialisedPatientIds, clinicalEvents));
    }
    if (!actualVirtualStudyToPatientId.isEmpty()) {
      for (Map.Entry<String, Set<String>> entry : actualVirtualStudyToPatientId.entrySet()) {
        String virtualStudyId = entry.getKey();
        Set<String> patientIdsSet = entry.getValue();
        VirtualStudy virtualStudy = virtualStudyById.get(virtualStudyId);
        List<Patient> vsPatients = getVsPatients(virtualStudy);
        Map<String, Patient> vsPatientMap =
            vsPatients.stream()
                .collect(
                    Collectors.toMap(
                        p -> p.getCancerStudyIdentifier() + "_" + p.getStableId(), p -> p));
        List<String> studyIds2 = new ArrayList<>();
        List<String> patientIds2 = new ArrayList<>();
        for (String patientId : patientIdsSet) {
          if (vsPatientMap.containsKey(patientId)) {
            Patient patient = vsPatientMap.get(patientId);
            studyIds2.add(patient.getCancerStudyIdentifier());
            patientIds2.add(patient.getStableId());
          }
        }
        result.addAll(
            clinicalEventRepository
                .getTimelineEvents(studyIds2, patientIds2, clinicalEvents)
                .stream()
                .map(ce -> virtualizeClinicalEvent(virtualStudyId, ce))
                .toList());
      }
    }
    return result;
  }

  @Override
  public List<ClinicalEvent> getClinicalEventsMeta(
      List<String> studyIds, List<String> patientIds, List<ClinicalEvent> clinicalEvents) {
    Map<String, VirtualStudy> virtualStudyById =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .collect(Collectors.toMap(VirtualStudy::getId, vs -> vs));
    List<String> materialisedStudyIds = new ArrayList<>();
    List<String> materialisedPatientIds = new ArrayList<>();
    Map<String, Set<String>> actualVirtualStudyToPatientId = new HashMap<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      String patientId = patientIds.get(i);
      if (virtualStudyById.containsKey(studyId)) {
        VirtualStudy virtualStudy = virtualStudyById.get(studyId);
        if (!actualVirtualStudyToPatientId.containsKey(virtualStudy.getId())) {
          actualVirtualStudyToPatientId.put(virtualStudy.getId(), new HashSet<>());
        }
        actualVirtualStudyToPatientId.get(virtualStudy.getId()).add(patientId);
      } else {
        materialisedStudyIds.add(studyId);
        materialisedPatientIds.add(patientId);
      }
    }
    List<ClinicalEvent> result = new ArrayList<>();
    if (!materialisedStudyIds.isEmpty() && !materialisedPatientIds.isEmpty()) {
      result.addAll(
          clinicalEventRepository.getClinicalEventsMeta(
              materialisedStudyIds, materialisedPatientIds, clinicalEvents));
    }
    if (!actualVirtualStudyToPatientId.isEmpty()) {
      for (Map.Entry<String, Set<String>> entry : actualVirtualStudyToPatientId.entrySet()) {
        String virtualStudyId = entry.getKey();
        Set<String> patientIdsSet = entry.getValue();
        VirtualStudy virtualStudy = virtualStudyById.get(virtualStudyId);
        List<Patient> vsPatients = getVsPatients(virtualStudy);
        Map<String, Patient> vsPatientMap =
            vsPatients.stream()
                .collect(
                    Collectors.toMap(
                        p -> p.getCancerStudyIdentifier() + "_" + p.getStableId(), p -> p));
        List<String> studyIds2 = new ArrayList<>();
        List<String> patientIds2 = new ArrayList<>();
        for (String patientId : patientIdsSet) {
          if (vsPatientMap.containsKey(patientId)) {
            Patient patient = vsPatientMap.get(patientId);
            studyIds2.add(patient.getCancerStudyIdentifier());
            patientIds2.add(patient.getStableId());
          }
        }
        result.addAll(
            clinicalEventRepository
                .getClinicalEventsMeta(studyIds2, patientIds2, clinicalEvents)
                .stream()
                .map(ce -> virtualizeClinicalEvent(virtualStudyId, ce))
                .toList());
      }
    }
    return result;
  }
}
