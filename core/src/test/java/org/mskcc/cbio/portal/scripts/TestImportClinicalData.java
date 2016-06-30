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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ConsoleUtil;
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
 * @author Ersin Ciftci
 * @author Pieter Lukasse
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportClinicalData {
	
	//To use in test cases where we expect an exception:
	@Rule
	public ExpectedException exception = ExpectedException.none();
    
	
	@Before 
	public void setUp() throws DaoException
	{
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
    public void testImportClinicalDataNewStudy() throws Exception {
		//new dummy study to simulate importing clinical data in empty study:
		CancerStudy cancerStudy = new CancerStudy("testnew","testnew","testnew","brca",true);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("testnew");
		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("src/test/resources/clinical_data_small.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
	}
	
    /**
     * Test importing of Patient Data File with duplication error.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportPatientDataNewStudy_WithDuplError() throws Exception {
		//new dummy study to simulate importing clinical data in empty study:
		CancerStudy cancerStudy = new CancerStudy("testnew3","testnew3","testnew3","brca",true);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("testnew3");
		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("src/test/resources/clinical_data_small_PATIENT.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "PATIENT_ATTRIBUTES", false);
        
        exception.expect(RuntimeException.class);
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
	}
    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataSurvival() throws Exception {

		//new dummy study to simulate importing clinical data in empty study:
		CancerStudy cancerStudy = new CancerStudy("testnew4","testnew4","testnew4","brca",true);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("testnew4");
        
		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("src/test/resources/clinical_data.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();

        LinkedHashSet <String> caseSet = new LinkedHashSet<String>();
        caseSet.add("TCGA-A1-A0SB");
        caseSet.add("TCGA-A1-A0SE");
        caseSet.add("TCGA-A1-A0SI");

        //get survival data, unsorted:
        List<Patient> clinicalCaseList = DaoClinicalData.getSurvivalData(cancerStudy.getInternalId(), caseSet);
        assertEquals (3, clinicalCaseList.size());
        
        int countChecks = 0;
        for (Patient patientData : clinicalCaseList) {
        	if (patientData.getStableId().equals("TCGA-A1-A0SB")) {
                assertEquals (new Double(79.04), patientData.getAgeAtDiagnosis());
                assertEquals ("DECEASED", patientData.getOverallSurvivalStatus());
                assertEquals ("Recurred/Progressed", patientData.getDiseaseFreeSurvivalStatus());
                assertEquals (new Double(43.8), patientData.getOverallSurvivalMonths());
                assertEquals (new Double(15.05), patientData.getDiseaseFreeSurvivalMonths());
                countChecks++;
        	}
        	else if (patientData.getStableId().equals("TCGA-A1-A0SE")) {
                assertEquals (null, patientData.getDiseaseFreeSurvivalMonths());
                countChecks++;
        	}
        	else {
        		assertEquals ("TCGA-A1-A0SI", patientData.getStableId());
        		assertEquals (new Double(55.53), patientData.getAgeAtDiagnosis());
                assertEquals ("LIVING", patientData.getOverallSurvivalStatus());
                assertEquals ("DiseaseFree", patientData.getDiseaseFreeSurvivalStatus());
                assertEquals (new Double(49.02), patientData.getOverallSurvivalMonths());
                assertEquals (new Double(49.02), patientData.getDiseaseFreeSurvivalMonths());
                countChecks++;
        	}
        }
        //make sure all entries were checked:
        assertEquals(3, countChecks);
	}
	
    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataSlice() throws Exception {
		//new dummy study to simulate importing clinical data in empty study:
		CancerStudy cancerStudy = new CancerStudy("testnew5","testnew5","testnew5","brca",true);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("testnew5");
        
		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("src/test/resources/clinical_data.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();

        List<ClinicalParameterMap> slice = DaoClinicalData.getDataSlice(cancerStudy.getInternalId(), Arrays.asList("PLATINUMSTATUS"));
        assertTrue(slice.size() >= 1);
        
		ClinicalParameterMap paramMap = slice.get(0);
		assertEquals ("PLATINUMSTATUS", paramMap.getName());
		assertEquals("Sensitive", paramMap.getValue("TCGA-A1-A0SB"));
        assertEquals("NA", paramMap.getValue("TCGA-A1-A0SE"));
        assertEquals(3, paramMap.getDistinctCategories().size());
	}
	
    /**
     * Test importing of Clinical Data File.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataParameters() throws Exception {
		//new dummy study to simulate importing clinical data in empty study:
		CancerStudy cancerStudy = new CancerStudy("testnew6","testnew6","testnew6","brca",true);
        DaoCancerStudy.addCancerStudy(cancerStudy);
        
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("testnew6");
		
		// TBD: change this to use getResourceAsStream()
        File clinicalFile = new File("src/test/resources/clinical_data.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();

		Set<String> paramSet = DaoClinicalData.getDistinctParameters(cancerStudy.getInternalId());
        assertEquals (9, paramSet.size());
    }
	

	/** 
	 * Tests to try out the MissingAttributeValues enum and ensure it filters out the 
	 * correct values. 
	 * 
	 */
    @Test
    public void testHasMethod() {
        assertTrue(ImportClinicalData.MissingAttributeValues.has("NA"));
        assertTrue(ImportClinicalData.MissingAttributeValues.has("na"));
        assertFalse(ImportClinicalData.MissingAttributeValues.has("n/a"));
    }
}
