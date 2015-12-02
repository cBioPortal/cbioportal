/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.mut_diagram;

import static com.google.common.collect.Maps.newHashMap;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for Sequence.
 */
public class TestSequence {
    private Sequence sequence;

    @Before
    public void setUp() {
        sequence = new Sequence();
        sequence.setLength(42);
        Options options = new Options();
        options.setBaseUrl("baseUrl");
        sequence.setOptions(options);
        Markup markup = new Markup();
        markup.setStart(24);
        sequence.setMarkups(ImmutableList.of(markup));
        Map<String, Object> metadata = newHashMap();
        metadata.put("key", "value");
        sequence.setMetadata(metadata);
        Motif motif = new Motif();
        motif.setStart(24);
        sequence.setMotifs(ImmutableList.of(motif));
        Region region = new Region();
        region.setStart(24);
        sequence.setRegions(ImmutableList.of(region));
    }

    @Test
    public void testSerializeToJson() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter, sequence);
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testDeserializeFromJsonO14640() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        InputStream inputStream = getClass().getResourceAsStream("/O14640.json");
        List<Sequence> sequences = objectMapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, Sequence.class));
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence s = sequences.get(0);
        assertEquals(695, s.getLength());
        assertEquals(5, s.getRegions().size());
        assertEquals(0, s.getMarkups().size());
        assertEquals(7, s.getMotifs().size());
        assertEquals("uniprot", s.getMetadata().get("database"));
    }

    @Test
    public void testDeserializeFromJsonEGFR_HUMAN() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        InputStream inputStream = getClass().getResourceAsStream("/EGFR_HUMAN.json");
        List<Sequence> sequences = objectMapper.readValue(inputStream, typeFactory.constructCollectionType(List.class, Sequence.class));
        assertNotNull(sequences);
        assertEquals(1, sequences.size());
        Sequence s = sequences.get(0);
        assertEquals(1210, s.getLength());
        assertEquals(4, s.getRegions().size());
        assertEquals(27, s.getMarkups().size());
        assertEquals(8, s.getMotifs().size());
        assertEquals("uniprot", s.getMetadata().get("database"));
    }

    @Test
    public void testDeepCopyNullMarkups() {
        sequence.setMarkups(null);
        Sequence copy = sequence.deepCopy();
        assertNull(copy.getMarkups());
    }

    @Test
    public void testDeepCopyNullMetadata() {
        sequence.setMetadata(null);
        Sequence copy = sequence.deepCopy();
        assertNull(copy.getMetadata());
    }

    @Test
    public void testDeepCopyNullMotifs() {
        sequence.setMotifs(null);
        Sequence copy = sequence.deepCopy();
        assertNull(copy.getMotifs());
    }

    @Test
    public void testDeepCopyNullOptions() {
        sequence.setOptions(null);
        Sequence copy = sequence.deepCopy();
        assertNull(copy.getOptions());
    }

    @Test
    public void testDeepCopyOptionsNullBaseUrl() {
        Options options = new Options();
        options.setBaseUrl(null);
        sequence.setOptions(options);
        Sequence copy = sequence.deepCopy();
        assertNotNull(copy.getOptions());
        assertNull(copy.getOptions().getBaseUrl());
    }

    @Test
    public void testDeepCopyNullRegions() {
        sequence.setRegions(null);
        Sequence copy = sequence.deepCopy();
        assertNull(copy.getRegions());
    }

    @Test
    public void testDeepCopy() {
        Sequence copy = sequence.deepCopy();
        assertNotSame(sequence, copy);
        assertEquals(sequence.getLength(), copy.getLength());
        assertEquals(sequence.getMarkups(), copy.getMarkups());
        assertNotSame(sequence.getMarkups(), copy.getMarkups());
        assertEquals(sequence.getMetadata(), copy.getMetadata());
        assertNotSame(sequence.getMetadata(), copy.getMetadata());
        assertEquals(sequence.getMotifs(), copy.getMotifs());
        assertNotSame(sequence.getMotifs(), copy.getMotifs());
        assertEquals(sequence.getOptions().getBaseUrl(), copy.getOptions().getBaseUrl());
        assertNotSame(sequence.getOptions(), copy.getOptions());
        assertEquals(sequence.getRegions(), copy.getRegions());
        assertNotSame(sequence.getRegions(), copy.getRegions());
    }

    @Test
    public void testWithMarkupsNullMarkups() {
        sequence.setMarkups(null);
        Sequence withMarkups = sequence.withMarkups(null);
        assertNull(withMarkups.getMarkups());
    }

    @Test
    public void testWithMarkupsNullAdditionalMarkups() {
        Sequence withMarkups = sequence.withMarkups(null);
        assertNotNull(withMarkups.getMarkups());
        assertEquals(1, withMarkups.getMarkups().size());
        assertEquals(24, withMarkups.getMarkups().get(0).getStart());
    }

    @Test
    public void testWithMarkupsEmptyAdditionalMarkups() {
        List<Markup> empty = Collections.emptyList();
        Sequence withMarkups = sequence.withMarkups(empty);
        assertNotNull(withMarkups.getMarkups());
        assertEquals(1, withMarkups.getMarkups().size());
        assertEquals(24, withMarkups.getMarkups().get(0).getStart());
    }

    @Test
    public void testWithMarkups() {
        Markup markup = new Markup();
        markup.setStart(13);
        Sequence withMarkups = sequence.withMarkups(ImmutableList.of(markup));
        assertNotNull(withMarkups.getMarkups());
        assertEquals(2, withMarkups.getMarkups().size());
        for (Markup m : withMarkups.getMarkups()) {
            assertTrue(m.getStart() == 13 || m.getStart() == 24);
        }
    }
}
