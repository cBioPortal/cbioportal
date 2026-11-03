/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.dao;

import java.util.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGenePanel;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test for DaoSample class
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoSampleProfile extends IntegrationTestBase {

	CancerStudy study;
    GenePanel genePanel;
	ArrayList<Integer> internalSampleIds;
	int geneticProfileId;

	@Before
	public void setUp() throws DaoException {
        CanonicalGene brca1 = DaoGeneOptimized.getInstance().getGene("BRCA1");
        CanonicalGene brca2 = DaoGeneOptimized.getInstance().getGene("BRCA2");
        CanonicalGene kras = DaoGeneOptimized.getInstance().getGene("KRAS");
        HashSet<CanonicalGene> canonicalGenes = new HashSet<CanonicalGene>();
        canonicalGenes.add(brca1);
        canonicalGenes.add(brca2);
        canonicalGenes.add(kras);
        DaoGenePanel.addGenePanel("testGenePanel", "Test gene panel description", canonicalGenes);
        genePanel = DaoGenePanel.getGenePanelByStableId("testGenePanel");

		study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
		geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mutations").getGeneticProfileId();

		internalSampleIds = new ArrayList<Integer>();
        Patient p = new Patient(study, "TCGA-12345");
        int pId = DaoPatient.addPatient(p);

        DaoSample.reCache();
        Sample s = new Sample("TCGA-12345-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
        s = new Sample("TCGA-123456-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
	}

	@Test
    public void testDaoSampleProfile() throws DaoException {

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(study.getInternalId(), "TCGA-12345");
        Sample sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-12345-01");

        DaoSampleProfile.upsertSampleToProfileMapping(List.of(
                new DaoSampleProfile.SampleProfileTuple(geneticProfileId, sample.getInternalId(), null)));

        boolean exists = DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId);
        assertTrue(exists);

        assertEquals(geneticProfileId, DaoSampleProfile.getProfileIdForSample(sample.getInternalId()));

        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-123456-01");
        DaoSampleProfile.upsertSampleToProfileMapping(List.of(
                new DaoSampleProfile.SampleProfileTuple(geneticProfileId, sample.getInternalId(), genePanel.getInternalId())));

        boolean existsByPanelId = DaoSampleProfile.sampleProfileMappingExistsByPanel(genePanel.getInternalId());
        assertTrue(existsByPanelId);

        ArrayList<Integer> sampleIds = DaoSampleProfile.getAllSampleIdsInProfile(geneticProfileId);
        assertEquals(9, sampleIds.size());
    }

}
