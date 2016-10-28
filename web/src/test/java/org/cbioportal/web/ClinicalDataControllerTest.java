package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.summary.ClinicalDataSummary;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.web.parameter.ClinicalDataIdentifier;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("/applicationContext-web.xml")
@Configuration
public class ClinicalDataControllerTest {

    public static final String TEST_ATTR_ID_1 = "test_attr_id_1";
    public static final String TEST_ATTR_VALUE_1 = "test_attr_value_1";
    public static final int TEST_INTERNAL_ID_1 = 1;
    public static final String TEST_ATTR_ID_2 = "test_attr_id_2";
    public static final String TEST_ATTR_VALUE_2 = "test_attr_value_2";
    public static final int TEST_INTERNAL_ID_2 = 2;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ClinicalDataService clinicalDataService;
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public ClinicalDataService clinicalDataService() {
        return Mockito.mock(ClinicalDataService.class);
    }

    @Before
    public void setUp() throws Exception {

        Mockito.reset(clinicalDataService);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyDefaultProjection() throws Exception {

        List<SampleClinicalData> sampleClinicalDataList = new ArrayList<>();
        SampleClinicalData sampleClinicalData1 = new SampleClinicalData();
        sampleClinicalData1.setAttrId(TEST_ATTR_ID_1);
        sampleClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        sampleClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        sampleClinicalDataList.add(sampleClinicalData1);
        SampleClinicalData sampleClinicalData2 = new SampleClinicalData();
        sampleClinicalData2.setAttrId(TEST_ATTR_ID_2);
        sampleClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        sampleClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        sampleClinicalDataList.add(sampleClinicalData2);
        Mockito.when(clinicalDataService.getAllClinicalDataOfSampleInStudy(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(sampleClinicalDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples/test_sample_id/clinical-data")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrValue").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].sample").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrValue").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].sample").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    public void getAllClinicalDataOfSampleInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(clinicalDataService.getMetaSampleClinicalData(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/samples/test_sample_id/clinical-data")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyDefaultProjection() throws Exception {

        List<PatientClinicalData> patientClinicalDataList = new ArrayList<>();
        PatientClinicalData patientClinicalData1 = new PatientClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        PatientClinicalData patientClinicalData2 = new PatientClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        Mockito.when(clinicalDataService.getAllClinicalDataOfPatientInStudy(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(patientClinicalDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/patients/test_patient_id/clinical-data")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrValue").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrValue").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    public void getAllClinicalDataOfPatientInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(clinicalDataService.getMetaPatientClinicalData(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/patients/test_patient_id/clinical-data")
                .param("projection", "META"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void getAllClinicalDataInStudyDefaultProjection() throws Exception {

        List<PatientClinicalData> patientClinicalDataList = new ArrayList<>();
        PatientClinicalData patientClinicalData1 = new PatientClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        PatientClinicalData patientClinicalData2 = new PatientClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        Mockito.<List<? extends ClinicalDataSummary>>when(clinicalDataService.getAllClinicalDataInStudy(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(patientClinicalDataList);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/clinical-data")
                .param("clinicalDataType", "PATIENT")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrValue").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrValue").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    public void getAllClinicalDataInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(clinicalDataService.getMetaAllClinicalData(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/studies/test_study_id/clinical-data")
                .param("projection", "META")
                .param("clinicalDataType", "PATIENT"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    public void fetchClinicalDataDefaultProjection() throws Exception {

        List<PatientClinicalData> patientClinicalDataList = new ArrayList<>();
        PatientClinicalData patientClinicalData1 = new PatientClinicalData();
        patientClinicalData1.setAttrId(TEST_ATTR_ID_1);
        patientClinicalData1.setAttrValue(TEST_ATTR_VALUE_1);
        patientClinicalData1.setInternalId(TEST_INTERNAL_ID_1);
        patientClinicalDataList.add(patientClinicalData1);
        PatientClinicalData patientClinicalData2 = new PatientClinicalData();
        patientClinicalData2.setAttrId(TEST_ATTR_ID_2);
        patientClinicalData2.setAttrValue(TEST_ATTR_VALUE_2);
        patientClinicalData2.setInternalId(TEST_INTERNAL_ID_2);
        patientClinicalDataList.add(patientClinicalData2);
        Mockito.<List<? extends ClinicalDataSummary>>when(clinicalDataService.fetchClinicalData(
                Mockito.anyListOf(String.class), Mockito.anyListOf(String.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(patientClinicalDataList);

        List<ClinicalDataIdentifier> clinicalDataIdentifiers = new ArrayList<>();
        ClinicalDataIdentifier clinicalDataIdentifier1 = new ClinicalDataIdentifier();
        clinicalDataIdentifier1.setStudyId("test_study1");
        clinicalDataIdentifier1.setId("test_patient1");
        clinicalDataIdentifiers.add(clinicalDataIdentifier1);
        ClinicalDataIdentifier clinicalDataIdentifier2 = new ClinicalDataIdentifier();
        clinicalDataIdentifier2.setStudyId("test_study2");
        clinicalDataIdentifier2.setId("test_patient2");
        clinicalDataIdentifiers.add(clinicalDataIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data/fetch")
                .param("clinicalDataType", "PATIENT")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinicalDataIdentifiers)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrId").value(TEST_ATTR_ID_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].attrValue").value(TEST_ATTR_VALUE_1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrId").value(TEST_ATTR_ID_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].attrValue").value(TEST_ATTR_VALUE_2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].patient").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute").doesNotExist());
    }

    @Test
    public void fetchClinicalDataMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(clinicalDataService.fetchMetaClinicalData(Mockito.anyListOf(String.class),
                Mockito.anyListOf(String.class), Mockito.anyString(), Mockito.anyString())).thenReturn(baseMeta);

        List<ClinicalDataIdentifier> clinicalDataIdentifiers = new ArrayList<>();
        ClinicalDataIdentifier clinicalDataIdentifier1 = new ClinicalDataIdentifier();
        clinicalDataIdentifier1.setStudyId("test_study1");
        clinicalDataIdentifier1.setId("test_patient1");
        clinicalDataIdentifiers.add(clinicalDataIdentifier1);
        ClinicalDataIdentifier clinicalDataIdentifier2 = new ClinicalDataIdentifier();
        clinicalDataIdentifier2.setStudyId("test_study2");
        clinicalDataIdentifier2.setId("test_patient2");
        clinicalDataIdentifiers.add(clinicalDataIdentifier2);

        mockMvc.perform(MockMvcRequestBuilders.post("/clinical-data/fetch")
                .param("projection", "META")
                .param("clinicalDataType", "PATIENT")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinicalDataIdentifiers)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }
}
