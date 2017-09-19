package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;

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

        Mockito.when(copyNumberSegmentRepository.getCopyNumberSegmentsInSampleInStudy(MOLECULAR_PROFILE_ID, SAMPLE_ID1, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedCopyNumberSegList);
        
        List<CopyNumberSeg> result = copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(STUDY_ID, MOLECULAR_PROFILE_ID, SAMPLE_ID1, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getCopyNumberSegmentsInSampleInStudySampleNotFound() throws Exception {
        
        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(STUDY_ID, MOLECULAR_PROFILE_ID, SAMPLE_ID1, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION);
    }

    @Test
    public void getMetaCopyNumberSegmentsInSampleInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(copyNumberSegmentRepository.getMetaCopyNumberSegmentsInSampleInStudy(MOLECULAR_PROFILE_ID, SAMPLE_ID1))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = copyNumberSegmentService.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, MOLECULAR_PROFILE_ID, SAMPLE_ID1);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = SampleNotFoundException.class)
    public void getMetaCopyNumberSegmentsInSampleInStudySampleNotFound() throws Exception {
        
        Mockito.when(sampleService.getSampleInStudy(STUDY_ID, SAMPLE_ID1)).thenThrow(new SampleNotFoundException(
            STUDY_ID, SAMPLE_ID1));
        copyNumberSegmentService.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, MOLECULAR_PROFILE_ID, SAMPLE_ID1);
    }

    @Test
    public void fetchCopyNumberSegments() throws Exception {

        List<CopyNumberSeg> expectedCopyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg = new CopyNumberSeg();
        expectedCopyNumberSegList.add(copyNumberSeg);

        Mockito.when(copyNumberSegmentRepository.fetchCopyNumberSegments(Arrays.asList(MOLECULAR_PROFILE_ID), 
            Arrays.asList(SAMPLE_ID1), PROJECTION)).thenReturn(expectedCopyNumberSegList);

        List<CopyNumberSeg> result = copyNumberSegmentService.fetchCopyNumberSegments(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1), PROJECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }

    @Test
    public void fetchMetaCopyNumberSegments() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(copyNumberSegmentRepository.fetchMetaCopyNumberSegments(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1))).thenReturn(expectedBaseMeta);
        BaseMeta result = copyNumberSegmentService.fetchMetaCopyNumberSegments(Arrays.asList(MOLECULAR_PROFILE_ID),
            Arrays.asList(SAMPLE_ID1));

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getCopyNumberSegmentsBySampleListId() throws Exception {
        
        List<CopyNumberSeg> expectedCopyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg = new CopyNumberSeg();
        copyNumberSeg.setMolecularProfileId("study_tcga_pub_methylation_hm27");
        copyNumberSeg.setChr("2");
        copyNumberSeg.setSampleStableId("TCGA-A1-A0SD-01");
        copyNumberSeg.setSampleId(2);
        copyNumberSeg.setSegId(50236593);
        copyNumberSeg.setCancerStudyId(1);
        copyNumberSeg.setEnd(190262486);
        copyNumberSeg.setStart(1402650);
        copyNumberSeg.setNumProbes(207);
        copyNumberSeg.setSegmentMean(new BigDecimal("0.0265"));
        expectedCopyNumberSegList.add(copyNumberSeg);

        Mockito.when(copyNumberSegmentRepository.getCopyNumberSegmentsBySampleListId(MOLECULAR_PROFILE_ID1, SAMPLE_LIST_ID, 
            PROJECTION)).thenReturn(expectedCopyNumberSegList);

        List<CopyNumberSeg> result = copyNumberSegmentService.getCopyNumberSegmentsBySampleListId(MOLECULAR_PROFILE_ID1, 
            SAMPLE_LIST_ID, PROJECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }
}