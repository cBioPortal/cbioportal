package org.cbioportal.legacy.persistence.util;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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

  private final String name;
  private final long ttlMinutes;
  private final RedissonClient redissonClient;

  /**
   * Create a new ConcurrentMapCache with the specified name.
   *
   * @param name the name of the cache
   */
  public CustomRedisCache(String name, RedissonClient client, long ttlMinutes) {
    super(true);
    this.name = name;
    this.redissonClient = client;
    this.ttlMinutes = ttlMinutes;
  }

  @Override
  public final String getName() {
    return name;
  }

  public final RedissonClient getNativeCache() {
    return this.redissonClient;
  }

  @Override
  @Nullable
  protected Object lookup(Object key) {
    if (redissonClient == null) {
      LOG.warn(
          "Redis client is null for cache '{}' key '{}'. Falling back to database query.",
          name,
          key);
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
          "Redis operation failed for cache '{}' key '{}': {}. Falling back to database query.",
          name,
          key,
          e.getMessage());
      return null;
    }
  }

  private void asyncRefresh(Object key) {
    if (ttlMinutes != INFINITE_TTL && redissonClient != null) {
      try {
        this.redissonClient
            .getBucket(name + DELIMITER + key)
            .expireAsync(ttlMinutes, TimeUnit.MINUTES);
      } catch (Exception e) {
        LOG.debug("Failed to refresh TTL for cache '{}' key '{}': {}", name, key, e.getMessage());
      }
    }
  }

  @Override
  @Nullable
  public <T> T get(Object key, Callable<T> valueLoader) {
    Object zippedValue = null;
    if (redissonClient != null) {
      try {
        zippedValue = this.redissonClient.getBucket(name + DELIMITER + key).get();
      } catch (Exception e) {
        LOG.warn(
            "Redis operation failed for cache '{}' key '{}': {}. Will call value loader.",
            name,
            key,
            e.getMessage());
      }
    } else {
      LOG.debug("Redis client is null for cache '{}' key '{}'. Will call value loader.", name, key);
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

    if (redissonClient == null) {
      LOG.debug(
          "Redis client is null for cache '{}' key '{}'. Cache put operation will be skipped.",
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
          "Failed to put value in cache '{}' for key '{}': {}. Cache operation will be skipped.",
          name,
          key,
          e.getMessage());
    }
  }

  @Override
  @Nullable
  public ValueWrapper putIfAbsent(Object key, @Nullable Object value) {
    if (redissonClient == null) {
      LOG.debug(
          "Redis client is null for cache '{}' key '{}'. putIfAbsent will return null.", name, key);
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
    if (redissonClient == null) {
      LOG.debug(
          "Redis client is null for cache '{}'. Cache evict operation will be skipped.", name);
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
            "Failed to evict cache entries for pattern '{}' in cache '{}': {}",
            pattern,
            name,
            e.getMessage());
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
    if (redissonClient == null) {
      LOG.debug(
          "Redis client is null for cache '{}'. Cache clear operation will be skipped.", name);
      return;
    }

    try {
      this.redissonClient.getKeys().deleteByPattern(name + DELIMITER + "*");
    } catch (Exception e) {
      LOG.warn("Failed to clear cache '{}': {}", name, e.getMessage());
    }
  }

  @Override
  public boolean invalidate() {
    if (redissonClient == null) {
      LOG.debug(
          "Redis client is null for cache '{}'. Cache invalidate operation will be skipped.", name);
      return false;
    }

    try {
      return this.redissonClient.getKeys().deleteByPattern(name + DELIMITER + "*") > 0;
    } catch (Exception e) {
      LOG.warn("Failed to invalidate cache '{}': {}", name, e.getMessage());
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
