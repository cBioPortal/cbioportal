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
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileSamples;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;

/**
 * JUnit tests for DaoGeneticAlteration class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestDaoGeneticAlteration extends IntegrationTestBase {
	
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

	@Test
    public void testDaoGeneticAlterationBulkOn() throws DaoException {
        
        // test with ClickHouseBulkLoader.isBulkLoad()
		runTheTest();
	}

	@Test
    public void testDaoGeneticAlterationBulkOff() throws DaoException {
        
        // test without ClickHouseBulkLoader.isBulkLoad()
        ClickHouseBulkLoader.bulkLoadOff();
        runTheTest();
        ClickHouseBulkLoader.bulkLoadOn();
    }
    
    private void runTheTest() throws DaoException{

        //  Add the Sample List
        int numRows = DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, internalSampleIds);
        assertEquals (1, numRows);

        //  Add Some Data
        String data = "200:400:600:800";
        String values[] = data.split(":");
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();
        numRows = dao.addGeneticAlterations(geneticProfileId, 672, values);
        assertEquals (1, numRows);

        // if bulkLoading, execute LOAD FILE
        if( ClickHouseBulkLoader.isBulkLoad()){
           ClickHouseBulkLoader.flushAll();
        }

        HashMap<Integer, String> valueMap = dao.getGeneticAlterationMap(geneticProfileId, 672);
        assertEquals ("200", valueMap.get(internalSampleIds.get(0)));
        assertEquals ("400", valueMap.get(internalSampleIds.get(1)));
        assertEquals ("600", valueMap.get(internalSampleIds.get(2)));
        assertEquals ("800", valueMap.get(internalSampleIds.get(3)));

        //  Test the getGenesInProfile method
        Set <CanonicalGene> geneSet = dao.getGenesInProfile(geneticProfileId);
        ArrayList <CanonicalGene> geneList = new ArrayList <CanonicalGene> (geneSet);
        assertEquals (1, geneList.size());
        CanonicalGene gene = geneList.get(0);
        assertEquals ("BRCA1", gene.getHugoGeneSymbolAllCaps());
        assertEquals (672, gene.getEntrezGeneId());
        
    }
}
