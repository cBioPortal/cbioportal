package org.cbioportal.legacy.persistence.util;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.lang.Nullable;

public class CustomRedisCache extends AbstractValueAdaptingCache {
  private static final Logger LOG = LoggerFactory.getLogger(CustomRedisCache.class);
  public static final String DELIMITER = ":";
  public static final int INFINITE_TTL = -1;

  // Redis health tracking
  private static final long DEFAULT_REDIS_HEALTH_CHECK_INTERVAL_MS = 30000; // 30 seconds
  private final AtomicBoolean redisHealthy = new AtomicBoolean(true);
  private final AtomicLong lastRedisFailureTime = new AtomicLong(0);
  private final long redisHealthCheckIntervalMs;

  private final String name;
  private final long ttlMinutes;
  private final RedissonClient redissonClient;

  /**
   * Create a new ConcurrentMapCache with the specified name.
   *
   * @param name the name of the cache
   */
  public CustomRedisCache(String name, RedissonClient client, long ttlMinutes) {
    this(name, client, ttlMinutes, DEFAULT_REDIS_HEALTH_CHECK_INTERVAL_MS);
  }

  /**
   * Create a new ConcurrentMapCache with the specified name and health check interval.
   *
   * @param name the name of the cache
   * @param client the Redisson client
   * @param ttlMinutes the TTL in minutes
   * @param redisHealthCheckIntervalMs the health check interval in milliseconds
   */
  public CustomRedisCache(
      String name, RedissonClient client, long ttlMinutes, long redisHealthCheckIntervalMs) {
    super(true);
    this.name = name;
    this.redissonClient = client;
    this.ttlMinutes = ttlMinutes;
    this.redisHealthCheckIntervalMs = redisHealthCheckIntervalMs;
  }

  @Override
  public final String getName() {
    return name;
  }

  public final RedissonClient getNativeCache() {
    return this.redissonClient;
  }

  /**
   * Check if Redis is currently healthy and should be used for operations. This prevents repeated
   * connection attempts when Redis is down.
   */
  private boolean isRedisHealthy() {
    if (redissonClient == null) {
      return false;
    }

    // If Redis was marked as unhealthy, check if enough time has passed to retry
    if (!redisHealthy.get()) {
      long timeSinceLastFailure = System.currentTimeMillis() - lastRedisFailureTime.get();
      if (timeSinceLastFailure < redisHealthCheckIntervalMs) {
        return false; // Still in unhealthy period, skip Redis operations
      }
      // Reset health status to try again
      redisHealthy.set(true);
    }

    return true;
  }

  /** Mark Redis as unhealthy after a failure. */
  private void markRedisUnhealthy() {
    redisHealthy.set(false);
    lastRedisFailureTime.set(System.currentTimeMillis());
  }

  @Override
  @Nullable
  protected Object lookup(Object key) {
    if (!isRedisHealthy()) {
      LOG.debug("Redis is unhealthy for cache '{}' key '{}'. Skipping Redis operation.", name, key);
      return null;
    }

    try {
      Object value = this.redissonClient.getBucket(name + DELIMITER + key).get();
      if (value != null) {
        value = fromStoreValue(value);
        asyncRefresh(key);
      }
      return value;
    } catch (Exception e) {
      LOG.warn(
          "Redis operation failed for cache '{}' key '{}': {}. Marking Redis as unhealthy and falling back to database query.",
          name,
          key,
          e.getMessage());
      markRedisUnhealthy();
      return null;
    }
  }

  private void asyncRefresh(Object key) {
    if (ttlMinutes != INFINITE_TTL && redissonClient != null && isRedisHealthy()) {
      try {
        this.redissonClient
            .getBucket(name + DELIMITER + key)
            .expireAsync(ttlMinutes, TimeUnit.MINUTES);
      } catch (Exception e) {
        LOG.debug("Failed to refresh TTL for cache '{}' key '{}': {}", name, key, e.getMessage());
        markRedisUnhealthy();
      }
    }
  }

  @Override
  @Nullable
  public <T> T get(Object key, Callable<T> valueLoader) {
    Object zippedValue = null;
    if (isRedisHealthy()) {
      try {
        zippedValue = this.redissonClient.getBucket(name + DELIMITER + key).get();
      } catch (Exception e) {
        LOG.warn(
            "Redis operation failed for cache '{}' key '{}': {}. Marking Redis as unhealthy and will call value loader.",
            name,
            key,
            e.getMessage());
        markRedisUnhealthy();
      }
    } else {
      LOG.debug("Redis is unhealthy for cache '{}' key '{}'. Will call value loader.", name, key);
    }

    T value = null;
    if (zippedValue != null) {
      try {
        value = (T) fromStoreValue(zippedValue);
        asyncRefresh(key);
      } catch (Exception e) {
        LOG.warn(
            "Failed to deserialize cached value for cache '{}' key '{}': {}. Will call value loader.",
            name,
            key,
            e.getMessage());
      }
    }

    try {
      return value == null ? valueLoader.call() : value;
    } catch (Exception ex) {
      throw new ValueRetrievalException(key, valueLoader, ex);
    }
  }

