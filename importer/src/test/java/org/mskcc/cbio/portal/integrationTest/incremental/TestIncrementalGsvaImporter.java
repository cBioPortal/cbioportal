/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.scripts.ImportTabDelimData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertThrows;

/**
 * Tests Incremental Import is not supported for GSVA data type
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestIncrementalGsvaImporter extends IntegrationTestBase {
    @Test
    public void testGsvaIsNotSupported() throws DaoException, IOException {
        GeneticProfile gsvaProfile = new GeneticProfile();
        gsvaProfile.setCancerStudyId(DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId());
        gsvaProfile.setStableId("gsva_scores");
        gsvaProfile.setDatatype("GENESET_SCORE");
        gsvaProfile.setGeneticAlterationType(GeneticAlterationType.GENESET_SCORE);
        gsvaProfile.setProfileName("gsva test platform");
        DaoGeneticProfile.addGeneticProfile(gsvaProfile);

        assertThrows(UnsupportedOperationException.class, () ->
                new ImportTabDelimData(File.createTempFile("gsva", "test"),
                        DaoGeneticProfile.getGeneticProfileByStableId("gsva_scores").getGeneticProfileId(),
                        null,
                        true,
                        DaoGeneOptimized.getInstance()));
    }

}
