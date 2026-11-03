/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import java.io.*;
import java.util.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.CopyNumberStatus;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.scripts.ImportTabDelimData;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * JUnit tests for ImportTabDelimData class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
public class TestImportTabDelimData extends IntegrationTestBase {

    private final Set<String> NON_CASE_ID_COLS = new HashSet<>(Arrays.asList(
            "Gene Symbol",
            "Hugo_Symbol",
            "Entrez_Gene_Id",
            "Locus ID",
            "Cytoband",
            "Composite.Element.Ref",
            "geneset_id"
    ));

    private int studyId;
    private int geneticProfileId;
    private int sample1;
    private int sample2;
    private int sample3;
    private int sample4;
    private int sample5;
    private CancerStudy study;

    @Before
    public void setUp() throws DaoException {
        DaoCancerStudy.reCacheAll();
        DaoGeneOptimized.getInstance().reCache();
        ProgressMonitor.resetWarnings();

        study = DaoCancerStudy.getCancerStudyByStableId("study_tcga_pub");
        studyId =study.getInternalId();

        GeneticProfile newGeneticProfile = new GeneticProfile();
        newGeneticProfile.setCancerStudyId(studyId);
        newGeneticProfile.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        newGeneticProfile.setStableId("study_tcga_pub_test");
        newGeneticProfile.setProfileName("Barry CNA Results");
        newGeneticProfile.setDatatype("test");
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

        ClickHouseBulkLoader.bulkLoadOff();
        runImportCnaData();
    }
    
    /**
     * Test importing of cna_test.txt file.
     * @throws Exception All Errors.
     */
	@Test
    public void testImportCnaDataBulkLoadOn() throws Exception {
		ClickHouseBulkLoader.bulkLoadOn();
        runImportCnaData();
    }
    
    private void runImportCnaData() throws Exception {

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
        ImportTabDelimData parser = new ImportTabDelimData(file, "Barry", geneticProfileId, null, false, DaoGeneOptimized.getInstance());
        parser.importData();

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
        // test with both values of ClickHouseBulkLoader.isBulkLoad()
        ClickHouseBulkLoader.bulkLoadOff();
        runImportCnaData2();
    }
    
    /**
     * Test importing of cna_test2.txt file.
     * This is identical to cna_test.txt, except there is no target line.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportCnaData2BulkLoadOn() throws Exception {
        // test with both values of ClickHouseBulkLoader.isBulkLoad()
    	ClickHouseBulkLoader.bulkLoadOn();
        runImportCnaData2();
    }
    
    private void runImportCnaData2() throws Exception {

        DaoGeneticAlteration dao = DaoGeneticAlteration.getInstance();

        ProgressMonitor.setConsoleMode(false);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/cna_test2.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, geneticProfileId, null, false, DaoGeneOptimized.getInstance());
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
        // test with both values of ClickHouseBulkLoader.isBulkLoad()
        ClickHouseBulkLoader.bulkLoadOff();
        runImportRnaData1();
    }
    
    /**
     * Test importing of mrna_test file.
     * @throws Exception All Errors.
     */
    @Test
    public void testImportmRnaData1BulkLoadOn() throws Exception {
        // test with both values of ClickHouseBulkLoader.isBulkLoad()
      	ClickHouseBulkLoader.bulkLoadOn();
        runImportRnaData1();
    }
    
    private void runImportRnaData1() throws Exception {

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
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_mrna").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(true);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/mrna_test.txt");
        addTestPatientAndSampleRecords(file);
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, null, false, DaoGeneOptimized.getInstance());
        parser.importData();
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
       	ClickHouseBulkLoader.bulkLoadOn();
        

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
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_mrna").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(true);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/tabDelimitedData/data_expression2.txt");
        addTestPatientAndSampleRecords(file);
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, null, false, DaoGeneOptimized.getInstance());
        parser.importData();
        
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
        assertEquals(4, countDuplicatedRowWarnings);
        assertEquals(3, countInvalidEntrez);
        assertEquals(4, countSkippedWarnings);
        
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
       	ClickHouseBulkLoader.bulkLoadOn();
        
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
        DaoGeneticProfile.addGeneticProfile(geneticProfile);
        
        int newGeneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId("gbm_rppa").getGeneticProfileId();

        ProgressMonitor.setConsoleMode(true);
		// TBD: change this to use getResourceAsStream()
        File file = new File("src/test/resources/tabDelimitedData/data_rppa.txt");
        addTestPatientAndSampleRecords(file);
        ImportTabDelimData parser = new ImportTabDelimData(file, newGeneticProfileId, null, false, DaoGeneOptimized.getInstance());
        parser.importData();
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

    private void addTestPatientAndSampleRecords(File file) throws FileNotFoundException, IOException, DaoException {
        // extract sample ids from header
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        String headerLine = buf.readLine();
        String parts[] = headerLine.split("\t");
        List<String> sampleIds = new ArrayList<>();
        for (int i=0; i<parts.length; i++) {
            boolean isSample = Boolean.TRUE;
            for (String nonCaseIdCol : NON_CASE_ID_COLS) {
                if (nonCaseIdCol.equalsIgnoreCase(parts[i])) {
                    isSample = Boolean.FALSE;
                    break;
                }
            }
            if (isSample) {
                sampleIds.add(parts[i]);
            }
        }
        reader.close();
        // add sample + patient records to db
        for (String sampleId : sampleIds) {
            // fetch patient from db or add new one if does not exist
            Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, sampleId);
            Integer pId = (p == null) ? DaoPatient.addPatient(new Patient(study, sampleId)) : p.getInternalId();
            DaoSample.addSample(new Sample(sampleId, pId, study.getTypeOfCancerId()));
        }
        ClickHouseBulkLoader.flushAll();
    }
}
