package org.cbioportal.legacy.persistence.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * Test class to verify that Redis failures during runtime are handled gracefully and the
 * application continues to function by falling back to database queries.
 */
class RedisRuntimeFailureTest {

  @Test
  void testRedisFailureDuringRuntime() {
    // Create a cache manager that simulates Redis becoming unavailable
    CacheManager cacheManager = createFailingRedisCacheManager();

    // Test that cache operations don't throw exceptions when Redis fails
    Cache cache = cacheManager.getCache("test-cache");
    assertNotNull(cache, "Cache should be created even when Redis fails");

    // Test cache get operation with Redis failure
    assertDoesNotThrow(
        () -> {
          Object result = cache.get("test-key");
          assertNull(result, "Cache get should return null when Redis fails");
        },
        "Cache get operation should not throw exception when Redis fails");

    // Test cache put operation with Redis failure
    assertDoesNotThrow(
        () -> {
          cache.put("test-key", "test-value");
        },
        "Cache put operation should not throw exception when Redis fails");

    // Test cache get with value loader when Redis fails
    assertDoesNotThrow(
        () -> {
          String result = cache.get("test-key", () -> "loaded-from-database");
          assertEquals(
              "loaded-from-database", result, "Value loader should be called when Redis fails");
        },
        "Cache get with value loader should not throw exception when Redis fails");

    // Test cache evict operation with Redis failure
    assertDoesNotThrow(
        () -> {
          cache.evict("test-key");
        },
        "Cache evict operation should not throw exception when Redis fails");

    // Test cache clear operation with Redis failure
    assertDoesNotThrow(
        () -> {
          cache.clear();
        },
        "Cache clear operation should not throw exception when Redis fails");
  }

  @Test
  void testValueLoaderCalledWhenRedisFails() {
    CacheManager cacheManager = createFailingRedisCacheManager();
    Cache cache = cacheManager.getCache("test-cache");

    // Test that value loader is called when Redis fails
    String result = cache.get("test-key", () -> "database-value");
    assertEquals(
        "database-value", result, "Value loader should be called when Redis is unavailable");

    // Test that value loader is called for different keys
    Integer intResult = cache.get("int-key", () -> 42);
    assertEquals(
        42,
        intResult,
        "Value loader should be called for different types when Redis is unavailable");
  }

  @Test
  void testCacheOperationsWithRedisFailure() {
    CacheManager cacheManager = createFailingRedisCacheManager();
    Cache cache = cacheManager.getCache("test-cache");

    // Test putIfAbsent with Redis failure
    assertDoesNotThrow(
        () -> {
          Cache.ValueWrapper result = cache.putIfAbsent("new-key", "new-value");
          // Should return null since Redis is failing
          assertNull(result, "putIfAbsent should return null when Redis fails");
        },
        "putIfAbsent should not throw exception when Redis fails");

    // Test get with type when Redis fails
    assertDoesNotThrow(
        () -> {
          String result = cache.get("test-key", String.class);
          assertNull(result, "get with type should return null when Redis fails");
        },
        "get with type should not throw exception when Redis fails");
  }

  @Test
  void testErrorHandlerWithRedisFailure() {
    // Test that the error handler properly handles Redis failures
    LoggingCacheErrorHandler errorHandler = new LoggingCacheErrorHandler();

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
            // Do nothing
          }

          @Override
          public ValueWrapper putIfAbsent(Object key, Object value) {
            return null;
          }

          @Override
          public void evict(Object key) {
            // Do nothing
          }

          @Override
          public void clear() {
            // Do nothing
          }
        };

    // Test that error handler doesn't throw exceptions
    assertDoesNotThrow(
        () -> {
          errorHandler.handleCacheGetError(
              new RuntimeException("Redis connection failed"), mockCache, "test-key");
        },
        "Error handler should not throw exceptions");

    assertDoesNotThrow(
        () -> {
          errorHandler.handleCachePutError(
              new RuntimeException("Redis connection failed"), mockCache, "test-key", "test-value");
        },
        "Error handler should not throw exceptions");

    assertDoesNotThrow(
        () -> {
          errorHandler.handleCacheEvictError(
              new RuntimeException("Redis connection failed"), mockCache, "test-key");
        },
        "Error handler should not throw exceptions");

    assertDoesNotThrow(
        () -> {
          errorHandler.handleCacheClearError(
              new RuntimeException("Redis connection failed"), mockCache);
        },
        "Error handler should not throw exceptions");
  }

  /** Creates a cache manager that simulates Redis failures during runtime. */
  private CacheManager createFailingRedisCacheManager() {
    return new CacheManager() {
      @Override
      public Cache getCache(String name) {
        // Create a CustomRedisCache with a null RedissonClient to simulate Redis failure
        return new CustomRedisCache(name, null, 60);
      }

      @Override
      public java.util.Collection<String> getCacheNames() {
        return java.util.Collections.emptyList();
      }
    };
  }
}
