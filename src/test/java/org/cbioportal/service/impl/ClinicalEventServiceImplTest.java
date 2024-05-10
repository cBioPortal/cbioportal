package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.Patient;
import org.cbioportal.model.SurvivalEvent;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClinicalEventServiceImplTest extends BaseServiceImplTest {
    private static final String TEST_CLINICAL_EVENT_TYPE = "SERVICE";
    private static final String TEST_SURVIVAL_PREFIX = "survival_prefix";
    
    @InjectMocks
    private ClinicalEventServiceImpl clinicalEventService;
    
    @Mock
    private ClinicalEventRepository clinicalEventRepository;
    @Mock
    private PatientService patientService;
    
    @Test
    public void getAllClinicalEventsOfPatientInStudy() throws Exception {

        List<ClinicalEvent> expectedClinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent = new ClinicalEvent();
        clinicalEvent.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventList.add(clinicalEvent);

        when(clinicalEventRepository.getAllClinicalEventsOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedClinicalEventList);

        List<ClinicalEventData> expectedClinicalEventDataList = new ArrayList<>();
        ClinicalEventData clinicalEventData = new ClinicalEventData();
        clinicalEventData.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventDataList.add(clinicalEventData);

        when(clinicalEventRepository.getDataOfClinicalEvents(Arrays.asList(CLINICAL_EVENT_ID)))
            .thenReturn(expectedClinicalEventDataList);

        List<ClinicalEvent> result = clinicalEventService.getAllClinicalEventsOfPatientInStudy(STUDY_ID, PATIENT_ID_1,
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        assertEquals(1, result.size());
        assertEquals(clinicalEvent, result.get(0));
        assertEquals(1, result.get(0).getAttributes().size());
        assertEquals(clinicalEventData, result.get(0).getAttributes().get(0));
    }

    @Test(expected = PatientNotFoundException.class)
    public void getAllClinicalEventsOfPatientInStudyPatientNotFound() throws Exception {

        when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalEventService.getAllClinicalEventsOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaPatientClinicalEvents() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        when(clinicalEventRepository.getMetaPatientClinicalEvents(STUDY_ID, PATIENT_ID_1))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = clinicalEventService.getMetaPatientClinicalEvents(STUDY_ID, PATIENT_ID_1);

        assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaPatientClinicalEventsPatientNotFound() throws Exception {
        
        when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalEventService.getMetaPatientClinicalEvents(STUDY_ID, PATIENT_ID_1);
    }

    @Test
    public void getAllClinicalEventsInStudy() throws Exception {

        List<ClinicalEvent> expectedClinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent = new ClinicalEvent();
        clinicalEvent.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventList.add(clinicalEvent);

        when(clinicalEventRepository.getAllClinicalEventsInStudy(STUDY_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedClinicalEventList);

        List<ClinicalEventData> expectedClinicalEventDataList = new ArrayList<>();
        ClinicalEventData clinicalEventData = new ClinicalEventData();
        clinicalEventData.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventDataList.add(clinicalEventData);

        when(clinicalEventRepository.getDataOfClinicalEvents(Arrays.asList(CLINICAL_EVENT_ID)))
            .thenReturn(expectedClinicalEventDataList);

        List<ClinicalEvent> result = clinicalEventService.getAllClinicalEventsInStudy(STUDY_ID,
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        assertEquals(1, result.size());
        assertEquals(clinicalEvent, result.get(0));
        assertEquals(1, result.get(0).getAttributes().size());
        assertEquals(clinicalEventData, result.get(0).getAttributes().get(0));
    }

    @Test
    public void getMetaClinicalEvents() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        when(clinicalEventRepository.getMetaClinicalEvents(STUDY_ID))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = clinicalEventService.getMetaClinicalEvents(STUDY_ID);

        assertEquals(expectedBaseMeta, result);
    }
    
    @Test
    public void getPatientsSamplesPerClinicalEventType() {
        List<String> studyIds = Arrays.asList(STUDY_ID);
        List<String> sampleIds = Arrays.asList(SAMPLE_ID1);
        
        Map<String, Set<String>> patientsSamplesPerEventType = new HashMap<>();
        patientsSamplesPerEventType.put(TEST_CLINICAL_EVENT_TYPE, new HashSet<>(sampleIds));
        
        when(clinicalEventRepository.getSamplesOfPatientsPerEventTypeInStudy(anyList(), anyList()))
            .thenReturn(patientsSamplesPerEventType);
        
        assertEquals(patientsSamplesPerEventType, 
            clinicalEventService.getPatientsSamplesPerClinicalEventType(studyIds, sampleIds));
    }
    
    @Test
    public void getClinicalEventTypeCounts() {
        List<String> studyIds = Arrays.asList(STUDY_ID);
        List<String> sampleIds = Arrays.asList(SAMPLE_ID1);
        
        Patient p = new Patient();
        p.setCancerStudyIdentifier(STUDY_ID);
        p.setStableId(PATIENT_ID_1);
        
        ClinicalEvent ce = new ClinicalEvent();
        ce.setEventType(TEST_CLINICAL_EVENT_TYPE);
        
        when(patientService.getPatientsOfSamples(anyList(), anyList()))
            .thenReturn(Arrays.asList(p));
        when(clinicalEventRepository.getPatientsDistinctClinicalEventInStudies(anyList(), anyList()))
            .thenReturn(Arrays.asList(ce));
        
        List<ClinicalEventTypeCount> eventTypeCounts = clinicalEventService.getClinicalEventTypeCounts(studyIds, sampleIds);
        assertEquals(1, eventTypeCounts.size());
        int eventTypeCount = eventTypeCounts.get(0).getCount();
        assertEquals(1, eventTypeCount);
    }

    @Test
    public void getSurvivalDataReturnsCorrectDataWhenEndEventsExist() throws Exception {
        List<String> studyIds = List.of(STUDY_ID);
        List<String> patientIds = Arrays.asList(PATIENT_ID_1, PATIENT_ID_2, PATIENT_ID_3);
        
        List<ClinicalEvent> clinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent1 = new ClinicalEvent();
        clinicalEvent1.setStudyId(STUDY_ID);
        clinicalEvent1.setPatientId(PATIENT_ID_1);
        clinicalEvent1.setStartDate(0);
        clinicalEvent1.setStopDate(500);

        ClinicalEvent clinicalEvent2 = new ClinicalEvent();
        clinicalEvent2.setStudyId(STUDY_ID);
        clinicalEvent2.setPatientId(PATIENT_ID_2);
        clinicalEvent2.setStartDate(100);
        clinicalEvent2.setStopDate(1000);
        
        clinicalEventList.add(clinicalEvent1);
        clinicalEventList.add(clinicalEvent2);
        
        SurvivalEvent survivalEvent = new SurvivalEvent();
        survivalEvent.setStartClinicalEventsMeta(clinicalEventList);
        survivalEvent.setStartPositionIdentifier(ClinicalEvent::getStartDate);
        survivalEvent.setEndClinicalEventsMeta(clinicalEventList);
        survivalEvent.setEndPositionIdentifier(ClinicalEvent::getStopDate);
        survivalEvent.setCensoredClinicalEventsMeta(clinicalEventList);
        survivalEvent.setCensoredPositionIdentifier(ClinicalEvent::getStopDate);

        when(clinicalEventRepository.getTimelineEvents(anyList(), anyList(), anyList()))
            .thenReturn(Arrays.asList(clinicalEvent1, clinicalEvent2));

        List<ClinicalData> result = clinicalEventService.getSurvivalData(studyIds, patientIds, TEST_SURVIVAL_PREFIX, survivalEvent);

        assertEquals(4, result.size());
        assertEquals("survival_prefix_MONTHS", result.get(0).getAttrId());
        assertEquals("16.447368421052634", result.get(0).getAttrValue());
        assertEquals("survival_prefix_STATUS", result.get(1).getAttrId());
        assertEquals("1:EVENT", result.get(1).getAttrValue());
        assertEquals("survival_prefix_MONTHS", result.get(2).getAttrId());
        assertEquals("29.60526315789474", result.get(2).getAttrValue());
        assertEquals("survival_prefix_STATUS", result.get(3).getAttrId());
        assertEquals("1:EVENT", result.get(3).getAttrValue());
    }

    @Test
    public void getSurvivalDataReturnsEmptyListWhenNoEventsExist() throws Exception {
        List<String> studyIds = List.of(STUDY_ID);
        List<String> patientIds = Arrays.asList(PATIENT_ID_1, PATIENT_ID_2, PATIENT_ID_3);

        List<ClinicalEvent> clinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent1 = new ClinicalEvent();
        clinicalEvent1.setStudyId(STUDY_ID);
        clinicalEvent1.setPatientId(PATIENT_ID_1);
        clinicalEvent1.setStartDate(0);
        clinicalEvent1.setStopDate(500);

        ClinicalEvent clinicalEvent2 = new ClinicalEvent();
        clinicalEvent2.setStudyId(STUDY_ID);
        clinicalEvent2.setPatientId(PATIENT_ID_2);
        clinicalEvent2.setStartDate(100);
        clinicalEvent2.setStopDate(1000);

        clinicalEventList.add(clinicalEvent1);
        clinicalEventList.add(clinicalEvent2);

        SurvivalEvent survivalEvent = new SurvivalEvent();
        survivalEvent.setStartClinicalEventsMeta(clinicalEventList);
        survivalEvent.setStartPositionIdentifier(ClinicalEvent::getStartDate);
        survivalEvent.setEndClinicalEventsMeta(clinicalEventList);
        survivalEvent.setEndPositionIdentifier(ClinicalEvent::getStopDate);
        survivalEvent.setCensoredClinicalEventsMeta(clinicalEventList);
        survivalEvent.setCensoredPositionIdentifier(ClinicalEvent::getStopDate);

        when(clinicalEventRepository.getTimelineEvents(anyList(), anyList(), anyList()))
            .thenReturn(new ArrayList<>());

        List<ClinicalData> result = clinicalEventService.getSurvivalData(studyIds, patientIds, TEST_SURVIVAL_PREFIX, survivalEvent);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getClinicalEventsMetaReturnsCorrectDataForValidInput() throws Exception {
        List<String> studyIds = List.of(STUDY_ID);
        List<String> patientIds = Arrays.asList(PATIENT_ID_1, PATIENT_ID_2);
        List<ClinicalEvent> clinicalEvents = new ArrayList<>();
        ClinicalEvent event1 = new ClinicalEvent();
        event1.setStudyId(STUDY_ID);
        event1.setPatientId(PATIENT_ID_1);
        ClinicalEvent event2 = new ClinicalEvent();
        event2.setStudyId(STUDY_ID);
        event2.setPatientId(PATIENT_ID_2);
        clinicalEvents.add(event1);
        clinicalEvents.add(event2);

        when(clinicalEventRepository.getClinicalEventsMeta(anyList(), anyList(), anyList())).thenReturn(clinicalEvents);

        List<ClinicalEvent> result = clinicalEventService.getClinicalEventsMeta(studyIds, patientIds, clinicalEvents);

        assertEquals(2, result.size());
        assertEquals("study_id", result.get(0).getStudyId());
        assertEquals("patient_id1", result.get(0).getPatientId());
        assertEquals("study_id", result.get(1).getStudyId());
        assertEquals("patient_id2", result.get(1).getPatientId());
    }

    @Test
    public void getClinicalEventsMetaReturnsEmptyListForInvalidInput() throws Exception {
        List<String> studyIds = List.of(STUDY_ID);
        List<String> patientIds = Arrays.asList(PATIENT_ID_1, PATIENT_ID_2);
        List<ClinicalEvent> clinicalEvents = new ArrayList<>();

        when(clinicalEventRepository.getClinicalEventsMeta(anyList(), anyList(), anyList())).thenReturn(new ArrayList<>());

        List<ClinicalEvent> result = clinicalEventService.getClinicalEventsMeta(studyIds, patientIds, clinicalEvents);

        assertTrue(result.isEmpty());
    }
}