/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;
import java.util.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.ImportTabDelimData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests Transaction for Incremental Import of Tab Delimited Data.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestIncrementalTabDelimDataTransaction extends IntegrationTestBase {

    /**
     * Test transaction
     */
    @ExtendWith(MockitoExtension.class)
    @Test
    public void testTransaction() throws Exception {
        GeneticProfile mrnaProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mrna");

        File dataFolder = new File("src/test/resources/incremental/mrna_expression/");
        File dataFile = new File(dataFolder, "data_expression_Zscores.txt");

        HashMap<Long, HashMap<Integer, String>> beforeResult = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(mrnaProfile.getGeneticProfileId(), null);

        DaoGeneOptimized mockedDao = mock(DaoGeneOptimized.class);

        when(mockedDao.getGene(anyLong()))
                .thenThrow(new RuntimeException("Simulated error"));
        /**
         * Test
         */
        try {
            new ImportTabDelimData(dataFile,
                    mrnaProfile.getGeneticProfileId(),
                    null,
                    true,
                    mockedDao).importData();
            fail("Import has to fail");
        } catch (RuntimeException runtimeException) {
            assertTrue(runtimeException.getMessage(), runtimeException.getMessage().contains("Simulated error"));
            assertTrue(true);
        }

        /**
         * After test assertions
         */
        HashMap<Long, HashMap<Integer, String>> afterResult = DaoGeneticAlteration.getInstance().getGeneticAlterationMap(mrnaProfile.getGeneticProfileId(), null);
        assertEquals(beforeResult, afterResult);
    }

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
    }
}
