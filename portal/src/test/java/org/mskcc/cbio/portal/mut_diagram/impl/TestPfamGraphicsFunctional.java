package org.mskcc.portal.test.mut_diagram.impl;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mskcc.portal.mut_diagram.FeatureService;
import org.mskcc.portal.mut_diagram.Sequence;
import org.mskcc.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import com.google.common.cache.CacheLoader;

/**
 * Functional test for CacheFeatureService+PfamGraphicsCacheLoader.
 */
public class TestPfamGraphicsFunctional {
    private FeatureService featureService;

    @Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        CacheLoader<String, List<Sequence>> cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        featureService = new CacheFeatureService(cacheLoader);
    }

    @Test
    public void testGetFeaturesO14640() {
        List<Sequence> sequences = featureService.getFeatures("O14640");
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        // note: this is a functional test, and will fail if e.g.
        //    the network is not available, or Pfam graphics returns different data
        assertEquals(695, sequence.getLength());
        assertEquals(5, sequence.getRegions().size());
        assertEquals(0, sequence.getMarkups().size());
        assertEquals(7, sequence.getMotifs().size());
        assertEquals("uniprot", sequence.getMetadata().get("database"));
    }

    @Test
    public void testGetFeaturesEGFR_HUMAN() {
        List<Sequence> sequences = featureService.getFeatures("EGFR_HUMAN");
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        // note: this is a functional test, and will fail if e.g.
        //    the network is not available, or Pfam graphics returns different data
        assertEquals(1210, sequence.getLength());
        assertEquals(4, sequence.getRegions().size());
        assertEquals(27, sequence.getMarkups().size());
        assertEquals(8, sequence.getMotifs().size());
        assertEquals("uniprot", sequence.getMetadata().get("database"));
    }
}
