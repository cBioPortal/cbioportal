package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;

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
  public void publishVirtualStudy(
      String id, String typeOfCancerId, String pmid, VirtualStudyData virtualStudyData) {
    virtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid, virtualStudyData);
  }

  @Override
  public void unPublishVirtualStudy(String id) {
    virtualStudyService.unPublishVirtualStudy(id);
  }

  @Override
  public void dropPublicVirtualStudy(String id) {
    virtualStudyService.dropPublicVirtualStudy(id);
  }
}
