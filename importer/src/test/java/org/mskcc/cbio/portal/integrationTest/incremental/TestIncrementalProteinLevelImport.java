/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;
import java.util.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
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
 * Tests Incremental Import of PROTEIN_LEVEL Data.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestIncrementalProteinLevelImport extends IntegrationTestBase {

    /**
     * Test incremental upload of PROTEIN_LEVEL
     */
    @Test
    public void testRppa() throws DaoException {
        /**
         * Prior checks
         */
        // Hugo_Symbol: CDK1
        final long newGeneEntrezId = 983l;
        // Gene that is part of the platform, but absent during the incremental upload
        // Hugo_Symbol: ARAF
        final long absentGeneEntrezId = 369l;
        final Set<Long> noChangeEntrezIds = Set.of(10000l, 207l, 208l,  3265l, 3845l,  472l,  4893l,  672l,  673l,  675l);
        final Set<Long> beforeEntrezIds = new HashSet<>(noChangeEntrezIds);
        beforeEntrezIds.add(absentGeneEntrezId);

        // stable_id: TCGA-A1-A0SB-01
        final int newSampleId = 1;
        // stable_id: TCGA-A1-A0SD-01
        final int updateSampleId = 2;
        final Set<Integer> noChangeSampleIds = Set.of(3, 6, 8, 9, 10, 12, 13);
        final Set<Integer> beforeSampleIds = new HashSet<>(noChangeSampleIds);
        beforeSampleIds.add(updateSampleId);

        GeneticProfile rppaProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_rppa");
        assertNotNull(rppaProfile);

        HashMap<Long, HashMap<Integer, String>> beforeResult = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(rppaProfile.getGeneticProfileId(), null);
        assertPriorDataState(beforeResult, beforeEntrezIds, beforeSampleIds);

        File dataFolder = new File("src/test/resources/incremental/protein_level/");
        File metaFile = new File(dataFolder, "meta_rppa.txt");
        File dataFile = new File(dataFolder, "data_rppa.txt");

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
        HashMap<Long, HashMap<Integer, String>> afterResult = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(rppaProfile.getGeneticProfileId(), null);
        assertEquals("After result should get exactly one new gene", beforeEntrezIds.size() + 1,
                afterResult.size());
        afterResult.values()
                .forEach(sampleToValue ->
                        assertEquals("Each gene row has to get one extra sample",beforeSampleIds.size() + 1, sampleToValue.size()));
        assertNoChange(beforeResult, afterResult, noChangeEntrezIds, noChangeSampleIds);
        assertEquals("-0.141047088398489", afterResult.get(newGeneEntrezId).get(newSampleId));
        assertEquals("1.61253243564957", afterResult.get(newGeneEntrezId).get(updateSampleId));
        assertEquals("", afterResult.get(absentGeneEntrezId).get(newSampleId));
        assertEquals("", afterResult.get(absentGeneEntrezId).get(updateSampleId));
    }

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
    }

}
