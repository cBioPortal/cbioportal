package org.cbioportal.legacy.persistence.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Callable;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

class NoOpCacheManagerTest {

  @Test
  void testNoOpCacheManager() {
    NoOpCacheManager manager = new NoOpCacheManager();

    // Test that cache names are empty
    assertTrue(manager.getCacheNames().isEmpty());

    // Test that we can get a cache
    Cache cache = manager.getCache("test-cache");
    assertNotNull(cache);
    assertEquals("test-cache", cache.getName());

    // Test that cache operations return null/empty
    assertNull(cache.get("test-key"));
    assertNull(cache.get("test-key", String.class));
    assertNull(cache.putIfAbsent("test-key", "test-value"));

    // Test that put operations don't throw exceptions
    assertDoesNotThrow(() -> cache.put("test-key", "test-value"));
    assertDoesNotThrow(() -> cache.evict("test-key"));
    assertDoesNotThrow(() -> cache.clear());

    // Test that evictIfPresent returns false
    assertFalse(cache.evictIfPresent("test-key"));

    // Test that invalidate returns false
    assertFalse(cache.invalidate());
  }

  @Test
  void testNoOpCacheWithValueLoader() {
    NoOpCacheManager manager = new NoOpCacheManager();
    Cache cache = manager.getCache("test-cache");

    // Test that valueLoader is called when cache is unavailable
    Callable<String> valueLoader = () -> "loaded-value";
    String result = cache.get("test-key", valueLoader);
    assertEquals("loaded-value", result);
  }

  @Test
  void testNoOpCacheWithExceptionInValueLoader() {
    NoOpCacheManager manager = new NoOpCacheManager();
    Cache cache = manager.getCache("test-cache");

    // Test that exceptions from valueLoader are propagated
    Callable<String> valueLoader =
        () -> {
          throw new RuntimeException("Test exception");
        };

    assertThrows(RuntimeException.class, () -> cache.get("test-key", valueLoader));
  }
}
