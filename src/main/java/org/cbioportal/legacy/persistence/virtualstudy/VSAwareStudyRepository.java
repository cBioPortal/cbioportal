package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;

public class VSAwareStudyRepository implements StudyRepository {

  private final VirtualStudyService virtualStudyService;
  private final StudyRepository studyRepository;

  public VSAwareStudyRepository(
      VirtualStudyService virtualStudyService, StudyRepository studyRepository) {
    this.virtualStudyService = virtualStudyService;
    this.studyRepository = studyRepository;
  }

  @Override
  public List<CancerStudy> getAllStudies(
      String keyword,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    List<CancerStudy> materialisedStudies =
        studyRepository.getAllStudies(keyword, projection, null, null, null, null);
    List<CancerStudy> virtualStudies =
        virtualStudyService.getPublishedVirtualStudies(keyword).stream()
            .map(virtualStudyService::toCancerStudy)
            .toList();

    Stream<CancerStudy> resultStream =
        Stream.concat(materialisedStudies.stream(), virtualStudies.stream());

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    List<CancerStudy> studyList = resultStream.toList();
    return studyList;
  }

  private Comparator<CancerStudy> composeComparator(String sortBy, String direction) {
    StudySortBy s = StudySortBy.valueOf(sortBy);
    Comparator<CancerStudy> result =
        switch (s) {
          case studyId -> Comparator.comparing(CancerStudy::getCancerStudyId);
          case cancerTypeId -> Comparator.comparing(CancerStudy::getTypeOfCancerId);
          case name -> Comparator.comparing(CancerStudy::getName);
          case description -> Comparator.comparing(CancerStudy::getDescription);
          case publicStudy -> Comparator.comparing(CancerStudy::getPublicStudy);
          case pmid -> Comparator.comparing(CancerStudy::getPmid);
          case citation -> Comparator.comparing(CancerStudy::getCitation);
          case groups -> Comparator.comparing(CancerStudy::getGroups);
          case status -> Comparator.comparing(CancerStudy::getStatus);
          case importDate -> Comparator.comparing(CancerStudy::getImportDate);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  @Override
  public BaseMeta getMetaStudies(String keyword) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(getAllStudies(keyword, Projection.ID.name(), null, null, null, null).size());
    return meta;
  }

  @Override
  public CancerStudy getStudy(String studyId, String projection) {
    return getAllStudies(null, projection, null, null, null, null).stream()
        .filter(study -> study.getCancerStudyIdentifier().equals(studyId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<CancerStudy> fetchStudies(List<String> studyIds, String projection) {
    return getAllStudies(null, projection, null, null, null, null).stream()
        .filter(study -> studyIds.contains(study.getCancerStudyIdentifier()))
        .toList();
  }

  @Override
  public BaseMeta fetchMetaStudies(List<String> studyIds) {
    BaseMeta meta = new BaseMeta();
    meta.setTotalCount(getAllStudies(null, Projection.ID.name(), null, null, null, null).size());
    return meta;
  }

  @Override
  public CancerStudyTags getTags(String studyId) {
    return studyRepository.getTags(studyId);
  }

  @Override
  public List<CancerStudyTags> getTagsForMultipleStudies(List<String> studyIds) {
    return studyRepository.getTagsForMultipleStudies(studyIds);
  }
}
