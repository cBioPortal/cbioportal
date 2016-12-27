package org.cbioportal.service.impl;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.exception.SampleNotFoundException;
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

    @Test
    public void getMetaSamplesInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.getMetaSamplesInStudy(STUDY_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.getMetaSamplesInStudy(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getSampleInStudyNotFound() throws Exception {

        Mockito.when(sampleRepository.getSampleInStudy(STUDY_ID, SAMPLE_ID)).thenReturn(null);
        sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID);
    }

    @Test
    public void getSampleInStudy() throws Exception {

        Sample expectedSample = new Sample();
        Mockito.when(sampleRepository.getSampleInStudy(STUDY_ID, SAMPLE_ID)).thenReturn(expectedSample);
        Sample result = sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID);

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

    @Test
    public void getMetaSamplesOfPatientInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.getMetaSamplesOfPatientInStudy(STUDY_ID, SAMPLE_ID)).thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.getMetaSamplesOfPatientInStudy(STUDY_ID, SAMPLE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void fetchSamples() throws Exception {

        List<Sample> expectedSampleList = new ArrayList<>();
        Sample sample = new Sample();
        expectedSampleList.add(sample);

        Mockito.when(sampleRepository.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID), PROJECTION))
                .thenReturn(expectedSampleList);

        List<Sample> result = sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID), PROJECTION);

        Assert.assertEquals(expectedSampleList, result);
    }

    @Test
    public void fetchMetaSamples() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(sampleRepository.fetchMetaSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID)))
                .thenReturn(expectedBaseMeta);
        BaseMeta result = sampleService.fetchMetaSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID));

        Assert.assertEquals(expectedBaseMeta, result);
    }
}