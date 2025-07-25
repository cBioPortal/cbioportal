package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.CopyNumberSeg;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.CopyNumberSegmentRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.CopyNumberSegmentSortBy;

public class VSAwareCopyNumberSegmentRepository implements CopyNumberSegmentRepository {
  private final VirtualizationService virtualizationService;
  private final CopyNumberSegmentRepository copyNumberSegmentRepository;
  private final VSAwareSampleListRepository sampleListRepository;

  public VSAwareCopyNumberSegmentRepository(
      VirtualizationService virtualStudyService,
      CopyNumberSegmentRepository copyNumberSegmentRepository,
      VSAwareSampleListRepository sampleListRepository) {
    this.virtualizationService = virtualStudyService;
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

    return virtualizationService.handleStudySampleData(
        studyIds,
        sampleIds,
        CopyNumberSeg::getCancerStudyIdentifier,
        CopyNumberSeg::getSampleStableId,
        // TODO takes a long time to run on study summary page
        (studyIdsList, sampleIdsList) ->
            copyNumberSegmentRepository.fetchCopyNumberSegments(
                studyIdsList, sampleIdsList, chromosome, projection),
        this::virtualizeCopyNumberSeg);
  }

  private CopyNumberSeg virtualizeCopyNumberSeg(String virtualStudyId, CopyNumberSeg segment) {
    CopyNumberSeg virtualSegment = new CopyNumberSeg();
    virtualSegment.setCancerStudyIdentifier(virtualStudyId);
    virtualSegment.setSampleStableId(segment.getSampleStableId());
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
