package org.cbioportal.service.impl;

import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
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
import java.util.Map;

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
            CLINICAL_ATTRIBUTE_ID_1, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedSampleClinicalDataList);

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataOfSampleInStudy(STUDY_ID, SAMPLE_ID1,
            CLINICAL_ATTRIBUTE_ID_1, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getAllClinicalDataOfSampleInStudySampleNotFound() throws Exception {
        
        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        clinicalDataService.getAllClinicalDataOfSampleInStudy(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID_1, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaSampleClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID_1))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID_1);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getMetaSampleClinicalDataSampleNotFound() throws Exception {

        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        clinicalDataService.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID1, CLINICAL_ATTRIBUTE_ID_1);
    }

    @Test
    public void getAllClinicalDataOfPatientInStudy() throws Exception {

        List<ClinicalData> expectedPatientClinicalDataList = new ArrayList<>();
        ClinicalData patientClinicalData = new ClinicalData();
        expectedPatientClinicalDataList.add(patientClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1, 
            CLINICAL_ATTRIBUTE_ID_1, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
            .thenReturn(expectedPatientClinicalDataList);

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1,
            CLINICAL_ATTRIBUTE_ID_1, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedPatientClinicalDataList, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getAllClinicalDataOfPatientInStudyPatientNotFound() throws Exception {
        
        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalDataService.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID_1, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaPatientClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID_1))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID_1);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaPatientClinicalDataPatientNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID_1));
        clinicalDataService.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID_1);
    }

    @Test
    public void getAllClinicalDataInStudy() throws Exception {

        List<ClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        ClinicalData sampleClinicalData = new ClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataInStudy(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1,
                CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedSampleClinicalDataList);

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataInStudy(STUDY_ID,
            CLINICAL_ATTRIBUTE_ID_1, CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER,
                SORT, DIRECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllClinicalDataInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalDataService.getAllClinicalDataInStudy(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1, CLINICAL_DATA_TYPE, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaAllClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1, CLINICAL_DATA_TYPE))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1, 
            CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaAllClinicalDataStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        clinicalDataService.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1, CLINICAL_DATA_TYPE);
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
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_1), CLINICAL_DATA_TYPE, PROJECTION))
            .thenReturn(expectedPatientClinicalDataList);

        List<ClinicalData> result = clinicalDataService.fetchClinicalData(studyIds, patientIds,
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_1), CLINICAL_DATA_TYPE, PROJECTION);

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
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_1), CLINICAL_DATA_TYPE)).thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.fetchMetaClinicalData(studyIds, patientIds, 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_1), CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test
    public void fetchClinicalDataCounts() throws Exception {

        List<ClinicalDataCount> clinicalDataCounts = new ArrayList<>();
        ClinicalDataCount clinicalDataCount1 = new ClinicalDataCount();
        clinicalDataCount1.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalDataCount1.setValue("value1");
        clinicalDataCount1.setCount(2);
        clinicalDataCounts.add(clinicalDataCount1);
        ClinicalDataCount clinicalDataCount2 = new ClinicalDataCount();
        clinicalDataCount2.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalDataCount2.setValue("NA");
        clinicalDataCount2.setCount(1);
        clinicalDataCounts.add(clinicalDataCount2);
        ClinicalDataCount clinicalDataCount3 = new ClinicalDataCount();
        clinicalDataCount3.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalDataCount3.setValue("value2");
        clinicalDataCount3.setCount(1);
        clinicalDataCounts.add(clinicalDataCount3);
        ClinicalDataCount clinicalDataCount4 = new ClinicalDataCount();
        clinicalDataCount4.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalDataCount4.setValue("value3");
        clinicalDataCount4.setCount(1);
        clinicalDataCounts.add(clinicalDataCount4);
        ClinicalDataCount clinicalDataCount5 = new ClinicalDataCount();
        clinicalDataCount5.setAttributeId(CLINICAL_ATTRIBUTE_ID_3);
        clinicalDataCount5.setValue("N/A");
        clinicalDataCount5.setCount(3);
        clinicalDataCounts.add(clinicalDataCount5);

        Mockito.when(clinicalDataRepository.fetchClinicalDataCounts(STUDY_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), 
            Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2, CLINICAL_ATTRIBUTE_ID_3), "PATIENT"))
            .thenReturn(clinicalDataCounts);

        Mockito.when(patientService.getPatientIdsOfSamples(Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3)))
            .thenReturn(Arrays.asList(PATIENT_ID_1, PATIENT_ID_2, PATIENT_ID_3));

        Map<String, List<ClinicalDataCount>> result = clinicalDataService.fetchClinicalDataCounts(STUDY_ID, 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2, 
            CLINICAL_ATTRIBUTE_ID_3), "PATIENT");

        Assert.assertEquals(3, result.size());
        List<ClinicalDataCount> counts1 = result.get(CLINICAL_ATTRIBUTE_ID_1);
        Assert.assertEquals(2, counts1.size());
        ClinicalDataCount count1 = counts1.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_1, count1.getAttributeId());
        Assert.assertEquals("value1", count1.getValue());
        Assert.assertEquals((Integer) 2, count1.getCount());
        ClinicalDataCount count2 = counts1.get(1);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_1, count2.getAttributeId());
        Assert.assertEquals("NA", count2.getValue());
        Assert.assertEquals((Integer) 1, count2.getCount());
        List<ClinicalDataCount> counts2 = result.get(CLINICAL_ATTRIBUTE_ID_2);
        Assert.assertEquals(3, counts2.size());
        ClinicalDataCount count3 = counts2.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_2, count3.getAttributeId());
        Assert.assertEquals("value2", count3.getValue());
        Assert.assertEquals((Integer) 1, count3.getCount());
        ClinicalDataCount count4 = counts2.get(1);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_2, count4.getAttributeId());
        Assert.assertEquals("value3", count4.getValue());
        Assert.assertEquals((Integer) 1, count4.getCount());
        ClinicalDataCount count5 = counts2.get(2);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_2, count5.getAttributeId());
        Assert.assertEquals("NA", count5.getValue());
        Assert.assertEquals((Integer) 1, count5.getCount());
        List<ClinicalDataCount> counts3 = result.get(CLINICAL_ATTRIBUTE_ID_3);
        Assert.assertEquals(1, counts3.size());
        ClinicalDataCount count6 = counts3.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_3, count6.getAttributeId());
        Assert.assertEquals("NA", count6.getValue());
        Assert.assertEquals((Integer) 3, count6.getCount());
    }
}
