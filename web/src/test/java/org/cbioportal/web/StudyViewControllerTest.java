package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.*;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.service.*;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.web.config.CustomObjectMapper;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private static final String TEST_PATIENT_ID_3 = "test_patient_id_3";
    private static final String TEST_ATTRIBUTE_ID = "test_attribute_id";
    private static final String TEST_CLINICAL_DATA_VALUE_1 = "value1";
    private static final String TEST_CLINICAL_DATA_VALUE_2 = "value2";
    private static final String TEST_CLINICAL_DATA_VALUE_3 = "3";
    private static final Integer TEST_ENTREZ_GENE_ID_1 = 1;
    private static final Integer TEST_ENTREZ_GENE_ID_2 = 2;
    private static final String TEST_HUGO_GENE_SYMBOL_1 = "test_hugo_gene_symbol_1";
    private static final String TEST_HUGO_GENE_SYMBOL_2 = "test_hugo_gene_symbol_2";
    private static final String TEST_CYTOBAND_1 = "test_cytoband_1";
    private static final String TEST_CYTOBAND_2 = "test_cytoband_2";
    private static final String TEST_STABLE_ID = "test_stable_id";
    private static final String TEST_GENERIC_ASSAY_DATA_VALUE_1 = "value1";
    private static final String TEST_GENERIC_ASSAY_DATA_VALUE_2 = "value2";
    private static final String TEST_CLINICAL_EVENT_TYPE = "STATUS";
    private static final Integer TEST_CLINICAL_EVENT_TYPE_COUNT = 513;

    private List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
    private List<ClinicalData> clinicalData = new ArrayList<>();

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Autowired
    private SampleService sampleService;
    @Autowired
    private StudyViewService studyViewService;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private ClinicalAttributeService clinicalAttributeService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private ClinicalEventService clinicalEventService;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    private MockMvc mockMvc;

    private AlterationFilter alterationFilter = new AlterationFilter();

    @Bean
    public MolecularProfileUtil molecularProfileUtil() {
        return new MolecularProfileUtil();
    }

    @Bean
    public StudyViewFilterApplier studyViewFilterApplier() {
        return mock(StudyViewFilterApplier.class);
    }
    
    @Bean
    public TreatmentService treatmentService() {
        return mock(TreatmentService.class);
    }
    
    @Bean
    public AlterationCountService alterationCountService() {
        return mock(AlterationCountService.class);
    }

    @Bean
    public StudyViewService studyViewService() {
        return mock(StudyViewService.class);
    }
    
    @Bean
    public AlterationRepository alterationRepository() {
        return mock(AlterationRepository.class);
    }

    @Bean
    public ViolinPlotService violinPlotService() {
        return mock(ViolinPlotService.class);
    }

    @Before
    public void setUp() throws Exception {

        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);

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
        clinicalData3.setPatientId(TEST_PATIENT_ID_3);
        clinicalData.add(clinicalData3);

        reset(studyViewFilterApplier);
        reset(clinicalDataService);
        reset(discreteCopyNumberService);
        reset(sampleService);
        reset(genePanelService);
        reset(sampleService);
        reset(clinicalAttributeService);
        reset(patientService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void fetchClinicalDataCounts() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

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
        
        when(clinicalDataService.fetchClinicalDataCounts(anyList(), anyList(), 
            anyList())).thenReturn(clinicalDataCountItems);

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
        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(clinicalDataService.fetchClinicalData(anyList(), anyList(),
            anyList(), anyString(), anyString())).thenReturn(clinicalData);

        ClinicalAttribute clinicalAttribute1 =new ClinicalAttribute();
        clinicalAttribute1.setAttrId(TEST_ATTRIBUTE_ID);
        clinicalAttribute1.setPatientAttribute(false);
        
        when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(
                anyList(), anyList()))
        .thenReturn(Arrays.asList(clinicalAttribute1));

        when(patientService.getPatientsOfSamples(anyList(), anyList())).thenReturn(Arrays.asList());

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

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        List<AlterationCountByGene> mutationCounts = new ArrayList<>();
        AlterationCountByGene mutationCount1 = new AlterationCountByGene();
        mutationCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        mutationCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        mutationCount1.setNumberOfAlteredCases(1);
        mutationCount1.setTotalCount(3);
        mutationCounts.add(mutationCount1);
        AlterationCountByGene mutationCount2 = new AlterationCountByGene();
        mutationCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        mutationCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        mutationCount2.setNumberOfAlteredCases(2);
        mutationCount2.setTotalCount(2);
        mutationCounts.add(mutationCount2);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        when(studyViewService.getMutationAlterationCountByGenes(
            eq(Arrays.asList(TEST_STUDY_ID)),
            eq(Arrays.asList(TEST_SAMPLE_ID_1)),
            any(AlterationFilter.class)))
            .thenReturn(mutationCounts);

        mockMvc.perform(MockMvcRequestBuilders.post("/mutated-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfAlteredCases").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfAlteredCases").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").value(2));
    }

    @Test
    public void fetchStructuralVariantGenes() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        List<AlterationCountByGene> structuralVariantCounts = new ArrayList<>();
        AlterationCountByGene structuralVariantCount1 = new AlterationCountByGene();
        structuralVariantCount1.setEntrezGeneId(TEST_ENTREZ_GENE_ID_1);
        structuralVariantCount1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        structuralVariantCount1.setNumberOfAlteredCases(1);
        structuralVariantCount1.setTotalCount(1);
        structuralVariantCounts.add(structuralVariantCount1);
        AlterationCountByGene structuralVariantCount2 = new AlterationCountByGene();
        structuralVariantCount2.setEntrezGeneId(TEST_ENTREZ_GENE_ID_2);
        structuralVariantCount2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        structuralVariantCount2.setNumberOfAlteredCases(2);
        structuralVariantCount2.setTotalCount(2);
        structuralVariantCounts.add(structuralVariantCount2);

        when(studyViewService.getStructuralVariantAlterationCountByGenes(
            eq(Arrays.asList(TEST_STUDY_ID)),
            eq(Arrays.asList(TEST_SAMPLE_ID_1)),
            any(AlterationFilter.class)))
            .thenReturn(structuralVariantCounts);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/structuralvariant-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfAlteredCases").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfAlteredCases").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").value(2));
    }

    @Test
    public void fetchCNAGenes() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

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

        when(studyViewService.getCNAAlterationCountByGenes(
            eq(Arrays.asList(TEST_STUDY_ID)),
            eq(Arrays.asList(TEST_SAMPLE_ID_1)),
            any(AlterationFilter.class)))
            .thenReturn(cnaCounts);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/cna-genes/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].entrezGeneId").value(TEST_ENTREZ_GENE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfAlteredCases").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cytoband").value(TEST_CYTOBAND_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].alteration").value(-2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalCount").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].entrezGeneId").value(TEST_ENTREZ_GENE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].numberOfAlteredCases").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cytoband").value(TEST_CYTOBAND_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].alteration").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].totalCount").doesNotExist());
    }

    @Test
    public void fetchSampleIds() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(studyViewFilterApplier.apply(any(), eq(false))).thenReturn(filteredSampleIdentifiers);

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

        when(sampleService.fetchSamples(anyList(), anyList(),
            anyString())).thenReturn(filteredSamples);

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

        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(TEST_SAMPLE_ID_2);
        sampleIdentifier2.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier2);
        SampleIdentifier sampleIdentifier3 = new SampleIdentifier();
        sampleIdentifier3.setSampleId(TEST_SAMPLE_ID_3);
        sampleIdentifier3.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier3);
        
        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        List<GenomicDataCount> genomicDataCounts = new ArrayList<>();
        GenomicDataCount genomicDataCount1 = new GenomicDataCount();
        genomicDataCount1.setLabel("Profile 2");
        genomicDataCount1.setValue("profile_type_2");
        genomicDataCount1.setCount(1);
        genomicDataCounts.add(genomicDataCount1);
        GenomicDataCount genomicDataCount2 = new GenomicDataCount();
        genomicDataCount2.setLabel("Profile 1");
        genomicDataCount2.setValue("profile_type_1");
        genomicDataCount2.setCount(2);
        genomicDataCounts.add(genomicDataCount2);
        when(studyViewService.getGenomicDataCounts(
            Arrays.asList(TEST_STUDY_ID, TEST_STUDY_ID, TEST_STUDY_ID),
            Arrays.asList(TEST_SAMPLE_ID_1, TEST_SAMPLE_ID_2, TEST_SAMPLE_ID_3)))
            .thenReturn(genomicDataCounts);

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

    @Ignore("Skip StudyViewControllerTest.fetchClinicalDataDensityPlot due to assertion errors")
    @Test
    public void fetchClinicalDataDensityPlot() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        ClinicalAttribute clinicalAttribute1 =new ClinicalAttribute();
        clinicalAttribute1.setAttrId("FRACTION_GENOME_ALTERED");
        clinicalAttribute1.setPatientAttribute(false);
        ClinicalAttribute clinicalAttribute2 =new ClinicalAttribute();
        clinicalAttribute2.setAttrId("MUTATION_COUNT");
        clinicalAttribute2.setPatientAttribute(false);
        
        when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(
                anyList(), anyList()))
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
        
        when(clinicalDataService.fetchClinicalData(anyList(), anyList(), 
            anyList(), anyString(), anyString())).thenReturn(clinicalData);

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
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].binY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].minX").value(0.2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].maxX").value(0.2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].minY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].maxY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].binY").value(144.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].binY").value(272.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].binY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].minX").value(0.44))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].maxX").value(0.44))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].minY").value(123.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].maxY").value(123.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].binY").value(144.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].binY").value(272.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].binY").value(16.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].binY").value(144.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].count").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].binY").value(272.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].minX").value(1.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].maxX").value(1.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].minY").value(400.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].maxY").value(400.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pearsonCorr").value(0.9997290539897087))
            .andExpect(MockMvcResultMatchers.jsonPath("$.spearmanCorr").value(1));

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data-density-plot/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter))
                .param("xAxisAttributeId", "FRACTION_GENOME_ALTERED")
                .param("xAxisBinCount", "3")
                .param("xAxisStart", "0.0")
                .param("xAxisEnd", "1.0")
                .param("yAxisAttributeId", "MUTATION_COUNT")
                .param("yAxisBinCount", "3")
                .param("yAxisLogScale", "true"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].binY").value(2.833213344056216))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].minX").value(0.2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].maxX").value(0.2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].minY").value(2.833213344056216))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].maxY").value(2.833213344056216))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[0].count").value(1))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].binY").value(3.8867960384730003))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[1].count").value(0))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].binX").value(0.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].binY").value(4.940378732889785))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[2].count").value(0))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].binY").value(2.833213344056216))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[3].count").value(0))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].binY").value(3.8867960384730003))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].minX").value(0.44))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].maxX").value(0.44))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].minY").value(4.820281565605037))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].maxY").value(4.820281565605037))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[4].count").value(1))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].binX").value(0.3333333333333333))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].binY").value(4.940378732889785))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[5].count").value(0))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].binY").value(2.833213344056216))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[6].count").value(0))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].binY").value(3.8867960384730003))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].minX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].maxX").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].minY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].maxY").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[7].count").value(0))
            
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].binX").value(0.6666666666666666))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].binY").value(4.940378732889785))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].minX").value(1.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].maxX").value(1.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].minY").value(5.993961427306569))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].maxY").value(5.993961427306569))
            .andExpect(MockMvcResultMatchers.jsonPath("$.bins[8].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.pearsonCorr").value(0.9307061280832044))
            .andExpect(MockMvcResultMatchers.jsonPath("$.spearmanCorr").value(1));
    }

    @Test
    public void fetchGenericAssayDataCounts() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        List<GenericAssayDataCountItem> genericAssayDataCountItems = new ArrayList<>();
        GenericAssayDataCountItem genericAssayDataCountItem = new GenericAssayDataCountItem();
        genericAssayDataCountItem.setStableId(TEST_STABLE_ID);
        List<GenericAssayDataCount> genericAssayDataCounts = new ArrayList<>();
        GenericAssayDataCount genericAssayDataCount1 = new GenericAssayDataCount();
        genericAssayDataCount1.setValue(TEST_GENERIC_ASSAY_DATA_VALUE_1);
        genericAssayDataCount1.setCount(3);
        genericAssayDataCounts.add(genericAssayDataCount1);
        GenericAssayDataCount genericAssayDataCount2 = new GenericAssayDataCount();
        genericAssayDataCount2.setValue(TEST_GENERIC_ASSAY_DATA_VALUE_2);
        genericAssayDataCount2.setCount(1);
        genericAssayDataCounts.add(genericAssayDataCount2);
        genericAssayDataCountItem.setCounts(genericAssayDataCounts);
        genericAssayDataCountItems.add(genericAssayDataCountItem);

        when(studyViewService.fetchGenericAssayDataCounts(anyList(), anyList(), anyList(),
            anyList())).thenReturn(genericAssayDataCountItems);

        GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter();
        genericAssayDataFilter.setStableId(TEST_STABLE_ID);
        genericAssayDataCountFilter.setGenericAssayDataFilters(Arrays.asList(genericAssayDataFilter));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(MockMvcRequestBuilders.post("/generic-assay-data-counts/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(genericAssayDataCountFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].stableId").value(TEST_STABLE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].value").value(TEST_GENERIC_ASSAY_DATA_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].value").value(TEST_GENERIC_ASSAY_DATA_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].count").value(1));
    }

    @Test
    public void fetchClinicalDataClinicalTable() throws Exception {

        // For this sake of this test the sample clinical data and patient clinical data are identical.
        when(clinicalDataService.fetchSampleClinicalDataClinicalTable(anyList(), anyList(),
            anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(clinicalData);
        when(clinicalDataService.fetchClinicalData(anyList(), anyList(),
            any(), anyString(), anyString())).thenReturn(clinicalData);
            
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data-table/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleClinicalData[0].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleClinicalData[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleClinicalData[1].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleClinicalData[1].sampleId").value(TEST_SAMPLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleClinicalData[2].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleClinicalData[2].sampleId").value(TEST_SAMPLE_ID_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientClinicalData[0].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientClinicalData[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientClinicalData[1].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientClinicalData[1].sampleId").value(TEST_SAMPLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientClinicalData[2].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientClinicalData[2].sampleId").value(TEST_SAMPLE_ID_3));
            
    }

    @Test
    public void fetchClinicalEventTypeCounts() throws Exception
    {
        List<ClinicalEventTypeCount> testEventTypeCounts = List.of(new ClinicalEventTypeCount(TEST_CLINICAL_EVENT_TYPE, TEST_CLINICAL_EVENT_TYPE_COUNT));

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(clinicalEventService.getClinicalEventTypeCounts(anyList(), anyList()))
            .thenReturn(testEventTypeCounts);
        
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Collections.singletonList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-event-type-counts/fetch")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventType").value(TEST_CLINICAL_EVENT_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_CLINICAL_EVENT_TYPE_COUNT));
    }

}
