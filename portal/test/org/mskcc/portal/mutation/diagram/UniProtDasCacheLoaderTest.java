package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for UniProtDasCacheLoader.
 */
public final class UniProtDasCacheLoaderTest {
    private UniProtDasCacheLoader cacheLoader;

    @Before
    public void setUp() {
        cacheLoader = new UniProtDasCacheLoader();
    }

    @Test
    public void testConstructor() {
        assertNotNull(cacheLoader);
    }

    @Test
    public void testLoad() throws Exception {
        // todo: this test is more of a functional test, and will fail if e.g.
        //    the network is not available, or UniProt DAS returns different data
        List<Domain> domains = cacheLoader.load("O14640");
        assertNotNull(domains);
        assertFalse(domains.isEmpty());

        boolean foundDix = false;
        boolean foundPdz = false;
        boolean foundDep = false;
        for (Domain domain : domains) {
            if ("DIX".equals(domain.getLabel())) {
                foundDix = true;
                assertEquals(1, domain.getStart());
                assertEquals(85, domain.getEnd());
            }
            else if ("PDZ".equals(domain.getLabel())) {
                foundPdz = true;
                assertEquals(251, domain.getStart());
                assertEquals(323, domain.getEnd());
            }
            else if ("DEP".equals(domain.getLabel())) {
                foundDep = true;
                assertEquals(425, domain.getStart());
                assertEquals(499, domain.getEnd());
            }
        }
        assertTrue(foundDix);
        assertTrue(foundPdz);
        assertTrue(foundDep);
    }
}
