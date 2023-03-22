package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.ClinicalEventTypeCount;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;

@RunWith(MockitoJUnitRunner.class)
public class ClinicalEventServiceImplTest extends BaseServiceImplTest {
    private static final String CLINICAL_EVENT_TYPE = "SERVICE";
    
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

        Mockito.when(clinicalEventRepository.getAllClinicalEventsOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedClinicalEventList);

        List<ClinicalEventData> expectedClinicalEventDataList = new ArrayList<>();
        ClinicalEventData clinicalEventData = new ClinicalEventData();
        clinicalEventData.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventDataList.add(clinicalEventData);

        Mockito.when(clinicalEventRepository.getDataOfClinicalEvents(Arrays.asList(CLINICAL_EVENT_ID)))
            .thenReturn(expectedClinicalEventDataList);

        List<ClinicalEvent> result = clinicalEventService.getAllClinicalEventsOfPatientInStudy(STUDY_ID, PATIENT_ID_1,
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(clinicalEvent, result.get(0));
        Assert.assertEquals(1, result.get(0).getAttributes().size());
        Assert.assertEquals(clinicalEventData, result.get(0).getAttributes().get(0));
    }

    @Test(expected = PatientNotFoundException.class)
    public void getAllClinicalEventsOfPatientInStudyPatientNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalEventService.getAllClinicalEventsOfPatientInStudy(STUDY_ID, PATIENT_ID_1, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaPatientClinicalEvents() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(clinicalEventRepository.getMetaPatientClinicalEvents(STUDY_ID, PATIENT_ID_1))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = clinicalEventService.getMetaPatientClinicalEvents(STUDY_ID, PATIENT_ID_1);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaPatientClinicalEventsPatientNotFound() throws Exception {
        
        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalEventService.getMetaPatientClinicalEvents(STUDY_ID, PATIENT_ID_1);
    }

    @Test
    public void getAllClinicalEventsInStudy() throws Exception {

        List<ClinicalEvent> expectedClinicalEventList = new ArrayList<>();
        ClinicalEvent clinicalEvent = new ClinicalEvent();
        clinicalEvent.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventList.add(clinicalEvent);

        Mockito.when(clinicalEventRepository.getAllClinicalEventsInStudy(STUDY_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedClinicalEventList);

        List<ClinicalEventData> expectedClinicalEventDataList = new ArrayList<>();
        ClinicalEventData clinicalEventData = new ClinicalEventData();
        clinicalEventData.setClinicalEventId(CLINICAL_EVENT_ID);
        expectedClinicalEventDataList.add(clinicalEventData);

        Mockito.when(clinicalEventRepository.getDataOfClinicalEvents(Arrays.asList(CLINICAL_EVENT_ID)))
            .thenReturn(expectedClinicalEventDataList);

        List<ClinicalEvent> result = clinicalEventService.getAllClinicalEventsInStudy(STUDY_ID,
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(clinicalEvent, result.get(0));
        Assert.assertEquals(1, result.get(0).getAttributes().size());
        Assert.assertEquals(clinicalEventData, result.get(0).getAttributes().get(0));
    }

    @Test
    public void getMetaClinicalEvents() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(clinicalEventRepository.getMetaClinicalEvents(STUDY_ID))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = clinicalEventService.getMetaClinicalEvents(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }
    
    @Test
    public void getPatientsSamplesPerClinicalEventType() {
        List<String> studyIds = Arrays.asList(STUDY_ID);
        List<String> sampleIds = Arrays.asList(SAMPLE_ID1);
        
        Map<String, Set<String>> patientsSamplesPerEventType = new HashMap<>();
        patientsSamplesPerEventType.put(CLINICAL_EVENT_TYPE, new HashSet<>(sampleIds));
        
        Mockito.when(clinicalEventRepository.getSamplesOfPatientsPerEventTypeInStudy(anyList(), anyList()))
            .thenReturn(patientsSamplesPerEventType);
        
        Assert.assertEquals(patientsSamplesPerEventType, 
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
        ce.setEventType(CLINICAL_EVENT_TYPE);
        
        Mockito.when(patientService.getPatientsOfSamples(anyList(), anyList()))
            .thenReturn(Arrays.asList(p));
        Mockito.when(clinicalEventRepository.getPatientsDistinctClinicalEventInStudies(anyList(), anyList()))
            .thenReturn(Arrays.asList(ce));
        
        List<ClinicalEventTypeCount> eventTypeCounts = clinicalEventService.getClinicalEventTypeCounts(studyIds, sampleIds);
        Assert.assertEquals(1, eventTypeCounts.size());
        int eventTypeCount = eventTypeCounts.get(0).getCount();
        Assert.assertEquals(1, eventTypeCount);
    }
}