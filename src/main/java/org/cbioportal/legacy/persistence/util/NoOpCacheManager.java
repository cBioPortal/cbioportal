package org.cbioportal.legacy.persistence.util;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * No-op implementation of CacheManager that does nothing when Redis is unavailable. This allows the
 * application to start and function without Redis caching.
 */
public class NoOpCacheManager implements CacheManager {

  private static final Logger LOG = LoggerFactory.getLogger(NoOpCacheManager.class);

  @Override
  public Cache getCache(String name) {
    LOG.debug("Creating no-op cache for: {}", name);
    return new NoOpCache(name);
  }

  @Override
  public Collection<String> getCacheNames() {
    return Collections.emptyList();
  }

  private static class NoOpCache implements Cache {
    private final String name;
    private static final Logger CACHE_LOG = LoggerFactory.getLogger(NoOpCache.class);

    public NoOpCache(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Object getNativeCache() {
      return null;
    }

    @Override
    public ValueWrapper get(Object key) {
      CACHE_LOG.debug("Cache miss for key '{}' in cache '{}' (Redis unavailable)", key, name);
      return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
      CACHE_LOG.debug("Cache miss for key '{}' in cache '{}' (Redis unavailable)", key, name);
      return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
      CACHE_LOG.debug(
          "Cache miss for key '{}' in cache '{}', loading from database (Redis unavailable)",
          key,
          name);
      try {
        return valueLoader.call();
      } catch (Exception e) {
        CACHE_LOG.error("Error loading value for key '{}' from database", key, e);
        throw new RuntimeException("Error loading value", e);
      }
    }

    @Override
    public void put(Object key, Object value) {
      CACHE_LOG.debug(
          "Cache put ignored for key '{}' in cache '{}' (Redis unavailable)", key, name);
      // No-op - Redis is unavailable
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
      CACHE_LOG.debug(
          "Cache putIfAbsent ignored for key '{}' in cache '{}' (Redis unavailable)", key, name);
      return null;
    }

    @Override
    public void evict(Object key) {
      CACHE_LOG.debug(
          "Cache evict ignored for key '{}' in cache '{}' (Redis unavailable)", key, name);
      // No-op - Redis is unavailable
    }

    @Override
    public boolean evictIfPresent(Object key) {
      CACHE_LOG.debug(
          "Cache evictIfPresent ignored for key '{}' in cache '{}' (Redis unavailable)", key, name);
      return false;
    }

    @Override
    public void clear() {
      CACHE_LOG.debug("Cache clear ignored for cache '{}' (Redis unavailable)", name);
      // No-op - Redis is unavailable
    }

    @Override
    public boolean invalidate() {
      CACHE_LOG.debug("Cache invalidate ignored for cache '{}' (Redis unavailable)", name);
      return false;
    }
  }
}
