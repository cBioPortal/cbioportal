package org.mskcc.cbio.portal.mut_diagram;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.cbio.portal.mut_diagram.FeatureService;
import org.mskcc.cbio.portal.mut_diagram.Sequence;
import org.mskcc.cbio.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.cbio.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import java.util.List;

/**
 * Tests the FeatureService.
 */
public class TestAbstractFeatureService extends TestCase {
    private FeatureService featureService;

    @Override
    protected void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        PfamGraphicsCacheLoader cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        featureService = new CacheFeatureService(cacheLoader);
    }

    public void testCreateFeatureService() {
        assertNotNull(featureService);
    }

    public void testNullUniProtId() {
        try {
            List<Sequence> sequenceList = featureService.getFeatures(null);
            fail("Null Pointer Exception should have been thrown.");
        } catch (NullPointerException e) {
        }
    }
}