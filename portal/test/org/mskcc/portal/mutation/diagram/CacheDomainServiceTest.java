package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;

/**
 * Unit test for CacheDomainService.
 */
public class CacheDomainServiceTest extends AbstractDomainServiceTest {
    @Mock
    private CacheLoader<String, List<Domain>> cacheLoader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setUp();
    }

    @Override
    protected DomainService createDomainService() {
        return new CacheDomainService(cacheLoader);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullCacheLoader() {
        new CacheDomainService(null);
    }

    @Test
    public void testGetDomains() throws Exception {
        Domain domain = new Domain("FOO", 0, 42);
        List<Domain> domains = ImmutableList.of(domain);
        when(cacheLoader.load(anyString())).thenReturn(domains);
        assertEquals(domains, domainService.getDomains("O14640"));
    }

    @Test
    public void testGetDomainsCacheLoaderException() throws Exception {
        when(cacheLoader.load(anyString())).thenThrow(new Exception());
        List<Domain> domains = domainService.getDomains("O14640");
        assertNotNull(domains);
        assertTrue(domains.isEmpty());
    }
}
