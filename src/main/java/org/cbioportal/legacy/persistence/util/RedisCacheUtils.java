package org.cbioportal.legacy.persistence.util;

import static org.cbioportal.legacy.persistence.util.CustomRedisCache.DELIMITER;

import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.shared.RedisCondition;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(RedisCondition.class)
public class RedisCacheUtils implements CacheUtils {

  @Autowired private CacheManager cacheManager;

  @Override
  public List<String> getKeys(String cacheName) {
    Cache cache = cacheManager.getCache(cacheName);

    if (cache == null) {
      throw new RuntimeException("Native cache not of class RedissonCache!!!");
    }

    if (cache.getNativeCache() instanceof RedissonClient) {
      return ((RedissonClient) cache.getNativeCache())
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
    if (cache != null) {
      cache.evict(pattern);
    }
  }
}
