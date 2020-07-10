package org.cbioportal.service.impl;

import org.cbioportal.persistence.util.EhCacheStatistics;
import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;

import java.lang.String;
import java.util.*;
import javax.cache.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class CacheStatisticsServiceImpl implements CacheStatisticsService {

    @Autowired
    public CacheManager cacheManager;

    @Autowired
    public EhCacheStatistics ehCacheStatistics;

    @Value("${cache.statistics_endpoint_enabled:false}")
    public boolean cacheStatisticsEndpointEnabled;

    private void checkIfCacheStatisticsEndpointEnabled() {
        if (!cacheStatisticsEndpointEnabled) {
            throw new AccessDeniedException("Cache statistics is not enabled for this instance of the portal.");
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
        checkIfCacheStatisticsEndpointEnabled();
        return ehCacheStatistics.getCacheStatistics();
    }
}
