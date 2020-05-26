package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.SampleNotFoundException;
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
public class CopyNumberSegmentServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private CopyNumberSegmentServiceImpl copyNumberSegmentService;
    
    @Mock
    private CopyNumberSegmentRepository copyNumberSegmentRepository;
    @Mock
    private SampleService sampleService;

    @Test
    public void getCopyNumberSegmentsInSampleInStudy() throws Exception {

        List<CopyNumberSeg> expectedCopyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg = new CopyNumberSeg();
        expectedCopyNumberSegList.add(copyNumberSeg);

        Mockito.when(copyNumberSegmentRepository.getCopyNumberSegmentsInSampleInStudy(
            STUDY_ID, SAMPLE_ID1, null, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedCopyNumberSegList);
        
        List<CopyNumberSeg> result = copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(
            STUDY_ID, SAMPLE_ID1, null, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getCopyNumberSegmentsInSampleInStudySampleNotFound() throws Exception {
        
        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID1, null, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaCopyNumberSegmentsInSampleInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(copyNumberSegmentRepository.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID1, null))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = copyNumberSegmentService.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID1, null);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getMetaCopyNumberSegmentsInSampleInStudySampleNotFound() throws Exception {
        
        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        copyNumberSegmentService.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID1, null);
    }

    @Test
    public void fetchCopyNumberSegments() throws Exception {

        List<CopyNumberSeg> expectedCopyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg = new CopyNumberSeg();
        expectedCopyNumberSegList.add(copyNumberSeg);

        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Arrays.asList(STUDY_ID), 
            Arrays.asList(PATIENT_ID_1), null, PROJECTION)).thenReturn(expectedCopyNumberSegList);

        List<CopyNumberSeg> result = copyNumberSegmentService.fetchCopyNumberSegments(Arrays.asList(STUDY_ID),
            Arrays.asList(PATIENT_ID_1), null, PROJECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }

    @Test
    public void fetchMetaCopyNumberSegments() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(copyNumberSegmentRepository.fetchMetaCopyNumberSegments(Arrays.asList(STUDY_ID), 
            Arrays.asList(PATIENT_ID_1), null)).thenReturn(expectedBaseMeta);
        BaseMeta result = copyNumberSegmentService.fetchMetaCopyNumberSegments(Arrays.asList(STUDY_ID), 
            Arrays.asList(PATIENT_ID_1), null);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getCopyNumberSegmentsBySampleListId() throws Exception {

        List<CopyNumberSeg> expectedCopyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg = new CopyNumberSeg();
        expectedCopyNumberSegList.add(copyNumberSeg);

        Mockito.when(copyNumberSegmentRepository.getCopyNumberSegmentsBySampleListId(STUDY_ID, SAMPLE_LIST_ID, null,
            PROJECTION)).thenReturn(expectedCopyNumberSegList);

        List<CopyNumberSeg> result = copyNumberSegmentService.getCopyNumberSegmentsBySampleListId(STUDY_ID, 
            SAMPLE_LIST_ID, null, PROJECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }
}