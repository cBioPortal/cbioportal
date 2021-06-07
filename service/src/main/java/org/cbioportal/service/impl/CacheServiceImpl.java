package org.cbioportal.service.impl;

import org.cbioportal.persistence.mybatis.util.CacheMapUtil;
import org.cbioportal.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheServiceImpl implements CacheService {

    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private CacheMapUtil cacheMapUtil;

    @Override
    public void evictAllCaches() {
        
        // Flush Spring-managed caches (only when cache strategy has been defined).
        if (cacheManager != null)
            cacheManager.getCacheNames().stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        
        // Flush cache used for user permission evaluation.
        cacheMapUtil.initializeCacheMemory();
        
        // Note: DAO classes in package org.mskcc.cbio.portal.dao do have their own
        // caching strategy. Since these classes are only used by the deprecated old
        // version of the r-library and may result in problems in the running instance
        // when flushing the cashes, we do not handle these caches in this service.
    }
}
