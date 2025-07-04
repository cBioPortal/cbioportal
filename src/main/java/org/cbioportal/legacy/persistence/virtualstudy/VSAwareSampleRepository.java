package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.sort.SampleSortBy;

public class VSAwareSampleRepository implements SampleRepository {

  private SampleRepository sampleRepository;
  private VirtualStudyService virtualStudyService;

  public VSAwareSampleRepository(
      VirtualStudyService virtualStudyService, SampleRepository sampleRepository) {
    this.virtualStudyService = virtualStudyService;
    this.sampleRepository = sampleRepository;
  }

  @Override
  public List<Sample> getAllSamples(
      String keyword,
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sort,
      String direction) {
    Stream<Sample> resultStream = fetchSamples(studyIds, projection).stream();
    if (keyword != null) {
      resultStream =
          resultStream.filter(
              sample -> sample.getStableId().toLowerCase().contains(keyword.toLowerCase()));
    }

    if (sort != null) {
      resultStream = resultStream.sorted(composeComparator(sort, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  private Comparator<Sample> composeComparator(String sortBy, String direction) {
    SampleSortBy ca = SampleSortBy.valueOf(sortBy);
    Comparator<Sample> result =
        switch (ca) {
          case sampleId -> Comparator.comparing(Sample::getStableId);
          case sampleType -> Comparator.comparing(Sample::getSampleType);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository
            .getAllSamples(keyword, studyIds, Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<Sample> getAllSamplesInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSamples(
        null, List.of(studyId), projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public BaseMeta getMetaSamplesInStudy(String studyId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository
            .getAllSamplesInStudy(studyId, Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<Sample> getAllSamplesInStudies(
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSamples(null, studyIds, projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public Sample getSampleInStudy(String studyId, String sampleId) {
    return fetchSamples(List.of(studyId), List.of(sampleId), Projection.DETAILED.name()).stream()
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Sample> getAllSamplesOfPatientInStudy(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSamplesInStudy(studyId, projection, pageSize, pageNumber, sortBy, direction)
        .stream()
        .filter(sample -> sample.getPatientStableId().equals(patientId))
        .toList();
  }

  @Override
  public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository
            .getAllSamplesOfPatientInStudy(
                studyId, patientId, Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<Sample> getAllSamplesOfPatientsInStudy(
      String studyId, List<String> patientIds, String projection) {
    return getAllSamplesInStudy(studyId, projection, null, null, null, null).stream()
        .filter(sample -> patientIds.contains(sample.getPatientStableId()))
        .toList();
  }

  @Override
  public List<Sample> getSamplesOfPatientsInMultipleStudies(
      List<String> studyIds, List<String> patientIds, String projection) {
    return getAllSamplesInStudies(studyIds, projection, null, null, null, null).stream()
        .filter(sample -> patientIds.contains(sample.getPatientStableId()))
        .toList();
  }

  // TODO simplify this method by reusing the logic from fetchSamples(List<String> studyIds,
  // List<String> sampleIds, String projection)
  public List<Sample> fetchSamples(List<String> studyIds, String projection) {
    List<VirtualStudy> allVirtualStudies = virtualStudyService.getPublishedVirtualStudies();
    Map<String, VirtualStudy> allVirtualStudyIds =
        allVirtualStudies.stream()
            .collect(Collectors.toMap(VirtualStudy::getId, virtualStudy -> virtualStudy));
    List<String> materializedStudyIds = new ArrayList<>();
    List<String> virtualStudyIds = new ArrayList<>();
    for (int i = 0; i < studyIds.size(); i++) {
      String studyId = studyIds.get(i);
      if (allVirtualStudyIds.containsKey(studyId)) {
        virtualStudyIds.add(studyId);
      } else {
        materializedStudyIds.add(studyId);
      }
    }
    List<Sample> resultSamples = new ArrayList<>();
    if (!materializedStudyIds.isEmpty()) {
      resultSamples.addAll(sampleRepository.fetchSamples(materializedStudyIds, null, projection));
    }
    if (!virtualStudyIds.isEmpty()) {
      LinkedHashSet<String> vMaterializedStudyIds = new LinkedHashSet<>();
      LinkedHashSet<String> vMaterializedSampleIds = new LinkedHashSet<>();
      Map<ImmutablePair<String, String>, LinkedHashSet<String>>
          virtualStudyIdsByMaterializedSamples = new HashMap<>();
      for (int i = 0; i < virtualStudyIds.size(); i++) {
        String virtualStudyId = virtualStudyIds.get(i);
        VirtualStudy virtualStudy = allVirtualStudyIds.get(virtualStudyId);
        virtualStudy.getData().getStudies().stream()
            .flatMap(vss -> vss.getSamples().stream().map(s -> new ImmutablePair<>(vss.getId(), s)))
            .forEach(
                pair -> {
                  vMaterializedStudyIds.add(pair.getLeft());
                  vMaterializedSampleIds.add(pair.getRight());
                  virtualStudyIdsByMaterializedSamples.computeIfAbsent(
                      pair, k -> new LinkedHashSet<>());
                  virtualStudyIdsByMaterializedSamples.get(pair).add(virtualStudyId);
                });
      }
      for (Sample sample :
          sampleRepository.fetchSamples(
              vMaterializedStudyIds.stream().toList(),
              vMaterializedSampleIds.stream().toList(),
              projection)) {
        LinkedHashSet<String> sampleRequestingVirtualStudyIds =
            virtualStudyIdsByMaterializedSamples.get(
                ImmutablePair.of(sample.getCancerStudyIdentifier(), sample.getStableId()));
        if (sampleRequestingVirtualStudyIds == null || sampleRequestingVirtualStudyIds.isEmpty()) {
          throw new IllegalStateException(
              "Virtual study IDs not found for materialized sample: "
                  + sample.getCancerStudyIdentifier()
                  + "_"
                  + sample.getStableId());
        }
        sampleRequestingVirtualStudyIds.forEach(
            virtualStudyId ->
                resultSamples.add(virtualStudyService.virtualizeSample(virtualStudyId, sample)));
      }
    }
    return resultSamples;
  }

  @Override
  public List<Sample> fetchSamples(
      List<String> studyIds, List<String> sampleIds, String projection) {
    List<Sample> resultSamples = new ArrayList<>();
    Map<StudyScopedId, Set<String>> materialisedStudySamplePairToStudyIds =
        virtualStudyService.toMaterializedStudySamplePairsMap(
            virtualStudyService.toStudySamplePairs(studyIds, sampleIds));
    if (materialisedStudySamplePairToStudyIds.isEmpty()) {
      return resultSamples; // No materialized study-sample pairs found
    }
    Pair<List<String>, List<String>> studyIdsAndSampleIds =
        virtualStudyService.toStudyAndSampleIdLists(materialisedStudySamplePairToStudyIds.keySet());
    Set<String> virtualStudyIds = virtualStudyService.getPublishedVirtualStudyIds();
    for (Sample sample :
        sampleRepository.fetchSamples(
            studyIdsAndSampleIds.getLeft(), studyIdsAndSampleIds.getRight(), projection)) {
      Set<String> sampleForStudyIds =
          materialisedStudySamplePairToStudyIds.get(
              new StudyScopedId(sample.getCancerStudyIdentifier(), sample.getStableId()));
      for (String studyId : sampleForStudyIds) {
        if (virtualStudyIds.contains(studyId)) {
          resultSamples.add(virtualStudyService.virtualizeSample(studyId, sample));
        } else {
          resultSamples.add(sample);
        }
      }
    }
    return resultSamples;
  }

  @Override
  public List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection) {
    // FIXME
    return sampleRepository.fetchSamplesBySampleListIds(sampleListIds, projection);
  }

  @Override
  public List<Sample> fetchSampleBySampleListId(String sampleListIds, String projection) {
    // FIXME
    return sampleRepository.fetchSampleBySampleListId(sampleListIds, projection);
  }

  @Override
  public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleRepository.fetchSamples(studyIds, sampleIds, Projection.ID.name()).size());
    return baseMeta;
  }

  @Override
  public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
    return sampleRepository.fetchMetaSamples(sampleListIds);
  }

  @Override
  public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
    return sampleRepository.getSamplesByInternalIds(internalIds);
  }
}
