package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Optional;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.springframework.cache.Cache;

public class CacheEvictPublishedVirtualStudyService implements VirtualStudyService {

  private final Cache cache;
  private final VirtualStudyService virtualStudyService;

  public CacheEvictPublishedVirtualStudyService(
      Cache cache, VirtualStudyService virtualStudyService) {
    this.cache = cache;
    this.virtualStudyService = virtualStudyService;
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
  public List<VirtualStudy> getPublishedVirtualStudies() {
    return virtualStudyService.getPublishedVirtualStudies();
  }

  @Override
  public void publishVirtualStudy(String id, String typeOfCancerId, String pmid) {
    virtualStudyService.publishVirtualStudy(id, typeOfCancerId, pmid);
    cache.clear();
  }

  @Override
  public void unPublishVirtualStudy(String id) {
    virtualStudyService.unPublishVirtualStudy(id);
    cache.clear();
  }
}
