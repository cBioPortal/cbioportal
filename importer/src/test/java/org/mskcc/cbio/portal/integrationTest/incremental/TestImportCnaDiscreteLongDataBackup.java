/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoCnaEvent;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;

import org.mskcc.cbio.portal.scripts.ImportCnaDiscreteLongData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestImportCnaDiscreteLongDataBackup extends AbstractBackupTransactionTest {

    private static final List<String> TABLES =
        List.of("genetic_alteration", "genetic_profile_samples", "sample_cna_event", "sample_profile");

    private static final File DATA_FILE =
        new File("src/test/resources/incremental/copy_number_alteration/data_cna_discrete_long.txt");

    private int profileId;

    // Sample IDs known to exist in the test database for study_tcga_pub
    private static final List<Integer> SEED_SAMPLE_IDS = List.of(2, 3, 6);

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        profileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gistic").getGeneticProfileId();
        // Seed sample_profile so the backup has real associations to verify against
        DaoSampleProfile.upsertSampleToProfileMapping(SEED_SAMPLE_IDS, profileId, null);
    }

    @Override
    protected List<String> getBackedUpTables() {
        return TABLES;
    }

    @Override
    protected void runSuccessfulImport() throws Exception {
        new ImportCnaDiscreteLongData(DATA_FILE, profileId, null, DaoGeneOptimized.getInstance(), Set.of(), true).importData();
    }

    @Ignore("Read timed out in ClickHouse during runSuccessfulImport")
    @Test
    @Override
    public void testOnSuccess_noBackupTablesExist() throws Exception {}

    @Override
    protected void runNonIncrementalImport() throws Exception {
        new ImportCnaDiscreteLongData(DATA_FILE, profileId, null, DaoGeneOptimized.getInstance(), Set.of(), false).importData();
    }

    @Override
    protected void runFailingImport() throws Exception {
        DaoGeneOptimized mockedDao = mock(DaoGeneOptimized.class);
        when(mockedDao.getGene(anyLong())).thenAnswer(invocation -> {
            assertBackupTablesExist();
            throw new RuntimeException("Simulated error");
        });
        new ImportCnaDiscreteLongData(DATA_FILE, profileId, null, mockedDao, Set.of(), true).importData();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object captureDataState() throws DaoException {
        return Map.of(
            "genetic_alteration", DaoGeneticAlteration.getInstance().getGeneticAlterationMap(profileId, null),
            "sample_cna_event",   Set.copyOf(DaoCnaEvent.getAllCnaEvents()),
            "sample_profile",     new ArrayList<>(DaoSampleProfile.getAllSampleIdsInProfile(profileId))
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void assertDataStateUnchanged(Object stateBefore) throws DaoException {
        Map<String, Object> before = (Map<String, Object>) stateBefore;
        assertEquals(before.get("genetic_alteration"),
            DaoGeneticAlteration.getInstance().getGeneticAlterationMap(profileId, null));
        assertEquals(before.get("sample_cna_event"),
            Set.copyOf(DaoCnaEvent.getAllCnaEvents()));
        assertEquals(before.get("sample_profile"),
            new ArrayList<>(DaoSampleProfile.getAllSampleIdsInProfile(profileId)));
    }
}
