package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.ClinicalEventRequest;
import org.cbioportal.web.parameter.ClinicalEventRequestIdentifier;
import org.cbioportal.web.parameter.OccurrencePosition;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.cbioportal.web.parameter.SurvivalRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {SurvivalController.class, TestConfig.class})
public class SurvivalControllerTest {
    
    private static final String TEST_CANCER_STUDY_IDENTIFIER = "test_study_id";
    private static final String TEST_PATIENT_ID_1 = "test_patient_id_1";
    private static final String TEST_PATIENT_ID_2 = "test_patient_id_2";
    private static final String TEST_ATTRIBUTE_ID_PREFIX = "test_attribute_id_prefix";
    private static final String TEST_CLINICAL_ATTRIBUTE_ID_1 = "test_clinical_attribute_id_1";
    private static final String TEST_CLINICAL_ATTRIBUTE_ID_2 = "test_clinical_attribute_id_2";
    private static final String TEST_CLINICAL_ATTRIBUTE_VALUE_1 = "test_clinical_attribute_value_1";
    private static final String TEST_CLINICAL_ATTRIBUTE_VALUE_2 = "test_clinical_attribute_value_2";
    private static final String TEST_CLINICAL_EVENT_TYPE = "test_clinical_event_type";
    

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private ClinicalEventService clinicalEventService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchSurvivalData() throws Exception {
        List<ClinicalData> clinicalDataList = createClinicalDataList();
        when(clinicalEventService.getSurvivalData(anyList(), anyList(), any(), any()))
            .thenReturn(clinicalDataList);

        SurvivalRequest survivalRequest = new SurvivalRequest();
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setPatientId(TEST_PATIENT_ID_1);
        patientIdentifier.setStudyId(TEST_CANCER_STUDY_IDENTIFIER);
        survivalRequest.setPatientIdentifiers(List.of(patientIdentifier));
        survivalRequest.setAttributeIdPrefix(TEST_ATTRIBUTE_ID_PREFIX);

        ClinicalEventRequest clinicalEventRequest = new ClinicalEventRequest();
        clinicalEventRequest.setEventType(TEST_CLINICAL_EVENT_TYPE);
        clinicalEventRequest.setAttributes(new ArrayList<>());

        ClinicalEventRequestIdentifier startEventRequestIdentifier = new ClinicalEventRequestIdentifier();
        startEventRequestIdentifier.setClinicalEventRequests(Set.of(clinicalEventRequest));
        startEventRequestIdentifier.setPosition(OccurrencePosition.FIRST);
        survivalRequest.setStartEventRequestIdentifier(startEventRequestIdentifier);

        ClinicalEventRequestIdentifier endEventRequestIdentifier = new ClinicalEventRequestIdentifier();
        endEventRequestIdentifier.setClinicalEventRequests(Set.of(clinicalEventRequest));
        endEventRequestIdentifier.setPosition(OccurrencePosition.LAST);
        survivalRequest.setEndEventRequestIdentifier(endEventRequestIdentifier);
        
        ClinicalEventRequestIdentifier censoredEventRequestIdentifier = new ClinicalEventRequestIdentifier();
        censoredEventRequestIdentifier.setClinicalEventRequests(Set.of(clinicalEventRequest));
        censoredEventRequestIdentifier.setPosition(OccurrencePosition.LAST);
        survivalRequest.setCensoredEventRequestIdentifier(censoredEventRequestIdentifier);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/survival-data/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(survivalRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttributeId").value(TEST_CLINICAL_ATTRIBUTE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].value").value(TEST_CLINICAL_ATTRIBUTE_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttributeId").value(TEST_CLINICAL_ATTRIBUTE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].value").value(TEST_CLINICAL_ATTRIBUTE_VALUE_2));
    }
    
    private List<ClinicalData> createClinicalDataList() {
        List<ClinicalData> clinicalDataList = new ArrayList<>();
        
        ClinicalData clinicalData1 = new ClinicalData();
        clinicalData1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER);
        clinicalData1.setPatientId(TEST_PATIENT_ID_1);
        clinicalData1.setAttrId(TEST_CLINICAL_ATTRIBUTE_ID_1);
        clinicalData1.setAttrValue(TEST_CLINICAL_ATTRIBUTE_VALUE_1);
        clinicalDataList.add(clinicalData1);
        
        ClinicalData clinicalData2 = new ClinicalData();
        clinicalData2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER);
        clinicalData2.setPatientId(TEST_PATIENT_ID_2);
        clinicalData2.setAttrId(TEST_CLINICAL_ATTRIBUTE_ID_2);
        clinicalData2.setAttrValue(TEST_CLINICAL_ATTRIBUTE_VALUE_2);
        clinicalDataList.add(clinicalData2);
        
        return clinicalDataList;
    }
}
