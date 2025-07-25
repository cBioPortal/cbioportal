package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.List;
import java.util.Optional;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class CachedPublishedVirtualStudyService implements VirtualStudyService {

  private final VirtualStudyService virtualStudyService;
  // TODO maybe inject Cache as a parameter instead of CacheManager
  private final CacheManager cacheManager;

  public CachedPublishedVirtualStudyService(
      VirtualStudyService virtualStudyService, CacheManager cacheManager) {
    this.virtualStudyService = virtualStudyService;
    this.cacheManager = cacheManager;
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
    Cache publishedVirtualStudies = cacheManager.getCache("publishedVirtualStudies");
    if (publishedVirtualStudies == null) {

      virtualStudyService.getPublishedVirtualStudies();
    }
    // TODO calculate key to satisfy the removing logic in CacheServiceImpl#buildEvictionRegex
    // TODO and how to detect that cahce is partially evicted?
    return publishedVirtualStudies.get("all", virtualStudyService::getPublishedVirtualStudies);
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
  public List<VirtualStudy> getPublishedVirtualStudies(String keyword) {
    return virtualStudyService.getPublishedVirtualStudies(keyword);
  }
}
