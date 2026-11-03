/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;
import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.ImportProfileData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mskcc.cbio.portal.integrationTest.incremental.GeneticAlterationsTestHelper.assertNoChange;
import static org.mskcc.cbio.portal.integrationTest.incremental.GeneticAlterationsTestHelper.assertPriorDataState;

/**
 * Tests Incremental Import of MRNA_EXPRESSION Data.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestIncrementalMrnaExpressionImport extends IntegrationTestBase {

    /**
     * Test incremental upload of MRNA_EXPRESSION
     */
    @Test
    public void testMrnaExpression() throws DaoException, IOException {
        /**
         * Prior checks
         */
        // Hugo_Symbol: CDK1
        final long newGeneEntrezId = 983l;
        // Gene that is part of the platform, but absent during the incremental upload
        // Hugo_Symbol: ARAF
        final long absentGeneEntrezId = 369l;
        final Set<Long> noChangeEntrezIds = Set.of(10000l, 207l, 208l, 3265l, 3845l, 472l, 4893l, 672l, 673l, 675l);
        final Set<Long> beforeEntrezIds = new HashSet<>(noChangeEntrezIds);
        beforeEntrezIds.add(absentGeneEntrezId);

        // stable_id: TCGA-A1-A0SB-01
        final int newSampleId = 1;
        // stable_id: TCGA-A1-A0SD-01
        final int updateSampleId = 2;
        final Set<Integer> noChangeSampleIds = Set.of(3, 6, 8, 9, 10, 12, 13);
        final Set<Integer> beforeSampleIds = new HashSet<>(noChangeSampleIds);
        beforeSampleIds.add(updateSampleId);

        GeneticProfile mrnaProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mrna");
        assertNotNull(mrnaProfile);

        HashMap<Long, HashMap<Integer, String>> beforeResult = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(mrnaProfile.getGeneticProfileId(), null);
        assertPriorDataState(beforeResult, beforeEntrezIds, beforeSampleIds);

        File dataFolder = new File("src/test/resources/incremental/mrna_expression/");
        File metaFile = new File(dataFolder, "meta_expression_Zscores.txt");
        File dataFile = new File(dataFolder, "data_expression_Zscores.txt");

        /**
         * Test
         */
        new ImportProfileData(new String[] {
                "--loadMode", "bulkLoad",
                "--meta", metaFile.getAbsolutePath(),
                "--data", dataFile.getAbsolutePath(),
                "--overwrite-existing",
        }).run();

        /**
         * After test assertions
         */
        HashMap<Long, HashMap<Integer, String>> afterResult = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(mrnaProfile.getGeneticProfileId(), null);
        assertEquals("After result should get exactly one new gene", beforeEntrezIds.size() + 1,
                afterResult.size());
        afterResult.values()
                .forEach(sampleToValue ->
                        assertEquals("Each gene row has to get one extra sample", beforeSampleIds.size() + 1, sampleToValue.size()));
        assertNoChange(beforeResult, afterResult, noChangeEntrezIds, noChangeSampleIds);
        HashMap<Integer, String> newGeneRow = afterResult.get(newGeneEntrezId);
        assertEquals("-0.1735", newGeneRow.get(newSampleId));
        assertEquals("-0.6412", newGeneRow.get(updateSampleId));
        HashMap<Integer, String> absentGeneRow = afterResult.get(absentGeneEntrezId);
        assertEquals("", absentGeneRow.get(newSampleId));
        assertEquals("", absentGeneRow.get(updateSampleId));
    }
}
