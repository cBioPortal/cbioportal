/*
 * Copyright (c) 2016 - 2022 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalAttributeMeta;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoCnaEvent;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleProfile;
import org.mskcc.cbio.portal.dao.DaoStructuralVariant;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.CnaEvent;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.StructuralVariant;

import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Pieter Lukasse pieter@thehyve.nl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore

@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestImportProfileData {

    int studyId;
    int geneticProfileId;

    @Before
    public void setUp() throws DaoException {
        ProgressMonitor.setConsoleMode(false);
        loadGenes();
    }

    @After
    public void cleanUp() throws DaoException {
        // each test assumes the mutation data hasn't been loaded yet
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_breast_mutations");
        if (geneticProfile != null) {
            DaoGeneticProfile.deleteGeneticProfile(geneticProfile);
            assertNull(DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_breast_mutations"));
        }
        geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_sv");
        if (geneticProfile != null) {
            DaoGeneticProfile.deleteGeneticProfile(geneticProfile);
            assertNull(DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_sv"));
        }
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testImportMutationsFile() throws Exception {
        /*
         * Complex test where we import a mutations file split over two data
         * files. The data includes germline mutations as well as silent
         * mutations. We make sure the nonsynonymous somatic and germline
         * mutations are added to the databases and the MUTATION_COUNT clinical
         * attributes are correctly computed.
         */
        String[] args = {
                "--data","src/test/resources/data_mutations_extended.txt",
                "--meta","src/test/resources/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData runner = new ImportProfileData(args);
        runner.run();

        // check the study exists
        String studyStableId = "study_tcga_pub";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
        assertNotNull(study);
        studyId = study.getInternalId();

        // Check if the ImportProfileData class indeed adds the study stable Id in front of the
        //dataset study id (e.g. studyStableId + "_breast_mutations"):
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_breast_mutations");
        assertNotNull(geneticProfile);
        geneticProfileId = geneticProfile.getGeneticProfileId();

        // check the mutation T433A has been imported
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();
        validateMutationAminoAcid(geneticProfileId, sampleId, 54407, "T433A");

        // data for the second sample should not exist before loading the next data file
        int secondSampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3665-01").getInternalId();
        assertEquals(DaoMutation.getMutations(geneticProfileId, secondSampleId).size(), 0);
        // the GENETIC_PROFILE_ID in sample_profile should be the same as the
        // genetic profile that was used for import
        int geneticProfileIdFromSampleProfile = DaoSampleProfile.getProfileIdForSample(sampleId);
        assertEquals(geneticProfileIdFromSampleProfile, geneticProfileId);

        // assume clinical data for MUTATION_COUNT was created
        ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum("MUTATION_COUNT", studyId);
        assertNotNull(clinicalAttribute);

        // assume a MUTATION_COUNT record has been added for the sample and the
        // count is 8 there 11 total mutations imported of which 3 germline (
        // not entirely sure why the rest doesn't get imported i see some silent
        // + intron, missing entrez id)
        List<ClinicalData> clinicalData = DaoClinicalData.getSampleData(study.getInternalId(), new ArrayList<String>(Arrays.asList("TCGA-AA-3664-01")), clinicalAttribute);
        assert(clinicalData.size() == 1);
        assertEquals("8", clinicalData.get(0).getAttrVal());

        // load a second mutation data file
        String[] secondArgs = {
                "--data","src/test/resources/data_mutations_extended_continued.txt",
                "--meta","src/test/resources/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData secondRunner = new ImportProfileData(secondArgs);
        secondRunner.run();

        // check mutation for second sample was imported
        validateMutationAminoAcid(geneticProfileId, secondSampleId, 2842, "L113P");

        // assume a MUTATION_COUNT record has been added for the sample and the
        // count is 1, the other one is a germline mutation
        // also confirm mutation count for first sample is still correct
        clinicalData = DaoClinicalData.getSampleData(study.getInternalId(), new ArrayList<String>(Arrays.asList("TCGA-AA-3664-01", "TCGA-AA-3665-01")), clinicalAttribute);
        assert(clinicalData.size() == 2);
        assertEquals("8", clinicalData.get(0).getAttrVal());
        assertEquals("1", clinicalData.get(1).getAttrVal());
    }

    @Test
    public void testImportSplitMutationsFile() throws Exception {
        /*
         * Mutations file split over two files with same stable id. Make sure
         * that the first time if a sample is in the #sequenced_samples the
         * MUTATION_COUNT is 0. After importing the second file make sure the
         * counts are added up i.e. mutations from both the first and second
         * file should be included in the MUTATION_COUNT record.
         */
        String[] args = {
                "--data","src/test/resources/splitMutationsData/data_mutations_extended.txt",
                "--meta","src/test/resources/splitMutationsData/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData runner = new ImportProfileData(args);
        runner.run();
        String studyStableId = "study_tcga_pub";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
        studyId = study.getInternalId();

        // assume clinical data for MUTATION_COUNT was created
        ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum("MUTATION_COUNT", studyId);
        assertNotNull(clinicalAttribute);

        // assume a MUTATION_COUNT record has been added for both samples
        List<ClinicalData> clinicalData = DaoClinicalData.getSampleData(study.getInternalId(), new ArrayList<String>(Arrays.asList("TCGA-AA-3664-01", "TCGA-AA-3665-01")), clinicalAttribute);
        assert(clinicalData.size() == 2);
        assertEquals("3", clinicalData.get(0).getAttrVal());
        assertEquals("0", clinicalData.get(1).getAttrVal());

        // load a second mutation data file
        String[] secondArgs = {
                "--data","src/test/resources/splitMutationsData/data_mutations_extended_continued.txt",
                "--meta","src/test/resources/splitMutationsData/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData secondRunner = new ImportProfileData(secondArgs);
        secondRunner.run();

        // assume a MUTATION_COUNT record has been updated for both samples (both +1)
        clinicalData = DaoClinicalData.getSampleData(study.getInternalId(), new ArrayList<String>(Arrays.asList("TCGA-AA-3664-01", "TCGA-AA-3665-01")), clinicalAttribute);
        assert(clinicalData.size() == 2);
        assertEquals("4", clinicalData.get(0).getAttrVal());
        assertEquals("1", clinicalData.get(1).getAttrVal());
    }

    @Test
    public void testImportGermlineOnlyFile() throws Exception {
        /* Mutations file split over two files with same stable id */
        String[] args = {
                "--data","src/test/resources/germlineOnlyMutationsData/data_mutations_extended.txt",
                "--meta","src/test/resources/germlineOnlyMutationsData/meta_mutations_extended.txt",
                "--loadMode", "bulkLoad"
        };
        ImportProfileData runner = new ImportProfileData(args);
        runner.run();
        String studyStableId = "study_tcga_pub";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
        studyId = study.getInternalId();
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-AA-3664-01").getInternalId();
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_breast_mutations");
        geneticProfileId = geneticProfile.getGeneticProfileId();

        // assume clinical data for MUTATION_COUNT was created
        ClinicalAttribute clinicalAttribute = DaoClinicalAttributeMeta.getDatum("MUTATION_COUNT", studyId);
        assertNotNull(clinicalAttribute);

        // assume a MUTATION_COUNT record has been added for one sample and the count is zero
        List<ClinicalData> clinicalData = DaoClinicalData.getSampleData(study.getInternalId(), new ArrayList<String>(Arrays.asList("TCGA-AA-3664-01")), clinicalAttribute);
        assert(clinicalData.size() == 1);
        assertEquals("0", clinicalData.get(0).getAttrVal());

        // check if the three germline mutations have been inserted
        validateMutationAminoAcid (geneticProfileId, sampleId, 64581, "T209A");
        validateMutationAminoAcid (geneticProfileId, sampleId, 50839, "G78S");
        validateMutationAminoAcid (geneticProfileId, sampleId, 2842, "L113P");

        // remove profile at the end
        DaoGeneticProfile.deleteGeneticProfile(geneticProfile);
        assertNull(DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_breast_mutations"));
    }

    private StructuralVariant constructExpectedStructuralVariant1() {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene site1Gene = daoGene.getGene("KIAA1549");
        CanonicalGene site2Gene = daoGene.getGene("BRAF");
        assertNotNull(site1Gene);
        assertNotNull(site2Gene);
        StructuralVariant sv = new StructuralVariant();
        sv.setInternalId(-1); // not retrieved from database DaoStructuralVariant.getAllStructuralVariants(), so no test
        sv.setGeneticProfileId(-1); // internal identifier for profile is not stable, so no test
        sv.setStructuralVariantId(-1); // not retrieved from database DaoStructuralVariant.getAllStructuralVariants(), so no test
        sv.setSampleIdInternal(-1); // internal identifier for sample is not stable, so no test
        sv.setSampleId(null);// not retrieved from database DaoStructuralVariant.getAllStructuralVariants(), so no test
        sv.setSite1EntrezGeneId(site1Gene.getEntrezGeneId());
        sv.setSite1HugoSymbol(null); // not retrieved from database DaoStructuralVariant.getAllStructuralVariants(), so no test
        sv.setSite1EnsemblTranscriptId("ENST00000242365");
        sv.setSite1Chromosome("7");
        sv.setSite1Position(138536968);
        sv.setSite1Contig("p11.2");
        sv.setSite1Region("intron");
        sv.setSite1RegionNumber(5);
        sv.setSite1Description("KIAA1549-BRAF.K16B10.COSF509_1");
        sv.setSite2EntrezGeneId(site2Gene.getEntrezGeneId());
        sv.setSite2HugoSymbol(null); // not retrieved from database DaoStructuralVariant.getAllStructuralVariants(), so no test
        sv.setSite2EnsemblTranscriptId("ENST00000288602");
        sv.setSite2Chromosome("7");
        sv.setSite2Position(140482957);
        sv.setSite2Contig("q14.1");
        sv.setSite2Region("exon");
        sv.setSite2RegionNumber(2);
        sv.setSite2Description("KIAA1549-BRAF.K16B10.COSF509_2");
        sv.setSite2EffectOnFrame("NA");
        sv.setNcbiBuild("GRCh37");
        sv.setDnaSupport("no");
        sv.setRnaSupport("yes");
        sv.setNormalReadCount(-1);
        sv.setTumorReadCount(1000);
        sv.setNormalVariantCount(-1);
        sv.setTumorVariantCount(900);
        sv.setNormalPairedEndReadCount(-1);
        sv.setTumorPairedEndReadCount(-1);
        sv.setNormalSplitReadCount(-1);
        sv.setTumorSplitReadCount(-1);
        sv.setAnnotation("KIAA1549-BRAF.K16B10.COSF509");
        sv.setBreakpointType("NA");
        sv.setConnectionType("NA");
        sv.setEventInfo("Fusion");
        sv.setVariantClass("NA");
        sv.setLength(-1);
        sv.setComments("Gain-of-Function");
        sv.setSvStatus("SOMATIC");
        sv.setDriverFilter("testdriver");
        sv.setDriverFilterAnn("testdriveranno");
        sv.setDriverTiersFilter("testdrivertiers");
        sv.setDriverTiersFilterAnn("testdrivertiersanno");
        return sv;
    }

    private <T> void registerDifference(StringBuilder response, String fieldId, T actual, T expected) {
        if (actual == expected) {
            return;
        }
        if (actual == null && expected != null) {
            response.append("actual " + fieldId + " value is null, but expected value is '" + expected + "'\n");
            return;
        }
        if (expected == null && actual != null) {
            response.append("actual " + fieldId + " value is '" + actual + "', but expected value is null" + expected + "\n");
            return;
        }
        if (!actual.equals(expected)) {
            response.append("actual " + fieldId + " value is '" + actual + "', but expected value is '" + expected + "'\n");
            return;
        }
    }

    private String differenceReport(StructuralVariant actualSV, StructuralVariant expectedSV) {
        StringBuilder response = new StringBuilder();
        // internalId not tested
        // geneticProfileId not tested
        // structuralVariantId not tested
        // sampleIdInternal not tested
        // sampleId not tested
        registerDifference(response, "site1EntrezGeneId", actualSV.getSite1EntrezGeneId(), expectedSV.getSite1EntrezGeneId());
        // site1HugoSymbol not tested
        registerDifference(response, "site1EnsemblTranscriptId", actualSV.getSite1EnsemblTranscriptId(), expectedSV.getSite1EnsemblTranscriptId());
        registerDifference(response, "site1Chromosome", actualSV.getSite1Chromosome(), expectedSV.getSite1Chromosome());
        registerDifference(response, "site1Position", actualSV.getSite1Position(), expectedSV.getSite1Position());
        registerDifference(response, "site1Contig", actualSV.getSite1Contig(), expectedSV.getSite1Contig());
        registerDifference(response, "site1Region", actualSV.getSite1Region(), expectedSV.getSite1Region());
        registerDifference(response, "site1RegionNumber", actualSV.getSite1RegionNumber(), expectedSV.getSite1RegionNumber());
        registerDifference(response, "site1Description", actualSV.getSite1Description(), expectedSV.getSite1Description());
        registerDifference(response, "site2EntrezGeneId", actualSV.getSite2EntrezGeneId(), expectedSV.getSite2EntrezGeneId());
        // site2HugoSymbol not tested
        registerDifference(response, "site2EnsemblTranscriptId", actualSV.getSite2EnsemblTranscriptId(), expectedSV.getSite2EnsemblTranscriptId());
        registerDifference(response, "site2Chromosome", actualSV.getSite2Chromosome(), expectedSV.getSite2Chromosome());
        registerDifference(response, "site2Position", actualSV.getSite2Position(), expectedSV.getSite2Position());
        registerDifference(response, "site2Contig", actualSV.getSite2Contig(), expectedSV.getSite2Contig());
        registerDifference(response, "site2Region", actualSV.getSite2Region(), expectedSV.getSite2Region());
        registerDifference(response, "site2RegionNumber", actualSV.getSite2RegionNumber(), expectedSV.getSite2RegionNumber());
        registerDifference(response, "site2Description", actualSV.getSite2Description(), expectedSV.getSite2Description());
        registerDifference(response, "site2EffectOnFrame", actualSV.getSite2EffectOnFrame(), expectedSV.getSite2EffectOnFrame());
        registerDifference(response, "ncbiBuild", actualSV.getNcbiBuild(), expectedSV.getNcbiBuild());
        registerDifference(response, "dnaSupport", actualSV.getDnaSupport(), expectedSV.getDnaSupport());
        registerDifference(response, "rnaSupport", actualSV.getRnaSupport(), expectedSV.getRnaSupport());
        registerDifference(response, "normalReadCount", actualSV.getNormalReadCount(), expectedSV.getNormalReadCount());
        registerDifference(response, "tumorReadCount", actualSV.getTumorReadCount(), expectedSV.getTumorReadCount());
        registerDifference(response, "normalVariantCount", actualSV.getNormalVariantCount(), expectedSV.getNormalVariantCount());
        registerDifference(response, "tumorVariantCount", actualSV.getTumorVariantCount(), expectedSV.getTumorVariantCount());
        registerDifference(response, "normalPairedEndReadCount", actualSV.getNormalPairedEndReadCount(), expectedSV.getNormalPairedEndReadCount());
        registerDifference(response, "tumorPairedEndReadCount", actualSV.getTumorPairedEndReadCount(), expectedSV.getTumorPairedEndReadCount());
        registerDifference(response, "normalSplitReadCount", actualSV.getNormalSplitReadCount(), expectedSV.getNormalSplitReadCount());
        registerDifference(response, "tumorSplitReadCount", actualSV.getTumorSplitReadCount(), expectedSV.getTumorSplitReadCount());
        registerDifference(response, "annotation", actualSV.getAnnotation(), expectedSV.getAnnotation());
        registerDifference(response, "breakpointType", actualSV.getBreakpointType(), expectedSV.getBreakpointType());
        registerDifference(response, "connectionType", actualSV.getConnectionType(), expectedSV.getConnectionType());
        registerDifference(response, "eventInfo", actualSV.getEventInfo(), expectedSV.getEventInfo());
        registerDifference(response, "variantClass", actualSV.getVariantClass(), expectedSV.getVariantClass());
        registerDifference(response, "length", actualSV.getLength(), expectedSV.getLength());
        registerDifference(response, "comments", actualSV.getComments(), expectedSV.getComments());
        registerDifference(response, "svStatus", actualSV.getSvStatus(), expectedSV.getSvStatus());
        registerDifference(response, "driverFilter", actualSV.getDriverFilter(), expectedSV.getDriverFilter());
        registerDifference(response, "driverFilterAnn", actualSV.getDriverFilterAnn(), expectedSV.getDriverFilterAnn());
        registerDifference(response, "driverTiersFilter", actualSV.getDriverTiersFilter(), expectedSV.getDriverTiersFilter());
        registerDifference(response, "driverTiersFilterAnn", actualSV.getDriverTiersFilterAnn(), expectedSV.getDriverTiersFilterAnn());
        return response.toString();
    }

    @Test
    public void testImportStructuralVariantFile() throws Exception {
        String[] sampleIds = {"TCGA-A1-A0SD-01", "TCGA-A1-A0SB-01"};
        String studyStableId = "study_tcga_pub";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
        int studyId = study.getInternalId();
        String testSampleStableId = "TCGA-A1-A0SB-01";
        int testSampleId = -1;
        // we need the internal sample id
        for (String sampleId : sampleIds) {
            String patientId = sampleId.substring(0, 12);
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, patientId);
            Integer patientIdInt = patient.getInternalId();
            Sample sample = DaoSample.getSampleByPatientAndSampleId(patientIdInt, sampleId);
            Integer sampleIdInt = sample.getInternalId();
            if (testSampleStableId.equals(sample.getStableId())) {
                testSampleId = sampleIdInt.intValue();
            }
        }

        assertNotEquals(-1, testSampleId);

        String[] args = {
                "--data","src/test/resources/data_structural_variants.txt",
                "--meta","src/test/resources/meta_structural_variants.txt" ,
                "--noprogress",
                "--loadMode", "bulkLoad"
        };

        try {
            ImportProfileData runner = new ImportProfileData(args);
            runner.run();
        } catch (Throwable e) {
            //useful info for when this fails:
            ConsoleUtil.showMessages();
            throw e;
        }

        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_tcga_pub_sv");
        assertNotNull(geneticProfile);

        int countStructuralVariantsInProfile = 0;
        boolean foundTestSample = false;

        List<StructuralVariant> structuralVariants = DaoStructuralVariant.getAllStructuralVariants();
        for (StructuralVariant structuralVariant : structuralVariants) {
            if (geneticProfile.getGeneticProfileId() == structuralVariant.getGeneticProfileId()) {
                countStructuralVariantsInProfile += 1;
                if (testSampleId == structuralVariant.getSampleIdInternal()) {
                    foundTestSample = true;
                    assertEquals("", differenceReport(structuralVariant, constructExpectedStructuralVariant1()));
                }
            }
        }
        assertEquals(5, countStructuralVariantsInProfile);
        assertEquals(true, foundTestSample);
    }

    @Test
    public void testImportCNAFile() throws Exception {
        //genes in this test:
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene(999999672, "TESTBRCA1"));
        daoGene.addGene(new CanonicalGene(999999675, "TESTBRCA2"));
        MySQLbulkLoader.flushAll();
        String[] args = {
                "--data","src/test/resources/data_CNA_sample.txt",
                "--meta","src/test/resources/meta_CNA.txt" ,
                "--noprogress",
                "--loadMode", "bulkLoad"
        };
        String[] sampleIds = {"TCGA-02-0001-01","TCGA-02-0003-01","TCGA-02-0004-01","TCGA-02-0006-01"};
        //This test is to check if the ImportProfileData class indeed adds the study stable Id in front of the
        //dataset study id (e.g. studyStableId + "_breast_mutations"):
        String studyStableId = "study_tcga_pub";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyStableId);
        studyId = study.getInternalId();
        //will be needed when relational constraints are active:
        for (String sampleId : sampleIds) {
            // fetch patient from db or add new one if does not exist
            Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(studyId, sampleId);
            Integer pId = (p == null) ? DaoPatient.addPatient(new Patient(study, sampleId)) : p.getInternalId();
            DaoSample.addSample(new Sample(sampleId, pId, study.getTypeOfCancerId()));
        }

        try {
            ImportProfileData runner = new ImportProfileData(args);
            runner.run();
        } catch (Throwable e) {
            //useful info for when this fails:
            ConsoleUtil.showMessages();
            throw e;
        }
        geneticProfileId = DaoGeneticProfile.getGeneticProfileByStableId(studyStableId + "_cna").getGeneticProfileId();
        List<Integer> sampleInternalIds = new ArrayList<Integer>();
        DaoSample.reCache();
        for (String sample : sampleIds) {
            sampleInternalIds.add(DaoSample.getSampleByCancerStudyAndSampleId(studyId, sample).getInternalId());
        }
        Collection<Short> cnaLevels = Arrays.asList((short)-2, (short)2);
        List<CnaEvent> cnaEvents = DaoCnaEvent.getCnaEvents(sampleInternalIds, null, geneticProfileId, cnaLevels);
        assertEquals(2, cnaEvents.size());
        //validate specific records. Data looks like:
        //999999672    TESTBRCA1    -2    0    1    0
        //999999675    TESTBRCA2    0    2    0    -1
        //Check if the first two samples are loaded correctly:
        int sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-02-0001-01").getInternalId();
        sampleInternalIds = Arrays.asList((int)sampleId);
        CnaEvent cnaEvent = DaoCnaEvent.getCnaEvents(sampleInternalIds, null, geneticProfileId, cnaLevels).get(0);
        assertEquals(-2, cnaEvent.getAlteration().getCode());
        assertEquals("TESTBRCA1", cnaEvent.getGeneSymbol());
        assertEquals("Putative_Passenger", cnaEvent.getDriverFilter());
        assertEquals("Test passenger", cnaEvent.getDriverFilterAnnotation());
        assertEquals("Class 2", cnaEvent.getDriverTiersFilter());
        assertEquals("Class 2 annotation", cnaEvent.getDriverTiersFilterAnnotation());
        sampleId = DaoSample.getSampleByCancerStudyAndSampleId(studyId, "TCGA-02-0003-01").getInternalId();
        sampleInternalIds = Arrays.asList((int)sampleId);
        cnaEvent = DaoCnaEvent.getCnaEvents(sampleInternalIds, null, geneticProfileId, cnaLevels).get(0);
        assertEquals(2, cnaEvent.getAlteration().getCode());
        assertEquals("TESTBRCA2", cnaEvent.getGeneSymbol());
        assertEquals("Putative_Driver", cnaEvent.getDriverFilter());
        assertEquals("Test driver", cnaEvent.getDriverFilterAnnotation());
        assertEquals("Class 1", cnaEvent.getDriverTiersFilter());
        assertEquals("Class 1 annotation", cnaEvent.getDriverTiersFilterAnnotation());
    }

    private void validateMutationAminoAcid (int geneticProfileId, Integer sampleId, long entrezGeneId, String expectedAminoAcidChange) throws DaoException {
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getMutations(geneticProfileId, sampleId, entrezGeneId);
        assertEquals(1, mutationList.size());
        assertEquals(expectedAminoAcidChange, mutationList.get(0).getProteinChange());
    }

    private static void loadGenes() throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        // genes for "data_mutations_extended.txt"
        daoGene.addGene(new CanonicalGene(114548L, "NLRP3"));
        daoGene.addGene(new CanonicalGene(3339L, "HSPG2"));
        daoGene.addGene(new CanonicalGene(282770L, "OR10AG1"));
        daoGene.addGene(new CanonicalGene(51806L, "CALML5"));
        daoGene.addGene(new CanonicalGene(343035L, "RD3"));
        daoGene.addGene(new CanonicalGene(80114L, "BICC1"));
        daoGene.addGene(new CanonicalGene(219736L, "STOX1"));
        daoGene.addGene(new CanonicalGene(6609L, "SMPD1"));
        daoGene.addGene(new CanonicalGene(51259L, "TMEM216"));
        daoGene.addGene(new CanonicalGene(89L, "ACTN3"));
        daoGene.addGene(new CanonicalGene(64581L, "CLEC7A"));
        daoGene.addGene(new CanonicalGene(50839L, "TAS2R10"));
        daoGene.addGene(new CanonicalGene(54407L, "SLC38A2"));
        daoGene.addGene(new CanonicalGene(6667L, "SP1"));
        daoGene.addGene(new CanonicalGene(2842L, "GPR19"));
        // additional genes for "data_mutations_oncotated.txt"
        daoGene.addGene(new CanonicalGene(55138L, "FAM90A1"));
        daoGene.addGene(new CanonicalGene(10628L, "TXNIP"));
        daoGene.addGene(new CanonicalGene(80343, "SEL1L2"));
        daoGene.addGene(new CanonicalGene(29102L, "DROSHA"));
        daoGene.addGene(new CanonicalGene(7204L, "TRIO"));
        daoGene.addGene(new CanonicalGene(57111L, "RAB25"));
        daoGene.addGene(new CanonicalGene(773L, "CACNA1A"));
        daoGene.addGene(new CanonicalGene(100132025L, "LOC100132025"));
        daoGene.addGene(new CanonicalGene(1769L, "DNAH8"));
        daoGene.addGene(new CanonicalGene(343171L, "OR2W3"));
        daoGene.addGene(new CanonicalGene(2901L, "GRIK5"));
        daoGene.addGene(new CanonicalGene(10568L, "SLC34A2"));
        daoGene.addGene(new CanonicalGene(140738L, "TMEM37"));
        daoGene.addGene(new CanonicalGene(94025L, "MUC16"));
        daoGene.addGene(new CanonicalGene(1915L, "EEF1A1"));
        daoGene.addGene(new CanonicalGene(65083L, "NOL6"));
        daoGene.addGene(new CanonicalGene(7094L, "TLN1"));
        daoGene.addGene(new CanonicalGene(51196L, "PLCE1"));
        daoGene.addGene(new CanonicalGene(1952L, "CELSR2"));
        daoGene.addGene(new CanonicalGene(2322L, "FLT3"));
        daoGene.addGene(new CanonicalGene(867L, "CBL"));
        MySQLbulkLoader.flushAll();
    }
}
