package org.mskcc.portal.mutation.diagram.pfam;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.junit.Test;
import org.mskcc.portal.mutation.diagram.pfam.Sequence;

/**
 * Unit test for Sequence.
 */
public class SequenceTest {

    @Test
    public void testSerializeToJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        Sequence sequence = new Sequence();
        sequence.setLength(42);
        objectMapper.writeValue(stringWriter, sequence);
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testDeserializeFromJsonO14640() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        InputStream inputStream = getClass().getResourceAsStream("O14640.json");
        List<Sequence> sequences = objectMapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, Sequence.class));
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        assertEquals(695, sequence.getLength());
        assertEquals(5, sequence.getRegions().size());
        assertEquals(0, sequence.getMarkups().size());
        assertEquals(7, sequence.getMotifs().size());
        assertEquals("uniprot", sequence.getMetadata().get("database"));
    }

    @Test
    public void testDeserializeFromJsonEGFR_HUMAN() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        InputStream inputStream = getClass().getResourceAsStream("EGFR_HUMAN.json");
        List<Sequence> sequences = objectMapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, Sequence.class));
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence sequence = sequences.get(0);
        assertEquals(1210, sequence.getLength());
        assertEquals(4, sequence.getRegions().size());
        assertEquals(27, sequence.getMarkups().size());
        assertEquals(8, sequence.getMotifs().size());
        assertEquals("uniprot", sequence.getMetadata().get("database"));
    }
}
