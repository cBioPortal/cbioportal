package org.cbioportal.legacy.service;

import org.cbioportal.legacy.service.exception.CacheOperationException;

public interface CacheService {
  void clearCaches(boolean springManagedCache) throws CacheOperationException;

  void clearCachesForStudy(String studyId, boolean springManagedCache)
      throws CacheOperationException;
}
