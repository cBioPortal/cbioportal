package org.cbioportal.service.impl;

import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;

import java.lang.String;
import java.util.*;
import javax.cache.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Profile({"ehcache-heap", "ehcache-disk", "ehcache-hybrid", "redis"})
public class CacheStatisticsServiceImpl implements CacheStatisticsService {

    @Autowired
    public org.springframework.cache.CacheManager cacheManager;

    @Value("${cache.statistics_endpoint_enabled:false}")
    public boolean cacheStatisticsEndpointEnabled;

    protected void checkIfCacheStatisticsEndpointEnabled() {
        if (!cacheStatisticsEndpointEnabled) {
            throw new AccessDeniedException("Cache statistics is not enabled for this instance of the portal.");
        }
    }

    @Override
    public List<String> getKeyCountsPerClass(String cacheName) throws CacheNotFoundException {
        return new ArrayList<>();
    }

    @Override
    public List<String> getKeysInCache(String cacheName) throws CacheNotFoundException {
        return new ArrayList<>();
    }

    @Override
    public String getCacheStatistics() {
        throw new UnsupportedOperationException("Requested API is not implemented yet");
    }
}
