package org.cbioportal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.ClinicalEventService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.ClinicalEventAttributeRequest;
import org.cbioportal.web.parameter.ClinicalEventRequest;
import org.cbioportal.web.parameter.HeaderKeyConstants;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ClinicalEventController.class, TestConfig.class})
public class ClinicalEventControllerTest {

    private static final int TEST_CLINICAL_EVENT_ID_1 = 1;
    private static final String TEST_CANCER_STUDY_IDENTIFIER_1 = "test_study_1";
    private static final String TEST_PATIENT_ID_1 = "test_patient_id_1";
    private static final String TEST_EVENT_TYPE_1 = "test_event_type_1";
    private static final int TEST_START_DATE_1 = 123;
    private static final int TEST_END_DATE_1 = 136;
    private static final String TEST_KEY_1 = "test_key_1";
    private static final String TEST_VALUE_1 = "test_value_1";
    private static final String TEST_KEY_2 = "test_key_2";
    private static final String TEST_VALUE_2 = "test_value_2";
    private static final int TEST_CLINICAL_EVENT_ID_2 = 2;
    private static final String TEST_PATIENT_ID_2 = "test_patient_id_2";
    private static final String TEST_EVENT_TYPE_2 = "test_event_type_2";
    private static final int TEST_START_DATE_2 = 223;
    private static final int TEST_END_DATE_2 = 236;
    private static final String TEST_KEY_3 = "test_key_3";
    private static final String TEST_VALUE_3 = "test_value_3";
    private static final String TEST_KEY_4 = "test_key_4";
    private static final String TEST_VALUE_4 = "test_value_4";
    private static final String TEST_UNIQUE_PATIENT_KEY_1 = "test_unique_patient_key_1";
    private static final String TEST_UNIQUE_PATIENT_KEY_2 = "test_unique_patient_key_2";

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @MockBean
    private ClinicalEventService clinicalEventService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void getAllClinicalEventsOfPatientInStudy() throws Exception {

        List<ClinicalEvent> clinicalEventList = createExampleClinicalEventList();

        when(clinicalEventService.getAllClinicalEventsOfPatientInStudy(any(), any(),
            any(), any(), any(), any(), any()))
            .thenReturn(clinicalEventList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients/test_patient_id/clinical-events")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalEventId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventType").value(TEST_EVENT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].startNumberOfDaysSinceDiagnosis").value(TEST_START_DATE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].endNumberOfDaysSinceDiagnosis").value(TEST_END_DATE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].key").value(TEST_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].value").value(TEST_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[1].key").value(TEST_KEY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[1].value").value(TEST_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalEventId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].eventType").value(TEST_EVENT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].startNumberOfDaysSinceDiagnosis").value(TEST_START_DATE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].endNumberOfDaysSinceDiagnosis").value(TEST_END_DATE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].key").value(TEST_KEY_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].value").value(TEST_VALUE_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[1].key").value(TEST_KEY_4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[1].value").value(TEST_VALUE_4));
    }

    @Test
    @WithMockUser
    public void getAllClinicalEventsOfPatientInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalEventService.getMetaPatientClinicalEvents(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/patients/test_patient_id/clinical-events")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void getAllClinicalEventsInStudy() throws Exception {

        List<ClinicalEvent> clinicalEventList = createExampleClinicalEventList();

        when(clinicalEventService.getAllClinicalEventsInStudy(any(),
            any(), any(), any(), any(), any()))
            .thenReturn(clinicalEventList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/clinical-events")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalEventId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventType").value(TEST_EVENT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].startNumberOfDaysSinceDiagnosis").value(TEST_START_DATE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].endNumberOfDaysSinceDiagnosis").value(TEST_END_DATE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].key").value(TEST_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].value").value(TEST_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[1].key").value(TEST_KEY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[1].value").value(TEST_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalEventId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].studyId").value(TEST_CANCER_STUDY_IDENTIFIER_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].patientId").value(TEST_PATIENT_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].eventType").value(TEST_EVENT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].startNumberOfDaysSinceDiagnosis").value(TEST_START_DATE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].endNumberOfDaysSinceDiagnosis").value(TEST_END_DATE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].key").value(TEST_KEY_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].value").value(TEST_VALUE_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[1].key").value(TEST_KEY_4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[1].value").value(TEST_VALUE_4));
    }

    @Test
    @WithMockUser
    public void getAllClinicalEventsInStudyMetaProjection() throws Exception {

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(2);

        when(clinicalEventService.getMetaClinicalEvents(Mockito.anyString()))
            .thenReturn(baseMeta);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/studies/test_study_id/clinical-events")
            .param("projection", "META"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.header().string(HeaderKeyConstants.TOTAL_COUNT, "2"));
    }

    @Test
    @WithMockUser
    public void fetchClinicalEventsMetaReturnsOkForValidRequest() throws Exception {
        List<ClinicalEvent> clinicalEventList = createExampleClinicalEventMetaList();
        when(clinicalEventService.getClinicalEventsMeta(anyList(), anyList(), anyList())).thenReturn(clinicalEventList);

        PatientIdentifier patientIdentifier1 = new PatientIdentifier();
        ClinicalEventAttributeRequest clinicalEventAttributeRequest = new ClinicalEventAttributeRequest();
        ClinicalEventRequest clinicalEventRequest = new ClinicalEventRequest();
        clinicalEventRequest.setEventType(TEST_EVENT_TYPE_1);
        clinicalEventRequest.setAttributes(new ArrayList<>());
        clinicalEventAttributeRequest.setClinicalEventRequests(Set.of(clinicalEventRequest));
        clinicalEventAttributeRequest.setPatientIdentifiers(Collections.singletonList(patientIdentifier1));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-events-meta/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinicalEventAttributeRequest)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalEventId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventType").value(TEST_EVENT_TYPE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey").value(TEST_UNIQUE_PATIENT_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].key").value(TEST_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].value").value(TEST_VALUE_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[1].key").value(TEST_KEY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[1].value").value(TEST_VALUE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalEventId").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].eventType").value(TEST_EVENT_TYPE_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].uniquePatientKey").value(TEST_UNIQUE_PATIENT_KEY_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].key").value(TEST_KEY_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].value").value(TEST_VALUE_3))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[1].key").value(TEST_KEY_4))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[1].value").value(TEST_VALUE_4));
    }

    @Test
    @WithMockUser
    public void fetchClinicalEventsMetaReturnsBadRequestForInvalidRequest() throws Exception {
        
        ClinicalEventAttributeRequest clinicalEventAttributeRequest = new ClinicalEventAttributeRequest();
        clinicalEventAttributeRequest.setClinicalEventRequests(new HashSet<>());
        clinicalEventAttributeRequest.setPatientIdentifiers(new ArrayList<>());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/clinical-events-meta/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clinicalEventAttributeRequest)))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    private final List<ClinicalEvent> createExampleClinicalEventList() {
        List<ClinicalEvent> clinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent1 = new ClinicalEvent();
        clinicalEvent1.setClinicalEventId(TEST_CLINICAL_EVENT_ID_1);
        clinicalEvent1.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        clinicalEvent1.setPatientId(TEST_PATIENT_ID_1);
        clinicalEvent1.setEventType(TEST_EVENT_TYPE_1);
        clinicalEvent1.setStartDate(TEST_START_DATE_1);
        clinicalEvent1.setStopDate(TEST_END_DATE_1);
        List<ClinicalEventData> clinicalEventDataList1 = new ArrayList<>();
        ClinicalEventData clinicalEventData1 = new ClinicalEventData();
        clinicalEventData1.setClinicalEventId(TEST_CLINICAL_EVENT_ID_1);
        clinicalEventData1.setKey(TEST_KEY_1);
        clinicalEventData1.setValue(TEST_VALUE_1);
        clinicalEventDataList1.add(clinicalEventData1);
        ClinicalEventData clinicalEventData2 = new ClinicalEventData();
        clinicalEventData2.setClinicalEventId(TEST_CLINICAL_EVENT_ID_1);
        clinicalEventData2.setKey(TEST_KEY_2);
        clinicalEventData2.setValue(TEST_VALUE_2);
        clinicalEventDataList1.add(clinicalEventData2);
        clinicalEvent1.setAttributes(clinicalEventDataList1);
        clinicalEventList.add(clinicalEvent1);
        ClinicalEvent clinicalEvent2 = new ClinicalEvent();
        clinicalEvent2.setClinicalEventId(TEST_CLINICAL_EVENT_ID_2);
        clinicalEvent2.setStudyId(TEST_CANCER_STUDY_IDENTIFIER_1);
        clinicalEvent2.setPatientId(TEST_PATIENT_ID_2);
        clinicalEvent2.setEventType(TEST_EVENT_TYPE_2);
        clinicalEvent2.setStartDate(TEST_START_DATE_2);
        clinicalEvent2.setStopDate(TEST_END_DATE_2);
        List<ClinicalEventData> clinicalEventDataList2 = new ArrayList<>();
        ClinicalEventData clinicalEventData3 = new ClinicalEventData();
        clinicalEventData3.setClinicalEventId(TEST_CLINICAL_EVENT_ID_2);
        clinicalEventData3.setKey(TEST_KEY_3);
        clinicalEventData3.setValue(TEST_VALUE_3);
        clinicalEventDataList2.add(clinicalEventData3);
        ClinicalEventData clinicalEventData4 = new ClinicalEventData();
        clinicalEventData4.setClinicalEventId(TEST_CLINICAL_EVENT_ID_2);
        clinicalEventData4.setKey(TEST_KEY_4);
        clinicalEventData4.setValue(TEST_VALUE_4);
        clinicalEventDataList2.add(clinicalEventData4);
        clinicalEvent2.setAttributes(clinicalEventDataList2);
        clinicalEventList.add(clinicalEvent2);
        return clinicalEventList;
    }

    private final List<ClinicalEvent> createExampleClinicalEventMetaList() {
        List<ClinicalEvent> clinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent1 = new ClinicalEvent();
        clinicalEvent1.setUniquePatientKey(TEST_UNIQUE_PATIENT_KEY_1);
        clinicalEvent1.setEventType(TEST_EVENT_TYPE_1);
        List<ClinicalEventData> clinicalEventDataList1 = new ArrayList<>();
        ClinicalEventData clinicalEventData1 = new ClinicalEventData();
        clinicalEventData1.setClinicalEventId(TEST_CLINICAL_EVENT_ID_1);
        clinicalEventData1.setKey(TEST_KEY_1);
        clinicalEventData1.setValue(TEST_VALUE_1);
        clinicalEventDataList1.add(clinicalEventData1);
        ClinicalEventData clinicalEventData2 = new ClinicalEventData();
        clinicalEventData2.setClinicalEventId(TEST_CLINICAL_EVENT_ID_1);
        clinicalEventData2.setKey(TEST_KEY_2);
        clinicalEventData2.setValue(TEST_VALUE_2);
        clinicalEventDataList1.add(clinicalEventData2);
        clinicalEvent1.setAttributes(clinicalEventDataList1);
        clinicalEventList.add(clinicalEvent1);
        ClinicalEvent clinicalEvent2 = new ClinicalEvent();
        clinicalEvent2.setUniquePatientKey(TEST_UNIQUE_PATIENT_KEY_2);
        clinicalEvent2.setEventType(TEST_EVENT_TYPE_2);
        List<ClinicalEventData> clinicalEventDataList2 = new ArrayList<>();
        ClinicalEventData clinicalEventData3 = new ClinicalEventData();
        clinicalEventData3.setClinicalEventId(TEST_CLINICAL_EVENT_ID_2);
        clinicalEventData3.setKey(TEST_KEY_3);
        clinicalEventData3.setValue(TEST_VALUE_3);
        clinicalEventDataList2.add(clinicalEventData3);
        ClinicalEventData clinicalEventData4 = new ClinicalEventData();
        clinicalEventData4.setClinicalEventId(TEST_CLINICAL_EVENT_ID_2);
        clinicalEventData4.setKey(TEST_KEY_4);
        clinicalEventData4.setValue(TEST_VALUE_4);
        clinicalEventDataList2.add(clinicalEventData4);
        clinicalEvent2.setAttributes(clinicalEventDataList2);
        clinicalEventList.add(clinicalEvent2);
        return clinicalEventList;
    }
}
