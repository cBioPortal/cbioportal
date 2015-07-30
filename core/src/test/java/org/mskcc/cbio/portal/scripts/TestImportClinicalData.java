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

package org.mskcc.cbio.portal.scripts;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

/**
 * Tests Import of Clinical Data.
 *
 * @author Ethan Cerami.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportClinicalData {

	CancerStudy study;
	
	ProgressMonitor pMonitor = new ProgressMonitor();

	@Before 
	public void setUp() {
		study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
		DaoGeneticProfile.reCache();
		DaoPatient.reCache();
		DaoSample.reCache();
	}


    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
	@Ignore("To be fixed")
    public void testImportClinicalData() throws Exception {

		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("target/test-classes/clinical_data.txt");
        ImportClinicalData importClinicalData = new ImportClinicalData(study, clinicalFile);
        importClinicalData.importData();
	}
	
    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
	@Ignore("To be fixed")
    public void testImportClinicalDataSurvival() throws Exception {

		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("target/test-classes/clinical_data.txt");
        ImportClinicalData importClinicalData = new ImportClinicalData(study, clinicalFile);
        importClinicalData.importData();

        LinkedHashSet <String> caseSet = new LinkedHashSet<String>();
        caseSet.add("TCGA-A1-A0SB");
        caseSet.add("TCGA-A1-A0SI");
        caseSet.add("TCGA-A1-A0SE");

        List<Patient> clinicalCaseList = DaoClinicalData.getSurvivalData(study.getInternalId(), caseSet);
        assertEquals (3, clinicalCaseList.size());

        Patient clinical0 = clinicalCaseList.get(0);
        assertEquals (new Double(79.04), clinical0.getAgeAtDiagnosis());
        assertEquals ("DECEASED", clinical0.getOverallSurvivalStatus());
        assertEquals ("Recurred/Progressed", clinical0.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(43.8), clinical0.getOverallSurvivalMonths());
        assertEquals (new Double(15.05), clinical0.getDiseaseFreeSurvivalMonths());

        Patient clinical1 = clinicalCaseList.get(1);
        assertEquals (new Double(55.53), clinical1.getAgeAtDiagnosis());
        assertEquals ("LIVING", clinical1.getOverallSurvivalStatus());
        assertEquals ("DiseaseFree", clinical1.getDiseaseFreeSurvivalStatus());
        assertEquals (new Double(49.02), clinical1.getOverallSurvivalMonths());
        assertEquals (new Double(49.02), clinical1.getDiseaseFreeSurvivalMonths());

        Patient clinical2 = clinicalCaseList.get(2);
        assertEquals (null, clinical2.getDiseaseFreeSurvivalMonths());
        
	}
	
    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
	@Ignore("To be fixed")
    public void testImportClinicalDataSlice() throws Exception {

		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("target/test-classes/clinical_data.txt");
        ImportClinicalData importClinicalData = new ImportClinicalData(study, clinicalFile);
        importClinicalData.importData();

        List<ClinicalParameterMap> slice = DaoClinicalData.getDataSlice(study.getInternalId(), Arrays.asList("PLATINUMSTATUS"));
        assertTrue(slice.size() >= 1);
        
		ClinicalParameterMap paramMap = slice.get(0);
		assertEquals ("PLATINUMSTATUS", paramMap.getName());
		assertEquals("Sensitive", paramMap.getValue("TCGA-A1-A0SD"));
        assertEquals("NA", paramMap.getValue("TCGA-A1-A0SE"));
        assertEquals(2, paramMap.getDistinctCategories().size());
	}
	
    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
	@Ignore("To be fixed")
    public void testImportClinicalDataParameters() throws Exception {

		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("target/test-classes/clinical_data.txt");
        ImportClinicalData importClinicalData = new ImportClinicalData(study, clinicalFile);
        importClinicalData.importData();

		Set<String> paramSet = DaoClinicalData.getDistinctParameters(study.getInternalId());
        assertEquals (9, paramSet.size());
    }
}
