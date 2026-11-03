/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGenericAssay;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.shared.GeneticEntity;
import org.mskcc.cbio.portal.scripts.ImportGenericAssayPatientLevelData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
public class TestImportGenericAssayPatientLevelDataBackup extends AbstractBackupTransactionTest {

    private static final List<String> TABLES = List.of("genetic_alteration", "genetic_profile_samples");

    private static final File GOOD_FILE =
        new File("src/test/resources/tabDelimitedData/data_patient_generic_assay.txt");
    private static final File BAD_FILE =
        new File("src/test/resources/tabDelimitedData/data_patient_generic_assay_bad_patient.txt");

    private int profileId;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();

        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");

        GeneticProfile profile = new GeneticProfile();
        profile.setCancerStudyId(study.getInternalId());
        profile.setGeneticAlterationType(GeneticAlterationType.GENERIC_ASSAY);
        profile.setStableId("study_tcga_pub_generic_assay_patient_backup_test");
        profile.setProfileName("Generic Assay Patient Level Backup Test");
        profile.setDatatype("LIMIT-VALUE");
        profile.setPatientLevel(true);
        DaoGeneticProfile.addGeneticProfile(profile);

        profileId = DaoGeneticProfile
            .getGeneticProfileByStableId("study_tcga_pub_generic_assay_patient_backup_test")
            .getGeneticProfileId();

        GeneticEntity entity1 = new GeneticEntity("GENERIC_ASSAY", "test_patient_generic_assay_1");
        entity1 = DaoGeneticEntity.addNewGeneticEntity(entity1);
        DaoGenericAssay.setGenericEntityProperty(entity1.getId(), "name", "test_patient_generic_assay_1");
        DaoGenericAssay.setGenericEntityProperty(entity1.getId(), "description", "test_patient_generic_assay_1");

        GeneticEntity entity2 = new GeneticEntity("GENERIC_ASSAY", "test_patient_generic_assay_2");
        entity2 = DaoGeneticEntity.addNewGeneticEntity(entity2);
        DaoGenericAssay.setGenericEntityProperty(entity2.getId(), "name", "test_patient_generic_assay_2");
        DaoGenericAssay.setGenericEntityProperty(entity2.getId(), "description", "test_patient_generic_assay_2");
    }

    @Override
    protected List<String> getBackedUpTables() {
        return TABLES;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void runSuccessfulImport() throws Exception {
        new ImportGenericAssayPatientLevelData(GOOD_FILE, null, profileId, null, "name,description", true).importData();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void runNonIncrementalImport() throws Exception {
        new ImportGenericAssayPatientLevelData(GOOD_FILE, null, profileId, null, "name,description", false).importData();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void runFailingImport() throws Exception {
        // BAD_FILE references TCGA-XX-FAKE which is not in the study, triggering
        // RuntimeException("Unknown patient id ...") after backups are created.
        // No mock injection point is available, so the mid-import assertion is
        // skipped for this importer; it is covered by the tab-delim and CNA tests.
        new ImportGenericAssayPatientLevelData(BAD_FILE, null, profileId, null, "name,description", true).importData();
    }

    @Override
    protected Object captureDataState() throws DaoException {
        try {
            return DaoGeneticAlteration.getInstance().getGeneticAlterationMapForEntityIds(profileId, null);
        } catch (IllegalArgumentException e) {
            // Profile has no samples yet (freshly created before import)
            return new HashMap<>();
        }
    }

    @Override
    protected void assertDataStateUnchanged(Object stateBefore) throws DaoException {
        HashMap<Integer, HashMap<Integer, String>> after;
        try {
            after = DaoGeneticAlteration.getInstance().getGeneticAlterationMapForEntityIds(profileId, null);
        } catch (IllegalArgumentException e) {
            // Profile has no samples (restored to pre-import state)
            after = new HashMap<>();
        }
        assertEquals(stateBefore, after);
    }
}
