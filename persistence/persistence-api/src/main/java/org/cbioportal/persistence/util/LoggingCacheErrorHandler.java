package org.cbioportal.persistence.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * Implementation of org.springframework.cache.interceptor.CacheErrorHandler
 * that logs the error messages when performing Redis operations.
 * Redis will throw a RuntimeException causing our APIs to return HTTP 500 responses, so we defined
 * this class to just log the errors and allow our app fallback to the non-cached version.
 */

public class LoggingCacheErrorHandler implements CacheErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        LOG.error(String.format("Cache '%s' failed to get entry with key '%s'", cache.getName(), key), exception);
        LOG.error(String.format("Cache error message: '%s'", exception.getMessage()));
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        LOG.error(String.format("Cache '%s' failed to put entry with key '%s'", cache.getName(), key), exception);
        LOG.error(String.format("Cache error message: '%s'", exception.getMessage()));
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        LOG.error(String.format("Cache '%s' failed to evict entry with key '%s'", cache.getName(), key), exception);
        LOG.error(String.format("Cache error message: '%s'", exception.getMessage()));
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        LOG.error(String.format("Cache '%s' failed to clear entries", cache.getName()), exception);
        LOG.error(String.format("Cache error message: '%s'", exception.getMessage()));
    }

}
