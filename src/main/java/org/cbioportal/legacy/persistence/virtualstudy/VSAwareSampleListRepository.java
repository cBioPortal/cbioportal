package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.model.SampleListToSampleId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.SampleListSortBy;

public class VSAwareSampleListRepository implements SampleListRepository {

  private final VirtualStudyService virtualStudyService;
  private final SampleListRepository sampleListRepository;

  public VSAwareSampleListRepository(
      VirtualStudyService virtualStudyService, SampleListRepository sampleListRepository) {
    this.virtualStudyService = virtualStudyService;
    this.sampleListRepository = sampleListRepository;
  }

  @Override
  public List<SampleList> getAllSampleLists(
      String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
    List<SampleList> sampleLists =
        sampleListRepository.getAllSampleLists(projection, null, null, null, null);
    List<SampleList> virtualSampleLists = getVirtualSampleLists(sampleLists);
    Stream<SampleList> resultStream =
        Stream.concat(sampleLists.stream(), virtualSampleLists.stream());

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }
    return resultStream.toList();
  }

  private Comparator<SampleList> composeComparator(String sortBy, String direction) {
    SampleListSortBy psb = SampleListSortBy.valueOf(sortBy);
    Comparator<SampleList> result =
        switch (psb) {
          case sampleListId -> Comparator.comparing(SampleList::getStableId);
          case category -> Comparator.comparing(SampleList::getCategory);
          case studyId -> Comparator.comparing(SampleList::getCancerStudyIdentifier);
          case name -> Comparator.comparing(SampleList::getName);
          case description -> Comparator.comparing(SampleList::getDescription);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  private List<SampleList> getVirtualSampleLists(List<SampleList> sampleLists) {
    Map<String, Map<String, Set<String>>> studyToSampleToVirtualStudyIds =
        virtualStudyService.getPublishedVirtualStudies().stream()
            .flatMap(
                vs ->
                    vs.getData().getStudies().stream()
                        .flatMap(
                            vss ->
                                vss.getSamples().stream()
                                    .map(s -> ImmutableTriple.of(vss.getId(), s, vs.getId()))))
            .collect(
                Collectors.groupingBy(
                    ImmutableTriple::getLeft,
                    Collectors.groupingBy(
                        ImmutableTriple::getMiddle,
                        Collectors.mapping(ImmutableTriple::getRight, Collectors.toSet()))));
    List<SampleList> result = new ArrayList<>();
    for (SampleList sampleList : sampleLists) {
      String studyId = sampleList.getCancerStudyIdentifier();
      if (studyToSampleToVirtualStudyIds.containsKey(studyId)) {
        Map<String, Set<String>> sampleToVirtualStudyIds =
            studyToSampleToVirtualStudyIds.get(studyId);
        Map<String, Set<String>> virtualStudyIdsBySample = new HashMap<>();
        List<String> sampleIds =
            sampleListRepository.getAllSampleIdsInSampleList(sampleList.getStableId());
        for (String sampleId : sampleIds) {
          if (sampleToVirtualStudyIds.containsKey(sampleId)) {
            Set<String> virtualStudyIds = sampleToVirtualStudyIds.get(sampleId);
            String vsStudyId = virtualStudyService.calculateVirtualSampleId(studyId, sampleId);
            for (String virtualStudyId : virtualStudyIds) {
              virtualStudyIdsBySample
                  .computeIfAbsent(virtualStudyId, k -> new HashSet<>())
                  .add(vsStudyId);
            }
          }
        }
        for (Map.Entry<String, Set<String>> entry : virtualStudyIdsBySample.entrySet()) {
          // TODO we have to merge sample lists of the same type with the same virtual study id
          SampleList virtualSampleList =
              getSampleList(sampleList, entry.getKey(), entry.getValue());
          result.add(virtualSampleList);
        }
      }
    }
    return result;
  }

  private static SampleList getSampleList(
      SampleList sampleList, String virtualStudyId, Set<String> virtualSampleIds) {
    SampleList virtualSampleList = new SampleList();
    virtualSampleList.setCancerStudyIdentifier(virtualStudyId);
    virtualSampleList.setStableId(calculateSampleListId(sampleList, virtualStudyId));
    virtualSampleList.setName(sampleList.getName());
    virtualSampleList.setDescription(sampleList.getDescription());
    virtualSampleList.setSampleIds(new ArrayList<>(virtualSampleIds));
    return virtualSampleList;
  }

  // TODO move to common vs service
  private static String calculateSampleListId(SampleList sampleList, String virtualStudyId) {
    return sampleList.getStableId().replace(sampleList.getCancerStudyIdentifier(), virtualStudyId);
  }

  @Override
  public BaseMeta getMetaSampleLists() {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        sampleListRepository
            .getAllSampleLists(Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public SampleList getSampleList(String sampleListId) {
    return getAllSampleLists(Projection.DETAILED.name(), null, null, null, null).stream()
        .filter(sampleList -> sampleList.getStableId().equals(sampleListId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<SampleList> getSampleLists(List<String> sampleListIds, String projection) {
    return getAllSampleLists(projection, null, null, null, null).stream()
        .filter(sampleList -> sampleListIds.contains(sampleList.getStableId()))
        .collect(Collectors.toList());
  }

  @Override
  public List<SampleList> getAllSampleListsInStudies(
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    return getAllSampleLists(projection, pageSize, pageNumber, sortBy, direction).stream()
        .filter(sampleList -> studyIds.contains(sampleList.getCancerStudyIdentifier()))
        .collect(Collectors.toList());
  }

  @Override
  public BaseMeta getMetaSampleListsInStudy(String studyId) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getAllSampleListsInStudies(List.of(studyId), Projection.ID.name(), null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<String> getAllSampleIdsInSampleList(String sampleListId) {
    // TODO vs will have sample ids merged available in the sample list
    // For regulare sample list we will need to call the repository method
    SampleList sampleList = getSampleList(sampleListId);
    return sampleList != null ? sampleList.getSampleIds() : List.of();
  }

  @Override
  public List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds) {
    // TODO deprecate and remove this method
    throw new UnsupportedOperationException("Method not implemented");
  }
}
