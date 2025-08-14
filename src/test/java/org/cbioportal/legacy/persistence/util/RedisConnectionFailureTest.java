package org.cbioportal.legacy.persistence.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/** Test class to verify Redis connection failure scenarios and fallback behavior. */
class RedisConnectionFailureTest {

  @Test
  void testRedisConnectionFailureFallback() {
    // Simulate Redis connection failure by using invalid configuration
    CustomRedisCachingProvider provider = new CustomRedisCachingProvider();

    // Set invalid Redis configuration
    setField(provider, "leaderAddress", "redis://invalid-host:6379");
    setField(provider, "followerAddress", "redis://invalid-host:6379");
    setField(provider, "database", 0);
    setField(provider, "password", "test");
    setField(provider, "redisName", "test");
    setField(provider, "expiryMins", 10000L);
    setField(provider, "clearOnStartup", true);

    // This should return null due to connection failure
    var redissonClient = provider.getRedissonClient();
    assertNull(redissonClient, "RedissonClient should be null when Redis is unavailable");

    // Get cache manager with null client (simulating connection failure)
    CacheManager cacheManager = provider.getCacheManager(null);
    assertNotNull(cacheManager, "CacheManager should not be null");
    assertTrue(
        cacheManager instanceof NoOpCacheManager,
        "Should use NoOpCacheManager when Redis is unavailable");

    // Test cache operations
    Cache cache = cacheManager.getCache("test-cache");
    assertNotNull(cache, "Cache should not be null");

    // Test cache miss behavior
    assertNull(cache.get("test-key"), "Cache get should return null");
    assertNull(cache.get("test-key", String.class), "Cache get with type should return null");

    // Test value loader fallback
    Callable<String> valueLoader = () -> "loaded-from-database";
    String result = cache.get("test-key", valueLoader);
    assertEquals(
        "loaded-from-database", result, "Should call valueLoader when cache is unavailable");

    // Test that put operations don't throw exceptions
    assertDoesNotThrow(() -> cache.put("test-key", "test-value"));
    assertDoesNotThrow(() -> cache.evict("test-key"));
    assertDoesNotThrow(() -> cache.clear());
  }

  @Test
  void testNoOpCacheManagerBehavior() {
    NoOpCacheManager manager = new NoOpCacheManager();

    // Test cache names
    assertTrue(manager.getCacheNames().isEmpty(), "Cache names should be empty");

    // Test cache creation
    Cache cache = manager.getCache("test-cache");
    assertNotNull(cache, "Cache should be created");
    assertEquals("test-cache", cache.getName(), "Cache name should match");

    // Test all cache operations
    assertNull(cache.get("key"), "Get should return null");
    assertNull(cache.get("key", String.class), "Get with type should return null");
    assertNull(cache.putIfAbsent("key", "value"), "putIfAbsent should return null");
    assertFalse(cache.evictIfPresent("key"), "evictIfPresent should return false");
    assertFalse(cache.invalidate(), "invalidate should return false");

    // Test that operations don't throw exceptions
    assertDoesNotThrow(() -> cache.put("key", "value"));
    assertDoesNotThrow(() -> cache.evict("key"));
    assertDoesNotThrow(() -> cache.clear());
  }

  @Test
  void testValueLoaderExceptionPropagation() {
    NoOpCacheManager manager = new NoOpCacheManager();
    Cache cache = manager.getCache("test-cache");

    // Test that exceptions from value loader are properly propagated
    assertThrows(
        RuntimeException.class,
        () -> {
          cache.get(
              "key",
              () -> {
                throw new RuntimeException("Database error");
              });
        });

    // Verify the exception message
    try {
      cache.get(
          "key",
          () -> {
            throw new RuntimeException("Database error");
          });
    } catch (RuntimeException e) {
      assertEquals("Error loading value", e.getMessage());
    }
  }

  private void setField(Object target, String fieldName, Object value) {
    try {
      var field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set field " + fieldName, e);
    }
  }
}
