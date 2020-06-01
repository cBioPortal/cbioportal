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

package org.mskcc.cbio.portal.mut_diagram.impl;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mskcc.cbio.portal.mut_diagram.FeatureService;
import org.mskcc.cbio.portal.mut_diagram.Sequence;
import org.mskcc.cbio.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.cbio.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheLoader;

/**
 * Functional test for CacheFeatureService+PfamGraphicsCacheLoader.
 * TODO we may want to move this class into the importer module
 */
public class TestPfamGraphicsFunctional {
    private FeatureService featureService;

    //@Before
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        CacheLoader<String, List<Sequence>> cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        featureService = new CacheFeatureService(cacheLoader);
    }

    //@Test
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

    //@Test
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
