package org.cbioportal.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class StudyViewControllerTest {

    private static final String TEST_STUDY_ID = "test_study_id";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final String TEST_ATTRIBUTE_ID_1 = "test_attribute_1";
    private static final String TEST_ATTRIBUTE_ID_2 = "test_attribute_2";
    private static final String TEST_CLINICAL_DATA_VALUE_1 = "value1";
    private static final String TEST_CLINICAL_DATA_VALUE_2 = "value2";
    private static final String TEST_CLINICAL_DATA_VALUE_3 = "value3";
    private static final String TEST_CLINICAL_DATA_VALUE_4 = "NA";
    private static final Integer TEST_ENTREZ_GENE_ID_1 = 1;
    private static final Integer TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public StudyViewFilterApplier studyViewFilterApplier() {
        return Mockito.mock(StudyViewFilterApplier.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(studyViewFilterApplier);
        Mockito.reset(clinicalDataService);
        Mockito.reset(mutationService);
        Mockito.reset(molecularProfileService);
        Mockito.reset(discreteCopyNumberService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchClinicalDataCounts() throws Exception {

        List<String> filteredSampleIds = new ArrayList<>();
        filteredSampleIds.add(TEST_SAMPLE_ID_1);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyString(), Mockito.anyObject())).thenReturn(filteredSampleIds);

        Map<String, List<ClinicalDataCount>> clinicalDataCountMap = new HashMap<>();
        List<ClinicalDataCount> clinicalDataCounts1 = new ArrayList<>();
        ClinicalDataCount clinicalDataCount1 = new ClinicalDataCount();
        clinicalDataCount1.setAttributeId(TEST_ATTRIBUTE_ID_1);
        clinicalDataCount1.setValue(TEST_CLINICAL_DATA_VALUE_1);
        clinicalDataCount1.setCount(3);
        clinicalDataCounts1.add(clinicalDataCount1);
        ClinicalDataCount clinicalDataCount2 = new ClinicalDataCount();
        clinicalDataCount2.setAttributeId(TEST_ATTRIBUTE_ID_1);
        clinicalDataCount2.setValue(TEST_CLINICAL_DATA_VALUE_2);
        clinicalDataCount2.setCount(1);
        clinicalDataCounts1.add(clinicalDataCount2);
        clinicalDataCountMap.put(TEST_ATTRIBUTE_ID_1, clinicalDataCounts1);
        List<ClinicalDataCount> clinicalDataCounts2 = new ArrayList<>();
        ClinicalDataCount clinicalDataCount3 = new ClinicalDataCount();
        clinicalDataCount3.setAttributeId(TEST_ATTRIBUTE_ID_2);
        clinicalDataCount3.setValue(TEST_CLINICAL_DATA_VALUE_3);
        clinicalDataCount3.setCount(2);
        clinicalDataCounts2.add(clinicalDataCount3);
        ClinicalDataCount clinicalDataCount4 = new ClinicalDataCount();
        clinicalDataCount4.setAttributeId(TEST_ATTRIBUTE_ID_2);
        clinicalDataCount4.setValue(TEST_CLINICAL_DATA_VALUE_4);
        clinicalDataCount4.setCount(2);
        clinicalDataCounts2.add(clinicalDataCount4);
        clinicalDataCountMap.put(TEST_ATTRIBUTE_ID_2, clinicalDataCounts2);
        
        Mockito.when(clinicalDataService.fetchClinicalDataCounts(Mockito.anyString(), Mockito.anyListOf(String.class), 
            Mockito.anyListOf(String.class), Mockito.anyString())).thenReturn(clinicalDataCountMap);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/studies/test_study_id/clinical-data-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ClinicalDataCountFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_1[0].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_1[0].value").value(TEST_CLINICAL_DATA_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_1[0].count").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_1[1].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_1[1].value").value(TEST_CLINICAL_DATA_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_1[1].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_2[0].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_2[0].value").value(TEST_CLINICAL_DATA_VALUE_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_2[0].count").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_2[1].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_2[1].value").value(TEST_CLINICAL_DATA_VALUE_4))
            .andExpect(MockMvcResultMatchers.jsonPath("$.test_attribute_2[1].count").value(2));
    }

    @Test
    public void fetchMutatedGenes() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        List<String> filteredSampleIds = new ArrayList<>();
        filteredSampleIds.add(TEST_SAMPLE_ID_1);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyString(), Mockito.anyObject())).thenReturn(filteredSampleIds);

        List<MutationCountByGene> mutationCounts = new ArrayList<>();
        MutationCountByGene mutationCount1 = new MutationCountByGene();
        mutationCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        mutationCount1.setCountByEntity(1);
        mutationCount1.setTotalCount(3);
        mutationCounts.add(mutationCount1);
        MutationCountByGene mutationCount2 = new MutationCountByGene();
        mutationCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        mutationCount2.setCountByEntity(2);
        mutationCount2.setTotalCount(2);
        mutationCounts.add(mutationCount2);

        Mockito.when(mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(Mockito.anyString(), 
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyBoolean())).thenReturn(mutationCounts);

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profiles/test_molecular_profile_id/mutated-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new StudyViewFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].countByEntity").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].countByEntity").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").value(3));
    }

    @Test
    public void fetchCNAGenes() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        List<String> filteredSampleIds = new ArrayList<>();
        filteredSampleIds.add(TEST_SAMPLE_ID_1);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyString(), Mockito.anyObject())).thenReturn(filteredSampleIds);

        List<CopyNumberCountByGene> cnaCounts = new ArrayList<>();
        CopyNumberCountByGene cnaCount1 = new CopyNumberCountByGene();
        cnaCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        cnaCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        cnaCount1.setCountByEntity(1);
        cnaCount1.setCytoband(TEST_CYTOBAND_1);
        cnaCount1.setAlteration(-2);
        cnaCounts.add(cnaCount1);
        CopyNumberCountByGene cnaCount2 = new CopyNumberCountByGene();
        cnaCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        cnaCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        cnaCount2.setCountByEntity(2);
        cnaCount2.setCytoband(TEST_CYTOBAND_2);
        cnaCount2.setAlteration(2);
        cnaCounts.add(cnaCount2);

        Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(Mockito.anyString(), 
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyListOf(Integer.class), 
            Mockito.anyBoolean())).thenReturn(cnaCounts);

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profiles/test_molecular_profile_id/cna-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new StudyViewFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].countByEntity").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].countByEntity").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(-2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").doesNotExist());
    }

    @Test
    public void fetchSampleIds() throws Exception {
    
        List<String> filteredSampleIds = new ArrayList<>();
        filteredSampleIds.add(TEST_SAMPLE_ID_1);
        filteredSampleIds.add(TEST_SAMPLE_ID_2);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyString(), Mockito.anyObject())).thenReturn(filteredSampleIds);
    
        mockMvc.perform(MockMvcRequestBuilders.post("/studies/test_study_id/sample-ids/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new StudyViewFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0]").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1]").value(TEST_SAMPLE_ID_2));
    }
}
