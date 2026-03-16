package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cbioportal.domain.clinical_event.usecase.GetPatientClinicalEventsUseCase;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.ClinicalEventData;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ColumnStoreClinicalEventController.class, TestConfig.class})
public class ColumnStoreClinicalEventControllerTest {

  private static final String TEST_STUDY_ID = "test_study_1";
  private static final String TEST_PATIENT_ID = "test_patient_1";
  private static final String TEST_EVENT_TYPE_1 = "STATUS";
  private static final String TEST_EVENT_TYPE_2 = "TREATMENT";
  private static final int TEST_START_DATE_1 = 100;
  private static final int TEST_STOP_DATE_1 = 200;
  private static final int TEST_START_DATE_2 = 300;
  private static final int TEST_STOP_DATE_2 = 400;

  @MockitoBean private GetPatientClinicalEventsUseCase getPatientClinicalEventsUseCase;

  @Autowired private MockMvc mockMvc;

  @Test
  @WithMockUser
  public void getAllClinicalEventsOfPatientInStudy() throws Exception {
    List<ClinicalEvent> events = createClinicalEvents();

    when(getPatientClinicalEventsUseCase.execute(
            eq(TEST_STUDY_ID), eq(TEST_PATIENT_ID), any(), any(), any(), any(), any()))
        .thenReturn(events);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/column-store/studies/{studyId}/patients/{patientId}/clinical-events",
                    TEST_STUDY_ID,
                    TEST_PATIENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value(TEST_STUDY_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value(TEST_PATIENT_ID))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].eventType").value(TEST_EVENT_TYPE_1))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].startNumberOfDaysSinceDiagnosis")
                .value(TEST_START_DATE_1))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].endNumberOfDaysSinceDiagnosis")
                .value(TEST_STOP_DATE_1))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes", Matchers.hasSize(2)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].attributes[0].key").value("STATUS_KEY"))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].attributes[0].value")
                .value("radiographic_progression"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].eventType").value(TEST_EVENT_TYPE_2))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].key").value("AGENT"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].attributes[0].value").value("Madeupanib"));
  }

  @Test
  @WithMockUser
  public void getAllClinicalEventsReturnsEmptyListForPatientWithNoEvents() throws Exception {
    when(getPatientClinicalEventsUseCase.execute(
            eq(TEST_STUDY_ID), eq(TEST_PATIENT_ID), any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/column-store/studies/{studyId}/patients/{patientId}/clinical-events",
                    TEST_STUDY_ID,
                    TEST_PATIENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
  }

  @Test
  @WithMockUser
  public void getAllClinicalEventsReturns404ForNonExistentPatient() throws Exception {
    when(getPatientClinicalEventsUseCase.execute(
            eq(TEST_STUDY_ID), eq("nonexistent_patient"), any(), any(), any(), any(), any()))
        .thenThrow(new PatientNotFoundException(TEST_STUDY_ID, "nonexistent_patient"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/column-store/studies/{studyId}/patients/{patientId}/clinical-events",
                    TEST_STUDY_ID,
                    "nonexistent_patient")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message")
                .value("Patient not found in study " + TEST_STUDY_ID + ": nonexistent_patient"));
  }

  @Test
  @WithMockUser
  public void getAllClinicalEventsReturns404ForNonExistentStudy() throws Exception {
    when(getPatientClinicalEventsUseCase.execute(
            eq("fake_study"), eq(TEST_PATIENT_ID), any(), any(), any(), any(), any()))
        .thenThrow(new StudyNotFoundException("fake_study"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/column-store/studies/{studyId}/patients/{patientId}/clinical-events",
                    "fake_study",
                    TEST_PATIENT_ID)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$.message").value("Study not found: fake_study"));
  }

  @Test
  @WithMockUser
  public void getAllClinicalEventsWithMetaProjectionReturnsTotalCount() throws Exception {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(5);

    when(getPatientClinicalEventsUseCase.executeMeta(TEST_STUDY_ID, TEST_PATIENT_ID))
        .thenReturn(baseMeta);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    "/api/column-store/studies/{studyId}/patients/{patientId}/clinical-events",
                    TEST_STUDY_ID,
                    TEST_PATIENT_ID)
                .param("projection", "META")
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.header().string("total-count", "5"));
  }

  private List<ClinicalEvent> createClinicalEvents() {
    List<ClinicalEvent> events = new ArrayList<>();

    ClinicalEvent event1 = new ClinicalEvent();
    event1.setStudyId(TEST_STUDY_ID);
    event1.setPatientId(TEST_PATIENT_ID);
    event1.setEventType(TEST_EVENT_TYPE_1);
    event1.setStartDate(TEST_START_DATE_1);
    event1.setStopDate(TEST_STOP_DATE_1);
    List<ClinicalEventData> attrs1 = new ArrayList<>();
    ClinicalEventData attr1 = new ClinicalEventData();
    attr1.setKey("STATUS_KEY");
    attr1.setValue("radiographic_progression");
    attrs1.add(attr1);
    ClinicalEventData attr2 = new ClinicalEventData();
    attr2.setKey("SAMPLE_ID");
    attr2.setValue("SAMPLE-01");
    attrs1.add(attr2);
    event1.setAttributes(attrs1);
    events.add(event1);

    ClinicalEvent event2 = new ClinicalEvent();
    event2.setStudyId(TEST_STUDY_ID);
    event2.setPatientId(TEST_PATIENT_ID);
    event2.setEventType(TEST_EVENT_TYPE_2);
    event2.setStartDate(TEST_START_DATE_2);
    event2.setStopDate(TEST_STOP_DATE_2);
    List<ClinicalEventData> attrs2 = new ArrayList<>();
    ClinicalEventData attr3 = new ClinicalEventData();
    attr3.setKey("AGENT");
    attr3.setValue("Madeupanib");
    attrs2.add(attr3);
    event2.setAttributes(attrs2);
    events.add(event2);

    return events;
  }
}
