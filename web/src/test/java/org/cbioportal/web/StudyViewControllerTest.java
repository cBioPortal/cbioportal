package org.cbioportal.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
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
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class StudyViewControllerTest {

    private static final String TEST_STUDY_ID = "test_study_id";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final String TEST_SAMPLE_ID_3 = "test_sample_id_3";
    private static final String TEST_PATIENT_ID_1 = "test_patient_id_1";
    private static final String TEST_PATIENT_ID_2 = "test_patient_id_2";
    private static final String TEST_ATTRIBUTE_ID = "test_attribute_id";
    private static final String TEST_MOLEULAR_PROFILE_ID_1 = "test_study_id_profile_type_1";
    private static final String TEST_MOLEULAR_PROFILE_ID_2 = "test_study_id_profile_type_2";
    private static final String TEST_CLINICAL_DATA_VALUE_1 = "value1";
    private static final String TEST_CLINICAL_DATA_VALUE_2 = "value2";
    private static final String TEST_CLINICAL_DATA_VALUE_3 = "3";
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
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private PatientService patientService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

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
        Mockito.reset(clinicalAttributeService);
        Mockito.reset(patientService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchClinicalDataCounts() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        List<ClinicalDataCountItem> clinicalDataCountItems = new ArrayList<>();
        ClinicalDataCountItem clinicalDataCountItem = new ClinicalDataCountItem();
        clinicalDataCountItem.setAttributeId(TEST_ATTRIBUTE_ID);
        List<ClinicalDataCount> clinicalDataCounts = new ArrayList<>();
        ClinicalDataCount clinicalDataCount1 = new ClinicalDataCount();
        clinicalDataCount1.setAttributeId(TEST_ATTRIBUTE_ID);
        clinicalDataCount1.setValue(TEST_CLINICAL_DATA_VALUE_1);
        clinicalDataCount1.setCount(3);
        clinicalDataCounts.add(clinicalDataCount1);
        ClinicalDataCount clinicalDataCount2 = new ClinicalDataCount();
        clinicalDataCount2.setAttributeId(TEST_ATTRIBUTE_ID);
        clinicalDataCount2.setValue(TEST_CLINICAL_DATA_VALUE_2);
        clinicalDataCount2.setCount(1);
        clinicalDataCounts.add(clinicalDataCount2);
        clinicalDataCountItem.setCounts(clinicalDataCounts);
        clinicalDataCountItems.add(clinicalDataCountItem);
        
        Mockito.when(clinicalDataService.fetchClinicalDataCounts(Mockito.anyListOf(String.class), Mockito.anyListOf(String.class), 
            Mockito.anyListOf(String.class))).thenReturn(clinicalDataCountItems);

        ClinicalDataCountFilter clinicalDataCountFilter = new ClinicalDataCountFilter();
        ClinicalDataFilter clinicalDataFilter = new ClinicalDataFilter();
        clinicalDataFilter.setAttributeId(TEST_ATTRIBUTE_ID);
        clinicalDataCountFilter.setAttributes(Arrays.asList(clinicalDataFilter));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        clinicalDataCountFilter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clinicalDataCountFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].value").value(TEST_CLINICAL_DATA_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].attributeId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].value").value(TEST_CLINICAL_DATA_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].count").value(1));
    }

    @Test
    public void fetchClinicalDataBinCounts() throws Exception
    {
        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        List<ClinicalData> clinicalData = new ArrayList<>();
        ClinicalData clinicalData1 = new ClinicalData();
        clinicalData1.setAttrId(TEST_ATTRIBUTE_ID);
        clinicalData1.setAttrValue(TEST_CLINICAL_DATA_VALUE_1);
        clinicalData1.setStudyId(TEST_STUDY_ID);
        clinicalData1.setSampleId(TEST_SAMPLE_ID_1);
        clinicalData1.setPatientId(TEST_PATIENT_ID_1);
        clinicalData.add(clinicalData1);
        ClinicalData clinicalData2 = new ClinicalData();
        clinicalData2.setAttrId(TEST_ATTRIBUTE_ID);
        clinicalData2.setAttrValue(TEST_CLINICAL_DATA_VALUE_2);
        clinicalData2.setStudyId(TEST_STUDY_ID);
        clinicalData2.setSampleId(TEST_SAMPLE_ID_2);
        clinicalData2.setPatientId(TEST_PATIENT_ID_2);
        clinicalData.add(clinicalData2);
        ClinicalData clinicalData3 = new ClinicalData();
        clinicalData3.setAttrId(TEST_ATTRIBUTE_ID);
        clinicalData3.setAttrValue(TEST_CLINICAL_DATA_VALUE_3);
        clinicalData3.setStudyId(TEST_STUDY_ID);
        clinicalData3.setSampleId(TEST_SAMPLE_ID_3);
        clinicalData.add(clinicalData3);

        Mockito.when(clinicalDataService.fetchClinicalData(Mockito.anyListOf(String.class), Mockito.anyListOf(String.class),
            Mockito.anyListOf(String.class), Mockito.any(String.class), Mockito.any(String.class))).thenReturn(clinicalData);

        ClinicalAttribute clinicalAttribute1 =new ClinicalAttribute();
        clinicalAttribute1.setAttrId(TEST_ATTRIBUTE_ID);
        clinicalAttribute1.setPatientAttribute(false);
        
        Mockito.when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(
                Mockito.anyList(), Mockito.anyList()))
        .thenReturn(Arrays.asList(clinicalAttribute1));

        Mockito.when(patientService.getPatientsOfSamples(Mockito.anyListOf(String.class), Mockito.anyListOf(String.class))).thenReturn(Arrays.asList());

        ClinicalDataBinCountFilter clinicalDataBinCountFilter = new ClinicalDataBinCountFilter();
        ClinicalDataBinFilter clinicalDataBinFilter = new ClinicalDataBinFilter();
        clinicalDataBinFilter.setAttributeId(TEST_ATTRIBUTE_ID);
        clinicalDataBinFilter.setDisableLogScale(false);
        clinicalDataBinCountFilter.setAttributes(Collections.singletonList(clinicalDataBinFilter));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Collections.singletonList(TEST_STUDY_ID));
        clinicalDataBinCountFilter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data-bin-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clinicalDataBinCountFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].specialValue").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].start").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].end").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].specialValue").value(TEST_CLINICAL_DATA_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].start").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].end").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].attributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].specialValue").value(TEST_CLINICAL_DATA_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].start").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].end").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].count").value(1));
    }

    @Test
    public void fetchMutatedGenes() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        List<MutationCountByGene> mutationCounts = new ArrayList<>();
        MutationCountByGene mutationCount1 = new MutationCountByGene();
        mutationCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        mutationCount1.setNumberOfAlteredCases(1);
        mutationCount1.setTotalCount(3);
        mutationCounts.add(mutationCount1);
        MutationCountByGene mutationCount2 = new MutationCountByGene();
        mutationCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        mutationCount2.setNumberOfAlteredCases(2);
        mutationCount2.setTotalCount(2);
        mutationCounts.add(mutationCount2);

        Mockito.when(mutationService.getSampleCountInMultipleMolecularProfiles(Mockito.anyListOf(String.class),
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(mutationCounts);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/mutated-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfAlteredCases").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfAlteredCases").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").value(3));
    }

    @Test
    public void fetchFusionGenes() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        List<MutationCountByGene> fusionCounts = new ArrayList<>();
        MutationCountByGene fusionCount1 = new MutationCountByGene();
        fusionCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        fusionCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        fusionCount1.setNumberOfAlteredCases(1);
        fusionCount1.setTotalCount(1);
        fusionCounts.add(fusionCount1);
        MutationCountByGene fusionCount2 = new MutationCountByGene();
        fusionCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        fusionCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        fusionCount2.setNumberOfAlteredCases(2);
        fusionCount2.setTotalCount(2);
        fusionCounts.add(fusionCount2);

        Mockito.when(mutationService.getSampleCountInMultipleMolecularProfilesForFusions(Mockito.anyListOf(String.class),
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(fusionCounts);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/fusion-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfAlteredCases").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfAlteredCases").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").value(1));
    }

    @Test
    public void fetchCNAGenes() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(TEST_STUDY_ID);
        Mockito.when(molecularProfileService.getMolecularProfile(Mockito.anyString())).thenReturn(molecularProfile);

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        List<CopyNumberCountByGene> cnaCounts = new ArrayList<>();
        CopyNumberCountByGene cnaCount1 = new CopyNumberCountByGene();
        cnaCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        cnaCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        cnaCount1.setNumberOfAlteredCases(1);
        cnaCount1.setCytoband(TEST_CYTOBAND_1);
        cnaCount1.setAlteration(-2);
        cnaCounts.add(cnaCount1);
        CopyNumberCountByGene cnaCount2 = new CopyNumberCountByGene();
        cnaCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        cnaCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        cnaCount2.setNumberOfAlteredCases(2);
        cnaCount2.setCytoband(TEST_CYTOBAND_2);
        cnaCount2.setAlteration(2);
        cnaCounts.add(cnaCount2);

        Mockito.when(discreteCopyNumberService.getSampleCountInMultipleMolecularProfiles(Mockito.anyListOf(String.class),
            Mockito.anyListOf(String.class), Mockito.anyListOf(Integer.class), Mockito.anyListOf(Integer.class), Mockito.anyBoolean(), Mockito.anyBoolean()))
            .thenReturn(cnaCounts);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/cna-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfAlteredCases").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfAlteredCases").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(-2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").doesNotExist());
    }

    @Test
    public void fetchSampleIds() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);

        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject(), Mockito.eq(false))).thenReturn(filteredSampleIdentifiers);

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

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/filtered-samples/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
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

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier1.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(TEST_SAMPLE_ID_2);
        sampleIdentifier2.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier2);
        SampleIdentifier sampleIdentifier3 = new SampleIdentifier();
        sampleIdentifier3.setSampleId(TEST_SAMPLE_ID_3);
        sampleIdentifier3.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier3);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        MolecularProfile molecularProfile1 = new MolecularProfile();
        molecularProfile1.setCancerStudyIdentifier(TEST_STUDY_ID);
        molecularProfile1.setStableId(TEST_MOLEULAR_PROFILE_ID_1);
        molecularProfile1.setName("Profile 1");

        MolecularProfile molecularProfile2 = new MolecularProfile();
        molecularProfile2.setCancerStudyIdentifier(TEST_STUDY_ID);
        molecularProfile2.setStableId(TEST_MOLEULAR_PROFILE_ID_2);
        molecularProfile2.setName("Profile 2");

        Mockito.when(molecularProfileService.getMolecularProfilesInStudies(Mockito.anyListOf(String.class),
                Mockito.anyString())).thenReturn(Arrays.asList(molecularProfile1, molecularProfile2));

        List<GenePanelData> genePanelDataList = new ArrayList<>();
        GenePanelData genePanelData1 = new GenePanelData();
        genePanelData1.setMolecularProfileId(TEST_MOLEULAR_PROFILE_ID_1);
        genePanelData1.setProfiled(true);
        genePanelDataList.add(genePanelData1);
        GenePanelData genePanelData2 = new GenePanelData();
        genePanelData2.setMolecularProfileId(TEST_MOLEULAR_PROFILE_ID_1);
        genePanelData2.setProfiled(true);
        genePanelDataList.add(genePanelData2);
        GenePanelData genePanelData3 = new GenePanelData();
        genePanelData3.setMolecularProfileId(TEST_MOLEULAR_PROFILE_ID_2);
        genePanelData3.setProfiled(true);
        genePanelDataList.add(genePanelData3);

        Mockito.when(genePanelService.fetchGenePanelDataInMultipleMolecularProfiles(Mockito.anyListOf(String.class),
            Mockito.anyListOf(String.class))).thenReturn(genePanelDataList);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/molecular-profile-sample-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].label").value("Profile 2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value("profile_type_2"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].label").value("Profile 1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value("profile_type_1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(2));
    }

    @Test
    public void fetchClinicalDataDensityPlot() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        Mockito.when(studyViewFilterApplier.apply(Mockito.anyObject())).thenReturn(filteredSampleIdentifiers);

        ClinicalAttribute clinicalAttribute1 =new ClinicalAttribute();
        clinicalAttribute1.setAttrId("FRACTION_GENOME_ALTERED");
        clinicalAttribute1.setPatientAttribute(false);
        ClinicalAttribute clinicalAttribute2 =new ClinicalAttribute();
        clinicalAttribute2.setAttrId("MUTATION_COUNT");
        clinicalAttribute2.setPatientAttribute(false);
        
        Mockito.when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(
                Mockito.anyList(), Mockito.anyList()))
        .thenReturn(Arrays.asList(clinicalAttribute1,clinicalAttribute2));
        
        
        List<ClinicalData> clinicalData = new ArrayList<>();
        ClinicalData clinicalData1 = new ClinicalData();
        clinicalData1.setAttrId("FRACTION_GENOME_ALTERED");
        clinicalData1.setAttrValue("0.2");
        clinicalData1.setStudyId(TEST_STUDY_ID);
        clinicalData1.setSampleId(TEST_SAMPLE_ID_1);
        clinicalData.add(clinicalData1);
        ClinicalData clinicalData2 = new ClinicalData();
        clinicalData2.setAttrId("MUTATION_COUNT");
        clinicalData2.setAttrValue("16");
        clinicalData2.setStudyId(TEST_STUDY_ID);
        clinicalData2.setSampleId(TEST_SAMPLE_ID_1);
        clinicalData.add(clinicalData2);
        ClinicalData clinicalData3 = new ClinicalData();
        clinicalData3.setAttrId("FRACTION_GENOME_ALTERED");
        clinicalData3.setAttrValue("0.44");
        clinicalData3.setStudyId(TEST_STUDY_ID);
        clinicalData3.setSampleId(TEST_SAMPLE_ID_2);
        clinicalData.add(clinicalData3);
        ClinicalData clinicalData4 = new ClinicalData();
        clinicalData4.setAttrId("MUTATION_COUNT");
        clinicalData4.setAttrValue("123");
        clinicalData4.setStudyId(TEST_STUDY_ID);
        clinicalData4.setSampleId(TEST_SAMPLE_ID_2);
        clinicalData.add(clinicalData4);
        ClinicalData clinicalData5 = new ClinicalData();
        clinicalData5.setAttrId("FRACTION_GENOME_ALTERED");
        clinicalData5.setAttrValue("1.0");
        clinicalData5.setStudyId(TEST_STUDY_ID);
        clinicalData5.setSampleId(TEST_SAMPLE_ID_3);
        clinicalData.add(clinicalData5);
        ClinicalData clinicalData6 = new ClinicalData();
        clinicalData6.setAttrId("MUTATION_COUNT");
        clinicalData6.setAttrValue("400");
        clinicalData6.setStudyId(TEST_STUDY_ID);
        clinicalData6.setSampleId(TEST_SAMPLE_ID_3);
        clinicalData.add(clinicalData6);
        
        Mockito.when(clinicalDataService.fetchClinicalData(Mockito.anyListOf(String.class), Mockito.anyListOf(String.class), 
            Mockito.anyListOf(String.class), Mockito.anyString(), Mockito.anyString())).thenReturn(clinicalData);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data-density-plot/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter))
            .param("xAxisAttributeId", "FRACTION_GENOME_ALTERED")
            .param("xAxisBinCount", "3")
            .param("xAxisStart", "0.0")
            .param("xAxisEnd", "1.0")
            .param("yAxisAttributeId", "MUTATION_COUNT")
            .param("yAxisBinCount", "3"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].binY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].minX").value(0.2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].maxX").value(0.2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].minY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].maxY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].binY").value(144.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].binY").value(272.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[2].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].binY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].minX").value(0.44))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].maxX").value(0.44))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].minY").value(123.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].maxY").value(123.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[3].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].binY").value(144.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[4].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].binY").value(272.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[5].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].binY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[6].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].binY").value(144.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[7].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].binY").value(272.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].minX").value(1.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].maxX").value(1.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].minY").value(400.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].maxY").value(400.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$[8].count").value(1));
    }
}
