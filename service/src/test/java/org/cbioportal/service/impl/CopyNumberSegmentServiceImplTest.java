package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.CopyNumberSegmentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CopyNumberSegmentServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private CopyNumberSegmentServiceImpl copyNumberSegmentService;
    
    @Mock
    private CopyNumberSegmentRepository copyNumberSegmentRepository;

    @Test
    public void getCopyNumberSegmentsInSampleInStudy() throws Exception {

        List<CopyNumberSeg> expectedCopyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg = new CopyNumberSeg();
        expectedCopyNumberSegList.add(copyNumberSeg);

        Mockito.when(copyNumberSegmentRepository.getCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedCopyNumberSegList);
        
        List<CopyNumberSeg> result = copyNumberSegmentService.getCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID, 
            PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedCopyNumberSegList, result);
    }

    @Test
    public void getMetaCopyNumberSegmentsInSampleInStudy() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();

        Mockito.when(copyNumberSegmentRepository.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = copyNumberSegmentService.getMetaCopyNumberSegmentsInSampleInStudy(STUDY_ID, SAMPLE_ID);

        org.junit.Assert.assertEquals(expectedBaseMeta, result);
    }
}