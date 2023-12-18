package org.cbioportal.service.impl;

import jakarta.annotation.PostConstruct;
import org.cbioportal.persistence.util.CustomEhcachingProvider;
import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class CacheStatisticsServiceImpl implements CacheStatisticsService {

    @Autowired
    private CustomEhcachingProvider customEhcachingProvider;

    private javax.cache.CacheManager cacheManager;

    @Value("${cache.statistics_endpoint_enabled:false}")
    public boolean cacheStatisticsEndpointEnabled;

    @PostConstruct
    public void initializeStatisticsService () {
        cacheManager = customEhcachingProvider.getCacheManager();
    }

    protected void checkIfCacheStatisticsEndpointEnabled() {
        if (!cacheStatisticsEndpointEnabled) {
            // TODO re-implement Service module level Exception?
            throw new RuntimeException("Cache statistics is not enabled for this instance of the portal.");
        }
    }

    @Override
    public List<String> getKeyCountsPerClass(String cacheName) throws CacheNotFoundException {
        checkIfCacheStatisticsEndpointEnabled();
        Cache<String, Object> cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new CacheNotFoundException(cacheName);
        }
        Map<String, Integer> classToKeyCount = new HashMap<String, Integer>();
        Iterator<Cache.Entry<String, Object>> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry<String, Object> entry = iterator.next();
            String cacheKey = entry.getKey();
            String className = cacheKey.split("_")[0];
            int keyCount = classToKeyCount.containsKey(className) ? classToKeyCount.get(className) : 0;
            classToKeyCount.put(className, keyCount + 1);
        }
        List<String> keyCountsPerClass = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : classToKeyCount.entrySet()) {
            keyCountsPerClass.add(entry.getKey().toString() + ": " + entry.getValue().toString() + " keys");
        }
        return keyCountsPerClass;
    }

    @Override
    public List<String> getKeysInCache(String cacheName) throws CacheNotFoundException {
        checkIfCacheStatisticsEndpointEnabled();
        Cache<String, Object> cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new CacheNotFoundException(cacheName);
        }
        Integer numberOfKeys = 0;
        List<String> keysInCache = new ArrayList<String>();
        Iterator<Cache.Entry<String, Object>> iterator = cache.iterator();
        while (iterator.hasNext()) {
            Cache.Entry<String, Object> entry = iterator.next();
            keysInCache.add(entry.getKey());
            numberOfKeys += 1;
        }
        keysInCache.add("Total Number of Keys: " + numberOfKeys.toString());
        return keysInCache;
    }

    @Override
    public String getCacheStatistics() {
        throw new UnsupportedOperationException("Requested API is not implemented yet");
    }
}
