package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.cbioportal.legacy.web.parameter.sort.PatientSortBy;

public class VSAwarePatientRepository implements PatientRepository {

  private final VirtualizationService virtualizationService;
  private final PatientRepository patientRepository;

  public VSAwarePatientRepository(
      VirtualizationService virtualizationService, PatientRepository patientRepository) {
    this.virtualizationService = virtualizationService;
    this.patientRepository = patientRepository;
  }

  @Override
  public List<Patient> getAllPatients(
      String keyword,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    List<Patient> materialisedPatients =
        patientRepository.getAllPatients(keyword, projection, null, null, null, null);
    List<Patient> virtualPatients =
        virtualizationService.getPublishedVirtualStudies().stream()
            .flatMap(
                virtualStudy -> {
                  List<String> studyIds =
                      virtualStudy.getData().getStudies().stream()
                          .flatMap(s -> s.getSamples().stream().map(s1 -> s.getId()))
                          .toList();
                  List<String> sampleIds =
                      virtualStudy.getData().getStudies().stream()
                          .flatMap(s -> s.getSamples().stream())
                          .toList();
                  if (studyIds.size() != sampleIds.size()) {
                    throw new IllegalStateException(
                        "Virtual study "
                            + virtualStudy.getId()
                            + " has different number of study ids and sample ids");
                  }
                  return patientRepository.getPatientsOfSamples(studyIds, sampleIds).stream()
                      .filter(p -> containsKeyword(p, keyword))
                      .map(p -> virtualizePatient(virtualStudy.getId(), p));
                })
            .toList();

    Stream<Patient> resultStream =
        Stream.concat(materialisedPatients.stream(), virtualPatients.stream());

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  private boolean containsKeyword(Patient patient, String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return true;
    }
    return patient.getStableId().contains(keyword);
  }

  private Patient virtualizePatient(String virtualStudyId, Patient patient) {
    var virtualPatient = new Patient();
    virtualPatient.setStableId(patient.getStableId());
    virtualPatient.setCancerStudyIdentifier(virtualStudyId);
    return virtualPatient;
  }

  private Comparator<Patient> composeComparator(String sortBy, String direction) {
    PatientSortBy psb = PatientSortBy.valueOf(sortBy);
    Comparator<Patient> result =
        switch (psb) {
          case patientId -> Comparator.comparing(Patient::getStableId);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public BaseMeta getMetaPatients(String keyword) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(getAllPatients(keyword, null, null, null, null, null).size());
    return baseMeta;
  }

  @Override
  public List<Patient> getAllPatientsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllPatients(null, projection, pageSize, pageNumber, sortBy, direction).stream()
        .filter(patient -> patient.getCancerStudyIdentifier().equals(studyId))
        .toList();
  }

  @Override
  public BaseMeta getMetaPatientsInStudy(String studyId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getAllPatientsInStudy(studyId, Projection.ID.name(), null, null, null, null).size());
    return baseMeta;
  }

  @Override
  public Patient getPatientInStudy(String studyId, String patientId) {
    return getAllPatientsInStudy(studyId, Projection.DETAILED.name(), null, null, null, null)
        .stream()
        .filter(patient -> patient.getStableId().equals(patientId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Patient> fetchPatients(
      List<String> studyIds, List<String> patientIds, String projection) {
    var pairs =
        IntStream.range(0, patientIds.size())
            .mapToObj(i -> ImmutablePair.of(studyIds.get(i), patientIds.get(i)))
            .collect(Collectors.toSet());
    return getAllPatients(null, projection, null, null, null, null).stream()
        .filter(
            patient ->
                pairs.contains(
                    ImmutablePair.of(patient.getCancerStudyIdentifier(), patient.getStableId())))
        .toList();
  }

  @Override
  public BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(fetchPatients(studyIds, patientIds, Projection.ID.name()).size());
    return baseMeta;
  }

  @Override
  public List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds) {
    Map<String, VirtualStudy> virtualStudyMap =
        virtualizationService.getPublishedVirtualStudies().stream()
            .collect(Collectors.toMap(VirtualStudy::getId, vs -> vs));
    List<String> materialisedStudyIds = new ArrayList<>();
    List<String> materialisedSampleIds = new ArrayList<>();
    Map<String, Set<String>> actualVirtualStudyToSample = new HashMap<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      String sampleId = sampleIds.get(i);
      if (virtualStudyMap.containsKey(studyId)) {
        VirtualStudy virtualStudy = virtualStudyMap.get(studyId);
        if (!actualVirtualStudyToSample.containsKey(virtualStudy.getId())) {
          actualVirtualStudyToSample.put(virtualStudy.getId(), new HashSet<>());
        }
        actualVirtualStudyToSample.get(virtualStudy.getId()).add(sampleId);
      } else {
        materialisedStudyIds.add(studyId);
        materialisedSampleIds.add(sampleId);
      }
    }

    Stream<Patient> materialisedPatients =
        materialisedStudyIds.isEmpty()
            ? Stream.empty()
            : patientRepository
                .getPatientsOfSamples(materialisedStudyIds, materialisedSampleIds)
                .stream();
    Stream<Patient> virtualPatients =
        actualVirtualStudyToSample.entrySet().stream()
            .flatMap(
                entry -> {
                  String virtualStudyId = entry.getKey();
                  Set<String> sampleIdsSet = entry.getValue();
                  VirtualStudy virtualStudy = virtualStudyMap.get(virtualStudyId);
                  List<String> studyIds2 = new ArrayList<>();
                  List<String> sampleIds2 = new ArrayList<>();
                  for (VirtualStudySamples vss : virtualStudy.getData().getStudies()) {
                    String studyId = vss.getId();
                    for (String sampleId : vss.getSamples()) {
                      if (sampleIdsSet.contains(sampleId)) {
                        studyIds2.add(studyId);
                        sampleIds2.add(sampleId);
                      }
                    }
                  }
                  return patientRepository.getPatientsOfSamples(studyIds2, sampleIds2).stream()
                      .map(p -> virtualizePatient(virtualStudyId, p));
                });

    return Stream.concat(materialisedPatients, virtualPatients).toList();
  }
}
