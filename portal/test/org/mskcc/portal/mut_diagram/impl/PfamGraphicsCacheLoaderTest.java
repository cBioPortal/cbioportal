package org.mskcc.portal.mut_diagram.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mskcc.portal.mut_diagram.Sequence;
import org.mskcc.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for PfamGraphicsCacheLoader.
 */
public final class PfamGraphicsCacheLoaderTest {
    private TypeFactory typeFactory;
    private PfamGraphicsCacheLoader cacheLoader;

    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        typeFactory = TypeFactory.defaultInstance();
    }

    @Test
    public void testConstructor() {
        assertNotNull(cacheLoader);
    }

    @Test
    public void testLoadNullUniProtId() throws Exception {
        List<Sequence> sequences = cacheLoader.load(null);
        assertNotNull(sequences);
        assertTrue(sequences.isEmpty());
    }

    @Test
    public void testLoadMalformedURLException() throws Exception {
        List<Sequence> sequences = cacheLoader.load(" ");
        assertNotNull(sequences);
        assertTrue(sequences.isEmpty());
    }

    @Test
    public void testLoadReadValueIOException() throws Exception {
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(objectMapper.readValue(any(URL.class), any(CollectionType.class))).thenThrow(new IOException("message"));
        List<Sequence> sequences = cacheLoader.load("O14640");
        assertNotNull(sequences);
        assertTrue(sequences.isEmpty());
    }

    @Test
    public void testLoadReadValueJsonParseException() throws Exception {
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(objectMapper.readValue(any(URL.class), any(CollectionType.class))).thenThrow(new JsonParseException("message", null));
        List<Sequence> sequences = cacheLoader.load("O14640");
        assertNotNull(sequences);
        assertTrue(sequences.isEmpty());
    }

    @Test
    public void testLoadReadValueJsonMappingException() throws Exception {
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(objectMapper.readValue(any(URL.class), any(CollectionType.class))).thenThrow(new JsonMappingException("message"));
        List<Sequence> sequences = cacheLoader.load("O14640");
        assertNotNull(sequences);
        assertTrue(sequences.isEmpty());
    }

    @Test
    public void testLoadReadValue() throws Exception {
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        List<Sequence> sequences = ImmutableList.of(sequence);
        when(objectMapper.getTypeFactory()).thenReturn(typeFactory);
        when(objectMapper.readValue(any(URL.class), any(CollectionType.class))).thenReturn(sequences);
        assertEquals(sequences, cacheLoader.load("O14640"));
    }
}
