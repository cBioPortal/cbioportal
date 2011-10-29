package org.mskcc.portal.mutation.diagram;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.loader.CacheLoader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for EhcacheDomainService.
 */
public final class EhcacheDomainServiceTest extends AbstractDomainServiceTest {
    @Mock
    private CacheLoader cacheLoader;
    @Mock
    private CacheManager cacheManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setUp();
    }

    @Override
    protected DomainService createDomainService() {
        return new EhcacheDomainService(cacheManager, cacheLoader);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullCacheManager() {
        new EhcacheDomainService(null, cacheLoader);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorCacheLoader() {
        new EhcacheDomainService(cacheManager, null);
    }

    @Test
    public void testGetDomains() {
        Domain domain = new Domain("FOO", 0, 42);
        List<Domain> domains = ImmutableList.of(domain);
        when(cacheLoader.load(anyString())).thenReturn(domains);
    }
}
