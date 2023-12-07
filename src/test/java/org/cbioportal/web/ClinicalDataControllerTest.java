package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.web.parameter.ClinicalDataSingleStudyFilter;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.hamcrest.Matchers;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ClinicalDataController.class, TestConfig.class})
public class ClinicalDataControllerTest {

    private static final String TEST_ATTR_ID_1 = "test_attr_id_1";
    private static final String TEST_ATTR_VALUE_1 = "test_attr_value_1";
    private static final int TEST_INTERNAL_ID_1 = 1;
    private static final String TEST_ATTR_ID_2 = "test_attr_id_2";
    private static final String TEST_ATTR_VALUE_2 = "test_attr_value_2";
    private static final int TEST_INTERNAL_ID_2 = 2;

    @MockBean
    private ClinicalDataService clinicalDataService;

    private ObjectMapper objectMapper = new CustomObjectMapper();

    @Autowired
    private MockMvc mockMvc;


    @Test
    @WithMockUser
    public void getAllClinicalDataOfSampleInStudyDefaultProjection() throws Exception {

        List<ClinicalData> sampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData1 = new ClinicalData();
        sampleClinicalData1.setAttrId(TEST_ATTR_ID_1);
        sampleClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        sampleClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        sampleClinicalDataList.add(sampleClinicalData1);
        ClinicalData sampleClinicalData2 = new ClinicalData();
        sampleClinicalData2.setAttrId(TEST_ATTR_ID_2);
        sampleClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        sampleClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        sampleClinicalDataList.add(sampleClinicalData2);
        when(clinicalDataService.getAllClinicalDataOfSampleInStudy(any(), any(),
                any(), any(), any(), any(),
                any(), any())).thenReturn(sampleClinicalDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/samples/test_sample_id/clinical-data")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getAllClinicalDataOfSampleInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalDataService.getMetaSampleClinicalData(any(), any(),
                any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/samples/test_sample_id/clinical-data")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getAllClinicalDataOfPatientInStudyDefaultProjection() throws Exception {

        List<ClinicalData> patientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        when(clinicalDataService.getAllClinicalDataOfPatientInStudy(any(), any(),
                any(), any(), any(), any(),
                any(), any())).thenReturn(patientClinicalDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients/test_patient_id/clinical-data")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getAllClinicalDataOfPatientInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalDataService.getMetaPatientClinicalData(any(), any(),
                any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients/test_patient_id/clinical-data")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getAllClinicalDataInStudyDefaultProjection() throws Exception {

        List<ClinicalData> patientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        when(clinicalDataService.getAllClinicalDataInStudy(
                any(), any(), any(), any(), any(),
                any(), any(), any())).thenReturn(patientClinicalDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/clinical-data")
                .param("clinicalDataType", "PATIENT")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getAllClinicalDataInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalDataService.getMetaAllClinicalData(any(), any(),
                any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/clinical-data")
                .param("projection", "META")
                .param("clinicalDataType", "PATIENT"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }


    @Test
    @WithMockUser
    public void fetchClinicalDataInStudyDefaultProjection() throws Exception {

        List<ClinicalData> patientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        when(clinicalDataService.fetchAllClinicalDataInStudy(any(),
            any(), any(), any(), any()))
            .thenReturn(patientClinicalDataList);

        List<String> ids = new ArrayList<>();
        ids.add("test_sample_id_1");
        ids.add("test_sample_id_2");
        ClinicalDataSingleStudyFilter clinicalDataSingleStudyFilter = new ClinicalDataSingleStudyFilter();
        clinicalDataSingleStudyFilter.setIds(ids);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/studies/test_study_id/clinical-data/fetch").with(csrf())
            .param("clinicalDataType", "SAMPLE")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clinicalDataSingleStudyFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_ATTR_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_ATTR_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchClinicalDataInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalDataService.fetchMetaClinicalDataInStudy(any(),
            any(), any(), any()))
            .thenReturn(baseMeta);

        List<String> ids = new ArrayList<>();
        ids.add("test_sample_id_1");
        ids.add("test_sample_id_2");
        ClinicalDataSingleStudyFilter clinicalDataSingleStudyFilter = new ClinicalDataSingleStudyFilter();
        clinicalDataSingleStudyFilter.setIds(ids);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/studies/test_study_id/clinical-data/fetch").with(csrf())
            .param("projection", "META")
            .param("clinicalDataType", "SAMPLE")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clinicalDataSingleStudyFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void fetchClinicalDataDefaultProjection() throws Exception {

        List<ClinicalData> patientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData1 = new ClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        ClinicalData patientClinicalData2 = new ClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        when(clinicalDataService.fetchClinicalData(
                any(), any(), any(), any(), any())).thenReturn(patientClinicalDataList);

        List<ClinicalDataIdentifier> clinicalDataIdentifiers = new ArrayList<>();
        ClinicalDataIdentifier clinicalDataIdentifier1 = new ClinicalDataIdentifier();
        clinicalDataIdentifier1.setStudyId("test_study1");
        clinicalDataIdentifier1.setEntityId("test_patient1");
        clinicalDataIdentifiers.add(clinicalDataIdentifier1);
        ClinicalDataIdentifier clinicalDataIdentifier2 = new ClinicalDataIdentifier();
        clinicalDataIdentifier2.setStudyId("test_study2");
        clinicalDataIdentifier2.setEntityId("test_patient2");
        clinicalDataIdentifiers.add(clinicalDataIdentifier2);
        ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter = new ClinicalDataMultiStudyFilter();
        clinicalDataMultiStudyFilter.setIdentifiers(clinicalDataIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-data/fetch").with(csrf())
                .param("clinicalDataType", "PATIENT")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinicalDataMultiStudyFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchClinicalDataMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalDataService.fetchMetaClinicalData(any(),
                any(), any(), any())).thenReturn(baseMeta);

        List<ClinicalDataIdentifier> clinicalDataIdentifiers = new ArrayList<>();
        ClinicalDataIdentifier clinicalDataIdentifier1 = new ClinicalDataIdentifier();
        clinicalDataIdentifier1.setStudyId("test_study1");
        clinicalDataIdentifier1.setEntityId("test_patient1");
        clinicalDataIdentifiers.add(clinicalDataIdentifier1);
        ClinicalDataIdentifier clinicalDataIdentifier2 = new ClinicalDataIdentifier();
        clinicalDataIdentifier2.setStudyId("test_study2");
        clinicalDataIdentifier2.setEntityId("test_patient2");
        clinicalDataIdentifiers.add(clinicalDataIdentifier2);
        ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter = new ClinicalDataMultiStudyFilter();
        clinicalDataMultiStudyFilter.setIdentifiers(clinicalDataIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-data/fetch").with(csrf())
                .param("projection", "META")
                .param("clinicalDataType", "PATIENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinicalDataMultiStudyFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }
}
