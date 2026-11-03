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
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileSamples;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;

/**
 * JUnit Tests for the Dao Genetic Profile Cases Class.
 *
 * @author Ethan Cerami.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoGeneticProfileSamples extends IntegrationTestBase {
	
	CancerStudy study;
	ArrayList<Integer> internalSampleIds;
	int geneticProfileId;
	
	@Before
	public void setUp() throws DaoException {
		study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
		geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_mutations").getGeneticProfileId();
		
		internalSampleIds = new ArrayList<Integer>();
        Patient p = new Patient(study, "TCGA-1");
        int pId = DaoPatient.addPatient(p);
        
        DaoSample.reCache();
        Sample s = new Sample("XCGA-A1-A0SB-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
        s = new Sample("XCGA-A1-A0SD-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
        s = new Sample("XCGA-A1-A0SE-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
        s = new Sample("XCGA-A1-A0SF-01", pId, "brca");
        internalSampleIds.add(DaoSample.addSample(s));
	}

    /**
     * Tests the Dao Genetic Profile Samples Class.
     * @throws DaoException Database Exception.
     */
	@Test
    public void testDaoGeneticProfileSamples() throws DaoException {

        ArrayList<Integer> orderedSampleList = new ArrayList<Integer>();
        int numRows = DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, internalSampleIds);

        assertEquals (1, numRows);

        orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(geneticProfileId);
        assertEquals (4, orderedSampleList.size());

        //  Test the Delete method
        DaoGeneticProfileSamples.deleteAllSamplesInGeneticProfile(geneticProfileId);
        orderedSampleList = DaoGeneticProfileSamples.getOrderedSampleList(geneticProfileId);
        assertEquals (0, orderedSampleList.size());
    }

}
