package org.mskcc.portal.mut_diagram.impl;

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
import org.mskcc.portal.mut_diagram.AbstractFeatureServiceTest;
import org.mskcc.portal.mut_diagram.FeatureService;
import org.mskcc.portal.mut_diagram.Sequence;
import org.mskcc.portal.mut_diagram.impl.CacheFeatureService;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;

/**
 * Unit test for CacheFeatureService.
 */
public final class CacheFeatureServiceTest extends AbstractFeatureServiceTest {
    @Mock
    private CacheLoader<String, List<Sequence>> cacheLoader;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        super.setUp();
    }

    @Override
    protected FeatureService createFeatureService() {
        return new CacheFeatureService(cacheLoader);
    }

    @Test(expected=NullPointerException.class)
    public void testConstructorNullCacheLoader() {
        new CacheFeatureService(null);
    }

    @Test
    public void testGetFeatures() throws Exception {
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        List<Sequence> sequences = ImmutableList.of(sequence);
        when(cacheLoader.load(anyString())).thenReturn(sequences);
        assertEquals(sequences, featureService.getFeatures("O14640"));
    }

    @Test
    public void testGetDomainsCacheLoaderException() throws Exception {
        when(cacheLoader.load(anyString())).thenThrow(new Exception());
        List<Sequence> sequences = featureService.getFeatures("O14640");
        assertNotNull(sequences);
        assertTrue(sequences.isEmpty());
    }
}
