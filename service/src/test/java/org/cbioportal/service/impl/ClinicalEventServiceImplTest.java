package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalEvent;
import org.cbioportal.model.ClinicalEventData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalEventRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ClinicalEventServiceImplTest extends BaseServiceImplTest {
    
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
}