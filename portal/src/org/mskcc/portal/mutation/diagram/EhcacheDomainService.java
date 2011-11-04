package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.loader.CacheLoader;

/**
 * Implementation of DomainService based on Ehcache.
 */
public final class EhcacheDomainService implements DomainService {
    private static final int EIGHT_HOURS = 28800;
    private static final List<Domain> EMPTY = Collections.emptyList();
    private final Cache cache;
    private final CacheLoader cacheLoader;

    /**
     * Create a new Ehcache domain service with a cache populated by the specified cache loader.
     *
     * @param cacheManager cache manager, must not be null
     * @param cacheLoader cache loader, must not be null
     */
    public EhcacheDomainService(final CacheManager cacheManager, final CacheLoader cacheLoader) {
        checkNotNull(cacheManager, "cacheManager must not be null");
        checkNotNull(cacheLoader, "cacheLoader must not be null");
        cache = new Cache("domainService", 0, false, false, EIGHT_HOURS, EIGHT_HOURS); 
        cacheManager.addCache(cache);
        this.cacheLoader = cacheLoader;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<Domain> getDomains(String uniProtId) {
        checkNotNull(uniProtId, "uniProdId must not be null");
        Element element = cache.getWithLoader(uniProtId, cacheLoader, EMPTY);
        return (List<Domain>) element.getValue();
    }
}
