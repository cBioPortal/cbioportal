/*
 * Copyright (c) 2016 - 2022 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.integrationTest.scripts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.mskcc.cbio.portal.dao.ClickHouseBulkLoader;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalAttributeMeta;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGeneset;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.dao.DaoMutation;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.dao.DaoSampleList;
import org.mskcc.cbio.portal.dao.DaoStructuralVariant;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.Geneset;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Gistic;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.SampleList;
import org.mskcc.cbio.portal.model.StructuralVariant;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.mskcc.cbio.portal.model.shared.GeneticEntity;
import org.mskcc.cbio.portal.scripts.ImportGenePanel;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.TransactionalScripts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.mskcc.cbio.portal.integrationTest.IntegrationTestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration test using the same data that is used by validation system test
 * "study_es_0". In the validation system test for "study_es_0" it is checked if
 * the study can pass validation without any errors or warnings. Here we submit
 * the same study to the data loading code and check that it also loads
 * correctly and completely into the DB.
 *
 * @author Pieter Lukasse
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/integrationTestScript.xml", "classpath:/applicationContext-dao.xml" })
public class TestIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        System.setIn(new InputStream() {
            @Override
            public int read() {
                throw new RuntimeException("None of the tasks in this test should be user-interactive");
            }
        });
        ProgressMonitor.setConsoleMode(true);
        ProgressMonitor.resetWarnings();
        DaoCancerStudy.reCacheAll();
        DaoGeneOptimized.getInstance().reCache();
        loadGenes();
        loadGenePanel();
    }

    /**
     * Test to check if study_es_0 can be loaded correctly into DB. Should fail if
     * any warning is given by loader classes or if expected data is not found in DB
     * at the end of the test.
     * 
     * @throws Throwable
     */
    @Test
    public void testLoadStudyEs0() throws Throwable {
        try {
            // === assumptions that we rely upon in the checks later on: ====
            // use this to get progress info/troubleshoot:
            // ProgressMonitor.setConsoleMode(true);

            int numberOfMutationsInDb = DaoMutation.getAllMutations().size();

            // ==== Load the data ====
            TransactionalScripts scripts = applicationContext.getBean(TransactionalScripts.class);
            scripts.run();

            // count warnings, but disregard warnings caused by
            // gene_symbol_disambiguation.txt
            ArrayList<String> warnings = ProgressMonitor.getWarnings();
            int countWarnings = 0;
            for (String warning : warnings) {
                if (!warning.contains("resources/gene_symbol_disambiguation.txt")) {
                    countWarnings++;
                }
            }
            // check that there are no warnings:
            assertEquals(0, countWarnings);

            // check that ALL data really got into DB correctly. In the spirit of
            // integration tests,
            // we want to query via the same service layer as the one used by the web API
            // here.
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_es_0");
            assertEquals("Test study es_0", cancerStudy.getName());

            // ===== Check MUTATION data ========
            List<ExtendedMutation> mutations = DaoMutation.getAllMutations();
            // check number of mutation records in the database
            // 3 in seed_mini.sql + 33 study_es_0/data_mutations_extended.maf (2 silent ignored)) 
            // so we expect +34 records in DB:
            assertEquals(numberOfMutationsInDb + 34, mutations.size());

            //===== Check STRUCTURAL VARIANT data ========
            // Add samples and molecular profile IDs
            GeneticProfile svGeneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_structural_variants");

            List<StructuralVariant> structuralVariants = DaoStructuralVariant.getAllStructuralVariants()
                    .stream()
                    .filter(sv ->
                            sv.getGeneticProfileId() == svGeneticProfile.getGeneticProfileId()
                    )
                    .collect(Collectors.toList());

            // Check if all 48 structural variants are imported
            assertEquals(48, structuralVariants.size());

            //===== Check CNA data ========
            DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
            ArrayList<Long> entrezIds = new ArrayList<Long>(Arrays.asList(116983L, 375790L, 55210L, 83858L, 219293L, 54998L, 2073L));
            GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_gistic");
            int countAMP_DEL = 0;
            int profileDataSize = 0;
            for (Long entrezId : entrezIds) {
                HashMap<Integer, String> cnaProfileData = daoGeneticAlteration.getGeneticAlterationMap(geneticProfile.getGeneticProfileId(), entrezId);
                profileDataSize += cnaProfileData.size();
                for (String profileData : cnaProfileData.values()) {
                    if (profileData.equals("2") || profileData.equals("-2")) {
                        countAMP_DEL++;
                    }
                }
            }
            //there is data for 7 genes x 788 samples:
            assertEquals(7*788, profileDataSize);
            //there are 63 CNA entries that have value == 2 or value == -2;
            assertEquals(63, countAMP_DEL);

            // log2CNA
            geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_log2CNA");
            int count0506 = 0;
            profileDataSize = 0;
            for (Long entrezId : entrezIds) {
                HashMap<Integer, String> cnaProfileData = daoGeneticAlteration.getGeneticAlterationMap(geneticProfile.getGeneticProfileId(), entrezId);
                profileDataSize += cnaProfileData.size();
                for (String profileData : cnaProfileData.values()) {
                    double profileDataValue = Double.parseDouble(profileData);
                    if (profileDataValue > -0.6 && profileDataValue <= -0.5) {
                        count0506++;
                    }
                }
            }
            //there is data for 7 genes x 778 samples:
            assertEquals(7*778, profileDataSize);
            // there are 273 CNA entries that have value between -0.6 and 0.5;
            assertEquals(273, count0506);

            // ===== Check CLINICAL data ========
            // in total 7 clinical attributes should be added (5 "patient type" including sample count
            // and 3 "sample type" attributes including MUTATION_COUNT and FRACTION_GENOME_ALTERED)
            // see also "assumptions" section at start of this test case
            int patientAttrCount = 0;
            int sampleAttrCount = 0;
            for (ClinicalAttribute ca : DaoClinicalAttributeMeta.getDataByStudy(cancerStudy.getInternalId())) {
                if (ca.isPatientAttribute()) {
                    patientAttrCount++;
                }
                else {
                    sampleAttrCount++;
                }
            }
            assertEquals(3, sampleAttrCount);
            assertEquals(5, patientAttrCount);

            Boolean mutationCountExists = false;
            Boolean fractionGenomeAlteredExists = false;
            for (ClinicalData cd : DaoClinicalData.getSampleData(cancerStudy.getInternalId(), Arrays.asList("TCGA-A2-A04P-01"))) {
                if (cd.getAttrId().equals("MUTATION_COUNT")) {
                    mutationCountExists = true;
                    assertEquals("TCGA-A2-A04P-01 should have one mutation in MUTATION_COUNT", "1", cd.getAttrVal());
                } else if (cd.getAttrId().equals("FRACTION_GENOME_ALTERED")) {
                    fractionGenomeAlteredExists = true;
                    assertEquals("TCGA-A2-A04P-01 should have 0.0 FRACTION_GENOME_ALTERED (the imported segment file spans a very small part of the genome)",
                                 0.0, Float.parseFloat(cd.getAttrVal()), 0.01);
                }
            }
            assertTrue("MUTATION_COUNT sample clinical attribute should have been added for TCGA-A2-A04P-01", mutationCountExists);
            assertTrue("FRACTION_GENOME_ALTERED sample clinical attribute should have been added for TCGA-A2-A04P-01", fractionGenomeAlteredExists);

            // ===== Check EXPRESSION data ========
            geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_mrna");
            entrezIds = new ArrayList<Long>(Arrays.asList(90993L, 6205L, 9240L, 4313L, 23051L, 2073L));
            profileDataSize = 0;
            int countGte2Lt3 = 0;
            for (Long entrezId : entrezIds) {
                HashMap<Integer, String> cnaProfileData = daoGeneticAlteration.getGeneticAlterationMap(geneticProfile.getGeneticProfileId(), entrezId);
                profileDataSize += cnaProfileData.size();
                for (String profileData : cnaProfileData.values()) {
                    double profileDataValue = Double.parseDouble(profileData);
                    if (profileDataValue >= 2.0 && profileDataValue < 3.0) {
                        countGte2Lt3++;
                    }
                }
            }
            // there is data for 6 genes x 526 samples:
            assertEquals(6 * 526, profileDataSize);
            // there are 50 entries with value between 2.0 and 3.0
            assertEquals(50, countGte2Lt3);

            // ===== check cancer_type
            TypeOfCancer cancerType = DaoTypeOfCancer.getTypeOfCancerById("brca-es0");
            assertEquals("Breast Invasive Carcinoma", cancerType.getName());

            // ===== check gistic data
            // servlet uses this query:
            ArrayList<Gistic> gistics = DaoGistic.getAllGisticByCancerStudyId(cancerStudy.getInternalId());
            assertEquals(11, gistics.size());
            Gistic gisticChr10 = null, gisticChr20 = null;
            for (Gistic gistic : gistics) {
                if (gistic.getChromosome() == 20) {
                    // assert not yet set:
                    assertEquals(null, gisticChr20);
                    gisticChr20 = gistic;
                } else if (gistic.getChromosome() == 10) {
                    // assert not yet set:
                    assertEquals(null, gisticChr10);
                    gisticChr10 = gistic;
                }
            }
            assertEquals(8, gisticChr10.getGenes_in_ROI().size());
            assertEquals(1, gisticChr20.getGenes_in_ROI().size());
            assertEquals("ZNF217", gisticChr20.getGenes_in_ROI().get(0).getHugoGeneSymbolAllCaps());

            // ===== check methylation
            geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_methylation_hm27");
            entrezIds = new ArrayList<Long>(Arrays.asList(487L, 7871L, 3232L, 24145L, 3613L, 389L, 8148L, 283234L));
            profileDataSize = 0;
            int count0506Pos = 0;
            for (Long entrezId : entrezIds) {
                HashMap<Integer, String> cnaProfileData = daoGeneticAlteration.getGeneticAlterationMap(geneticProfile.getGeneticProfileId(), entrezId);
                profileDataSize += cnaProfileData.size();
                for (String profileData : cnaProfileData.values()) {
                    double profileDataValue = Double.parseDouble(profileData);
                    if (profileDataValue >= 0.5 && profileDataValue < 0.6) {
                        count0506Pos++;
                    }
                }
            }
            // there is data for 8 genes x 311 samples:
            assertEquals(8 * 311, profileDataSize);
            // simple check: there are 199 entries that have value between 0.5 and 0.6;
            assertEquals(199, count0506Pos);

            // ===== check case lists
            // study is set to generate global "all" case list, so check this:
            // study has 2 lists:
            DaoSampleList daoSampleList = new DaoSampleList();
            List<SampleList> sampleLists = daoSampleList.getAllSampleLists(cancerStudy.getInternalId());
            assertEquals(2, sampleLists.size());
            // for list "all" there are 826 samples expected:
            SampleList allSampleList = daoSampleList.getSampleListByStableId("study_es_0_all");
            assertEquals("All cases in study", allSampleList.getName());

            // there is a custom case list with 778 samples, and name "this is an optional custom case list" ,
            // so check this:
            SampleList customSampleList = daoSampleList.getSampleListByStableId("study_es_0_custom");
            assertEquals("this is an optional custom case list", customSampleList.getName());

            // ===== check mutational signature
            String testMutationalSignatureStableIds = "mean_1";
            GeneticEntity mutationSignatureGeneticEntity = DaoGeneticEntity.getGeneticEntityByStableId(testMutationalSignatureStableIds);
            assertNotNull(mutationSignatureGeneticEntity);

            String testMutationalSignatureMolecularProfileIds = "study_es_0_mutational_signature";
            GeneticProfile mutationSignatureProfile = DaoGeneticProfile.getGeneticProfileByStableId(testMutationalSignatureMolecularProfileIds);
            assertNotNull(mutationSignatureProfile);
            // ENTITY_STABLE_ID name description TCGA-A1-A0SB-01 TCGA-A1-A0SD-01
            // TCGA-A1-A0SE-01 TCGA-A1-A0SH-01 TCGA-A2-A04U-01 TCGA-B6-A0RS-01
            // TCGA-BH-A0HP-01 TCGA-BH-A18P-01
            // mean_1 ... ... ... 0.370266873	0.010373016	0.005419294	0.022753384	0.037687823	0.016708976	0.100042446	0.104214723
            HashMap<Integer, String> mutationalSignatureData = DaoGeneticAlteration
                    .getInstance()
                    .getGeneticAlterationMapForEntityIds(
                            mutationSignatureProfile.getGeneticProfileId(),
                            List.of(mutationSignatureGeneticEntity.getId())).get(mutationSignatureGeneticEntity.getId());
            Sample sbSample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), "TCGA-A1-A0SB-01");
            Sample shSample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), "TCGA-A1-A0SH-01");
            assertEquals("0.370266873", mutationalSignatureData.get(sbSample.getInternalId()));
            assertEquals("0.022753384", mutationalSignatureData.get(shSample.getInternalId()));

            // ===== check GSVA data
            // ...
            String testGenesetExternalId = "GO_ATP_DEPENDENT_CHROMATIN_REMODELING";
            Geneset testGeneset = DaoGeneset.getGenesetByExternalId(testGenesetExternalId);
            assertEquals(4, testGeneset.getGenesetGeneIds().size());
            // scores: TCGA-A1-A0SB-01 TCGA-A1-A0SD-01 TCGA-A1-A0SE-01 TCGA-A1-A0SH-01
            // TCGA-A2-A04U-01
            // GO_ATP_DEPENDENT_CHROMATIN_REMODELING -0.293861251463613 -0.226227563676626
            // -0.546556962547473 -0.0811115513543749 0.56919171543422
            // using new api:
            GeneticProfile gsvaScoresProfile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_gsva_scores");
            HashMap<Integer, String> genesetData = DaoGeneticAlteration
                    .getInstance()
                    .getGeneticAlterationMapForEntityIds(
                            gsvaScoresProfile.getGeneticProfileId(),
                            List.of(testGeneset.getGeneticEntityId())).get(testGeneset.getGeneticEntityId());
            assertEquals(5, genesetData.size());

            String sbSampleGenesetValueString = genesetData.get(sbSample.getInternalId());
            String shSampleGenesetValuesString = genesetData.get(shSample.getInternalId());
            assertEquals(-0.293861251463613, Double.parseDouble(sbSampleGenesetValueString), 0.00001);
            assertEquals(-0.0811115513543749, Double.parseDouble(shSampleGenesetValuesString), 0.00001);

            // ===== check treatment (profile) data
            // ...
            String testTreatmentStableId = "Irinotecan";
            GeneticEntity testTreatmentGeneticEntity = DaoGeneticEntity.getGeneticEntityByStableId(testTreatmentStableId);
            assertNotNull(testTreatmentGeneticEntity);
            // ENTITY_STABLE_ID NAME DESCRIPTION URL TCGA-A1-A0SB-01 TCGA-A1-A0SD-01
            // TCGA-A1-A0SE-01 TCGA-A1-A0SH-01 TCGA-A2-A04U-01 TCGA-B6-A0RS-01
            // TCGA-BH-A0HP-01 TCGA-BH-A18P-01
            // Irinotecan ... ... ... NA 0.080764666 NA 0.06704437 0.069568723 0.034992039
            // 0.740817904 0.209220141
            GeneticProfile treatmentIc50Profile = DaoGeneticProfile.getGeneticProfileByStableId("study_es_0_treatment_ic50");
            HashMap<Integer, String> treatmentData = DaoGeneticAlteration
                    .getInstance()
                    .getGeneticAlterationMapForEntityIds(
                            treatmentIc50Profile.getGeneticProfileId(),
                            List.of(testTreatmentGeneticEntity.getId())).get(testTreatmentGeneticEntity.getId());
            assertEquals(8, treatmentData.size());
            String sbSampleIrinotecanTraetmentValuesString = treatmentData.get(sbSample.getInternalId());
            assertEquals("NA", sbSampleIrinotecanTraetmentValuesString);
            String shSampleIrinotecanTraetmentValuesString = treatmentData.get(shSample.getInternalId());
            assertEquals(0.06704437, Double.parseDouble(shSampleIrinotecanTraetmentValuesString), 0.00001);

            // ===== check study status
            assertEquals(DaoCancerStudy.Status.AVAILABLE, DaoCancerStudy.getStatus("study_es_0"));

        } catch (Throwable t) {
            ConsoleUtil.showWarnings();
            System.err.println("\nABORTED! " + t.toString());
            if (t.getMessage() == null)
                t.printStackTrace();
            throw t;
        }
    }

    /**
     * Loads the genes used by this test.
     * 
     * @throws DaoException
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     * 
     */
    private void loadGenes() throws DaoException, JsonParseException, JsonMappingException, IOException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        // read the respective genes.json and genesaliases.json files from the system
        // test study_es_0 to
        // load the genes and genes aliases into the DB for which this scenario is
        // written:

        Map<Integer, Set<String>> aliasesMap = new HashMap<Integer, Set<String>>();
        InputStream inputStream = new FileInputStream(
                "tests/test_data/api_json_system_tests/genesaliases.json");
        // parse json file:
        ObjectMapper mapper = new ObjectMapper();
        TestGeneAlias[] genesAliases = mapper.readValue(inputStream, TestGeneAlias[].class);

        // build up aliases map:
        for (TestGeneAlias testGeneAlias : genesAliases) {
            Set<String> aliases = aliasesMap.get(testGeneAlias.entrezGeneId);
            if (aliases == null) {
                aliases = new HashSet<String>();
                aliasesMap.put(testGeneAlias.entrezGeneId, aliases);
            }
            aliases.add(testGeneAlias.geneAlias);
        }

        inputStream = new FileInputStream("tests/test_data/api_json_system_tests/genes.json");
        // parse json file:
        mapper = new ObjectMapper();
        TestGene[] genes = mapper.readValue(inputStream, TestGene[].class);

        // add genes to db:
        for (TestGene testGene : genes) {
            CanonicalGene gene = new CanonicalGene(testGene.entrezGeneId, testGene.hugoGeneSymbol);
            // get aliases from map:
            gene.setAliases(aliasesMap.get(testGene.entrezGeneId));
            daoGene.addGene(gene);
        }

        ClickHouseBulkLoader.flushAll();

    }

    /**
     * Loads a gene panel used by this test.
     * 
     */
    private void loadGenePanel() throws Exception {
        ImportGenePanel gp = new ImportGenePanel(null);
        gp.setFile(new File("tests/test_data/study_es_0/data_gene_panel_testpanel1.txt"));
        gp.importData();
        gp.setFile(new File("tests/test_data/study_es_0/data_gene_panel_testpanel2.txt"));
        gp.importData();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestGene {
        @JsonProperty("hugoGeneSymbol")
        String hugoGeneSymbol;
        @JsonProperty("entrezGeneId")
        int entrezGeneId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestGeneAlias {
        @JsonProperty("alias")
        String geneAlias;
        @JsonProperty("entrezGeneId")
        int entrezGeneId;
    }

}
