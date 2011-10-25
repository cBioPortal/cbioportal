package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Unit test for ListMultimapDomainService.
 */
public final class ListMultimapDomainServiceTest extends AbstractDomainServiceTest {
    private static final Domain DSH_C = new Domain("DSH_C", 503, 685);
    private static final ListMultimap<String, Domain> DOMAINS = ArrayListMultimap.create();

    @BeforeClass
    public static void populateDomains() {
        DOMAINS.put("O14640", new Domain("DIX", 1, 85));
        DOMAINS.put("O14640", new Domain("DVL1", 144, 215));
        DOMAINS.put("O14640", new Domain("PDZ", 251, 336));
        DOMAINS.put("O14640", new Domain("DEP", 428, 497));
        DOMAINS.put("O14640", DSH_C);
    }

    @Override
    protected DomainService createDomainService() {
        return new ListMultimapDomainService(DOMAINS);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructurNullDomains() {
        new ListMultimapDomainService(null);
    }

    @Test
    public void testGetDomains() {
        List<Domain> domains = domainService.getDomains("O14640");
        assertNotNull(domains);
        assertFalse(domains.isEmpty());
        assertEquals(5, domains.size());
        assertTrue(domains.contains(DSH_C));
    }

    @Test
    public void testGetDomainsUniProtIdNotFound() {
        List<Domain> domains = domainService.getDomains("notFound");
        assertNotNull(domains);
        assertTrue(domains.isEmpty());
    }
}
