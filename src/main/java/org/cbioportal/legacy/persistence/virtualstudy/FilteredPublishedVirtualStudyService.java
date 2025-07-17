package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.legacy.model.StudyScopedId;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

/**
 * This class is a version of the VirtualStudyService that overrides the getPublishedVirtualStudies
 * methods to return only some published virtual studies. As the rest of published virtual studies
 * will be served by the backend as a regular studies. Hence, will support study-level security.
 */
public class FilteredPublishedVirtualStudyService implements VirtualStudyService {
  private final VirtualStudyService virtualStudyService;
  private final Predicate<VirtualStudy> shouldServeAsPublishedVirtualStudy;

  public FilteredPublishedVirtualStudyService(
      VirtualStudyService virtualStudyService,
      Predicate<VirtualStudy> shouldServeAsPublishedVirtualStudy) {
    this.virtualStudyService = virtualStudyService;
    this.shouldServeAsPublishedVirtualStudy = shouldServeAsPublishedVirtualStudy;
  }

  /**
   * Modified method to return published virtual studies that satisfy the
   * `shouldServeAsPublishedVirtualStudy` filter.
   *
   * @return published virtual studies that satisfy the filter
   */
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies() {
    return virtualStudyService.getPublishedVirtualStudies().stream()
        .filter(shouldServeAsPublishedVirtualStudy)
        .toList();
  }

  /**
   * Modified method to return published virtual studies that satisfy the
   * `shouldServeAsPublishedVirtualStudy` filter.
   *
   * @param keyword
   * @return published virtual studies that satisfy the filter and match the keyword
   */
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies(String keyword) {
    return virtualStudyService.getPublishedVirtualStudies(keyword).stream()
        .filter(shouldServeAsPublishedVirtualStudy)
        .toList();
  }

  @Override
  public Set<String> getPublishedVirtualStudyIds() {
    return getPublishedVirtualStudies().stream()
        .map(VirtualStudy::getId)
        .collect(java.util.stream.Collectors.toSet());
  }

  @Override
  public Map<StudyScopedId, Set<String>> toMaterializedStudySamplePairsMap(
      List<StudyScopedId> studyScopedIds) {
    return virtualStudyService.toMaterializedStudySamplePairsMap(studyScopedIds);
  }

  @Override
  public Map<StudyScopedId, Set<String>> toMaterializedStudyPatientPairsMap(
      List<StudyScopedId> studyScopedIds) {
    return virtualStudyService.toMaterializedStudyPatientPairsMap(studyScopedIds);
  }

  @Override
  public Map<String, Pair<String, String>> toMolecularProfileInfo(Set<String> molecularProfileIds) {
    return virtualStudyService.toMolecularProfileInfo(molecularProfileIds);
  }

  @Override
  public VirtualStudy getVirtualStudy(String id) {
    return getVirtualStudyByIdIfExists(id).orElse(null);
  }

  @Override
  public Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id) {
    var result = virtualStudyService.getVirtualStudyByIdIfExists(id);
    if (result.isEmpty()) {
      return Optional.empty();
    }
    if (result.get().getData().isPublished()
        && !shouldServeAsPublishedVirtualStudy.test(result.get())) {
      return Optional.empty();
    }
    return result;
  }

  @Override
  public List<VirtualStudy> getUserVirtualStudies(String user) {
    return virtualStudyService.getUserVirtualStudies(user).stream()
        .filter(vs -> !vs.getData().isPublished() || shouldServeAsPublishedVirtualStudy.test(vs))
        .toList();
  }

  @Override
  public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
    virtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid);
  }

  @Override
  public void unPublishVirtualStudy(String id) {
    virtualStudyService.unPublishVirtualStudy(id);
  }
}
