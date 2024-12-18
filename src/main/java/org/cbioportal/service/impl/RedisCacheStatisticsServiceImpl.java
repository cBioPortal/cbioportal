package org.cbioportal.service.impl;

import org.cbioportal.persistence.util.CustomKeyGenerator;
import org.cbioportal.persistence.util.CustomRedisCache;
import org.cbioportal.service.CacheStatisticsService;
import org.cbioportal.service.exception.CacheNotFoundException;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnExpression(
    "#{environment['persistence.cache_type'] == 'redis' or environment['persistence.cache_type_clickhouse'] == 'redis'}"
)
public class RedisCacheStatisticsServiceImpl implements CacheStatisticsService {

    @Autowired
    public CacheManager cacheManager;

    @Value("${cache.statistics_endpoint_enabled:false}")
    public boolean cacheStatisticsEndpointEnabled;

    protected void checkIfCacheStatisticsEndpointEnabled() {
        if (!cacheStatisticsEndpointEnabled) {
            throw new AccessDeniedException("Cache statistics is not enabled for this instance of the portal.");
        }
    }

    @Override
    public List<String> getKeyCountsPerClass(String cacheName) throws CacheNotFoundException {
        checkIfCacheStatisticsEndpointEnabled();
        if (!cacheManager.getCacheNames().contains(cacheName)) {
            throw new CacheNotFoundException(cacheName);
        }

        Cache cache = cacheManager.getCache(cacheName);
        if (cache instanceof CustomRedisCache) {
            CustomRedisCache redisCache = (CustomRedisCache) cache;
            Map<String, Long> keyCountPerClass = redisCache.getNativeCache()
                .getKeys()
                .getKeysStream()
                .filter(k -> k.startsWith(redisCache.getName()))
                // cut off cache name from key
                .map(k -> k.substring(redisCache.getName().length() + CustomRedisCache.DELIMITER.length()))
                // cut off everything after the class of the class of the cached method
                .map(k -> k.substring(0, k.indexOf(CustomKeyGenerator.CACHE_KEY_PARAM_DELIMITER)))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            
            return keyCountPerClass.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue() + " keys")
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

    @Override
    public List<String> getKeysInCache(String cacheName) throws CacheNotFoundException {
        checkIfCacheStatisticsEndpointEnabled();
        if (!cacheManager.getCacheNames().contains(cacheName)) {
            throw new CacheNotFoundException(cacheName);
        }
        
        Cache cache = cacheManager.getCache(cacheName);        
        
        if (cache instanceof CustomRedisCache) {
            CustomRedisCache redisCache = (CustomRedisCache) cache;
            return redisCache.getNativeCache()
                .getKeys()
                .getKeysStream()
                .filter(k -> k.startsWith(redisCache.getName()))
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }

    @Override
    public String getCacheStatistics() {
        throw new UnsupportedOperationException("Requested API is not implemented yet");
    }
}
