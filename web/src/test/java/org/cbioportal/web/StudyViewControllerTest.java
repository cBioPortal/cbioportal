package org.cbioportal.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
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
    private static final String TEST_SAMPLE_ID_3 = "test_sample_id_3";
    private static final String TEST_PATIENT_ID_1 = "test_patient_id_1";
    private static final String TEST_PATIENT_ID_2 = "test_patient_id_2";
    private static final String TEST_ATTRIBUTE_ID = "test_attribute_id";
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
    @Autowired
    private SampleService sampleService;
    @Autowired
    private GenePanelService genePanelService;
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
        Mockito.reset(sampleService);
        Mockito.reset(genePanelService);
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
        clinicalDataCount1.setAttributeId(TEST_ATTRIBUTE_ID);
        clinicalDataCount1.setValue(TEST_CLINICAL_DATA_VALUE_1);
        clinicalDataCount1.setCount(3);
        clinicalDataCounts1.add(clinicalDataCount1);
        ClinicalDataCount clinicalDataCount2 = new ClinicalDataCount();
        clinicalDataCount2.setAttributeId(TEST_ATTRIBUTE_ID);
        clinicalDataCount2.setValue(TEST_CLINICAL_DATA_VALUE_2);
        clinicalDataCount2.setCount(1);
        clinicalDataCounts1.add(clinicalDataCount2);
        clinicalDataCountMap.put(TEST_ATTRIBUTE_ID, clinicalDataCounts1);
        
        Mockito.when(clinicalDataService.fetchClinicalDataCounts(Mockito.anyString(), Mockito.anyListOf(String.class), 
            Mockito.anyListOf(String.class), Mockito.anyString())).thenReturn(clinicalDataCountMap);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/studies/test_study_id/attributes/test_attribute_id/clinical-data-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new StudyViewFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_CLINICAL_DATA_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_CLINICAL_DATA_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(1));
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

        List<Sample> filteredSamples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setStableId(TEST_SAMPLE_ID_1);
        sample1.setPatientStableId(TEST_PATIENT_ID_1);
        sample1.setCancerStudyIdentifier(TEST_STUDY_ID);
        filteredSamples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setStableId(TEST_SAMPLE_ID_2);
        sample2.setPatientStableId(TEST_PATIENT_ID_2);
        sample2.setCancerStudyIdentifier(TEST_STUDY_ID);
        filteredSamples.add(sample2);

        Mockito.when(sampleService.fetchSamples(Mockito.anyListOf(String.class), Mockito.anyListOf(String.class), 
            Mockito.anyString())).thenReturn(filteredSamples);
    
        mockMvc.perform(MockMvcRequestBuilders.post("/studies/test_study_id/samples/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new StudyViewFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_STUDY_ID));
    }

    @Test
    public void fetchSampleCounts() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        List<String> filteredSampleIds = new ArrayList<>();
        filteredSampleIds.add(TEST_SAMPLE_ID_1);
        filteredSampleIds.add(TEST_SAMPLE_ID_2);
        filteredSampleIds.add(TEST_SAMPLE_ID_3);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyString(), Mockito.anyObject())).thenReturn(filteredSampleIds);

        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData1 = new GenePanelData();
        genePanelData1.setProfiled(true);
        genePanelDataList.add(genePanelData1);
        GenePanelData genePanelData2 = new GenePanelData();
        genePanelData2.setProfiled(true);
        genePanelDataList.add(genePanelData2);
        GenePanelData genePanelData3 = new GenePanelData();
        genePanelData3.setProfiled(false);
        genePanelDataList.add(genePanelData3);

        Mockito.when(genePanelService.fetchGenePanelData(Mockito.anyString(), Mockito.anyListOf(String.class)))
            .thenReturn(genePanelDataList);

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profiles/test_molecular_profile_id/sample-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new StudyViewFilter())))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfProfiledSamples").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfUnprofiledSamples").value(1));
    }
}
