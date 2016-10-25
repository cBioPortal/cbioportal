package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.model.summary.ClinicalDataSummary;
import org.cbioportal.persistence.ClinicalDataRepository;
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

    @Test
    public void getAllClinicalDataOfSampleInStudy() throws Exception {

        List<SampleClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        SampleClinicalData sampleClinicalData = new SampleClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfSampleInStudy(Arrays.asList(STUDY_ID),
                Arrays.asList(SAMPLE_ID), ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedSampleClinicalDataList);

        List<SampleClinicalData> result = clinicalDataService.getAllClinicalDataOfSampleInStudy(STUDY_ID, SAMPLE_ID,
                ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test
    public void getMetaSampleClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaSampleClinicalData(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID),
                ATTRIBUTE_ID)).thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaSampleClinicalData(STUDY_ID, SAMPLE_ID, ATTRIBUTE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getAllClinicalDataOfPatientInStudy() throws Exception {

        List<PatientClinicalData> expectedPatientClinicalDataList = new ArrayList<>();
        PatientClinicalData patientClinicalData = new PatientClinicalData();
        expectedPatientClinicalDataList.add(patientClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfPatientInStudy(Arrays.asList(STUDY_ID),
                Arrays.asList(PATIENT_ID), ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedPatientClinicalDataList);

        List<PatientClinicalData> result = clinicalDataService.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID,
                ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedPatientClinicalDataList, result);
    }

    @Test
    public void getMetaPatientClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaPatientClinicalData(Arrays.asList(STUDY_ID),
                Arrays.asList(PATIENT_ID), ATTRIBUTE_ID)).thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID, ATTRIBUTE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getAllClinicalDataInStudySampleClinicalDataType() throws Exception {

        List<SampleClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        SampleClinicalData sampleClinicalData = new SampleClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfSampleInStudy(Arrays.asList(STUDY_ID), null,
                ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedSampleClinicalDataList);

        List<? extends ClinicalDataSummary> result = clinicalDataService.getAllClinicalDataInStudy(STUDY_ID,
                ATTRIBUTE_ID, ClinicalDataServiceImpl.SAMPLE_CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER,
                SORT, DIRECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test
    public void getAllClinicalDataInStudyPatientClinicalDataType() throws Exception {

        List<PatientClinicalData> expectedPatientClinicalDataList = new ArrayList<>();
        PatientClinicalData patientClinicalData = new PatientClinicalData();
        expectedPatientClinicalDataList.add(patientClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfPatientInStudy(Arrays.asList(STUDY_ID), null,
                ATTRIBUTE_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION))
                .thenReturn(expectedPatientClinicalDataList);

        List<? extends ClinicalDataSummary> result = clinicalDataService.getAllClinicalDataInStudy(STUDY_ID,
                ATTRIBUTE_ID, PATIENT_CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER,
                SORT, DIRECTION);

        Assert.assertEquals(expectedPatientClinicalDataList, result);
    }

    @Test
    public void getMetaAllClinicalDataSampleClinicalDataType() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.getMetaSampleClinicalData(Arrays.asList(STUDY_ID), null, ATTRIBUTE_ID))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaAllClinicalData(STUDY_ID, ATTRIBUTE_ID,
                ClinicalDataServiceImpl.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test
    public void getMetaAllClinicalDataPatientClinicalDataType() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.getMetaPatientClinicalData(Arrays.asList(STUDY_ID), null, ATTRIBUTE_ID))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaAllClinicalData(STUDY_ID, ATTRIBUTE_ID,
                PATIENT_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test
    public void fetchClinicalDataSampleClinicalDataType() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(SAMPLE_ID);

        List<SampleClinicalData> expectedSampleClinicalDataList = new ArrayList<>();
        SampleClinicalData sampleClinicalData = new SampleClinicalData();
        expectedSampleClinicalDataList.add(sampleClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfSampleInStudy(studyIds, sampleIds, ATTRIBUTE_ID,
                PROJECTION, 0, 0, null, null)).thenReturn(expectedSampleClinicalDataList);

        List<? extends ClinicalDataSummary> result = clinicalDataService.fetchClinicalData(studyIds, sampleIds,
                ATTRIBUTE_ID, ClinicalDataServiceImpl.SAMPLE_CLINICAL_DATA_TYPE, PROJECTION);

        Assert.assertEquals(expectedSampleClinicalDataList, result);
    }

    @Test
    public void fetchClinicalDataPatientClinicalDataType() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        List<String> patientIds = new ArrayList<>();
        patientIds.add(PATIENT_ID);

        List<PatientClinicalData> expectedPatientClinicalDataList = new ArrayList<>();
        PatientClinicalData patientClinicalData = new PatientClinicalData();
        expectedPatientClinicalDataList.add(patientClinicalData);

        Mockito.when(clinicalDataRepository.getAllClinicalDataOfPatientInStudy(studyIds, patientIds, ATTRIBUTE_ID,
                PROJECTION, 0, 0, null, null)).thenReturn(expectedPatientClinicalDataList);

        List<? extends ClinicalDataSummary> result = clinicalDataService.fetchClinicalData(studyIds, patientIds,
                ATTRIBUTE_ID, PATIENT_CLINICAL_DATA_TYPE, PROJECTION);

        Assert.assertEquals(expectedPatientClinicalDataList, result);
    }

    @Test
    public void fetchMetaClinicalDataSampleClinicalDataType() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add(SAMPLE_ID);

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.getMetaSampleClinicalData(studyIds, sampleIds, ATTRIBUTE_ID))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.fetchMetaClinicalData(studyIds, sampleIds, ATTRIBUTE_ID,
                ClinicalDataServiceImpl.SAMPLE_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }

    @Test
    public void fetchMetaClinicalDataPatientClinicalDataType() throws Exception {

        List<String> studyIds = new ArrayList<>();
        studyIds.add(STUDY_ID);
        List<String> patientIds = new ArrayList<>();
        patientIds.add(PATIENT_ID);

        BaseMeta expectedBaseMeta = new BaseMeta();
        expectedBaseMeta.setTotalCount(5);

        Mockito.when(clinicalDataRepository.getMetaPatientClinicalData(studyIds, patientIds, ATTRIBUTE_ID))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.fetchMetaClinicalData(studyIds, patientIds, ATTRIBUTE_ID,
                PATIENT_CLINICAL_DATA_TYPE);

        Assert.assertEquals((Integer) 5, result.getTotalCount());
    }
}