package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.*;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

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
    @Mock
    private ClinicalAttributeService clinicalAttributeService;
    @Spy
    private ClinicalAttributeUtil clinicalAttributeUtil = new ClinicalAttributeUtil();

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

        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1))
                .thenThrow(new SampleNotFoundException(STUDY_ID, SAMPLE_ID1));
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

        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1))
                .thenThrow(new SampleNotFoundException(STUDY_ID, SAMPLE_ID1));
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

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1))
                .thenThrow(new PatientNotFoundException(STUDY_ID, PATIENT_ID_1));
        clinicalDataService.getAllClinicalDataOfPatientInStudy(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID_1,
                PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaPatientClinicalData() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(clinicalDataRepository.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1, CLINICAL_ATTRIBUTE_ID_1))
                .thenReturn(expectedBaseMeta);

        BaseMeta result = clinicalDataService.getMetaPatientClinicalData(STUDY_ID, PATIENT_ID_1,
                CLINICAL_ATTRIBUTE_ID_1);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaPatientClinicalDataPatientNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID_1))
                .thenThrow(new PatientNotFoundException(STUDY_ID, PATIENT_ID_1));
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

        List<ClinicalData> result = clinicalDataService.getAllClinicalDataInStudy(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1,
                CLINICAL_DATA_TYPE, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

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

        Mockito.when(
                clinicalDataRepository.getMetaAllClinicalData(STUDY_ID, CLINICAL_ATTRIBUTE_ID_1, CLINICAL_DATA_TYPE))
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

        ClinicalAttribute clinicalAttribute1 = new ClinicalAttribute();
        clinicalAttribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalAttribute1.setPatientAttribute(true);
        ClinicalAttribute clinicalAttribute2 = new ClinicalAttribute();
        clinicalAttribute2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalAttribute2.setPatientAttribute(true);
        ClinicalAttribute clinicalAttribute3 = new ClinicalAttribute();
        clinicalAttribute3.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        clinicalAttribute3.setPatientAttribute(false);

        ClinicalAttribute clinicalAttribute4 = new ClinicalAttribute();
        clinicalAttribute4.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalAttribute4.setPatientAttribute(false);

        Mockito.when(clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(
                Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2, CLINICAL_ATTRIBUTE_ID_3)))
                .thenReturn(
                        Arrays.asList(clinicalAttribute1, clinicalAttribute2, clinicalAttribute3, clinicalAttribute4));

        ClinicalDataCount clinicalDataCount2 = new ClinicalDataCount();
        clinicalDataCount2.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalDataCount2.setValue("NA");
        clinicalDataCount2.setCount(1);
        ClinicalDataCount clinicalDataCount5 = new ClinicalDataCount();
        clinicalDataCount5.setAttributeId(CLINICAL_ATTRIBUTE_ID_3);
        clinicalDataCount5.setValue("N/A");
        clinicalDataCount5.setCount(3);
        ClinicalDataCount clinicalDataCount6 = new ClinicalDataCount();
        clinicalDataCount6.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalDataCount6.setValue("value1");
        clinicalDataCount6.setCount(2);

        Mockito.when(clinicalDataRepository.fetchClinicalDataCounts(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_3, CLINICAL_ATTRIBUTE_ID_1), "SAMPLE", "SUMMARY"))
                .thenReturn(Arrays.asList(clinicalDataCount2, clinicalDataCount5, clinicalDataCount6));

        ClinicalDataCount clinicalDataCount3 = new ClinicalDataCount();
        clinicalDataCount3.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalDataCount3.setValue("value2");
        clinicalDataCount3.setCount(1);
        ClinicalDataCount clinicalDataCount4 = new ClinicalDataCount();
        clinicalDataCount4.setAttributeId(CLINICAL_ATTRIBUTE_ID_2);
        clinicalDataCount4.setValue("value3");
        clinicalDataCount4.setCount(1);

        Mockito.when(clinicalDataRepository.fetchClinicalDataCounts(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), Arrays.asList(CLINICAL_ATTRIBUTE_ID_2), "PATIENT",
                "SUMMARY")).thenReturn(Arrays.asList(clinicalDataCount3, clinicalDataCount4));

        ClinicalDataCount clinicalDataCount1 = new ClinicalDataCount();
        clinicalDataCount1.setAttributeId(CLINICAL_ATTRIBUTE_ID_1);
        clinicalDataCount1.setValue("value1");
        clinicalDataCount1.setCount(2);

        Mockito.when(clinicalDataRepository.fetchClinicalDataCounts(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), Arrays.asList(CLINICAL_ATTRIBUTE_ID_1), "PATIENT",
                "DETAILED")).thenReturn(Arrays.asList(clinicalDataCount1));

        List<Patient> patients = new ArrayList<>();
        Patient patient1 = new Patient();
        patient1.setStableId(PATIENT_ID_1);
        patient1.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient1);
        Patient patient2 = new Patient();
        patient2.setStableId(PATIENT_ID_2);
        patient2.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient2);
        Patient patient3 = new Patient();
        patient3.setStableId(PATIENT_ID_3);
        patient3.setCancerStudyIdentifier(STUDY_ID);
        patients.add(patient3);

        Mockito.when(patientService.getPatientsOfSamples(Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID),
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3))).thenReturn(patients);

        List<ClinicalDataCountItem> result = clinicalDataService.fetchClinicalDataCounts(
                Arrays.asList(STUDY_ID, STUDY_ID, STUDY_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                Arrays.asList(CLINICAL_ATTRIBUTE_ID_1, CLINICAL_ATTRIBUTE_ID_2, CLINICAL_ATTRIBUTE_ID_3));

        Assert.assertEquals(3, result.size());

        ClinicalDataCountItem counts3 = result.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_3, counts3.getAttributeId());
        List<ClinicalDataCount> clinicalDataCounts3 = counts3.getCounts();
        Assert.assertEquals(1, clinicalDataCounts3.size());
        ClinicalDataCount count5 = clinicalDataCounts3.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_3, count5.getAttributeId());
        Assert.assertEquals("NA", count5.getValue());
        Assert.assertEquals((Integer) 3, count5.getCount());

        ClinicalDataCountItem counts2 = result.get(1);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_2, counts2.getAttributeId());
        List<ClinicalDataCount> clinicalDataCounts2 = counts2.getCounts();
        Assert.assertEquals(3, clinicalDataCounts2.size());
        ClinicalDataCount count3 = clinicalDataCounts2.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_2, count3.getAttributeId());
        Assert.assertEquals("value2", count3.getValue());
        Assert.assertEquals((Integer) 1, count3.getCount());
        ClinicalDataCount count4 = clinicalDataCounts2.get(1);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_2, count4.getAttributeId());
        Assert.assertEquals("value3", count4.getValue());
        Assert.assertEquals((Integer) 1, count4.getCount());

        ClinicalDataCountItem counts1 = result.get(2);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_1, counts1.getAttributeId());
        List<ClinicalDataCount> clinicalDataCounts1 = counts1.getCounts();
        Assert.assertEquals(1, clinicalDataCounts1.size());
        ClinicalDataCount count1 = clinicalDataCounts1.get(0);
        Assert.assertEquals(CLINICAL_ATTRIBUTE_ID_1, count1.getAttributeId());
        Assert.assertEquals("value1", count1.getValue());
        Assert.assertEquals((Integer) 4, count1.getCount());

    }
}
