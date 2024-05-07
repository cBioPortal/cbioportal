package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.GenericAssayDataCount;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleClinicalDataCollection;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StructuralVariantSpecialValue;
import org.cbioportal.model.StudyViewStructuralVariantFilter;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.service.AlterationCountService;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleListService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.TreatmentService;
import org.cbioportal.service.ViolinPlotService;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.cbioportal.utils.Encoder;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.GenericAssayDataCountFilter;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
import org.cbioportal.web.parameter.GenomicDataCountFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.ClinicalDataBinUtil;
import org.cbioportal.web.util.ClinicalDataFetcher;
import org.cbioportal.web.util.DataBinHelper;
import org.cbioportal.web.util.DataBinner;
import org.cbioportal.web.util.DiscreteDataBinner;
import org.cbioportal.web.util.LinearDataBinner;
import org.cbioportal.web.util.LogScaleDataBinner;
import org.cbioportal.web.util.ScientificSmallDataBinner;
import org.cbioportal.web.util.StudyViewFilterApplier;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
// TODO clean up dependencies for this test (use Mocks better)
// TODO Rework to accurately test StudyViewController
@ContextConfiguration(classes = {StudyViewController.class, StudyViewFilterUtil.class, MolecularProfileUtil.class, ClinicalDataBinUtil.class, DataBinner.class,
    DiscreteDataBinner.class, LinearDataBinner.class, ScientificSmallDataBinner.class, LogScaleDataBinner.class, ClinicalDataBinUtil.class,
    DataBinHelper.class, TestConfig.class})
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
    private static final String TEST_CNA_ALTERATION_NAME_1 = "test_cna_event_type_1";
    private static final String TEST_CNA_ALTERATION_NAME_2 = "test_cna_event_type_2";
    private static final String TEST_CNA_ALTERATION_VALUE_1 = "2";
    private static final String TEST_CNA_ALTERATION_VALUE_2 = "-2";
    private static final String TEST_MOLECULAR_PROFILE_TYPE = "test_molecular_profile_type";
    private static final String TEST_MUTATION_TYPE = "test_mutation_type";

    private List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
    private List<ClinicalData> clinicalData = new ArrayList<>();
    private SampleClinicalDataCollection tableClinicalData;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private StudyViewFilterApplier studyViewFilterApplier;
    @MockBean
    private ClinicalDataService clinicalDataService;
    @MockBean
    private DiscreteCopyNumberService discreteCopyNumberService;
    @MockBean
    private SampleService sampleService;
    @MockBean
    private GenePanelService genePanelService;
    @MockBean
    private ClinicalAttributeService clinicalAttributeService;
    @MockBean
    private PatientService patientService;

    @MockBean
    public MolecularProfileUtil molecularProfileUtil;

    @MockBean
    public TreatmentService treatmentService;

    @MockBean
    public AlterationCountService alterationCountService;

    @MockBean
    public StudyViewService studyViewService;

    @MockBean
    public AlterationRepository alterationRepository;

    @MockBean
    private ClinicalDataFetcher clinicalDataFetcher;

    @MockBean
    private ClinicalAttributeUtil clinicalAttributeUtil;

    @MockBean
    private SampleListService sampleListService;

    @MockBean
    private MolecularProfileService molecularProfileService;
    
    @MockBean
    private ClinicalEventService clinicalEventService;
    
    @MockBean
    private GeneService geneService;
    
    @MockBean
    private ViolinPlotService violinPlotService;
    
    @MockBean
    private ClinicalDataBinUtil clinicalDataBinUtil;
    
    @Autowired
    private MockMvc mockMvc;

    private AlterationFilter alterationFilter = new AlterationFilter();

    private ArrayList<Sample> filteredSamples = new ArrayList<>();
    
    private String uniqueKeySample1;

    @Before
    public void setUp() throws Exception {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);

        Sample sample1 = new Sample();
        sample1.setStableId(TEST_SAMPLE_ID_1);
        sample1.setPatientStableId(TEST_PATIENT_ID_1);
        sample1.setCancerStudyIdentifier(TEST_STUDY_ID);
        Sample sample2 = new Sample();
        sample2.setStableId(TEST_SAMPLE_ID_2);
        sample2.setPatientStableId(TEST_PATIENT_ID_2);
        sample2.setCancerStudyIdentifier(TEST_STUDY_ID);
        filteredSamples.add(sample1);
        filteredSamples.add(sample2);

        uniqueKeySample1 = Encoder.calculateBase64(TEST_SAMPLE_ID_1, TEST_STUDY_ID);

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
        
        Map<String, List<ClinicalData>> tableClinicalDataMap = new HashMap<>();
        tableClinicalDataMap.put(uniqueKeySample1, List.of(clinicalData1, clinicalData2, clinicalData3));
        tableClinicalData = SampleClinicalDataCollection.builder().withByUniqueSampleKey(tableClinicalDataMap).build();

        reset(studyViewFilterApplier);
        reset(clinicalDataService);
        reset(discreteCopyNumberService);
        reset(sampleService);
        reset(genePanelService);
        reset(sampleService);
        reset(clinicalAttributeService);
        reset(patientService);
    }

    @Test
    @WithMockUser
    public void fetchClinicalDataCounts() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-data-counts/fetch").with(csrf())
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
    @WithMockUser
    @Ignore
    //TODO: Update Test currently out of scope of StudyViewController (need to make a new unit test to test ClinicalDataBinUtil)
    public void fetchClinicalDataBinCounts() throws Exception
    {
        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

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

        when(clinicalDataService.fetchClinicalData(anyList(), anyList(),
            anyList(), any(String.class), any(String.class))).thenReturn(clinicalData);

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
        
        when(clinicalDataBinUtil.removeSelfFromFilter(any())).thenReturn(studyViewFilter);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-data-bin-counts/fetch").with(csrf())
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
    @WithMockUser
    public void fetchMutatedGenes() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/mutated-genes/fetch").with(csrf())
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
    @WithMockUser
    public void fetchFusionGenes() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/structuralvariant-genes/fetch").with(csrf())
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
    @WithMockUser
    public void fetchCNAGenes() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/cna-genes/fetch").with(csrf())
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
    @WithMockUser
    public void fetchSampleIds() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);

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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/filtered-samples/fetch").with(csrf())
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
    @WithMockUser
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/molecular-profile-sample-counts/fetch").with(csrf())
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
    @WithMockUser
    public void fetchGenomicDataCounts() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        List<GenomicDataCountItem> genomicDataCountItems = new ArrayList<>();

        GenomicDataCount genomicDataCount1 = new GenomicDataCount();
        genomicDataCount1.setLabel(TEST_CNA_ALTERATION_NAME_1);
        genomicDataCount1.setValue(TEST_CNA_ALTERATION_VALUE_1);
        genomicDataCount1.setCount(1);
        
        GenomicDataCount genomicDataCount2 = new GenomicDataCount();
        genomicDataCount2.setLabel(TEST_CNA_ALTERATION_NAME_2);
        genomicDataCount2.setValue(TEST_CNA_ALTERATION_VALUE_2);
        genomicDataCount2.setCount(1);

        GenomicDataCountItem genomicDataCountItem1 = new GenomicDataCountItem();
        List<GenomicDataCount> genomicDataCounts1 = new ArrayList<>();
        genomicDataCounts1.add(genomicDataCount1);
        genomicDataCounts1.add(genomicDataCount2);
        genomicDataCountItem1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        genomicDataCountItem1.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataCountItem1.setCounts(genomicDataCounts1);

        GenomicDataCountItem genomicDataCountItem2 = new GenomicDataCountItem();
        List<GenomicDataCount> genomicDataCounts2 = new ArrayList<>();
        genomicDataCounts2.add(genomicDataCount1);
        genomicDataCounts2.add(genomicDataCount2);
        genomicDataCountItem2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        genomicDataCountItem2.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataCountItem2.setCounts(genomicDataCounts2);

        genomicDataCountItems.add(genomicDataCountItem1);
        genomicDataCountItems.add(genomicDataCountItem2);

        when(studyViewService.getCNAAlterationCountsByGeneSpecific(
            anyList(),
            anyList(),
            anyList()))
            .thenReturn(genomicDataCountItems);

        GenomicDataCountFilter genomicDataCountFilter = new GenomicDataCountFilter();
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        
        GenomicDataFilter genomicDataFilter1 = new GenomicDataFilter();
        genomicDataFilter1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        genomicDataFilter1.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataFilters.add(genomicDataFilter1);
        
        GenomicDataFilter genomicDataFilter2 = new GenomicDataFilter();
        genomicDataFilter2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        genomicDataFilter2.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataFilters.add(genomicDataFilter2);
        
        genomicDataCountFilter.setGenomicDataFilters(genomicDataFilters);
        
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        genomicDataCountFilter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/genomic-data-counts/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(genomicDataCountFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].profileType").value(TEST_MOLECULAR_PROFILE_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].label").value(TEST_CNA_ALTERATION_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].value").value(TEST_CNA_ALTERATION_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].label").value(TEST_CNA_ALTERATION_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].value").value(TEST_CNA_ALTERATION_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].profileType").value(TEST_MOLECULAR_PROFILE_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].label").value(TEST_CNA_ALTERATION_NAME_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].value").value(TEST_CNA_ALTERATION_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].label").value(TEST_CNA_ALTERATION_NAME_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].value").value(TEST_CNA_ALTERATION_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].count").value(1));
    }
    
    @Ignore("Skip StudyViewControllerTest.fetchClinicalDataDensityPlot due to assertion errors")
    @Test
    @WithMockUser
    public void fetchClinicalDataDensityPlot() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
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

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-data-density-plot/fetch").with(csrf())
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
    @WithMockUser
    public void fetchGenericAssayDataCounts() throws Exception {

        List<SampleIdentifier> filteredSampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(TEST_SAMPLE_ID_1);
        sampleIdentifier.setStudyId(TEST_STUDY_ID);
        filteredSampleIdentifiers.add(sampleIdentifier);
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

        when(studyViewService.fetchGenericAssayDataCounts(anyList(), anyList(),
            anyList(), anyList())).thenReturn(genericAssayDataCountItems);

        GenericAssayDataCountFilter genericAssayDataCountFilter = new GenericAssayDataCountFilter();
        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter();
        genericAssayDataFilter.setStableId(TEST_STABLE_ID);
        genericAssayDataCountFilter.setGenericAssayDataFilters(Arrays.asList(genericAssayDataFilter));
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        genericAssayDataCountFilter.setStudyViewFilter(studyViewFilter);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/generic-assay-data-counts/fetch").with(csrf())
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
    @WithMockUser
    public void fetchClinicalDataClinicalTable() throws Exception {
        // For this sake of this test the sample clinical data and patient clinical data are identical.
        when(clinicalDataService.fetchSampleClinicalTable(anyList(), anyList(),
            anyInt(), anyInt(), anyString(), any(), anyString())).thenReturn(
                new ImmutablePair<>(tableClinicalData, 100)
            );

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        
        String jsonPath = "$.byUniqueSampleKey." + uniqueKeySample1;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-data-table/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath( jsonPath+"[0].clinicalAttributeId", uniqueKeySample1).value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath(jsonPath+"[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath(jsonPath+"[1].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath(jsonPath+"[1].sampleId").value(TEST_SAMPLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath(jsonPath+"[2].clinicalAttributeId").value(TEST_ATTRIBUTE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath(jsonPath+"[2].sampleId").value(TEST_SAMPLE_ID_3));

    }

    @Test
    @WithMockUser
    public void fetchClinicalEventTypeCounts() throws Exception
    {
        List<ClinicalEventTypeCount> testEventTypeCounts = Arrays.asList(new ClinicalEventTypeCount(TEST_CLINICAL_EVENT_TYPE, TEST_CLINICAL_EVENT_TYPE_COUNT));

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(clinicalEventService.getClinicalEventTypeCounts(anyList(), anyList()))
            .thenReturn(testEventTypeCounts);
        
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Collections.singletonList(TEST_STUDY_ID));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-event-type-counts/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventType").value(TEST_CLINICAL_EVENT_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].count").value(TEST_CLINICAL_EVENT_TYPE_COUNT));
    }

    @Test
    @WithMockUser
    public void validateStructVarFilter() throws Exception {

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

        final StructuralVariantFilterQuery structVarFilterQuery = new StructuralVariantFilterQuery("A", null, "B", null,
            true, true, true, Select.all(),
            true, true, true, true);
        final StudyViewStructuralVariantFilter structuralVariantFilter = new StudyViewStructuralVariantFilter();
        structuralVariantFilter.setStructVarQueries(Arrays.asList(Arrays.asList(structVarFilterQuery)));
        studyViewFilter.setStructuralVariantFilters(Arrays.asList(structuralVariantFilter));

        // Test case:
        structVarFilterQuery.getGene1Query().setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        structVarFilterQuery.getGene2Query().setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/filtered-samples/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    @Test
    @WithMockUser
    public void validateStructVarFilterBothAnyGene() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(studyViewFilterApplier.apply(any(), eq(false))).thenReturn(filteredSampleIdentifiers);
        when(sampleService.fetchSamples(anyList(), anyList(), anyString())).thenReturn(filteredSamples);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        final StructuralVariantFilterQuery structVarFilterQuery = new StructuralVariantFilterQuery("A", null, "B", null,
            true, true, true, Select.all(),
            true, true, true, true);
        final StudyViewStructuralVariantFilter structuralVariantFilter = new StudyViewStructuralVariantFilter();
        structuralVariantFilter.setStructVarQueries(Arrays.asList(Arrays.asList(structVarFilterQuery)));
        studyViewFilter.setStructuralVariantFilters(Arrays.asList(structuralVariantFilter));

        // Test case:
        structVarFilterQuery.getGene1Query().setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        structVarFilterQuery.getGene2Query().setSpecialValue(StructuralVariantSpecialValue.ANY_GENE);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/filtered-samples/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    public void validateStructVarFilterBothNoGene() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(studyViewFilterApplier.apply(any(), eq(false))).thenReturn(filteredSampleIdentifiers);
        when(sampleService.fetchSamples(anyList(), anyList(), anyString())).thenReturn(filteredSamples);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        final StructuralVariantFilterQuery structVarFilterQuery = new StructuralVariantFilterQuery("A", null, "B", null,
            true, true, true, Select.all(),
            true, true, true, true);
        final StudyViewStructuralVariantFilter structuralVariantFilter = new StudyViewStructuralVariantFilter();
        structuralVariantFilter.setStructVarQueries(Arrays.asList(Arrays.asList(structVarFilterQuery)));
        studyViewFilter.setStructuralVariantFilters(Arrays.asList(structuralVariantFilter));

        // Test case:
        structVarFilterQuery.getGene1Query().setSpecialValue(StructuralVariantSpecialValue.NO_GENE);
        structVarFilterQuery.getGene2Query().setSpecialValue(StructuralVariantSpecialValue.NO_GENE);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/filtered-samples/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    public void validateStructVarFilterBothNoGeneId() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(studyViewFilterApplier.apply(any(), eq(false))).thenReturn(filteredSampleIdentifiers);
        when(sampleService.fetchSamples(anyList(), anyList(), anyString())).thenReturn(filteredSamples);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        // Test case:
        final StructuralVariantFilterQuery structVarFilterQuery = new StructuralVariantFilterQuery(null, null, null, null,
            true, true, true, Select.all(),
            true, true, true, true);
            
        final StudyViewStructuralVariantFilter structuralVariantFilter = new StudyViewStructuralVariantFilter();
        structuralVariantFilter.setStructVarQueries(Arrays.asList(Arrays.asList(structVarFilterQuery)));
        studyViewFilter.setStructuralVariantFilters(Arrays.asList(structuralVariantFilter));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/filtered-samples/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    public void validateStructVarFilterBothGeneIdAndSpecialValueNull() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);
        when(studyViewFilterApplier.apply(any(), eq(false))).thenReturn(filteredSampleIdentifiers);
        when(sampleService.fetchSamples(anyList(), anyList(), anyString())).thenReturn(filteredSamples);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));

        // Test case:
        final StructuralVariantFilterQuery structVarFilterQuery = new StructuralVariantFilterQuery(null, null, "B", null,
            true, true, true, Select.all(),
            true, true, true, true);
        structVarFilterQuery.getGene1Query().setSpecialValue(null);

        final StudyViewStructuralVariantFilter structuralVariantFilter = new StudyViewStructuralVariantFilter();
        structuralVariantFilter.setStructVarQueries(Arrays.asList(Arrays.asList(structVarFilterQuery)));
        studyViewFilter.setStructuralVariantFilters(Arrays.asList(structuralVariantFilter));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/filtered-samples/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyViewFilter)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void fetchMutationDataCounts() throws Exception {

        when(studyViewFilterApplier.apply(any())).thenReturn(filteredSampleIdentifiers);

        List<GenomicDataCountItem> genomicDataCountItems = new ArrayList<>();

        GenomicDataCount genomicDataCount1 = new GenomicDataCount();
        genomicDataCount1.setLabel(TEST_MUTATION_TYPE);
        genomicDataCount1.setValue(TEST_MUTATION_TYPE);
        genomicDataCount1.setCount(1);

        GenomicDataCount genomicDataCount2 = new GenomicDataCount();
        genomicDataCount2.setLabel(TEST_MUTATION_TYPE);
        genomicDataCount2.setValue(TEST_MUTATION_TYPE);
        genomicDataCount2.setCount(1);

        GenomicDataCountItem genomicDataCountItem1 = new GenomicDataCountItem();
        List<GenomicDataCount> genomicDataCounts1 = new ArrayList<>();
        genomicDataCounts1.add(genomicDataCount1);
        genomicDataCounts1.add(genomicDataCount2);
        genomicDataCountItem1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        genomicDataCountItem1.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataCountItem1.setCounts(genomicDataCounts1);

        GenomicDataCountItem genomicDataCountItem2 = new GenomicDataCountItem();
        List<GenomicDataCount> genomicDataCounts2 = new ArrayList<>();
        genomicDataCounts2.add(genomicDataCount1);
        genomicDataCounts2.add(genomicDataCount2);
        genomicDataCountItem2.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_2);
        genomicDataCountItem2.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataCountItem2.setCounts(genomicDataCounts2);

        genomicDataCountItems.add(genomicDataCountItem1);
        genomicDataCountItems.add(genomicDataCountItem2);

        when(studyViewService.getMutationCountsByGeneSpecific(
            anyList(),
            anyList(),
            anyList(),
            any(AlterationFilter.class)))
            .thenReturn(genomicDataCountItems);

        when(studyViewService.getMutationTypeCountsByGeneSpecific(
            anyList(),
            anyList(),
            anyList()))
            .thenReturn(genomicDataCountItems);

        GenomicDataCountFilter genomicDataCountFilter = new GenomicDataCountFilter();
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();

        GenomicDataFilter genomicDataFilter1 = new GenomicDataFilter();
        genomicDataFilter1.setHugoGeneSymbol(TEST_HUGO_GENE_SYMBOL_1);
        genomicDataFilter1.setProfileType(TEST_MOLECULAR_PROFILE_TYPE);
        genomicDataFilters.add(genomicDataFilter1);

        genomicDataCountFilter.setGenomicDataFilters(genomicDataFilters);

        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(TEST_STUDY_ID));
        studyViewFilter.setAlterationFilter(alterationFilter);
        genomicDataCountFilter.setStudyViewFilter(studyViewFilter);

        ResultActions result1 = mockMvc.perform(MockMvcRequestBuilders.post("/api/mutation-data-counts/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .param("projection", "SUMMARY")
                .content(objectMapper.writeValueAsString(genomicDataCountFilter)));
            
        result1
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].profileType").value(TEST_MOLECULAR_PROFILE_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].profileType").value(TEST_MOLECULAR_PROFILE_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].count").value(1));

        ResultActions result2 = mockMvc.perform(MockMvcRequestBuilders.post("/api/mutation-data-counts/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .param("projection", "DETAILED")
            .content(objectMapper.writeValueAsString(genomicDataCountFilter)));

        result2
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].profileType").value(TEST_MOLECULAR_PROFILE_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].counts[1].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].hugoGeneSymbol").value(TEST_HUGO_GENE_SYMBOL_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].profileType").value(TEST_MOLECULAR_PROFILE_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[0].count").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].label").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].value").value(TEST_MUTATION_TYPE))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].counts[1].count").value(1));
    }
}
