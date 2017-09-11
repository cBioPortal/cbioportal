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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
		DaoCancerStudy.reCacheAll();
		DaoGeneOptimized.getInstance().reCache();
		ProgressMonitor.resetWarnings();
		
		studyId = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub").getInternalId();
		
		GeneticProfile newGeneticProfile = new GeneticProfile();
		newGeneticProfile.setCancerStudyId(studyId);
		newGeneticProfile.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
		newGeneticProfile.setStableId("study_tcga_pub_test");
		newGeneticProfile.setProfileName("Barry CNA Results");
		newGeneticProfile.setDatatype("test");
		newGeneticProfile.setReferenceGenomeId(1);
		DaoGeneticProfile.addGeneticProfile(newGeneticProfile);
		
		geneticProfileId =  DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_test").getGeneticProfileId();
		
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
    }
    
    /**
     * Test importing of cna_test.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportCnaDataBulkLoadOn() throws Exception {
		MySQLbulkLoader.bulkLoadOn();
        runImportCnaData();
    }
    
    private void runImportCnaData() throws DaoException, IOException{

        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        // the largest current true Entrez gene ID counts 8 digits
        daoGene.addGene(new CanonicalGene(999999207, "TESTAKT1"));
        daoGene.addGene(new CanonicalGene(999999208, "TESTAKT2"));
        daoGene.addGene(new CanonicalGene(999910000, "TESTAKT3"));
        daoGene.addGene(new CanonicalGene(999999369, "TESTARAF"));
        daoGene.addGene(new CanonicalGene(999999472, "TESTATM"));
        daoGene.addGene(new CanonicalGene(999999673, "TESTBRAF"));
        daoGene.addGene(new CanonicalGene(999999672, "TESTBRCA1"));
        daoGene.addGene(new CanonicalGene(999999675, "TESTBRCA2"));

        ProgressMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/cna_test.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, "Barry", geneticProfileId, null);
        int numLines = FileUtil.getNumLines(file);
        parser.importData(numLines);

        String value = dao.getGeneticAlteration(geneticProfileId, sample1, 999999207);
        assertEquals ("0", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample4, 999999207);
        assertEquals ("-1", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample2, 999999207);
        assertEquals ("0", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample2, 999910000);
        assertEquals ("2", value);
        value = dao.getGeneticAlteration(geneticProfileId, sample3, 999910000);
        assertEquals ("2", value);

        int cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample3, 999910000));
        assertEquals(CopyNumberStatus.COPY_NUMBER_AMPLIFICATION, cnaStatus);
        cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample2, 999910000));
        assertEquals(CopyNumberStatus.COPY_NUMBER_AMPLIFICATION, cnaStatus);
        cnaStatus = Integer.parseInt(dao.getGeneticAlteration(geneticProfileId, sample4, 999999207));
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
    }
    
    /**
     * Test importing of cna_test2.txt file.
     * This is identical to cna_test.txt, except there is no target line.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportCnaData2BulkLoadOn() throws Exception {
        // test with both values of MySQLbulkLoader.isBulkLoad()
    	MySQLbulkLoader.bulkLoadOn();
        runImportCnaData2();
    }
    
    private void runImportCnaData2() throws DaoException, IOException{

        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        ProgressMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/cna_test2.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, geneticProfileId, null);
        int numLines = FileUtil.getNumLines(file);
        parser.importData(numLines);

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
    }
    
    /**
     * Test importing of mrna_test file.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportmRnaData1BulkLoadOn() throws Exception {
        // test with both values of MySQLbulkLoader.isBulkLoad()
      	MySQLbulkLoader.bulkLoadOn();
        runImportRnaData1();
    }
    
    private void runImportRnaData1() throws DaoException, IOException{

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        daoGene.addGene(new CanonicalGene(999999780, "A"));
        daoGene.addGene(new CanonicalGene(999995982, "B"));
        daoGene.addGene(new CanonicalGene(999993310, "C"));
        daoGene.addGene(new CanonicalGene(999997849, "D"));
        daoGene.addGene(new CanonicalGene(999992978, "E"));
        daoGene.addGene(new CanonicalGene(999997067, "F"));
        daoGene.addGene(new CanonicalGene(999911099, "G"));
        daoGene.addGene(new CanonicalGene(999999675, "6352"));

        GeneticProfile geneticProfile = new GeneticProfile();

        geneticProfile.setCancerStudyId(studyId);
        geneticProfile.setStableId("gbm_mrna");
        geneticProfile.setGeneticAlterationType(GeneticAlterationType.MRNA_EXPRESSION);
        geneticProfile.setDatatype("CONTINUOUS");
        geneticProfile.setProfileName("MRNA Data");
        geneticProfile.setProfileDescription("mRNA Data");
        geneticProfile.setReferenceGenomeId(1);
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_mrna").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(true);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/mrna_test.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, null);
        int numLines = FileUtil.getNumLines(file);
        parser.importData(numLines);
        ConsoleUtil.showMessages();
        
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "DD639").getInternalId();
        String value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999992978);
        assertEquals ("2.01", value );

        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "DD638").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997849);
        assertEquals ("0.55", value );
    }

    
    /**
     * Test importing of data_expression file.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportmRnaData2() throws Exception {
       	MySQLbulkLoader.bulkLoadOn();
        

        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        //Gene with alias:
        daoGene.addGene(makeGeneWithAlias(999997504, "TESTXK", "NA"));
        //Other genes:
        daoGene.addGene(new CanonicalGene(999999999, "TESTNAT1"));

        daoGene.addGene(new CanonicalGene(999997124, "TESTTNF"));
        daoGene.addGene(new CanonicalGene(999991111, "TESTCHEK1"));
        daoGene.addGene(new CanonicalGene(999999919, "TESTABCA1"));
        // will get generated negative id:
        daoGene.addGene(new CanonicalGene(-1, "TESTphosphoprotein"));
        		
        GeneticProfile geneticProfile = new GeneticProfile();

        geneticProfile.setCancerStudyId(studyId);
        geneticProfile.setStableId("gbm_mrna");
        geneticProfile.setGeneticAlterationType(GeneticAlterationType.MRNA_EXPRESSION);
        geneticProfile.setDatatype("CONTINUOUS");
        geneticProfile.setProfileName("MRNA Data");
        geneticProfile.setProfileDescription("mRNA Data");
        geneticProfile.setReferenceGenomeId(1);
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_mrna").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(true);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/tabDelimitedData/data_expression2.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, null);
        int numLines = FileUtil.getNumLines(file);
        parser.importData(numLines);
        
        // check if expected warnings are given:
        ArrayList<String> warnings = ProgressMonitor.getWarnings();
        int countDuplicatedRowWarnings = 0;
        int countInvalidEntrez = 0;
        int countSkippedWarnings = 0;
        for (String warning: warnings) {
            if (warning.contains("Duplicated row")) {
                countDuplicatedRowWarnings++;
            }
            if (warning.contains("invalid Entrez_Id")) {
                //invalid Entrez
                countInvalidEntrez++;
            }
            if (warning.contains("Record will be skipped")) {
                //Entrez is a valid number, but not found
                countSkippedWarnings++;
            }
        }
        //check that we have 11 warning messages:
        assertEquals(2, countDuplicatedRowWarnings);
        assertEquals(3, countInvalidEntrez);
        assertEquals(6, countSkippedWarnings);
        
        Set<Integer> geneticEntityIds = DaoGeneticAlteration.getEntityIdsInProfile(newGeneticProfileId);
        // data will be loaded for 5 of the genes
        assertEquals(5, geneticEntityIds.size());
        HashMap<Integer, HashMap<Integer, String>> dataMap = dao.getGeneticAlterationMapForEntityIds(newGeneticProfileId, geneticEntityIds);
        assertEquals(5, dataMap.entrySet().size());
        
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE1").getInternalId();
        String value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997124);
        assertEquals ("770", value );
        
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE3").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997124);
        assertEquals ("220", value );

        //gene should also be loaded via its alias "NA" as defined above:
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE3").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997504);
        assertEquals ("9940", value );
    }
    
    
    /**
     * Test importing of data_rppa file.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportRppaData() throws Exception {
       	MySQLbulkLoader.bulkLoadOn();
        
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        //Genes with alias:
        daoGene.addGene(makeGeneWithAlias(999999931,"TESTACACA", "TESTACC1"));
        daoGene.addGene(makeGeneWithAlias(999999207,"TESTAKT1", "TESTAKT"));
        daoGene.addGene(makeGeneWithAlias(999999597,"TESTSANDER", "TESTACC1"));
        daoGene.addGene(makeGeneWithAlias(999997158,"TESTTP53BP1", "TEST53BP1"));
        // test for NA being a special case in RPPA, and not the usual alias
        daoGene.addGene(makeGeneWithAlias(999997504, "XK", "NA"));
        //Other genes:
        daoGene.addGene(new CanonicalGene(999999932,"TESTACACB"));
        daoGene.addGene(new CanonicalGene(999999208,"TESTAKT2"));
        daoGene.addGene(new CanonicalGene(999999369,"TESTARAF"));
        daoGene.addGene(new CanonicalGene(999991978, "TESTEIF4EBP1"));
        daoGene.addGene(new CanonicalGene(999995562,"TESTPRKAA1"));
        daoGene.addGene(new CanonicalGene(999997531,"TESTYWHAE"));
        daoGene.addGene(new CanonicalGene(999910000,"TESTAKT3"));
        daoGene.addGene(new CanonicalGene(999995578,"TESTPRKCA"));
        
        
        GeneticProfile geneticProfile = new GeneticProfile();

        geneticProfile.setCancerStudyId(studyId);
        geneticProfile.setStableId("gbm_rppa");
        geneticProfile.setGeneticAlterationType(GeneticAlterationType.PROTEIN_LEVEL);
        geneticProfile.setDatatype("LOG2-VALUE");
        geneticProfile.setProfileName("RPPA Data");
        geneticProfile.setProfileDescription("RPPA Data");
        geneticProfile.setReferenceGenomeId(1);
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_rppa").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(true);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/tabDelimitedData/data_rppa.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, null);
        int numLines = FileUtil.getNumLines(file);
        parser.importData(numLines);
        ConsoleUtil.showMessages();
        
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE1").getInternalId();
        String value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997531);
        assertEquals ("1.5", value );
        
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE4").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997531);
        assertEquals ("2", value );
        
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE4").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999997504);
        assertEquals ("NaN", value ); //"NA" is not expected to be stored because of workaround for bug in firehose. See also https://github.com/cBioPortal/cbioportal/issues/839#issuecomment-203523078
        
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "SAMPLE1").getInternalId();
        value = dao.getGeneticAlteration(newGeneticProfileId, sampleId, 999995578);
        assertEquals ("1.5", value );
    }

	private CanonicalGene makeGeneWithAlias(int entrez, String symbol, String alias) {
		CanonicalGene gene = new CanonicalGene(entrez, symbol);
        Set<String> aliases = new HashSet<String>();
        aliases.add(alias);
        gene.setAliases(aliases);
        return gene;
	}
    
}