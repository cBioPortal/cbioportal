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
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.model.SampleListCategory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit test for DaoCase List.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoSampleList extends IntegrationTestBase {
	
	CancerStudy study;
	
	@Before
	public void setUp() throws DaoException {
		study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        Patient p = new Patient(study, "TCGA-1");
        int pId = DaoPatient.addPatient(p);
        DaoSample.addSample(new Sample("TCGA-1-S1", pId, "brca"));

        p = new Patient(study, "TCGA-2");
        pId = DaoPatient.addPatient(p);
        DaoSample.addSample(new Sample("TCGA-2-S1", pId, "brca"));
	}

	@Test
    public void testDaoSampleList() throws DaoException {
        DaoSampleList daoSampleList = new DaoSampleList();
        SampleList sampleList = new SampleList();
        sampleList.setName("Name0");
        sampleList.setDescription("Description0");
        sampleList.setStableId("stable_0");
        sampleList.setCancerStudyId(study.getInternalId());
        sampleList.setSampleListCategory(SampleListCategory.ALL_CASES_WITH_CNA_DATA);
        ArrayList<String> samples = new ArrayList<String>();
        samples.add("TCGA-1-S1");
        samples.add("TCGA-2-S1");
        sampleList.setSampleList(samples);
        daoSampleList.addSampleList(sampleList);
        assertTrue(sampleList.getSampleListId() > 0);
        
        // Only patients with samples are returned. No samples, no returny in the listy.
        SampleList sampleListFromDb = daoSampleList.getSampleListByStableId("stable_0");
        assertEquals(sampleList.getSampleListId(), sampleListFromDb.getSampleListId());
        assertEquals("Name0", sampleListFromDb.getName());
        assertEquals("Description0", sampleListFromDb.getDescription());
        assertEquals(SampleListCategory.ALL_CASES_WITH_CNA_DATA, sampleListFromDb.getSampleListCategory());
        assertEquals("stable_0", sampleListFromDb.getStableId());
        assertEquals(2, sampleListFromDb.getSampleList().size());
    }

}
