package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

/**
 * Abstract unit test for implementations of DomainService.
 */
public abstract class AbstractDomainServiceTest {
    protected DomainService domainService;

    /**
     * Create and return a new instance of an implementation of DomainService to test.
     *
     * @return a new instance of an implementation of DomainService to test
     */
    protected abstract DomainService createDomainService();

    @Before
    public final void setUp() {
        domainService = createDomainService();
    }

    @Test
    public final void testCreateDomainService() {
        assertNotNull(domainService);
    }

    @Test(expected=NullPointerException.class)
    public final void testGetDomainsNullUniProtId() {
        domainService.getDomains(null);
    }
}