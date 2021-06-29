package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.SampleFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.hamcrest.Matchers;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web-test.xml")
@Configuration
public class SampleControllerTest {

    private static final int TEST_INTERNAL_ID_1 = 1;
    private static final String TEST_STABLE_ID_1 = "test_stable_id_1";
    private static final int TEST_PATIENT_ID_1 = 1;
    private static final String TEST_PATIENT_STABLE_ID_1 = "test_patient_stable_id_1";
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final int TEST_INTERNAL_ID_2 = 2;
    private static final String TEST_STABLE_ID_2 = "test_stable_id_2";
    private static final int TEST_PATIENT_ID_2 = 2;
    private static final String TEST_PATIENT_STABLE_ID_2 = "test_patient_stable_id_2";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SampleService sampleService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {

        Mockito.reset(sampleService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllSamplesInStudyDefaultProjection() throws Exception {

        List<Sample> sampleList = createExampleSamples();

        Mockito.when(sampleService.getAllSamplesInStudy(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(sampleList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist());
    }

    @Test
    public void getAllSamplesInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(sampleService.getMetaSamplesInStudy(Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getSampleInStudyNotFound() throws Exception {

        Mockito.when(sampleService.getSampleInStudy(Mockito.anyString(), Mockito.anyString())).thenThrow(
            new SampleNotFoundException("test_study_id", "test_sample_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples/test_sample_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                .value("Sample not found in study test_study_id: test_sample_id"));
    }

    @Test
    public void getAllSamples() throws Exception {
        List<Sample> samples = createExampleSamples();
        
        Mockito
            .when(sampleService.getAllSamples(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any()
            )).thenReturn(samples);
        
        mockMvc.perform(
            MockMvcRequestBuilders.get("/samples").accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist());
    }

    @Test
    public void getSampleInStudy() throws Exception {

        Sample sample = new Sample();
        sample.setInternalId(TEST_INTERNAL_ID_1);
        sample.setStableId(TEST_STABLE_ID_1);
        sample.setPatientId(TEST_PATIENT_ID_1);
        sample.setPatientStableId(TEST_PATIENT_STABLE_ID_1);
        sample.setSampleType(Sample.SampleType.PRIMARY_SOLID_TUMOR);
        sample.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);

        Mockito.when(sampleService.getSampleInStudy(Mockito.anyString(), Mockito.anyString())).thenReturn(sample);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples/test_sample_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientId").value(TEST_PATIENT_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1));

    }

    @Test
    public void getAllSamplesOfPatientInStudyDefaultProjection() throws Exception {

        List<Sample> sampleList = createExampleSamples();

        Mockito.when(sampleService.getAllSamplesOfPatientInStudy(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(sampleList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/patients/test_patient_id/samples")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist());
    }

    @Test
    public void getAllSamplesOfPatientInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(sampleService.getMetaSamplesOfPatientInStudy(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/patients/test_patient_id/samples")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchSamplesDefaultProjection() throws Exception {

        List<Sample> sampleList = createExampleSamples();

        Mockito.when(sampleService.fetchSamples(Mockito.anyList(), Mockito.anyList(),
            Mockito.anyString())).thenReturn(sampleList);

        SampleFilter sampleFilter = new SampleFilter();
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier1.setSampleId(TEST_STABLE_ID_1);
        sampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier2.setSampleId(TEST_STABLE_ID_2);
        sampleIdentifiers.add(sampleIdentifier2);
        sampleFilter.setSampleIdentifiers(sampleIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/samples/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist());
    }

    @Test
    public void fetchSamplesByUniqueSampleKeysDefaultProjection() throws Exception {

        List<Sample> sampleList = createExampleSamples();

        Mockito.when(sampleService.fetchSamples(Mockito.anyList(), Mockito.anyList(),
            Mockito.anyString())).thenReturn(sampleList);

        SampleFilter sampleFilter = new SampleFilter();
        List<String> uniqueSampleKeys = new ArrayList<>();
        uniqueSampleKeys.add("dGVzdF9zdGFibGVfaWRfMTp0ZXN0X3N0dWR5XzE");
        uniqueSampleKeys.add("dGVzdF9zdGFibGVfaWRfMjp0ZXN0X3N0dWR5XzE");
        sampleFilter.setUniqueSampleKeys(uniqueSampleKeys);

        mockMvc.perform(MockMvcRequestBuilders.post("/samples/fetch")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientStableId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleType")
                .value(Sample.SampleType.PRIMARY_SOLID_TUMOR.getValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist());
    }

    @Test
    public void fetchSamplesMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(sampleService.fetchMetaSamples(Mockito.anyList(),
            Mockito.anyList())).thenReturn(baseMeta);

        SampleFilter sampleFilter = new SampleFilter();
        List<SampleIdentifier> sampleIdentifiers = new ArrayList<>();
        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier1.setSampleId(TEST_STABLE_ID_1);
        sampleIdentifiers.add(sampleIdentifier1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleIdentifier2.setSampleId(TEST_STABLE_ID_2);
        sampleIdentifiers.add(sampleIdentifier2);
        sampleFilter.setSampleIdentifiers(sampleIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/samples/fetch")
            .param("projection", "META")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(sampleFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private List<Sample> createExampleSamples() {

        List<Sample> sampleList = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setInternalId(TEST_INTERNAL_ID_1);
        sample1.setStableId(TEST_STABLE_ID_1);
        sample1.setPatientId(TEST_PATIENT_ID_1);
        sample1.setPatientStableId(TEST_PATIENT_STABLE_ID_1);
        sample1.setSampleType(Sample.SampleType.PRIMARY_SOLID_TUMOR);
        sample1.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleList.add(sample1);
        Sample sample2 = new Sample();
        sample2.setInternalId(TEST_INTERNAL_ID_2);
        sample2.setStableId(TEST_STABLE_ID_2);
        sample2.setPatientId(TEST_PATIENT_ID_2);
        sample2.setPatientStableId(TEST_PATIENT_STABLE_ID_2);
        sample2.setSampleType(Sample.SampleType.PRIMARY_SOLID_TUMOR);
        sample2.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        sampleList.add(sample2);
        return sampleList;
    }
}
