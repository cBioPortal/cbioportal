package org.cbioportal.legacy.persistence.virtualstudy;

import static org.cbioportal.legacy.persistence.virtualstudy.VirtualisationUtils.checkSingleSourceStudy;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.CancerStudyTags;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.CancerTypeRepository;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;

public class VSAwareStudyRepository implements StudyRepository {

  private final VirtualizationService virtualizationService;
  private final StudyRepository studyRepository;
  private final CancerTypeRepository cancerTypeRepository;

  public VSAwareStudyRepository(
      VirtualizationService virtualizationService,
      StudyRepository studyRepository,
      CancerTypeRepository cancerTypeRepository) {
    this.virtualizationService = virtualizationService;
    this.studyRepository = studyRepository;
    this.cancerTypeRepository = cancerTypeRepository;
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
        virtualizationService.getPublishedVirtualStudies().stream()
            .filter(virtualStudyKeywordFilter(keyword))
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

  private static Predicate<? super VirtualStudy> virtualStudyKeywordFilter(String keyword) {
    if (keyword == null || keyword.isEmpty()) {
      return virtualStudy -> true;
    }
    var lcKeyword = keyword.toLowerCase();
    return virtualStudy -> {
      VirtualStudyData data = virtualStudy.getData();
      return (data.getName() != null && data.getName().toLowerCase().contains(lcKeyword))
          || (data.getDescription() != null
              && data.getDescription().toLowerCase().contains(lcKeyword));
    };
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
    checkSingleSourceStudy(vs);
    String studyId = vs.getData().getStudies().iterator().next().getId();
    CancerStudy referredStudy = studyRepository.getStudy(studyId, Projection.DETAILED.name());
    VirtualStudyData vsd = vs.getData();
    CancerStudy cs = new CancerStudy();
    cs.setCancerStudyIdentifier(vs.getId());
    cs.setName(vsd.getName());
    cs.setDescription(vsd.getDescription());
    cs.setPmid(vsd.getPmid());
    cs.setReferenceGenome(referredStudy.getReferenceGenome());
    String typeOfCancerId = vsd.getTypeOfCancerId();
    if (typeOfCancerId != null && !typeOfCancerId.isEmpty()) {
      cs.setTypeOfCancer(cancerTypeRepository.getCancerType(typeOfCancerId));
    } else {
      String cancerTypeId = referredStudy.getTypeOfCancerId();
      cs.setTypeOfCancer(cancerTypeRepository.getCancerType(cancerTypeId));
      cs.setTypeOfCancerId(cancerTypeId);
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
