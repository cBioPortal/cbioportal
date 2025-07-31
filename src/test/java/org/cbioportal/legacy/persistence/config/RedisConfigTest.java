package org.cbioportal.legacy.persistence.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.KeyGenerator;

/** Test class to verify Redis configuration behavior. */
class RedisConfigTest {

  @Test
  void testRedisConfigWithInvalidRedisConnection() {
    // Test that we can create the Redis config components directly
    RedisConfig redisConfig = new RedisConfig();

    // Test that cache manager can be created (will use NoOpCacheManager when Redis is unavailable)
    CacheManager cacheManager = redisConfig.cacheManager();
    assertNotNull(cacheManager, "CacheManager should be created even with invalid Redis");

    // Test that we get a NoOpCacheManager when Redis is unavailable
    assertTrue(
        cacheManager instanceof org.cbioportal.legacy.persistence.util.NoOpCacheManager,
        "Should use NoOpCacheManager when Redis is unavailable");

    // Test that cache operations work without throwing exceptions
    var cache = cacheManager.getCache("test-cache");
    assertNotNull(cache, "Cache should be created");

    // Test basic cache operations
    assertDoesNotThrow(() -> cache.put("test-key", "test-value"));
    assertDoesNotThrow(() -> cache.get("test-key"));
    assertDoesNotThrow(() -> cache.evict("test-key"));
    assertDoesNotThrow(() -> cache.clear());
  }

  @Test
  void testRedisConfigComponents() {
    // Test that all required components can be created
    RedisConfig redisConfig = new RedisConfig();

    // Test cache manager creation
    CacheManager cacheManager = redisConfig.cacheManager();
    assertNotNull(cacheManager, "CacheManager should be created");

    // Test error handler creation
    CacheErrorHandler errorHandler = redisConfig.errorHandler();
    assertNotNull(errorHandler, "CacheErrorHandler should be created");

    // Test key generator creation
    KeyGenerator keyGenerator = redisConfig.keyGenerator();
    assertNotNull(keyGenerator, "KeyGenerator should be created");

    // Test custom Redis caching provider creation
    var customProvider = redisConfig.customRedisCachingProvider();
    assertNotNull(customProvider, "CustomRedisCachingProvider should be created");

    // Test cache manager behavior
    assertTrue(cacheManager.getCacheNames().isEmpty(), "Cache names should be empty initially");

    // Test that we can create caches
    var cache1 = cacheManager.getCache("cache1");
    var cache2 = cacheManager.getCache("cache2");
    assertNotNull(cache1, "First cache should be created");
    assertNotNull(cache2, "Second cache should be created");
    assertNotSame(cache1, cache2, "Different caches should be different instances");

    // Test cache names after creating caches
    assertTrue(
        cacheManager.getCacheNames().isEmpty(),
        "Cache names should still be empty (NoOp behavior)");
  }

  @Test
  void testCacheErrorHandlerBehavior() {
    // Test that cache error handler is properly configured
    RedisConfig redisConfig = new RedisConfig();
    CacheErrorHandler errorHandler = redisConfig.errorHandler();
    assertNotNull(errorHandler, "CacheErrorHandler should be created");

    // Create a mock cache for testing
    var mockCache =
        new org.springframework.cache.Cache() {
          @Override
          public String getName() {
            return "test-cache";
          }

          @Override
          public Object getNativeCache() {
            return null;
          }

          @Override
          public ValueWrapper get(Object key) {
            return null;
          }

          @Override
          public <T> T get(Object key, Class<T> type) {
            return null;
          }

          @Override
          public <T> T get(Object key, Callable<T> valueLoader) {
            return null;
          }

          @Override
          public void put(Object key, Object value) {
            // No-op
          }

          @Override
          public ValueWrapper putIfAbsent(Object key, Object value) {
            return null;
          }

          @Override
          public void evict(Object key) {
            // No-op
          }

          @Override
          public void clear() {
            // No-op
          }
        };

    // Test that error handler doesn't throw exceptions for basic operations
    assertDoesNotThrow(
        () -> {
          errorHandler.handleCacheGetError(
              new RuntimeException("Test error"), mockCache, "test-key");
        });

    assertDoesNotThrow(
        () -> {
          errorHandler.handleCachePutError(
              new RuntimeException("Test error"), mockCache, "test-key", "test-value");
        });

    assertDoesNotThrow(
        () -> {
          errorHandler.handleCacheEvictError(
              new RuntimeException("Test error"), mockCache, "test-key");
        });

    assertDoesNotThrow(
        () -> {
          errorHandler.handleCacheClearError(new RuntimeException("Test error"), mockCache);
        });
  }

  @Test
  void testCacheManagerWithValueLoader() {
    RedisConfig redisConfig = new RedisConfig();
    var cacheManager = redisConfig.cacheManager();
    var cache = cacheManager.getCache("test-cache");
    assertNotNull(cache, "Cache should be created");

    // Test value loader functionality
    var result = cache.get("test-key", () -> "loaded-value");
    assertEquals("loaded-value", result, "Value loader should be called and return expected value");

    // Test that cache miss returns null
    assertNull(cache.get("non-existent-key"), "Cache miss should return null");

    // Test that value loader is called for cache miss
    var loadedValue = cache.get("non-existent-key", () -> "loaded-from-database");
    assertEquals(
        "loaded-from-database", loadedValue, "Value loader should be called for cache miss");
  }

  @Test
  void testNoOpCacheManagerBehavior() {
    // Test the NoOpCacheManager directly
    var manager = new org.cbioportal.legacy.persistence.util.NoOpCacheManager();

    // Test that cache names are empty
    assertTrue(manager.getCacheNames().isEmpty(), "Cache names should be empty");

    // Test that we can get a cache
    var cache = manager.getCache("test-cache");
    assertNotNull(cache, "Cache should be created");
    assertEquals("test-cache", cache.getName());

    // Test that cache operations return null/empty
    assertNull(cache.get("test-key"));
    assertNull(cache.get("test-key", String.class));

    // Test that put operations don't throw exceptions
    assertDoesNotThrow(() -> cache.put("test-key", "test-value"));

    // Test that evict operations don't throw exceptions
    assertDoesNotThrow(() -> cache.evict("test-key"));

    // Test that clear operations don't throw exceptions
    assertDoesNotThrow(() -> cache.clear());

    // Test that value loader is called
    var result = cache.get("test-key", () -> "loaded-value");
    assertEquals("loaded-value", result, "Value loader should be called");
  }
}
