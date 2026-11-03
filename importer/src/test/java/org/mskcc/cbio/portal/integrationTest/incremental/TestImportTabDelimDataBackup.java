/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.scripts.ImportTabDelimData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestImportTabDelimDataBackup extends AbstractBackupTransactionTest {

    private static final List<String> TABLES = List.of("genetic_alteration", "genetic_profile_samples", "sample_profile");

    private static final File DATA_FILE =
        new File("src/test/resources/incremental/mrna_expression/data_expression_Zscores.txt");

    // Sample IDs known to exist in the test database for study_tcga_pub
    private static final List<Integer> SEED_SAMPLE_IDS = List.of(2, 3, 6);

    private int profileId;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        profileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mrna").getGeneticProfileId();
        // Ensure sample_profile has real associations for this profile so the backup is meaningful
        DaoSampleProfile.upsertSampleToProfileMapping(SEED_SAMPLE_IDS, profileId, null);
    }

    @Override
    protected List<String> getBackedUpTables() {
        return TABLES;
    }

    @Override
    protected void runSuccessfulImport() throws Exception {
        new ImportTabDelimData(DATA_FILE, profileId, null, true, DaoGeneOptimized.getInstance()).importData();
    }

    @Override
    protected void runNonIncrementalImport() throws Exception {
        new ImportTabDelimData(DATA_FILE, profileId, null, false, DaoGeneOptimized.getInstance()).importData();
    }

    @Override
    protected void runFailingImport() throws Exception {
        DaoGeneOptimized mockedDao = mock(DaoGeneOptimized.class);
        when(mockedDao.getGene(anyLong())).thenAnswer(invocation -> {
            assertBackupTablesExist();
            throw new RuntimeException("Simulated error");
        });
        new ImportTabDelimData(DATA_FILE, profileId, null, true, mockedDao).importData();
    }

    @Override
    protected Object captureDataState() throws DaoException {
        HashMap<Long, HashMap<Integer, String>> alterations =
            DaoGeneticAlteration.getInstance().getGeneticAlterationMap(profileId, null);
        ArrayList<Integer> sampleIds = DaoSampleProfile.getAllSampleIdsInProfile(profileId);
        return Map.of("alterations", alterations, "sampleProfile", sampleIds);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void assertDataStateUnchanged(Object stateBefore) throws DaoException {
        Map<String, Object> before = (Map<String, Object>) stateBefore;

        HashMap<Long, HashMap<Integer, String>> alterationsAfter =
            DaoGeneticAlteration.getInstance().getGeneticAlterationMap(profileId, null);
        assertEquals(before.get("alterations"), alterationsAfter);

        ArrayList<Integer> sampleProfileAfter = DaoSampleProfile.getAllSampleIdsInProfile(profileId);
        assertEquals(before.get("sampleProfile"), sampleProfileAfter);
    }
}
