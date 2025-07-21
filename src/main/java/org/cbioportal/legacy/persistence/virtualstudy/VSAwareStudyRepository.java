package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.service.CancerTypeService;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.service.exception.CancerTypeNotFoundException;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;

public class VSAwareStudyRepository implements StudyRepository {

  private final VirtualStudyService virtualStudyService;
  private final StudyRepository studyRepository;
  private final CancerTypeService cancerTypeService;

  public VSAwareStudyRepository(
      VirtualStudyService virtualStudyService,
      StudyRepository studyRepository,
      CancerTypeService cancerTypeService) {
    this.virtualStudyService = virtualStudyService;
    this.studyRepository = studyRepository;
    this.cancerTypeService = cancerTypeService;
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
            .map(this::toCancerStudy)
            .toList();

    Stream<CancerStudy> resultStream =
        Stream.concat(materialisedStudies.stream(), virtualStudies.stream());

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  /**
   * Converts a VirtualStudy object to a CancerStudy object.
   *
   * @param vs the VirtualStudy object to convert
   * @param vs - the VirtualStudy object to convert
   * @return the converted CancerStudy object
   * @return the converted CancerStudy object
   */
  // TODO: check if sample counts of the bean are still used
  public CancerStudy toCancerStudy(VirtualStudy vs) {
    VirtualStudyData vsd = vs.getData();
    CancerStudy cs = new CancerStudy();
    cs.setCancerStudyIdentifier(vs.getId());
    cs.setName(vsd.getName());
    cs.setDescription(vsd.getDescription());
    cs.setPmid(vsd.getPmid());
    // TODO has to be calculated based on the study view filter
    cs.setReferenceGenome("hg19");
    String typeOfCancerId = vsd.getTypeOfCancerId();
    if (typeOfCancerId != null && !typeOfCancerId.isEmpty()) {
      try {
        cs.setTypeOfCancer(cancerTypeService.getCancerType(typeOfCancerId));
      } catch (CancerTypeNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      try {
        cs.setTypeOfCancer(cancerTypeService.getCancerType("acc"));
        cs.setTypeOfCancerId("acc");
      } catch (CancerTypeNotFoundException e) {
        throw new RuntimeException(e);
      }
      // FIXME the study won't be shown on the landing page if there is no such type of cancer
      //            cs.setTypeOfCancer(mixedTypeOfCancer);
      //            cs.setTypeOfCancerId(mixedTypeOfCancer.getTypeOfCancerId());
    }
    cs.setAllSampleCount(
        vsd.getStudies().stream().map(s -> s.getSamples().size()).reduce(0, Integer::sum));
    // TODO add sample counts based on sample lists
    cs.setGroups("");
    return cs;
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
