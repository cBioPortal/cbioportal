package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Unit test for MultimapIdMappingService.
 */
public final class MultimapIdMappingServiceTest extends AbstractIdMappingServiceTest {
    private static final Multimap<String, String> UNIPROT_IDS = HashMultimap.create();

    @BeforeClass
    public static void populateIdMappings() {
        UNIPROT_IDS.put("DVL1", "DVL1_HUMAN");
        UNIPROT_IDS.put("DVL1", "O14640");
        UNIPROT_IDS.put("BRCA2", "BRCA2_HUMAN");
        UNIPROT_IDS.put("BRCA2", "P51587");
    }

    @Override
    protected IdMappingService createIdMappingService() {
        return new MultimapIdMappingService(UNIPROT_IDS);
    }

    @Test(expected=NullPointerException.class)
        public void testConstructorNullUniProtIds() {
        new MultimapIdMappingService(null);
    }

    @Test
    public void testGetUniProtId() {
        String uniProtId = idMappingService.getUniProtId("DVL1");
        assertTrue("DVL1_HUMAN".equals(uniProtId) || "O14640".equals(uniProtId));
    }

   @Test
    public void testGetUniProtIdHugoGeneSymbolNotFound() {
       assertNull(idMappingService.getUniProtId("notFound"));
    }
}