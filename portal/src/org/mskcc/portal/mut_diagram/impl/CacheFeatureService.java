package org.mskcc.portal.mut_diagram.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.mskcc.portal.mut_diagram.FeatureService;
import org.mskcc.portal.mut_diagram.Sequence;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.inject.Inject;

/**
 * Implementation of FeatureService based on CacheBuilder.
 */
public final class CacheFeatureService implements FeatureService {
    private static final List<Sequence> EMPTY = Collections.emptyList();
    private static final Logger logger = Logger.getLogger(CacheFeatureService.class);
    private final Cache<String, List<Sequence>> cache;

    /**
     * Create a new cache feature service with a cache populated by the specified cache loader.
     *
     * @param cacheLoader cache loader, must not be null
     */
    @Inject
    public CacheFeatureService(final CacheLoader<String, List<Sequence>> cacheLoader) {
        checkNotNull(cacheLoader, "cacheLoader must not be null");
        cache = CacheBuilder.newBuilder().build(cacheLoader);
    }

    /** {@inheritDoc} */
    public List<Sequence> getFeatures(final String uniProtId) {
        checkNotNull(uniProtId, "uniProtId must not be null");
        try {
            return cache.get(uniProtId);
        }
        catch (Exception e) {
            logger.error("could not load features from cache for " + uniProtId, e);
            return EMPTY;
        }
    }
}
