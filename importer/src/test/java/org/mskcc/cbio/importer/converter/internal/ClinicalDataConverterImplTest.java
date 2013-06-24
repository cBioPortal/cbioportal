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
import org.mskcc.cbio.importer.model.DataMatrix;

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
        assertEquals("abc", clinicalDataConverter.cleanAlias("abc"));
        assertEquals("abcv1.0", clinicalDataConverter.cleanAlias("abcv1.0"));
        assertEquals("drugs.drugabc.a", clinicalDataConverter.cleanAlias("drugs.drugabc-1.a"));
        assertEquals("followups.followup.", clinicalDataConverter.cleanAlias("followups.followupv5.0."));
        assertEquals("followups.followup.1", clinicalDataConverter.cleanAlias("followups.followupv5.0-1"));
        assertEquals("followups.followup.a", clinicalDataConverter.cleanAlias("followups.followupv5.0.a-5"));
        assertEquals("patient.followups.followup.daystodeath",
                clinicalDataConverter.cleanAlias("patient.followups.followupv1.5-2.daystodeath"));
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

    @Test
    public void removeDuplicateRowsTest() throws Exception {
        String in = "CASE_ID\tAGE\tDAYS_TO_DEATH\tDAYS_TO_LAST_FOLLOWUP\tETHNICITY\tGENDER\tVITAL_STATUS\n" +
                "#Patient identifier\tAge\tdays to death\tdays to last followup\tEthnicity\tSex\tVital status\n" +
                "#Patient identifier\tPatient age at diagnosis\tnumber of days until the patient died\tnumber of days until the last followup\tPatient ethnicity\tPatient sex\tPatient vital status as of last follow-up\n" +
                "#STRING\tNUMBER\tNUMBER\tNUMBER\tSTRING\tSTRING\tBOOLEAN\n" +
                "tcga-a1-a0sb\t70\tNA\t259\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sd\t59\tNA\t437\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sb\t70\tNA\t259\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sd\t59\tNA\t437\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0se\t56\tNA\t1320\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0se\t56\tNA\t1320\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sf\t54\tNA\t1463\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sf\t54\tNA\t1463\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sg\t61\tNA\t433\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sg\t61\tNA\t433\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sh\t39\tNA\t1437\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sh\t39\tNA\t1437\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0si\t52\tNA\t634\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0si\t52\tNA\t634\tnot hispanic or latino\tfemale\tliving\n";

        DataMatrix testMatrix = DataMatrix.fromString(in);
        testMatrix =  clinicalDataConverter.removeDuplicateRows(testMatrix);

        testMatrix.write(System.out);
    }

    @Test
    public void processMatrixTest() throws Exception {
        // no unit tests, just a playground

        // test DataMatrix
        String in = "CASE_ID\tAGE\tDAYS_TO_DEATH\tDAYS_TO_LAST_FOLLOWUP\tETHNICITY\tGENDER\tVITAL_STATUS\n" +
                "#Patient identifier\tAge\tdays to death\tdays to last followup\tEthnicity\tSex\tVital status\n" +
                "#Patient identifier\tPatient age at diagnosis\tnumber of days until the patient died\tnumber of days until the last followup\tPatient ethnicity\tPatient sex\tPatient vital status as of last follow-up\n" +
                "#STRING\tNUMBER\tNUMBER\tNUMBER\tSTRING\tSTRING\tBOOLEAN\n" +
                "tcga-a1-a0sb\t70\tNA\t259\tnot hispanic or latino\tfemale\tliving tcga-a1-a0sd\t59\tNA\t437\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0se\t56\tNA\t1320\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sf\t54\tNA\t1463\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sg\t61\tNA\t433\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sh\t39\tNA\t1437\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0si\t52\tNA\t634\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sj\t39\tNA\t426\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sk\t54\t967\tNA\tnot hispanic or latino\tfemale\tdeceased\n" +
                "tcga-a1-a0sm\t77\tNA\t242\tnot hispanic or latino\tmale\tliving\n" +
                "tcga-a1-a0sn\t50\tNA\t1196\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0so\t67\tNA\t852\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sp\t40\tNA\t583\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a1-a0sq\t45\tNA\t553\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a04n\t66\tNA\t3153\thispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a04p\t36\t547\tNA\tNA\tfemale\tdeceased\n" +
                "tcga-a2-a04q\t48\tNA\t1275\tNA\tfemale\tliving\n" +
                "tcga-a2-a04r\t36\tNA\t2364\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a04t\t62\tNA\t1950\tNA\tfemale\tliving\n" +
                "tcga-a2-a04u\t47\tNA\t670\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a04v\t39\t1920\tNA\tnot hispanic or latino\tfemale\tdeceased\n" +
                "tcga-a2-a04w\t50\tNA\t1918\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a04x\t34\tNA\t1349\tNA\tfemale\tliving\n" +
                "tcga-a2-a04y\t53\tNA\t763\tNA\tfemale\tliving\n" +
                "tcga-a2-a0cl\t37\tNA\t1827\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cm\t40\t754\tNA\tnot hispanic or latino\tfemale\tdeceased\n" +
                "tcga-a2-a0cp\t60\tNA\t2495\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cq\t62\tNA\t2392\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cs\t73\tNA\t2298\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0ct\t71\tNA\t1917\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cu\t73\t157\tNA\tnot hispanic or latino\tfemale\tdeceased\n" +
                "tcga-a2-a0cv\t41\tNA\t1870\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cw\t67\tNA\t1750\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cx\t52\tNA\t1303\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cy\t63\tNA\t1288\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0cz\t46\tNA\t1338\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0d0\t60\tNA\t643\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0d1\t76\tNA\t786\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0d2\t45\tNA\t761\tnot hispanic or latino\tfemale\tliving\n" +
                "tcga-a2-a0d3\t42\tNA\t736\tnot hispanic or latino\tfemale\tliving\n";

        DataMatrix testDataMatrix = DataMatrix.fromString(in);
        DataMatrix processed = clinicalDataConverter.processMatrix(testDataMatrix);

//        processed.write(System.out);
    }

    @Test
    public void addOverAllSurvivalTest() throws Exception {
        // just a playground

        List<String> daysToLastFollowUp = new ArrayList<String>();
        daysToLastFollowUp.add("1");
        daysToLastFollowUp.add("2");
        daysToLastFollowUp.add("3");

        List<String> daysToDeath = new ArrayList<String>();
        daysToDeath.add("4");
        daysToDeath.add("5");
        daysToDeath.add("6");

        DataMatrix testMatrix = DataMatrix.fromString("");

//        clinicalDataConverter.addOverAllSurvival(daysToLastFollowUp, daysToDeath, testDataMatrix);
    }

    @Test
    public void calcLatestAttributeTest() throws Exception {

        String in = "patient.daystodeath\t10\n"
                + "patient.followups.followupv1.5.daystodeath\tNA\n"
                + "patient.followups.followupv1.5-2.daystodeath\tNA\n";

        DataMatrix dataMatrix = DataMatrix.fromString(in);

        assertEquals("10", clinicalDataConverter.calcLatestAttribute(dataMatrix, "daystodeath"));
    }
}
