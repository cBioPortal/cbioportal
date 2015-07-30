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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

/**
 * JUnit test for DaoCase List.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestDaoPatientList {
	
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
    public void testDaoPatientList() throws DaoException {
        DaoPatientList daoPatientList = new DaoPatientList();
        PatientList patientList = new PatientList();
        patientList.setName("Name0");
        patientList.setDescription("Description0");
        patientList.setStableId("stable_0");
        patientList.setCancerStudyId(study.getInternalId());
        patientList.setPatientListCategory(PatientListCategory.ALL_CASES_WITH_CNA_DATA);
        ArrayList<String> patients = new ArrayList<String>();
        patients.add("TCGA-1-S1");
        patients.add("TCGA-2-S1");
        patientList.setPatientList(patients);
        daoPatientList.addPatientList(patientList);
        
        // Only patients with samples are returned. No samples, no returny in the listy.
        PatientList patientListFromDb = daoPatientList.getPatientListByStableId("stable_0");
        assertEquals("Name0", patientListFromDb.getName());
        assertEquals("Description0", patientListFromDb.getDescription());
        assertEquals(PatientListCategory.ALL_CASES_WITH_CNA_DATA, patientListFromDb.getPatientListCategory());
        assertEquals("stable_0", patientListFromDb.getStableId());
        assertEquals(2, patientListFromDb.getPatientList().size());
    }

}
