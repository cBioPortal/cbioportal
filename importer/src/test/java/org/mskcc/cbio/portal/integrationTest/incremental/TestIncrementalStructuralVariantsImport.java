/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.incremental;

import java.io.*;
import java.util.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.dao.DaoStructuralVariant;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.scripts.ImportProfileData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests Incremental Import of Structural Variants Data.
 *
 * @author Ruslan Forostianov
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestIncrementalStructuralVariantsImport extends IntegrationTestBase {

    public static final String STUDY_ID = "study_tcga_pub";
    private CancerStudy cancerStudy;

    @Before
    public void setUp() throws DaoException {
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId(STUDY_ID);
    }
    /**
     * Test incremental upload of SV data
     */
	@Test
    public void testIncrementalUpload() throws DaoException {
        GeneticProfile svGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_structural_variants");
        assertNotNull(svGeneticProfile);
        String svDataSampleId = "TCGA-A1-A0SE-01";
        /**
         * this sample does not have SV data attached
         */
        Sample svDataSample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), svDataSampleId);

        StructuralVariant structuralVariant = new StructuralVariant();
        structuralVariant.setSampleIdInternal(svDataSample.getInternalId());
        structuralVariant.setGeneticProfileId(svGeneticProfile.getGeneticProfileId());
        structuralVariant.setAnnotation("TESTANNOT");
        structuralVariant.setDriverFilter("DRVFILTER");
        structuralVariant.setSite1RegionNumber(1);
        structuralVariant.setSite2RegionNumber(2);
        structuralVariant.setComments("This record has to be overwritten");
        DaoStructuralVariant.addStructuralVariantToBulkLoader(structuralVariant);
        ClickHouseBulkLoader.flushAll();
        DaoSampleProfile.upsertSampleToProfileMapping(List.of(
                new DaoSampleProfile.SampleProfileTuple(svGeneticProfile.getGeneticProfileId(), svDataSample.getInternalId(), null)));

        File singleTcgaSampleFolder = new File("src/test/resources/incremental/structural_variants/");
        File metaFile = new File(singleTcgaSampleFolder, "meta_structural_variants.txt");
        File dataFile = new File(singleTcgaSampleFolder, "data_structural_variants.txt");

        ImportProfileData importProfileData = new ImportProfileData(new String[] {
                "--loadMode", "bulkLoad",
                "--meta", metaFile.getAbsolutePath(),
                "--data", dataFile.getAbsolutePath(),
                "--overwrite-existing",
        });
        importProfileData.run();

        List<StructuralVariant> structuralVariants = DaoStructuralVariant.getAllStructuralVariants();
        assertEquals(3, structuralVariants.size());
        Set.of("site1_test_desc_1", "site1_test_desc_2", "site1_test_desc_3").forEach(site1Desc -> {
                Optional<StructuralVariant> osv = structuralVariants.stream()
                        .filter(sv -> site1Desc.equals(sv.getSite1Description())
                                && sv.getSampleIdInternal() == svDataSample.getInternalId()
                                && sv.getGeneticProfileId() == svGeneticProfile.getGeneticProfileId()).findFirst();
                assertTrue(osv.isPresent());
                assertNotNull(osv.get().getDriverFilter());
        });
        GenePanel genePanel = DaoGenePanel.getGenePanelByStableId("TSTGNPNLSV");
        assertEquals("Sample profile has to point to TSTGNPNLSV panel",
                genePanel.getInternalId(),
                DaoSampleProfile.getPanelId(svDataSample.getInternalId(), svGeneticProfile.getGeneticProfileId()));
    }

}
