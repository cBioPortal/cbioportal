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
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
@Rollback
@Transactional
public class TestImportClinicalData {
	
	//To use in test cases where we expect an exception:
	@Rule
	public ExpectedException exception = ExpectedException.none();
    
	private CancerStudy cancerStudy = null;

	/**
	 * This is executed n times, for each of the n test methods below:
	 * @throws DaoException
	 */
    @Before 
    public void setUp() throws DaoException
    {
        DaoCancerStudy.reCacheAll();
        ProgressMonitor.resetWarnings();
        
        // new dummy study to simulate importing clinical data in empty study:
        cancerStudy = new CancerStudy("testnew","testnew","testnew","brca",true);
        cancerStudy.setReferenceGenome("hg19");
        DaoCancerStudy.addCancerStudy(cancerStudy);
        // implicit test:
        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("testnew");
	}

    /**
     * Test importing of Clinical Data File with a duplicated record in "MIXED_ATTRIBUTES" 
     * type of file. 
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalData_WithDuplInMixedAttrFormat() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data_small.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
        
        ArrayList<String> warnings = ProgressMonitor.getWarnings();
        //expect 1 warnings: about duplicated TCGA-BH-A18K-01
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("Sample TCGA-BH-A18K-01 found to be duplicated"));
	}

    /**
     * Test importing of Clinical Data File when there is data a line with 
     * wrong number of columns. 
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataNewStudy_WithWrongNrCols() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data_small_WRONG_NR_COLS.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Number of columns in line is not as expected");
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
        File clinicalFile = new File("src/test/resources/clinical_data_small_PATIENT_dupl_error.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "PATIENT_ATTRIBUTES", false);
        
        exception.expect(RuntimeException.class);
        exception.expectMessage("Duplicated patient");
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
	}
	
	/**
     * Test importing of Patient Data File with data type error.
     *
     */
	@Test
    public void testImportPatientDataNewStudy_WithDataTypeError() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data_small_PATIENT_datatype_error.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "PATIENT_ATTRIBUTES", false);
        
        exception.expect(RuntimeException.class);
        exception.expectMessage("Invalid value for datatype");
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
	}
	
    /**
     * Test importing of Clinical Data File, using the new data format, i.e. with data 
     * split over PATIENT and SAMPLES files. 
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataSurvival_SplitMode() throws Exception {
		// import sample data first:
        File clinicalFile = new File("src/test/resources/clinical_data_SAMPLE.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "SAMPLE_ATTRIBUTES", false);
        importClinicalData.importData();
        
        // import patient data:
        clinicalFile = new File("src/test/resources/clinical_data_PATIENT.txt");
        importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "PATIENT_ATTRIBUTES", false);
        importClinicalData.importData();
        
		checkSurvivalDataAndSampleCount(cancerStudy);
	}
	
    /**
     * Test importing of Clinical Data File, using old MIXED_ATTRIBUTES format.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataSurvival() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();

		checkSurvivalDataAndSampleCount(cancerStudy);
    }
    
    @Test
    public void testImportClinicalDataTwoSampleFiles() throws Exception {
		
        File clinicalFile = new File("src/test/resources/clinical_data_small_SAMPLE.txt");
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        importClinicalData.setFile(cancerStudy, clinicalFile, "SAMPLE_ATTRIBUTES", false);
        importClinicalData.importData();
        
        clinicalFile = new File("src/test/resources/clinical_data_small_SAMPLE2.txt");
        importClinicalData = new ImportClinicalData(null);
        importClinicalData.setFile(cancerStudy, clinicalFile, "SAMPLE_ATTRIBUTES", false);
        importClinicalData.importData();
        
		LinkedHashSet <String> caseSet = new LinkedHashSet<String>();
        caseSet.add("TEST-A2-A04P");
        
        List<Patient> clinicalCaseList = DaoClinicalData.getSurvivalData(cancerStudy.getInternalId(), caseSet);
        assertEquals (Integer.valueOf(2), clinicalCaseList.get(0).getSampleCount());        
	}

	private void checkSurvivalDataAndSampleCount(CancerStudy cancerStudy) throws DaoException {
        LinkedHashSet <String> caseSet = new LinkedHashSet<String>();
        caseSet.add("TCGA-A1-A0SB");
        caseSet.add("TCGA-A1-A0SE");
        caseSet.add("TCGA-A1-A0SI");
        caseSet.add("TCGA-A1-A0SC");
        caseSet.add("TCGA-04-1341");
        
        //get survival data, unsorted:
        List<Patient> clinicalCaseList = DaoClinicalData.getSurvivalData(cancerStudy.getInternalId(), caseSet);
        assertEquals (5, clinicalCaseList.size());
        
        int countChecks = 0;
        for (Patient patientData : clinicalCaseList) {
        	if (patientData.getStableId().equals("TCGA-A1-A0SB")) {
                assertEquals (Double.valueOf(79.04), patientData.getAgeAtDiagnosis());
                assertEquals ("1:DECEASED", patientData.getOverallSurvivalStatus());
                assertEquals ("1:Recurred/Progressed", patientData.getDiseaseFreeSurvivalStatus());
                assertEquals (Double.valueOf(43.8), patientData.getOverallSurvivalMonths());
                assertEquals (Double.valueOf(15.05), patientData.getDiseaseFreeSurvivalMonths());
                assertEquals (Integer.valueOf(1), patientData.getSampleCount());
                countChecks++;
        	}
        	else if (patientData.getStableId().equals("TCGA-A1-A0SE")) {
                assertEquals (null, patientData.getDiseaseFreeSurvivalMonths());
                assertEquals (Integer.valueOf(1), patientData.getSampleCount());
                countChecks++;
        	}
        	else if (patientData.getStableId().equals("TCGA-A1-A0SI")) {
        		assertEquals ("TCGA-A1-A0SI", patientData.getStableId());
        		assertEquals (Double.valueOf(55.53), patientData.getAgeAtDiagnosis());
                assertEquals ("0:LIVING", patientData.getOverallSurvivalStatus());
                assertEquals ("0:DiseaseFree", patientData.getDiseaseFreeSurvivalStatus());
                assertEquals (Double.valueOf(49.02), patientData.getOverallSurvivalMonths());
                assertEquals (Double.valueOf(49.02), patientData.getDiseaseFreeSurvivalMonths());
                assertEquals (Integer.valueOf(1), patientData.getSampleCount());
                countChecks++;
        	} else if (patientData.getStableId().equals("TCGA-A1-A0SC")) {
        		assertEquals ("TCGA-A1-A0SC", patientData.getStableId());
        		assertEquals (Double.valueOf(70.64), patientData.getAgeAtDiagnosis());
                assertEquals ("1:DECEASED", patientData.getOverallSurvivalStatus());
                assertEquals ("1:Recurred/Progressed", patientData.getDiseaseFreeSurvivalStatus());
                assertEquals (Double.valueOf(40.89), patientData.getOverallSurvivalMonths());
                assertEquals (Double.valueOf(12.95), patientData.getDiseaseFreeSurvivalMonths());
                assertEquals (Integer.valueOf(1), patientData.getSampleCount());
                countChecks++;
            } else {
        		assertEquals ("TCGA-04-1341", patientData.getStableId());
        		assertEquals (Double.valueOf(85.52), patientData.getAgeAtDiagnosis());
                assertEquals ("0:LIVING", patientData.getOverallSurvivalStatus());
                assertEquals ("0:DiseaseFree", patientData.getDiseaseFreeSurvivalStatus());
                assertEquals (null, patientData.getOverallSurvivalMonths());
                assertEquals (null, patientData.getDiseaseFreeSurvivalMonths());
                assertEquals (Integer.valueOf(1), patientData.getSampleCount());
                countChecks++;
            }
        }
        //make sure all entries were checked:
        assertEquals(5, countChecks);
	}
	
	/**
     * Test importing of Clinical Data File, using the new data format, i.e. with data 
     * split over PATIENT and SAMPLES files. 
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataSlice_SplitMode() throws Exception {
        // import sample data first:
        File clinicalFile = new File("src/test/resources/clinical_data_SAMPLE.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "SAMPLE_ATTRIBUTES", false);
        importClinicalData.importData();
        
        // import patient data:
        clinicalFile = new File("src/test/resources/clinical_data_PATIENT.txt");
        importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "PATIENT_ATTRIBUTES", false);
        importClinicalData.importData();

        checkDataSlice(cancerStudy);
	}
	
	/**
     * Test importing of Clinical Data File, using old MIXED_ATTRIBUTES format.
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalDataSlice() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();

        checkDataSlice(cancerStudy);
	}
	
	private void checkDataSlice(CancerStudy cancerStudy) throws DaoException {
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
        File clinicalFile = new File("src/test/resources/clinical_data.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "MIXED_ATTRIBUTES", false);
        importClinicalData.importData();

		Set<String> paramSet = DaoClinicalData.getDistinctParameters(cancerStudy.getInternalId());
        assertEquals (10, paramSet.size());
    }
	
    /**
     * Test mandatory SAMPLE_ID field
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalData_SampleIdError() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data_small_missing_SAMPLEID.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "SAMPLE_ATTRIBUTES", false);
        
        exception.expect(RuntimeException.class);
        exception.expectMessage("failure to find SAMPLE_ID");
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
	}
	
    /**
     * Test mandatory PATIENT_ID field
     *
     * @throws DaoException Database Access Error.
     * @throws IOException  IO Error.
     */
	@Test
    public void testImportClinicalData_PatientIdError() throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data_small_missing_PATIENTID.txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, "SAMPLE_ATTRIBUTES", false);
        
        exception.expect(RuntimeException.class);
        exception.expectMessage("failure to find PATIENT_ID");
        importClinicalData.importData();
        ConsoleUtil.showWarnings();
	}		
	
	/** 
	 * Test loading a correct file twice. Should give duplication error.
	 * @throws Exception
	 */
	@Test
	public void testImportClinicalData_CorrectFileTwice1() throws Exception {
		checkCorrectFileTwice("PATIENT");
	}
	public void testImportClinicalData_CorrectFileTwice2() throws Exception {
		checkCorrectFileTwice("SAMPLE");
	}	
	private void checkCorrectFileTwice(String type) throws Exception {
        File clinicalFile = new File("src/test/resources/clinical_data_small_" + type + ".txt");
        // initialize an ImportClinicalData instance without args to parse
        ImportClinicalData importClinicalData = new ImportClinicalData(null);
        // set the info usually parsed from args
        importClinicalData.setFile(cancerStudy, clinicalFile, type + "_ATTRIBUTES", false);
        importClinicalData.importData();
        // loading twice should also give error
        exception.expect(DaoException.class);
        // it is not a specific "duplication" error message but a general DB error since the 
        // validation only gives specific error when in same file (maybe at some point we want to support clinical data 
        // in multiple PATIENT and SAMPLE files(?) ):
        exception.expectMessage("DB Error");
        importClinicalData.importData();
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
