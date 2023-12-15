package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PatientFilter;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {PatientController.class, TestConfig.class})
public class PatientControllerTest {

    private static final int TEST_INTERNAL_ID_1 = 1;
    private static final String TEST_STABLE_ID_1 = "test_stable_id_1";
    private static final int TEST_CANCER_STUDY_ID_1 = 1;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final int TEST_INTERNAL_ID_2 = 2;
    private static final String TEST_STABLE_ID_2 = "test_stable_id_2";
    private static final int TEST_CANCER_STUDY_ID_2 = 2;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_2 = "test_study_2";
    private static final String TEST_TYPE_OF_CANCER_ID_1 = "test_type_of_cancer_id_1";
    private static final String TEST_NAME_1 = "test_name_1";
    private static final String TEST_SHORT_NAME_1 = "test_short_name_1";
    private static final String TEST_DESCRIPTION_1 = "test_description_1";

    @MockBean
    private PatientService patientService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getAllPatientsDefaultProjection() throws Exception {

        List<Patient> patientList = createExamplePatients();

        Mockito.when(patientService.getAllPatients(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(patientList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/patients")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudy").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudy").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getAllPatientsMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(patientService.getMetaPatients(Mockito.any())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/patients")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getAllPatientsInStudyDefaultProjection() throws Exception {

        List<Patient> patientList = createExamplePatients();

        Mockito.when(patientService.getAllPatientsInStudy(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(patientList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudy").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudy").doesNotExist());
    }

    @Test
    @WithMockUser
    public void getAllPatientsInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(patientService.getMetaPatientsInStudy(Mockito.anyString())).thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getPatientInStudyNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(Mockito.anyString(), Mockito.anyString())).thenThrow(
            new PatientNotFoundException("test_study_id", "test_patient_id"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients/test_patient_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isNotFound())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                .value("Patient not found in study test_study_id: test_patient_id"));
    }

    @Test
    @WithMockUser
    public void getPatientInStudy() throws Exception {

        Patient patient = new Patient();
        patient.setInternalId(TEST_INTERNAL_ID_1);
        patient.setStableId(TEST_STABLE_ID_1);
        patient.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        patient.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        CancerStudy cancerStudy = new CancerStudy();
        cancerStudy.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        cancerStudy.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        cancerStudy.setTypeOfCancerId(TEST_TYPE_OF_CANCER_ID_1);
        cancerStudy.setName(TEST_NAME_1);
        cancerStudy.setDescription(TEST_DESCRIPTION_1);
        patient.setCancerStudy(cancerStudy);

        Mockito.when(patientService.getPatientInStudy(Mockito.anyString(), Mockito.anyString())).thenReturn(patient);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients/test_patient_id")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$.patientId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cancerStudyIdentifier").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchPatientsDefaultProjection() throws Exception {

        List<Patient> patientList = createExamplePatients();

        Mockito.when(patientService.fetchPatients(Mockito.anyList(), Mockito.anyList(),
            Mockito.anyString())).thenReturn(patientList);

        PatientFilter patientFilter = new PatientFilter();
        List<PatientIdentifier> patientIdentifiers = new ArrayList<>();
        PatientIdentifier patientIdentifier1 = new PatientIdentifier();
        patientIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        patientIdentifier1.setPatientId(TEST_STABLE_ID_1);
        patientIdentifiers.add(patientIdentifier1);
        PatientIdentifier patientIdentifier2 = new PatientIdentifier();
        patientIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_2);
        patientIdentifier2.setPatientId(TEST_STABLE_ID_2);
        patientIdentifiers.add(patientIdentifier2);
        patientFilter.setPatientIdentifiers(patientIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/patients/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patientFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudy").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudy").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchPatientsByUniquePatientKeysDefaultProjection() throws Exception {

        List<Patient> patientList = createExamplePatients();

        Mockito.when(patientService.fetchPatients(Mockito.anyList(), Mockito.anyList(),
            Mockito.anyString())).thenReturn(patientList);

        PatientFilter patientFilter = new PatientFilter();
        List<String> uniquePatientKeys = new ArrayList<>();
        uniquePatientKeys.add("dGVzdF9zdGFibGVfaWRfMTp0ZXN0X3N0dWR5XzE");
        uniquePatientKeys.add("dGVzdF9zdGFibGVfaWRfMjp0ZXN0X3N0dWR5XzI");
        patientFilter.setUniquePatientKeys(uniquePatientKeys);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/patients/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patientFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_STABLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].cancerStudy").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].internalId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_STABLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudyIdentifier").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].cancerStudy").doesNotExist());
    }

    @Test
    @WithMockUser
    public void fetchPatientsMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        Mockito.when(patientService.fetchMetaPatients(Mockito.anyList(),
            Mockito.anyList())).thenReturn(baseMeta);

        PatientFilter patientFilter = new PatientFilter();
        List<PatientIdentifier> patientIdentifiers = new ArrayList<>();
        PatientIdentifier patientIdentifier1 = new PatientIdentifier();
        patientIdentifier1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        patientIdentifier1.setPatientId(TEST_STABLE_ID_1);
        patientIdentifiers.add(patientIdentifier1);
        PatientIdentifier patientIdentifier2 = new PatientIdentifier();
        patientIdentifier2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        patientIdentifier2.setPatientId(TEST_STABLE_ID_2);
        patientIdentifiers.add(patientIdentifier2);
        patientFilter.setPatientIdentifiers(patientIdentifiers);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/patients/fetch").with(csrf())
            .param("projection", "META")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(patientFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    private List<Patient> createExamplePatients() {

        List<Patient> patientList = new ArrayList<>();
        Patient patient1 = new Patient();
        patient1.setInternalId(TEST_INTERNAL_ID_1);
        patient1.setStableId(TEST_STABLE_ID_1);
        patient1.setCancerStudyId(TEST_CANCER_STUDY_ID_1);
        patient1.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_1);
        patientList.add(patient1);
        Patient patient2 = new Patient();
        patient2.setInternalId(TEST_INTERNAL_ID_2);
        patient2.setStableId(TEST_STABLE_ID_2);
        patient2.setCancerStudyId(TEST_CANCER_STUDY_ID_2);
        patient2.setCancerStudyIdentifier(TEST_CANCER_STUDY_IDENTIFIER_2);
        patientList.add(patient2);
        return patientList;
    }
}
