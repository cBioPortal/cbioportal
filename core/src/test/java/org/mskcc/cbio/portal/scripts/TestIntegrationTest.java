/*
 * Copyright (c) 2016 The Hyve B.V.
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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mskcc.cbio.portal.model.Mutation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.dao.DaoGistic;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.persistence.GeneticProfileMapperLegacy;
import org.mskcc.cbio.portal.persistence.MutationMapperLegacy;
import org.mskcc.cbio.portal.service.ApiService;
import org.mskcc.cbio.portal.util.SpringUtil;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.TransactionalScripts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.service.GenesetDataService;
import org.mskcc.cbio.portal.dao.DaoGeneset;


/**
 * Integration test using the same data that is used by validation system test "study_es_0".
 * In the validation system test for "study_es_0" it is checked if the study can pass validation 
 * without any errors or warnings. Here we submit the same study to the data loading code 
 * and check that it also loads correctly and completely into the DB. 
 *
 * @author Pieter Lukasse
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/integrationTestScript.xml", "classpath:/applicationContext-dao.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class TestIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Before
    public void setUp() throws DaoException, JsonParseException, JsonMappingException, IOException, Exception {
        SpringUtil.setApplicationContext(applicationContext);
        ProgressMonitor.setConsoleMode(false);
        ProgressMonitor.resetWarnings();
        DaoCancerStudy.reCacheAll();
        DaoGeneOptimized.getInstance().reCache();
        loadGenes();
        loadGenePanel();
    }
    
    /**
     * Test to check if study_es_0 can be loaded correctly into DB. 
     * Should fail if any warning is given by loader classes or if expected
     * data is not found in DB at the end of the test.
     * @throws Throwable 
     */
    @Test
    public void testLoadStudyEs0() throws Throwable {
        try {
            //=== assumptions that we rely upon in the checks later on: ====
            ApiService apiService = applicationContext.getBean(ApiService.class);
            //assumption 1: there are no clinical attributes at the start of the test:
            assertEquals(0, apiService.getClinicalAttributes().size());
            
            //use this to get progress info/troubleshoot: 
            //ProgressMonitor.setConsoleMode(true);
            
            //==== Load the data ====
            TransactionalScripts scripts = applicationContext.getBean(TransactionalScripts.class);
            scripts.run();

            //count warnings, but disregard warnings caused by gene_symbol_disambiguation.txt
            ArrayList<String> warnings = ProgressMonitor.getWarnings();
            int countWarnings = 0;
            for (String warning: warnings) {
                if (!warning.contains("resources/gene_symbol_disambiguation.txt")) {
                    countWarnings++;
                }
            }
            //check that there are no warnings:
            assertEquals(0, countWarnings);

            //check that ALL data really got into DB correctly. In the spirit of integration tests,
            //we want to query via the same service layer as the one used by the web API here.
            CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId("study_es_0");
            assertEquals("Test study es_0", cancerStudy.getName());
            
            //===== Check MUTATION data ========
            MutationMapperLegacy mutationMapperLegacy = applicationContext.getBean(MutationMapperLegacy.class);
            List<String> geneticProfileStableIds = new ArrayList<String>();
            geneticProfileStableIds.add("study_es_0_mutations");
            List<Mutation> mutations = mutationMapperLegacy.getMutationsDetailed(geneticProfileStableIds,null,null,null);
            //there are 31 records in the mutation file, of which 3 are filtered, and there are 3 extra records added from fusion
            //so we expect 31 records in DB:
            assertEquals(31, mutations.size());

            //===== Check FUSION data ========
            // Are there 3 fusion entries in mutation profile? true
            int countFusions = 0;
            for (Mutation mutation : mutations) {
                if (mutation.getMutationEvent().getMutationType().equals("Fusion")) {
                    countFusions++;
                }
            }
            assertEquals(countFusions, 3);

            // Is there a seperate fusion profile? -> false
            GeneticProfileMapperLegacy geneticProfileMapperLegacy = applicationContext.getBean(GeneticProfileMapperLegacy.class);
            geneticProfileStableIds = new ArrayList<String>();
            geneticProfileStableIds.add("study_es_0_fusion");
            List<DBGeneticProfile> geneticProfiles = geneticProfileMapperLegacy.getGeneticProfiles(geneticProfileStableIds);
            assertEquals(geneticProfiles.size(), 0);

            //===== Check CNA data ========
            geneticProfileStableIds = new ArrayList<String>();
            geneticProfileStableIds.add("study_es_0_gistic");
            List<String> hugoGeneSymbols = new ArrayList<String>(Arrays.asList("ACAP3","AGRN","ATAD3A","ATAD3B","ATAD3C","AURKAIP1","ERCC5"));
            List<DBProfileData> cnaProfileData = apiService.getGeneticProfileData(geneticProfileStableIds, hugoGeneSymbols, null, null);
            //there is data for 7 genes x 778 samples:
            assertEquals(7*778, cnaProfileData.size());
            //there are 63 CNA entries that have value == 2 or value == -2;
            int countAMP_DEL = 0;
            for (Serializable profileEntry: cnaProfileData) {
                String profileData = ((DBSimpleProfileData)profileEntry).profile_data;
                if (profileData.equals("2") || profileData.equals("-2")) {
                    countAMP_DEL++;
                }
            }
            assertEquals(63, countAMP_DEL);
            //log2CNA
            geneticProfileStableIds = new ArrayList<String>();
            geneticProfileStableIds.add("study_es_0_log2CNA");
            hugoGeneSymbols = new ArrayList<String>(Arrays.asList("ACAP3","AGRN","ATAD3A","ATAD3B","ATAD3C","AURKAIP1","ERCC5"));
            cnaProfileData = apiService.getGeneticProfileData(geneticProfileStableIds, hugoGeneSymbols, null, null);
            //there is data for 7 genes x 778 samples:
            assertEquals(7*778, cnaProfileData.size());
            //there are 273 CNA entries that have value between -0.6 and 0.5;
            int count0506 = 0;
            for (Serializable profileEntry: cnaProfileData) {
                String profileData = ((DBSimpleProfileData)profileEntry).profile_data;
                double profileDataValue = Double.parseDouble(profileData);
                if (profileDataValue > -0.6 && profileDataValue <= -0.5) {
                    count0506++;
                }
            }
            assertEquals(273, count0506);
            
            //===== Check CLINICAL data ========
            //in total 7 clinical attributes should be added (4 "patient type" 
            //and 3 "sample type" attributes including MUTATION_COUNT and FRACTION_GENOME_ALTERED) 
            //see also "assumptions" section at start of this test case
            List<DBClinicalField> clinicalAttributes = apiService.getSampleClinicalAttributes();
            assertEquals(3, clinicalAttributes.size());
            clinicalAttributes = apiService.getPatientClinicalAttributes();
            assertEquals(5, clinicalAttributes.size());
            List<DBClinicalSampleData> clinicalComputedSampleData = apiService.getSampleClinicalData("study_es_0", Arrays.asList("MUTATION_COUNT","FRACTION_GENOME_ALTERED"), Arrays.asList("TCGA-A2-A04P-01"));
            Boolean mutationCountExists = false;
            Boolean fractionGenomeAlteredExists = false;
            for (DBClinicalSampleData dbClinicalSampleData: clinicalComputedSampleData) {
                if (dbClinicalSampleData.attr_id.equals("MUTATION_COUNT")) {
                    mutationCountExists = true;
                    assertEquals("TCGA-A2-A04P-01 should have one mutation in MUTATION_COUNT", "1", dbClinicalSampleData.attr_val);
                } else if (dbClinicalSampleData.attr_id.equals("FRACTION_GENOME_ALTERED")) {
                    fractionGenomeAlteredExists = true;
                    assertEquals("TCGA-A2-A04P-01 should have 0.0 FRACTION_GENOME_ALTERED (the imported segment file spans a very small part of the genome)", 0.0, Float.parseFloat(dbClinicalSampleData.attr_val), 0.01);
                }
            }
            assertTrue("MUTATION_COUNT sample clinical attribute should have been added for TCGA-A2-A04P-01", mutationCountExists);
            assertTrue("FRACTION_GENOME_ALTERED sample clinical attribute should have been added for TCGA-A2-A04P-01", fractionGenomeAlteredExists);
            
            //===== Check EXPRESSION data ========
            geneticProfileStableIds = new ArrayList<String>();
            geneticProfileStableIds.add("study_es_0_mrna");
            hugoGeneSymbols = new ArrayList<String>(Arrays.asList("CREB3L1","RPS11","PNMA1","MMP2","ZHX3","ERCC5"));
            List<DBProfileData> expressionData = apiService.getGeneticProfileData(geneticProfileStableIds, hugoGeneSymbols, null, null);
            //there is data for 6 genes x 526 samples:
            assertEquals(6*526, expressionData.size());
            //there are 50 entries with value between 2.0 and 3.0
            int countGte2Lt3 = 0;
            for (Serializable profileEntry: expressionData) {
                String profileData = ((DBSimpleProfileData)profileEntry).profile_data;
                double dataValue = Double.parseDouble(profileData); 
                if (dataValue >= 2.0 && dataValue < 3.0) {
                    countGte2Lt3++;
                }
            }
            assertEquals(50, countGte2Lt3);
            
            //===== check cancer_type
            List<DBCancerType> cancerTypes = apiService.getCancerTypes(Arrays.asList("brca-es0"));
            assertEquals(1, cancerTypes.size());
            assertEquals("Breast Invasive Carcinoma", cancerTypes.get(0).name);
            
            //===== check gistic data
            //servlet uses this query:
            ArrayList<Gistic> gistics = DaoGistic.getAllGisticByCancerStudyId(cancerStudy.getInternalId());
            assertEquals(14, gistics.size());
            Gistic gisticChr10 = null, gisticChr20 = null;
            for (Gistic gistic: gistics) {
                if (gistic.getChromosome() == 20) {
                    //assert not yet set:
                    assertEquals(null, gisticChr20);
                    gisticChr20 = gistic;
                }
                else if (gistic.getChromosome() == 10) {
                    //assert not yet set:
                    assertEquals(null, gisticChr10);
                    gisticChr10 = gistic;
                }
            }
            assertEquals(8, gisticChr10.getGenes_in_ROI().size());
            assertEquals(1, gisticChr20.getGenes_in_ROI().size());
            assertEquals("ZNF217", gisticChr20.getGenes_in_ROI().get(0).getHugoGeneSymbolAllCaps());
            
            //===== check methylation
            geneticProfileStableIds = new ArrayList<String>();
            geneticProfileStableIds.add("study_es_0_methylation_hm27");
            hugoGeneSymbols = new ArrayList<String>(Arrays.asList("ATP2A1","SLMAP","HOXD3","PANX1","IMPA2","RHOC","TAF15","CCDC88B"));
            List<DBProfileData> methylationProfileData = apiService.getGeneticProfileData(geneticProfileStableIds, hugoGeneSymbols, null, null);
            //there is data for 8 genes x 311 samples:
            assertEquals(8*311, methylationProfileData.size());
            //simple check: there are 199 entries that have value between 0.5 and 0.6;
            int count0506Pos = 0;
            for (Serializable profileEntry: methylationProfileData) {
                String profileData = ((DBSimpleProfileData)profileEntry).profile_data;
                double profileDataValue = Double.parseDouble(profileData);
                if (profileDataValue >= 0.5 && profileDataValue < 0.6) {
                    count0506Pos++;
                }
            }
            assertEquals(199, count0506Pos);
            
            //===== check case lists
            //study is set to generate global "all" case list, so check this:
            //study has 2 lists:
            List<DBSampleList> sampleLists = apiService.getSampleLists("study_es_0");
            assertEquals(2, sampleLists.size());
            //for list "all" there are 826 samples expected:
            sampleLists = apiService.getSampleLists(Arrays.asList("study_es_0_all"));
            assertEquals(1, sampleLists.size());
            assertEquals("All cases in study", sampleLists.get(0).name);
            
            //there is a custom case list with 778 samples, and name "this is an optional custom case list", 
            //so check this:
            sampleLists = apiService.getSampleLists(Arrays.asList("study_es_0_custom"));
            assertEquals(1, sampleLists.size());
            assertEquals("this is an optional custom case list", sampleLists.get(0).name);
            
            //===== check mutsig
            //TODO
            
            //===== check GSVA data
            //...
            String testGeneset = "GO_ATP_DEPENDENT_CHROMATIN_REMODELING";
            assertEquals(4, DaoGeneset.getGenesetByExternalId(testGeneset).getGenesetGeneIds().size());
            //scores:                                        TCGA-A1-A0SB-01     TCGA-A1-A0SD-01      TCGA-A1-A0SE-01     TCGA-A1-A0SH-01     TCGA-A2-A04U-01
            //        GO_ATP_DEPENDENT_CHROMATIN_REMODELING  -0.293861251463613  -0.226227563676626  -0.546556962547473  -0.0811115513543749  0.56919171543422
            //using new api:
            GenesetDataService genesetDataService = applicationContext.getBean(GenesetDataService.class);
            List<GenesetMolecularData> genesetData = genesetDataService.fetchGenesetData("study_es_0_gsva_scores", "study_es_0_all",  Arrays.asList(testGeneset));
            assertEquals(5, genesetData.size());

            genesetData = genesetDataService.fetchGenesetData("study_es_0_gsva_scores", Arrays.asList("TCGA-A1-A0SB-01", "TCGA-A1-A0SH-01"), Arrays.asList(testGeneset));
            assertEquals(2, genesetData.size());
            assertEquals(-0.293861251463613, Double.parseDouble(genesetData.get(0).getValue()), 0.00001);
            assertEquals(-0.0811115513543749, Double.parseDouble(genesetData.get(1).getValue()), 0.00001);

            //===== check study status
            assertEquals(DaoCancerStudy.Status.AVAILABLE, DaoCancerStudy.getStatus("study_es_0"));
            
        }
        catch (Throwable t) {
            ConsoleUtil.showWarnings();
            System.err.println ("\nABORTED! " + t.toString());
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

        //read the respective genes.json and genesaliases.json files from the system test study_es_0 to 
        //load the genes and genes aliases into the DB for which this scenario is written:
        
        Map<Integer, Set<String>> aliasesMap = new HashMap<Integer, Set<String>>();
        InputStream inputStream = new FileInputStream("src/test/scripts/test_data/api_json_system_tests/genesaliases.json");
        //parse json file:
        ObjectMapper mapper = new ObjectMapper();
        TestGeneAlias[] genesAliases = mapper.readValue(inputStream, TestGeneAlias[].class);

        //build up aliases map:
        for (TestGeneAlias testGeneAlias: genesAliases) {
            Set<String> aliases = aliasesMap.get(testGeneAlias.entrezGeneId);
            if (aliases == null) {
                aliases = new HashSet<String>();
                aliasesMap.put(testGeneAlias.entrezGeneId, aliases);
            }
            aliases.add(testGeneAlias.geneAlias);
        }
        
        inputStream = new FileInputStream("src/test/scripts/test_data/api_json_system_tests/genes.json");
        //parse json file:
        mapper = new ObjectMapper();
        TestGene[] genes = mapper.readValue(inputStream, TestGene[].class);

        //add genes to db:
        for (TestGene testGene: genes) {
            CanonicalGene gene = new CanonicalGene(testGene.entrezGeneId, testGene.hugoGeneSymbol);
            //get aliases from map:
            gene.setAliases(aliasesMap.get(testGene.entrezGeneId));
            daoGene.addGene(gene);
        }

        MySQLbulkLoader.flushAll();
        
    }

    /**
     * Loads a gene panel used by this test.
     * 
     */
    private void loadGenePanel() throws Exception {
        ImportGenePanel gp = new ImportGenePanel(null);
        gp.setFile(new File("src/test/scripts/test_data/study_es_0/gene_panel_example.txt"));
        gp.importData();
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestGene {
        @JsonProperty("hugo_gene_symbol")
        String hugoGeneSymbol;
        @JsonProperty("entrez_gene_id")
        int entrezGeneId;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestGeneAlias {
        @JsonProperty("gene_alias")
        String geneAlias;
        @JsonProperty("entrez_gene_id")
        int entrezGeneId;
    } 

}
