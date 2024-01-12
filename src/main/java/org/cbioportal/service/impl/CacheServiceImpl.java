package org.cbioportal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.persistence.cachemaputil.StaticRefCacheMapUtil;
import org.cbioportal.persistence.util.CacheUtils;
import org.cbioportal.service.CacheService;
import org.cbioportal.service.exception.CacheOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CacheServiceImpl implements CacheService {
    
    private static final Log LOG = LogFactory.getLog(CacheServiceImpl.class);
    
    @Autowired(required = false)
    private CacheManager cacheManager;

    @Autowired
    private CacheMapUtil cacheMapUtil;
    
    // When caching is disabled there is no CacheUtils bean.
    @Autowired(required = false)
    private CacheUtils cacheUtils;

    @Autowired
    private StudyRepository studyRepository;
    
    @Override
    public void clearCaches(boolean clearSpringManagedCache) throws CacheOperationException {
        
        // Flush Spring-managed caches (only when cache strategy has been defined).
        if (clearSpringManagedCache) {
            attemptEvictSpringManagedCache(".*");
        }

        // Flush cache used for user permission evaluation.
        // Only needed when using cache not managed by the Spring caches.
        if (cacheMapUtil instanceof StaticRefCacheMapUtil) {
            ((StaticRefCacheMapUtil) cacheMapUtil).initializeCacheMemory();
        }
        
        // Note: DAO classes in package org.mskcc.cbio.portal.dao do have their own
        // caching strategy. Since these classes are only used by the deprecated old
        // version of the r-library and may result in problems in the running instance
        // when flushing the cashes, we do not handle these caches in this service.
    }

    // This evicts keys from the general and static caches when updating/adding/deleting a study.
    public void clearCachesForStudy(String studyId, boolean clearSpringManagedCache) throws CacheOperationException {

        List<String> allStudyIds = studyRepository.getAllStudies(null, "SUMMARY", null, null, null, null)
            .stream()
            .map(study -> study.getCancerStudyIdentifier())
            .collect(Collectors.toList());
            
        // Flush Spring-managed caches (only when cache strategy has been defined).
        if (clearSpringManagedCache) {
            attemptEvictSpringManagedCache(buildEvictionRegex(studyId, allStudyIds));
        }

        // Flush cache used for user permission evaluation.
        // Only needed when using cache not managed by the Spring caches.
        if (cacheMapUtil instanceof StaticRefCacheMapUtil) {
            ((StaticRefCacheMapUtil) cacheMapUtil).initializeCacheMemory();
        }
        
    }
    
    private void attemptEvictSpringManagedCache(String pattern) throws CacheOperationException {
        try {
            if (cacheManager != null) {
                cacheManager.getCacheNames().stream()
                    .forEach(cacheName -> {
                        cacheUtils.evictByPattern(cacheName, pattern);
                    });
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOG.error("Error while evicting cache." + e.getMessage());
            throw new CacheOperationException("Error while evicting cache.", e);
        }
    }
    
    // Regex that selects keys that match id of deleted study
    // or lacking any study id completely. For example:
    // ^(?=.*study_id_1).*|^(?!.*study_id_1)(?!.*study_id_2)(?!.*study_id_3).*
    // https://stackoverflow.com/a/8240998/11651683
    private String buildEvictionRegex(String studyId, List<String> allStudyIds) {
        
        // make sure imported studyId is in all studies.
        List<String> all = new ArrayList<>();
        all.add(studyId);
        all.addAll(allStudyIds);

        String allIdsRegex = all.stream()
            .map(id -> "(?!.*" + id + ")")
            .collect(Collectors.joining(""));

        return "^(?=.*" + studyId + ").*|^" + allIdsRegex + ".*";
    }
    
}
