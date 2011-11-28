package org.mskcc.portal.mutation.diagram;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Unit test for MultimapIdMappingService.
 */
public final class MultimapIdMappingServiceTest extends AbstractIdMappingServiceTest {
    private static final ListMultimap<String, String> UNIPROT_IDS = ArrayListMultimap.create();

    @BeforeClass
    public static void populateIdMappings() {
        UNIPROT_IDS.put("DVL1", "DVL1_HUMAN");
        UNIPROT_IDS.put("DVL1", "O14640");
        UNIPROT_IDS.put("BRCA2", "BRCA2_HUMAN");
        UNIPROT_IDS.put("BRCA2", "P51587");
    }

    @Override
    protected IdMappingService createIdMappingService() {
        return new ListMultimapIdMappingService(UNIPROT_IDS);
    }

    @Test(expected=NullPointerException.class)
        public void testConstructorNullUniProtIds() {
        new ListMultimapIdMappingService(null);
    }

    @Test
    public void testGetUniProtIds() {
        List<String> uniProtIds = idMappingService.getUniProtIds("DVL1");
        assertTrue(uniProtIds.contains("DVL1_HUMAN"));
        assertTrue(uniProtIds.contains("O14640"));
    }

   @Test
    public void testGetUniProtIdHugoGeneSymbolNotFound() {
       List<String> uniProtIds = idMappingService.getUniProtIds("notFound");
       assertNotNull(uniProtIds);
       assertTrue(uniProtIds.isEmpty());
    }
}