  @Override
  public void put(Object key, @Nullable Object value) {
    if (value == null) {
      LOG.warn("Storing null value for key {} in cache. That's probably not great.", key);
    }

    if (!isRedisHealthy()) {
      LOG.debug(
          "Redis is unhealthy for cache '{}' key '{}'. Cache put operation will be skipped.",
          name,
          key);
      return;
    }

    try {
      if (ttlMinutes == INFINITE_TTL) {
        this.redissonClient.getBucket(name + DELIMITER + key).setAsync(toStoreValue(value));
      } else {
        this.redissonClient
            .getBucket(name + DELIMITER + key)
            .setAsync(toStoreValue(value), ttlMinutes, TimeUnit.MINUTES);
      }
    } catch (Exception e) {
      LOG.warn(
          "Failed to put value in cache '{}' for key '{}': {}. Marking Redis as unhealthy and skipping cache operation.",
          name,
          key,
          e.getMessage());
      markRedisUnhealthy();
    }
  }

  @Override
  @Nullable
  public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
    if (!isRedisHealthy()) {
      LOG.debug(
          "Redis is unhealthy for cache '{}' key '{}'. putIfAbsent will return null.", name, key);
      return null;
    }

    Object cached = lookup(key);
    if (cached != null) {
      return toValueWrapper(cached);
    } else {
      put(key, value);
    }
    return toValueWrapper(value);
  }

  @Override
  public void evict(Object pattern) {
    evictIfPresent(pattern);
  }

  @Override
  public boolean evictIfPresent(Object pattern) {
    if (!isRedisHealthy()) {
      LOG.debug("Redis is unhealthy for cache '{}'. Cache evict operation will be skipped.", name);
      return false;
    }

    // Pattern is expected to be a regular expression
    if (pattern instanceof String) {
      try {
        String[] keys =
            redissonClient
                .getKeys()
                .getKeysStream()
                .filter(key -> key.startsWith(name))
                .filter(key -> key.matches((String) pattern))
                .toArray(String[]::new);
        // Calling delete() with empty array causes an error in the Redisson client.
        if (keys.length > 0) return redissonClient.getKeys().delete(keys) > 0;
      } catch (Exception e) {
        LOG.warn(
            "Failed to evict cache entries for pattern '{}' in cache '{}': {}. Marking Redis as unhealthy.",
            pattern,
            name,
            e.getMessage());
        markRedisUnhealthy();
        return false;
      }
    } else {
      LOG.warn(
          "Pattern passed for cache key eviction is not of String type. Cache eviction could not be performed.");
    }
    return false;
  }

  @Override
  public void clear() {
    if (!isRedisHealthy()) {
      LOG.debug("Redis is unhealthy for cache '{}'. Cache clear operation will be skipped.", name);
      return;
    }

    try {
      this.redissonClient.getKeys().deleteByPattern(name + DELIMITER + "*");
    } catch (Exception e) {
      LOG.warn("Failed to clear cache '{}': {}. Marking Redis as unhealthy.", name, e.getMessage());
      markRedisUnhealthy();
    }
  }

  @Override
  public boolean invalidate() {
    if (!isRedisHealthy()) {
      LOG.debug(
          "Redis is unhealthy for cache '{}'. Cache invalidate operation will be skipped.", name);
      return false;
    }

    try {
      return this.redissonClient.getKeys().deleteByPattern(name + DELIMITER + "*") > 0;
    } catch (Exception e) {
      LOG.warn(
          "Failed to invalidate cache '{}': {}. Marking Redis as unhealthy.", name, e.getMessage());
      markRedisUnhealthy();
      return false;
    }
  }

  @Override
  protected Object toStoreValue(@Nullable Object userValue) {
    if (userValue == null) {
      return null;
    }

    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream objectOut;
    try {
      // serialize to byte array
      objectOut = new ObjectOutputStream(byteOut);
      objectOut.writeObject(userValue);
      objectOut.flush();
      byte[] uncompressedByteArray = byteOut.toByteArray();

      // compress byte array
      byteOut = new ByteArrayOutputStream(uncompressedByteArray.length);
      GZIPOutputStream g = new GZIPOutputStream(byteOut);
      g.write(uncompressedByteArray);
      g.close();
      return byteOut.toByteArray();
    } catch (IOException e) {
      LOG.warn("Error compressing object for cache: ", e);
      return null;
    }
  }

  @Override
  protected Object fromStoreValue(@Nullable Object storeValue) {
    if (storeValue == null) {
      return null;
    }

    byte[] bytes = (byte[]) storeValue;
    ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    GZIPInputStream gzipIn;
    try {
      // inflate to byte array
      gzipIn = new GZIPInputStream(byteIn);
      byte[] buffer = new byte[1024];
      int len;
      while ((len = gzipIn.read(buffer)) != -1) {
        byteOut.write(buffer, 0, len);
      }
      byte[] unzippedBytes = byteOut.toByteArray();

      // deserialize byte array to object
      byteIn = new ByteArrayInputStream(unzippedBytes);
      ObjectInputStream oi = new ObjectInputStream(byteIn);
      return oi.readObject();
    } catch (IOException | ClassNotFoundException e) {
      LOG.warn("Error inflating object from cache: ", e);
      return null;
    }
  }

  @Nullable
  @Override
  protected Cache.ValueWrapper toValueWrapper(@Nullable Object storeValue) {
    return (storeValue != null ? new SimpleValueWrapper(storeValue) : null);
  }
}
