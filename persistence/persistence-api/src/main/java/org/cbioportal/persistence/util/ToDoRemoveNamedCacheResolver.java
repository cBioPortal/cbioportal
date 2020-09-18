package org.cbioportal.persistence.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.springframework.cache.interceptor.NamedCacheResolver;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

public class ToDoRemoveNamedCacheResolver extends NamedCacheResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ToDoRemoveNamedCacheResolver.class);

    protected Collection<String> getCacheNames(CacheOperationInvocationContext<?> context) {
        Collection<String> cacheNames = super.getCacheNames(context);
        LOG.error("In getCacheNames()");
        for (String cacheName : cacheNames) {
            LOG.error("Found cache name: " + cacheName);
        }
        return cacheNames;
    }
}

