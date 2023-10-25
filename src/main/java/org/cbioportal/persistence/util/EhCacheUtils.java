package org.cbioportal.persistence.util;


import jakarta.annotation.PostConstruct;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "persistence.cache_type", havingValue = {"ehcache-heap", "ehcache-disk", "ehcache-hybrid"})
public class EhCacheUtils implements CacheUtils {
    
    @Autowired
    private CustomEhcachingProvider customEhcachingProvider;
    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        this.cacheManager = customEhcachingProvider.getCacheManager();
    }
    
    @Override
    public List<String> getKeys(String cacheName) throws IllegalArgumentException {
        javax.cache.Cache<String, Object> cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalArgumentException("Cannot find cache with name '" + cacheName + "'");
        }
        List<String> keysInCache = new ArrayList<>();
        cache.iterator().forEachRemaining(entry -> keysInCache.add(entry.getKey()));
        return keysInCache;
    }

    @Override
    public void evictByPattern(String cacheName, String pattern) {
        javax.cache.Cache<String, Object> cache = cacheManager.getCache(cacheName);
        this.getKeys(cacheName).stream()
            .filter(key -> key.matches(pattern))
            .forEach(key -> {
                cache.remove(key);
            });
    }
}
