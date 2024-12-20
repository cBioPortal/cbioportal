package org.cbioportal.persistence.util;

import static org.cbioportal.persistence.util.CustomRedisCache.DELIMITER;


import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ConditionalOnExpression(
    "#{environment['persistence.cache_type'] == 'redis' or environment['persistence.cache_type_clickhouse'] == 'redis'}"
)
public class RedisCacheUtils implements CacheUtils {

    @Autowired
    private CacheManager cacheManager;
    
    @Override
    public List<String> getKeys(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        
        if(cache == null) {
        	throw new RuntimeException("Native cache not of class RedissonCache!!!");
        }
        
        if (cache.getNativeCache() instanceof RedissonClient) {
            return ((RedissonClient)cache.getNativeCache())
                .getKeys()
                .getKeysStream()
                .filter(k -> k.startsWith(cache.getName() + DELIMITER))
                .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Native cache not of class RedissonCache!!!");
        }
    }
    
    @Override
    public void evictByPattern(String cacheName, String pattern) {
        Cache cache = cacheManager.getCache(cacheName);
        if(cache != null) {
        	cache.evict(pattern);
        }
    }
}
