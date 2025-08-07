package org.cbioportal.legacy.persistence.util;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class CustomRedisCacheManager implements CacheManager {
  private final ConcurrentMap<String, CustomRedisCache> caches = new ConcurrentHashMap<>();
  private final RedissonClient client;
  private final long ttlInMins;
  private final long redisHealthCheckIntervalMs;

  public CustomRedisCacheManager(RedissonClient client, long ttlInMins) {
    this(client, ttlInMins, 30000); // Default 30 seconds health check interval
  }

  public CustomRedisCacheManager(
      RedissonClient client, long ttlInMins, long redisHealthCheckIntervalMs) {
    this.client = client;
    this.ttlInMins = ttlInMins;
    this.redisHealthCheckIntervalMs = redisHealthCheckIntervalMs;
  }

  /**
   * Get the cache associated with the given name.
   *
   * <p>Note that the cache may be lazily created at runtime if the native provider supports it.
   *
   * @param name the cache identifier (must not be {@code null})
   * @return the associated cache, or {@code null} if such a cache does not exist or could be not
   *     created
   */
  @Override
  public Cache getCache(String name) {
    // !name.toLowerCase().contains("static") is a hack. Sometimes spring calls this getCache method
    // from
    // a place I can't control, so I needed a way in this method to determine whether or not the
    // cache
    // it's getting should have a ttl.
    // In practice, any cache we have that is static should not expire.
    // (I mean, we have two caches, so this isn't rocket science)
    return getCache(name, !name.toLowerCase().contains("static"));
  }

  @NotNull
  public Cache getCache(String name, boolean expires) {
    long clientTTLInMinutes = expires ? ttlInMins : CustomRedisCache.INFINITE_TTL;
    return caches.computeIfAbsent(
        name,
        k -> new CustomRedisCache(name, client, clientTTLInMinutes, redisHealthCheckIntervalMs));
  }

  /**
   * Get a collection of the cache names known by this manager.
   *
   * @return the names of all caches known by the cache manager
   */
  @Override
  public Collection<String> getCacheNames() {
    return caches.keySet();
  }
}
