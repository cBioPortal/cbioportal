package org.cbioportal.service.impl;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.PatientService;
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
public class SampleServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private SampleServiceImpl sampleService;

    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private StudyService studyService;
    @Mock
    private PatientService patientService;

    @Test
    public void getAllSamplesInStudy() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.getAllSamplesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION)).thenReturn(expectedSampleList);

        List<Sample> result = sampleService.getAllSamplesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT,
                DIRECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getAllSamplesInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleService.getAllSamplesInStudy(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaSamplesInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.getMetaSamplesInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.getMetaSamplesInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaSamplesInStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleService.getMetaSamplesInStudy(STUDY_ID);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getSampleInStudySampleNotFound() throws Exception {

        Mockito.when(sampleRepository.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenReturn(null);
        sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getSampleInStudyNotFound() throws Exception {

        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1);
    }

    @Test
    public void getSampleInStudy() throws Exception {

        Sample expectedSample = new Sample();
        Mockito.when(sampleRepository.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenReturn(expectedSample);
        Sample result = sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1);

        Assert.assertEquals(expectedSample, result);
    }

    @Test
    public void getAllSamplesOfPatientInStudy() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID, PROJECTION, PAGE_SIZE,
                PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedSampleList);

        List<Sample> result = sampleService.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID, PROJECTION, PAGE_SIZE,
                PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getAllSamplesOfPatientInStudyPatientNotFound() throws Exception {

        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID));
        sampleService.getAllSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION);
    }

    @Test
    public void getMetaSamplesOfPatientInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.getMetaSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.getMetaSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = PatientNotFoundException.class)
    public void getMetaSamplesOfPatientInStudyPatientNotFound() throws Exception {
        
        Mockito.when(patientService.getPatientInStudy(STUDY_ID, PATIENT_ID)).thenThrow(new PatientNotFoundException(
            STUDY_ID, PATIENT_ID));
        sampleService.getMetaSamplesOfPatientInStudy(STUDY_ID, PATIENT_ID);
    }

    @Test
    public void fetchSamples() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), PROJECTION))
                .thenReturn(expectedSampleList);

        List<Sample> result = sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), PROJECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test
    public void fetchMetaSamples() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.fetchMetaSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1)))
                .thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.fetchMetaSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1));

        Assert.assertEquals(expectedBaseMeta, result);
    }
}