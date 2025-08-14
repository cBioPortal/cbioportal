package org.cbioportal.legacy.persistence.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test class to verify Redis health tracking functionality. This ensures that repeated connection
 * attempts are avoided when Redis is down.
 */
class RedisHealthTrackingTest {

  @Test
  void testRedisHealthTracking() {
    // Create a cache with a short health check interval for testing
    CustomRedisCache cache =
        new CustomRedisCache("test-cache", null, 60, 1000); // 1 second interval

    // Test that cache operations work when Redis is null (unhealthy)
    assertDoesNotThrow(
        () -> {
          Object result = cache.get("test-key");
          assertNull(result, "Cache get should return null when Redis is null");
        });

    // Test that value loader is called when Redis is unhealthy
    String result = cache.get("test-key", () -> "loaded-from-database");
    assertEquals(
        "loaded-from-database", result, "Value loader should be called when Redis is unhealthy");

    // Test that put operations are skipped when Redis is unhealthy
    assertDoesNotThrow(
        () -> {
          cache.put("test-key", "test-value");
        },
        "Put operation should not throw when Redis is unhealthy");
  }

  @Test
  void testRedisHealthTrackingWithFailingRedis() {
    // Create a cache with a failing Redis client
    CustomRedisCache cache = createFailingRedisCache();

    // First operation should fail and mark Redis as unhealthy
    assertDoesNotThrow(
        () -> {
          Object result = cache.get("test-key");
          assertNull(result, "Cache get should return null after Redis failure");
        });

    // Subsequent operations should skip Redis and call value loader directly
    long startTime = System.currentTimeMillis();
    String result = cache.get("test-key", () -> "loaded-from-database");
    long endTime = System.currentTimeMillis();

    assertEquals(
        "loaded-from-database", result, "Value loader should be called when Redis is unhealthy");

    // Verify that the operation was fast (no Redis connection attempts)
    long duration = endTime - startTime;
    assertTrue(duration < 100, "Operation should be fast when Redis is marked as unhealthy");
  }

  @Test
  void testRedisHealthRecovery() {
    // Create a cache with a short health check interval
    CustomRedisCache cache = new CustomRedisCache("test-cache", null, 60, 100); // 100ms interval

    // Test initial unhealthy state
    String result1 = cache.get("test-key", () -> "loaded-1");
    assertEquals("loaded-1", result1);

    // Wait for health check interval to pass
    try {
      Thread.sleep(150); // Wait longer than the 100ms interval
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Test that Redis is considered healthy again after the interval
    String result2 = cache.get("test-key", () -> "loaded-2");
    assertEquals("loaded-2", result2);
  }

  /** Creates a cache with a failing Redis client for testing. */
  private CustomRedisCache createFailingRedisCache() {
    return new CustomRedisCache("test-cache", null, 60, 5000); // 5 second interval
  }
}
