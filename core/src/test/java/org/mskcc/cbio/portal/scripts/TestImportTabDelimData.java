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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit tests for ImportTabDelimData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestImportTabDelimData {

	int studyId;
	int geneticProfileId;
	int sample1;
	int sample2;
	int sample3;
	int sample4;
	int sample5;
	
	@Before
	public void setUp() throws DaoException {
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		
		GeneticProfile newGeneticProfile = new GeneticProfile();
		newGeneticProfile.setCancerStudyId(studyId);
		newGeneticProfile.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
		newGeneticProfile.setStableId("study_tcga_pub_test");
		newGeneticProfile.setProfileName("Barry CNA Results");
		newGeneticProfile.setDatatype("test");
		DaoGeneticProfile.addGeneticProfile(newGeneticProfile);
		
		geneticProfileId =  DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_test").getGeneticProfileId();
		
		DaoPatient.reCache();
		
        DaoSample.reCache();
		sample1 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SB-01").getInternalId();
		sample2 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SD-01").getInternalId();
		sample3 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SE-01").getInternalId();
		sample4 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SF-01").getInternalId();
		sample5 = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-A1-A0SG-01").getInternalId();
	}

    /**
     * Test importing of cna_test.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportCnaDataBulkLoadOff() throws Exception {

        MySQLbulkLoader.bulkLoadOff();
        runImportCnaData();
        MySQLbulkLoader.bulkLoadOn();
    }
    
    /**
     * Test importing of cna_test.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportCnaDataBulkLoadOn() throws Exception {

        runImportCnaData();
    }
    
    private void runImportCnaData() throws DaoException, IOException{

        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();
//        daoGene.addGene(new CanonicalGene(207, "AKT1"));
//        daoGene.addGene(new CanonicalGene(208, "AKT2"));
//        daoGene.addGene(new CanonicalGene(10000, "AKT3"));
//        daoGene.addGene(new CanonicalGene(369, "ARAF"));
//        daoGene.addGene(new CanonicalGene(472, "ATM"));
//        daoGene.addGene(new CanonicalGene(673, "BRAF"));
//        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
//        daoGene.addGene(new CanonicalGene(675, "BRCA2"));

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/cna_test.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, ImportTabDelimData.BARRY_TARGET, geneticProfileId, pMonitor);
        parser.importData();

        String value = dao.getGeneticAlteration(geneticProfileId, sample1, 207);
        assertEquals ("0", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample4, 207);
        assertEquals ("-1", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample2, 207);
        assertEquals ("0", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample2, 10000);
        assertEquals ("2", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample3, 10000);
        assertEquals ("2", value);

        int cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample3, 10000));
        assertEquals(CopyNumberStatus.COPY_NUMBER_AMPLIFICATION, cnaStatus);
        cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample2, 10000));
        assertEquals(CopyNumberStatus.COPY_NUMBER_AMPLIFICATION, cnaStatus);
        cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample4, 207));
        assertEquals(CopyNumberStatus.HEMIZYGOUS_DELETION, cnaStatus);

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "TCGA-A1-A0SB");
        Sample sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SB-01");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));
 
        patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "TCGA-A1-A0SJ");
        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SJ-01");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));

        ArrayList caseIds = DaoSampleProfile.getAllSampleIdsInProfile(geneticProfileId);
        assertEquals(14, caseIds.size());
    }

    /**
     * Test importing of cna_test2.txt file.
     * This is identical to cna_test.txt, except there is no target line.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportCnaData2BulkLoadOff() throws Exception {
        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runImportCnaData2();
        MySQLbulkLoader.bulkLoadOn();
    }
    
    /**
     * Test importing of cna_test2.txt file.
     * This is identical to cna_test.txt, except there is no target line.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportCnaData2BulkLoadOn() throws Exception {
        // test with both values of MySQLbulkLoader.isBulkLoad()
        runImportCnaData2();
    }
    
    private void runImportCnaData2() throws DaoException, IOException{

        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/cna_test2.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, null, geneticProfileId, pMonitor);
        parser.importData();

        String value = dao.getGeneticAlteration(geneticProfileId, sample1, 207);
        assertEquals (value, "0");
        value = dao.getGeneticAlteration(geneticProfileId, sample4, 207);
        assertEquals (value, "-1");
        value = dao.getGeneticAlteration(geneticProfileId, sample2, 207);
        assertEquals (value, "0");
        value = dao.getGeneticAlteration(geneticProfileId, sample2, 10000);
        assertEquals (value, "2");
        value = dao.getGeneticAlteration(geneticProfileId, sample3, 10000);
        assertEquals (value, "2");

        int cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample3, 10000));
        assertEquals(CopyNumberStatus.COPY_NUMBER_AMPLIFICATION, cnaStatus);
        cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample2, 10000));
        assertEquals(CopyNumberStatus.COPY_NUMBER_AMPLIFICATION, cnaStatus);
        cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample4, 207));
        assertEquals(CopyNumberStatus.HEMIZYGOUS_DELETION, cnaStatus);

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "TCGA-A1-A0SB");
        Sample sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SB-01");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));

        patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, "TCGA-A1-A0SJ");
        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-A1-A0SJ-01");
        assertTrue(DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId));
        ArrayList sampleIds = DaoSampleProfile.getAllSampleIdsInProfile(geneticProfileId);
        assertEquals(14, sampleIds.size());
    }

    /**
     * Test importing of mrna_test file.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportmRnaData1BulkLoadOff() throws Exception {
        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runImportRnaData1();
        MySQLbulkLoader.bulkLoadOn();
    }
    
    /**
     * Test importing of mrna_test file.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportmRnaData1BulkLoadOn() throws Exception {
        // test with both values of MySQLbulkLoader.isBulkLoad()
        runImportRnaData1();
    }
    
    private void runImportRnaData1() throws DaoException, IOException{

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        daoGene.addGene(new CanonicalGene(780, "A"));
        daoGene.addGene(new CanonicalGene(5982, "B"));
        daoGene.addGene(new CanonicalGene(3310, "C"));
        daoGene.addGene(new CanonicalGene(7849, "D"));
        daoGene.addGene(new CanonicalGene(2978, "E"));
        daoGene.addGene(new CanonicalGene(7067, "F"));
        daoGene.addGene(new CanonicalGene(11099, "G"));
        daoGene.addGene(new CanonicalGene(675, "6352"));

        GeneticProfile geneticProfile = new GeneticProfile();

        geneticProfile.setCancerStudyId(studyId);
        geneticProfile.setStableId("gbm_mrna");
        geneticProfile.setGeneticAlterationType(GeneticAlterationType.MRNA_EXPRESSION);
        geneticProfile.setDatatype("CONTINUOUS");
        geneticProfile.setProfileName("MRNA Data");
        geneticProfile.setProfileDescription("mRNA Data");
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_mrna").getGeneticProfileId();

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/mrna_test.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, pMonitor);
        parser.importData();
        
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "DD639").getInternalId();
        String value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 2978);
        assertEquals ("2.01", value );

        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "DD638").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 7849);
        assertEquals ("0.55", value );
    }

}