/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Junit tests for DaoMicroRnaAlteration class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestDaoMicroRnaAlteration {

	CancerStudy study;
	ArrayList<Integer> internalSampleIds;
	int geneticProfileId;
	
	@Before
	public void setUp() throws DaoException {
		study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
		
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
        
        // Make a new empty genetic profile
		GeneticProfile geneticProfile = new GeneticProfile();
		geneticProfile.setCancerStudyId(study.getInternalId());
		geneticProfile.setProfileName("test profile");
		geneticProfile.setStableId("test");
		geneticProfile.setGeneticAlterationType(GeneticAlterationType.MUTATION_EXTENDED);
		geneticProfile.setDatatype("test");
		DaoGeneticProfile.addGeneticProfile(geneticProfile);

		geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("test");
		geneticProfileId = geneticProfile.getGeneticProfileId();
	}

	@Test
    public void testDaoMicroRnaAlterationBulkloadOff() throws DaoException {
        
        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        MySQLbulkLoader.bulkLoadOn();
    }
    
	@Test
    public void testDaoMicroRnaAlterationBulkloadOn() throws DaoException {
        
        // test with both values of MySQLbulkLoader.isBulkLoad()
        runTheTest();
    }
    
    private void runTheTest() throws DaoException{

        //  Add the Case List
        int numRows = DaoGeneticProfileSamples.addGeneticProfileSamples(geneticProfileId, internalSampleIds);
        assertEquals (1, numRows);

        String data = "1.2:1.4:1.6:1.8";
        String values[] = data.split(":");

        DaoMicroRnaAlteration dao = DaoMicroRnaAlteration.getInstance();
        int num = dao.addMicroRnaAlterations(geneticProfileId, "hsa-123", values);

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
           MySQLbulkLoader.flushAll();
        }

        String value = dao.getMicroRnaAlteration(geneticProfileId, internalSampleIds.get(0), "hsa-123");
        assertEquals("1.2", value);
        value = dao.getMicroRnaAlteration(geneticProfileId, internalSampleIds.get(1), "hsa-123");
        assertEquals("1.4", value);

        HashMap<Integer, String> map = dao.getMicroRnaAlterationMap(geneticProfileId, "hsa-123");
        assertEquals (4, map.size());
        assertTrue (map.containsKey(internalSampleIds.get(1)));
        assertTrue (map.containsKey(internalSampleIds.get(2)));

        Set<String> microRnaSet = dao.getGenesInProfile(geneticProfileId);
        assertEquals (1, microRnaSet.size());
    }
}