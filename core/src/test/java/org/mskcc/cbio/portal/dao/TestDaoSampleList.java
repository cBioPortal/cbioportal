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

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * JUnit test for DaoCase List.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoSampleList {
	
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
        
        // Only patients with samples are returned. No samples, no returny in the listy.
        SampleList sampleListFromDb = daoSampleList.getSampleListByStableId("stable_0");
        assertEquals("Name0", sampleListFromDb.getName());
        assertEquals("Description0", sampleListFromDb.getDescription());
        assertEquals(SampleListCategory.ALL_CASES_WITH_CNA_DATA, sampleListFromDb.getSampleListCategory());
        assertEquals("stable_0", sampleListFromDb.getStableId());
        assertEquals(2, sampleListFromDb.getSampleList().size());
    }

}
