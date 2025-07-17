package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.toStudyAndSampleIdLists;
import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.toStudySamplePairs;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.CopyNumberSegmentRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.CopyNumberSegmentSortBy;

public class VSAwareCopyNumberSegmentRepository implements CopyNumberSegmentRepository {
  private final VirtualStudyService virtualStudyService;
  private final CopyNumberSegmentRepository copyNumberSegmentRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareCopyNumberSegmentRepository(
      VirtualStudyService virtualStudyService,
      CopyNumberSegmentRepository copyNumberSegmentRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualStudyService = virtualStudyService;
    this.copyNumberSegmentRepository = copyNumberSegmentRepository;
    this.sampleListRepository = sampleListRepository;
  }

  @Override
  public List<CopyNumberSeg> getCopyNumberSegmentsInSampleInStudy(
      String studyId,
      String sampleId,
      String chromosome,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    Stream<CopyNumberSeg> resultStream =
        fetchCopyNumberSegments(List.of(studyId), List.of(sampleId), chromosome, projection)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Comparator<CopyNumberSeg> composeComparator(String sortBy, String direction) {
    CopyNumberSegmentSortBy cns = CopyNumberSegmentSortBy.valueOf(sortBy);
    Comparator<CopyNumberSeg> result =
        switch (cns) {
          case chromosome -> Comparator.comparing(CopyNumberSeg::getChr);
          case start -> Comparator.comparing(CopyNumberSeg::getStart);
          case end -> Comparator.comparing(CopyNumberSeg::getEnd);
          case numberOfProbes -> Comparator.comparing(CopyNumberSeg::getNumProbes);
          case segmentMean -> Comparator.comparing(CopyNumberSeg::getSegmentMean);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public BaseMeta getMetaCopyNumberSegmentsInSampleInStudy(
      String studyId, String sampleId, String chromosome) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(
        getCopyNumberSegmentsInSampleInStudy(
                studyId, sampleId, chromosome, null, null, null, null, null)
            .size());
    return baseMeta;
  }

  @Override
  public List<StudyScopedId> fetchSamplesWithCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome) {
    return fetchCopyNumberSegments(studyIds, sampleIds, chromosome, Projection.ID.name()).stream()
        .map(
            segment ->
                new StudyScopedId(segment.getCancerStudyIdentifier(), segment.getSampleStableId()))
        .toList();
  }

  @Override
  public List<CopyNumberSeg> fetchCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome, String projection) {
    Map<StudyScopedId, Set<String>> mapping =
        virtualStudyService.toMaterializedStudySamplePairsMap(
            toStudySamplePairs(studyIds, sampleIds));
    Pair<List<String>, List<String>> pair = toStudyAndSampleIdLists(mapping.keySet());
    List<String> materializedStudyIds = pair.getLeft();
    List<String> materializedSampleIds = pair.getRight();
    List<CopyNumberSeg> segments =
        copyNumberSegmentRepository.fetchCopyNumberSegments(
            materializedStudyIds, materializedSampleIds, chromosome, projection);
    return segments.stream()
        .flatMap(
            segment ->
                mapping
                    .get(
                        new StudyScopedId(
                            segment.getCancerStudyIdentifier(), segment.getSampleStableId()))
                    .stream()
                    .map(
                        studyId ->
                            studyId.equals(segment.getCancerStudyIdentifier())
                                ? segment
                                : virtualizeCopyNumberSeg(studyId, segment)))
        .toList();
  }

  private CopyNumberSeg virtualizeCopyNumberSeg(String virtualStudyId, CopyNumberSeg segment) {
    CopyNumberSeg virtualSegment = new CopyNumberSeg();
    virtualSegment.setCancerStudyIdentifier(virtualStudyId);
    virtualSegment.setSampleStableId(segment.getSampleStableId());
    // TODO we need to decide if we want to use internal IDs or not
    virtualSegment.setSampleId(segment.getSampleId());

    virtualSegment.setPatientId(segment.getPatientId());
    virtualSegment.setChr(segment.getChr());
    virtualSegment.setStart(segment.getStart());
    virtualSegment.setEnd(segment.getEnd());
    virtualSegment.setNumProbes(segment.getNumProbes());
    virtualSegment.setSegmentMean(segment.getSegmentMean());
    return virtualSegment;
  }

  @Override
  public BaseMeta fetchMetaCopyNumberSegments(
      List<String> studyIds, List<String> sampleIds, String chromosome) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(fetchCopyNumberSegments(studyIds, sampleIds, chromosome, null).size());
    return baseMeta;
  }

  @Override
  public List<CopyNumberSeg> getCopyNumberSegmentsBySampleListId(
      String studyId, String sampleListId, String chromosome, String projection) {
    List<String> sampleIds = sampleListRepository.getAllSampleIdsInSampleList(sampleListId);
    if (sampleIds.isEmpty()) {
      return List.of();
    }
    return fetchCopyNumberSegments(List.of(studyId), sampleIds, chromosome, projection);
  }
}
