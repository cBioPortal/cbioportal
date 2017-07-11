package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.service.CopyNumberSegmentService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class FractionGenomeAlteredServiceImplTest extends BaseServiceImplTest {

    private static final double CUTOFF = 0.2;
    
    @InjectMocks
    private FractionGenomeAlteredServiceImpl fractionGenomeAlteredService;
    
    @Mock
    private CopyNumberSegmentService copyNumberSegmentService;
    
    @Test
    public void getFractionGenomeAltered() throws Exception {

        List<CopyNumberSeg> copyNumberSegList = createCopyNumberSegList();
        
        Mockito.when(copyNumberSegmentService.getCopyNumberSegmentsBySampleListId(PROFILE_ID, SAMPLE_LIST_ID, "SUMMARY"))
            .thenReturn(copyNumberSegList);
        
        List<FractionGenomeAltered> result = fractionGenomeAlteredService.getFractionGenomeAltered(PROFILE_ID,
            SAMPLE_LIST_ID, CUTOFF);

        Assert.assertEquals(2, result.size());
        FractionGenomeAltered fractionGenomeAltered1 = result.get(0);
        Assert.assertEquals(PROFILE_ID, fractionGenomeAltered1.getProfileId());
        Assert.assertEquals(SAMPLE_ID1, fractionGenomeAltered1.getSampleId());
        Assert.assertEquals(new BigDecimal("0.18845872899926955"), fractionGenomeAltered1.getValue());
        FractionGenomeAltered fractionGenomeAltered2 = result.get(1);
        Assert.assertEquals(PROFILE_ID, fractionGenomeAltered2.getProfileId());
        Assert.assertEquals(SAMPLE_ID2, fractionGenomeAltered2.getSampleId());
        Assert.assertEquals(new BigDecimal("1.0"), fractionGenomeAltered2.getValue());
    }

    @Test
    public void fetchFractionGenomeAltered() throws Exception {

        List<CopyNumberSeg> copyNumberSegList = createCopyNumberSegList();

        Mockito.when(copyNumberSegmentService.fetchCopyNumberSegments(Arrays.asList(PROFILE_ID),
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "SUMMARY")).thenReturn(copyNumberSegList);

        List<FractionGenomeAltered> result = fractionGenomeAlteredService.fetchFractionGenomeAltered(PROFILE_ID,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), CUTOFF);

        Assert.assertEquals(2, result.size());
        FractionGenomeAltered fractionGenomeAltered1 = result.get(0);
        Assert.assertEquals(PROFILE_ID, fractionGenomeAltered1.getProfileId());
        Assert.assertEquals(SAMPLE_ID1, fractionGenomeAltered1.getSampleId());
        Assert.assertEquals(new BigDecimal("0.18845872899926955"), fractionGenomeAltered1.getValue());
        FractionGenomeAltered fractionGenomeAltered2 = result.get(1);
        Assert.assertEquals(PROFILE_ID, fractionGenomeAltered2.getProfileId());
        Assert.assertEquals(SAMPLE_ID2, fractionGenomeAltered2.getSampleId());
        Assert.assertEquals(new BigDecimal("1.0"), fractionGenomeAltered2.getValue());
    }

    private List<CopyNumberSeg> createCopyNumberSegList() {
        
        List<CopyNumberSeg> copyNumberSegList = new ArrayList<>();
        CopyNumberSeg copyNumberSeg1 = new CopyNumberSeg();
        copyNumberSeg1.setSampleStableId(SAMPLE_ID1);
        copyNumberSeg1.setSegmentMean(new BigDecimal(0.3));
        copyNumberSeg1.setStart(3245343);
        copyNumberSeg1.setEnd(3245601);
        copyNumberSegList.add(copyNumberSeg1);
        CopyNumberSeg copyNumberSeg2 = new CopyNumberSeg();
        copyNumberSeg2.setSampleStableId(SAMPLE_ID1);
        copyNumberSeg2.setSegmentMean(new BigDecimal(0.1));
        copyNumberSeg2.setStart(1234);
        copyNumberSeg2.setEnd(2345);
        copyNumberSegList.add(copyNumberSeg2);
        CopyNumberSeg copyNumberSeg3 = new CopyNumberSeg();
        copyNumberSeg3.setSampleStableId(SAMPLE_ID2);
        copyNumberSeg3.setSegmentMean(new BigDecimal(-1.3));
        copyNumberSeg3.setStart(13);
        copyNumberSeg3.setEnd(124);
        copyNumberSegList.add(copyNumberSeg3);
        CopyNumberSeg copyNumberSeg4 = new CopyNumberSeg();
        copyNumberSeg4.setSampleStableId(SAMPLE_ID2);
        copyNumberSeg4.setSegmentMean(new BigDecimal(2.4));
        copyNumberSeg4.setStart(564565445);
        copyNumberSeg4.setEnd(574565445);
        copyNumberSegList.add(copyNumberSeg4);
        return copyNumberSegList;
    }
}