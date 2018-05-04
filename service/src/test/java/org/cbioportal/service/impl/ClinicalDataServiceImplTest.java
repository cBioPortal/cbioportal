package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ClinicalDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private ClinicalDataServiceImpl clinicalDataService;

    @Mock
    private ClinicalDataRepository clinicalDataRepository;
    @Mock
    private StudyService studyService;
    @Mock
    private PatientService patientService;
    @Mock
    private SampleService sampleService;

    @Test
    public void getAllClinicalDataOfSampleInStudy() throws Exception {

        List<ClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData = new ClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfSampleInStudy(STUDY_ID, SAMPLE_ID1, 
            CLINICAL_ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedSampleClinicalDataList);

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataOfSampleInStudy(STUDY_ID, SAMPLE_ID1,
            CLINICAL_ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getAllClinicalDataOfSampleInStudySampleNotFound() throws Exception {
        
        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        clinicalDataService.getAllClinicalDataOfSampleInStudy(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaSampleClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getMetaSampleClinicalDataSampleNotFound() throws Exception {

        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        clinicalDataService.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID);
    }

    @Test
    public void getAllClinicalDataOfPatientInStudy() throws Exception {

        List<ClinicalData> expectedPatientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData = new ClinicalData();
        expectedPatientClinicalDataList.add(patientClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1, 
            CLINICAL_ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedPatientClinicalDataList);

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1,
            CLINICAL_ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedPatientClinicalDataList, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getAllClinicalDataOfPatientInStudyPatientNotFound() throws Exception {
        
        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalDataService.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaPatientClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaPatientClinicalDataPatientNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalDataService.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID);
    }

    @Test
    public void getAllClinicalDataInStudy() throws Exception {

        List<ClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData = new ClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataInStudy(STUDY_ID, CLINICAL_ATTRIBUTE_ID,
                CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedSampleClinicalDataList);

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataInStudy(STUDY_ID,
            CLINICAL_ATTRIBUTE_ID, CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER,
                SORT, DIRECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllClinicalDataInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalDataService.getAllClinicalDataInStudy(STUDY_ID, CLINICAL_ATTRIBUTE_ID, CLINICAL_DATA_TYPE, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaAllClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID, CLINICAL_DATA_TYPE))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID, 
            CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaAllClinicalDataStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalDataService.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID, CLINICAL_DATA_TYPE);
    }

    @Test
    public void fetchClinicalDataPatientClinicalDataType() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        List<String> patientIds = new ArrayList<>();
        patientIds.add(PATIENT_ID_1);

        List<ClinicalData> expectedPatientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData = new ClinicalData();
        expectedPatientClinicalDataList.add(patientClinicalData);

        Mockito.when(clinicalDataRepository.fetchClinicalData(studyIds, patientIds, 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID), CLINICAL_DATA_TYPE, PROJECTION))
            .thenReturn(expectedPatientClinicalDataList);

        List<ClinicalData> result = clinicalDataService.fetchClinicalData(studyIds, patientIds,
            Arrays.asList(CLINICAL_ATTRIBUTE_ID), CLINICAL_DATA_TYPE, PROJECTION);

        Assert.assertEquals(expectedPatientClinicalDataList, result);
    }

    @Test
    public void fetchMetaClinicalDataPatientClinicalDataType() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        List<String> patientIds = new ArrayList<>();
        patientIds.add(PATIENT_ID_1);

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.fetchMetaClinicalData(studyIds, patientIds, 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID), CLINICAL_DATA_TYPE)).thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.fetchMetaClinicalData(studyIds, patientIds, 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID), CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }
}