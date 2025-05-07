package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;

/**
 * This class is a silenced version of the VirtualStudyService. It overrides the
 * getPublishedVirtualStudies methods to return an empty list. This is used to silence the published
 * virtual studies in the application.
 */
public class SilencedPublishedVSService implements VirtualStudyService {
  private final VirtualStudyService virtualStudyService;

  public SilencedPublishedVSService(VirtualStudyService virtualStudyService) {
    this.virtualStudyService = virtualStudyService;
  }

  /**
   * Silenced method to return published virtual studies.
   *
   * @return empty list
   */
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies() {
    return Collections.emptyList();
  }

  /**
   * Silenced method to return published virtual studies with a keyword.
   *
   * @param keyword
   * @return empty list
   */
  @Override
  public List<VirtualStudy> getPublishedVirtualStudies(String keyword) {
    return Collections.emptyList();
  }

  @Override
  public VirtualStudy getVirtualStudy(String id) {
    return virtualStudyService.getVirtualStudy(id);
  }

  @Override
  public Optional<VirtualStudy> getVirtualStudyByIdIfExists(String id) {
    return virtualStudyService.getVirtualStudyByIdIfExists(id);
  }

  @Override
  public List<VirtualStudy> getUserVirtualStudies(String user) {
    return virtualStudyService.getUserVirtualStudies(user);
  }

  @Override
  public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
    virtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid);
  }

  @Override
  public void unPublishVirtualStudy(String id) {
    virtualStudyService.unPublishVirtualStudy(id);
  }

  @Override
  public CancerStudy toCancerStudy(VirtualStudy vs) {
    return virtualStudyService.toCancerStudy(vs);
  }
}
