/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.util;

import java.io.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutSig;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.MutSig;
import org.mskcc.cbio.portal.util.MutSigReader;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestMutSigReader extends IntegrationTestBase {

	// TBD: change these to use getResourceAsStream()
    File properties = new File("target/test-classes/testCancerStudy.txt");
    File mutSigFile = new File("target/test-classes/test_mut_sig_data.txt");

	int studyId;
	
	@Before 
	public void setUp() throws DaoException
	{
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		DaoGeneticProfile.reCache();
	}

    @Test
    public void testloadMutSig() throws Exception {

        ProgressMonitor.setConsoleMode(false);

        MutSigReader.loadMutSig(studyId, mutSigFile);
        
        // Is the data in the database?
        MutSig mutSig = DaoMutSig.getMutSig("AKT1", studyId);
        assertTrue(mutSig != null);
        CanonicalGene testGene = mutSig.getCanonicalGene();
        assertTrue(testGene != null);

        assertTrue("AKT1".equals(testGene.getHugoGeneSymbolAllCaps()));
        assertEquals(mutSig.getNumMutations(), 5);
        assertEquals(mutSig.getNumBasesCovered(), 150306);
        assertTrue(Math.abs(mutSig.getpValue() - 1.82E-7) < 1E-12);
        assertTrue(Math.abs(mutSig.getqValue() - 2.7E-5) < 1E-12);
    }
}
