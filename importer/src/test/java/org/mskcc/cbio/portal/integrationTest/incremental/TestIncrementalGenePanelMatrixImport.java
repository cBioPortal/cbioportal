/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;
import java.util.*;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.scripts.ImportGenePanelProfileMap;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests Incremental Import of Gene Panel Matrix Data.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestIncrementalGenePanelMatrixImport extends IntegrationTestBase {

    /**
     * Test incremental upload
     */
	@Test
    public void testIncrementalUpload() throws DaoException {
        File dataFolder = new File("src/test/resources/incremental/gene_panel_matrix/");
        File metaFile = new File(dataFolder, "meta_gene_panel_matrix.txt");
        File dataFile = new File(dataFolder, "data_gene_panel_matrix.txt");

        ImportGenePanelProfileMap importGenePanelProfileMap = new ImportGenePanelProfileMap(new String[] {
                "--meta", metaFile.getAbsolutePath(),
                "--data", dataFile.getAbsolutePath(),
                "--overwrite-existing",
        });
        importGenePanelProfileMap.run();

        GenePanel mutationGenePanel = DaoGenePanel.getGenePanelByStableId("TSTGNPNLMUTEXT");
        GeneticProfile mutationsProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mutations");
        GenePanel longGenePanel = DaoGenePanel.getGenePanelByStableId("TESTPANEL_CNA_DISCRETE_LONG_FORMAT");
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_gistic");
        GeneticProfile ic50Profile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_treatment_ic50");
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), "TCGA-A1-A0SB-01");
        assertEquals(mutationGenePanel.getInternalId(),
                DaoSampleProfile.getPanelId(sample.getInternalId(), mutationsProfile.getGeneticProfileId()));
        assertEquals(longGenePanel.getInternalId(),
                DaoSampleProfile.getPanelId(sample.getInternalId(), geneticProfile.getGeneticProfileId()));
        assertNull(DaoSampleProfile.getPanelId(sample.getInternalId(), ic50Profile.getGeneticProfileId()));
    }

}
