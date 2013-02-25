/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.mskcc.cbio.importer.converter.internal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.model.ClinicalAttributesMetadata;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(JUnit4.class)
public class ClinicalDataConverterImplTest {

    private ClinicalDataConverterImpl clinicalDataConverter = null;

    @Before
    public void setUp() throws Exception {
        Config mockConfig = mock(Config.class);
        FileUtils mockFileUtils = mock(FileUtils.class);
        CaseIDs mockCaseIDs = mock(CaseIDs.class);
        IDMapper mockIdmapper = mock(IDMapper.class);
        clinicalDataConverter = new ClinicalDataConverterImpl(mockConfig, mockFileUtils, mockCaseIDs, mockIdmapper);
    }

    @Test
    public void createStringToClinicalAttributemetaDataTest() {
        Collection<ClinicalAttributesMetadata> emptyAttrs = new ArrayList<ClinicalAttributesMetadata>();
        HashMap<String, ClinicalAttributesMetadata> empty
                = clinicalDataConverter.createStringToClinicalAttributeMetaData(emptyAttrs);
        assertTrue(empty.isEmpty());

        Collection<ClinicalAttributesMetadata> singleAttrs = new ArrayList<ClinicalAttributesMetadata>();
        ClinicalAttributesMetadata attr = new ClinicalAttributesMetadata();
        singleAttrs.add(attr);
        HashMap<String, ClinicalAttributesMetadata> singleTrivial
                = clinicalDataConverter.createStringToClinicalAttributeMetaData(singleAttrs);
        assertTrue(singleTrivial.get("") != null);

        Collection<ClinicalAttributesMetadata> twoAttrs = new ArrayList<ClinicalAttributesMetadata>();
        ClinicalAttributesMetadata attr1 = new ClinicalAttributesMetadata();
        attr1.setAliases("attr1");
        ClinicalAttributesMetadata attr2 = new ClinicalAttributesMetadata();
        attr2.setAliases("attr2");
        twoAttrs.add(attr1);
        twoAttrs.add(attr2);

        HashMap<String, ClinicalAttributesMetadata> doubleTrivial
                = clinicalDataConverter.createStringToClinicalAttributeMetaData(twoAttrs);

        assertTrue(doubleTrivial.get("attr1") != null);
        assertTrue(doubleTrivial.get("attr2") != null);
    }

    @Test
    public void cleanUpAliasTest() {
        assertEquals("abc", clinicalDataConverter.cleanUpAlias("abc"));
        assertEquals("abcv1.0", clinicalDataConverter.cleanUpAlias("abcv1.0"));
        assertEquals("drugs.drugabc.a", clinicalDataConverter.cleanUpAlias("drugs.drugabc-1.a"));
        assertEquals("followups.followup.", clinicalDataConverter.cleanUpAlias("followups.followupv5.0."));
        assertEquals("followups.followup.1", clinicalDataConverter.cleanUpAlias("followups.followupv5.0-1"));
        assertEquals("followups.followup.a", clinicalDataConverter.cleanUpAlias("followups.followupv5.0.a-5"));
    }

    @Test
    public void appendToFilteredRowsTest() {
        HashMap<String, ClinicalAttributesMetadata> aliasToAttribute
                = new HashMap<String, ClinicalAttributesMetadata>();
        List<String> row = new ArrayList<String>();
        String alias = "alias";
        List<List<String>> rows = new ArrayList<List<String>>();
        clinicalDataConverter.appendToFilteredRows(aliasToAttribute, row, alias, rows);
        assertTrue(rows.isEmpty());

        aliasToAttribute.put("alias", new ClinicalAttributesMetadata());
        clinicalDataConverter.appendToFilteredRows(aliasToAttribute, row, alias, rows);
        assertTrue(rows.isEmpty());

        ClinicalAttributesMetadata okayed = new ClinicalAttributesMetadata();
        okayed.setAnnotationStatus(ClinicalDataConverterImpl.OK);
        aliasToAttribute = new HashMap<String, ClinicalAttributesMetadata>();
        aliasToAttribute.put("alias", okayed);
        row.add("foo");
        clinicalDataConverter.appendToFilteredRows(aliasToAttribute, row, alias, rows);
        assertTrue(rows.get(0).get(0).equals("foo"));
    }

    @Test
    public void appendToNewAttributesTest() {
        HashMap<String, ClinicalAttributesMetadata> oldAttributes = new HashMap<String, ClinicalAttributesMetadata>();
        List<String> rows = new ArrayList<String>();
        String alias = "";
        HashMap<String, ClinicalAttributesMetadata> newAttributes = new HashMap<String, ClinicalAttributesMetadata>();

        clinicalDataConverter.appendToNewAttributes(oldAttributes, rows, alias, newAttributes);
        assert(newAttributes.get("") != null);
        assert(newAttributes.size() == 1);
        clinicalDataConverter.appendToNewAttributes(oldAttributes, rows, alias, newAttributes);
        assert(newAttributes.size() == 1);
    }

    @Test
    public void transposeTest() {
        List<List<String>> vectors = new ArrayList<List<String>>();

        List<String> vec1 = new ArrayList<String>();
        vec1.add("11");
        vec1.add("12");
        List<String> vec2 = new ArrayList<String>();
        vec2.add("21");
        vec2.add("22");
        vectors.add(vec1);
        vectors.add(vec2);
        List<LinkedList<String>> transposed = clinicalDataConverter.transpose(vectors);

        assertTrue(transposed.get(0).get(1).equals("21"));
        assertTrue(transposed.get(1).get(0).equals("12"));
    }
}
