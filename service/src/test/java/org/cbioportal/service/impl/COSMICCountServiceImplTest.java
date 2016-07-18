package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.COSMICCount;
import org.cbioportal.persistence.COSMICCountRepository;

@RunWith(MockitoJUnitRunner.class)
public class COSMICCountServiceImplTest {

    @InjectMocks
    private COSMICCountServiceImpl cosmicCountService;

    @Mock
    private COSMICCountRepository cosmicCountRepository;
    
    @Test
    public void getCOSMICCountsByKeywords() throws Exception {

	    
        ArrayList<String> testKeywords = new ArrayList<String>();
        testKeywords.add("test_keyword");

        List<COSMICCount> expectedCOSMICCountList = new ArrayList<>();
	COSMICCount cosmicCount = new COSMICCount();
	expectedCOSMICCountList.add(cosmicCount);

        Mockito.when(cosmicCountRepository.getCOSMICCountsByKeywords(testKeywords)).thenReturn(expectedCOSMICCountList);

	List<COSMICCount> resultCOSMICCountList = cosmicCountService.getCOSMICCountsByKeywords(testKeywords);
	Assert.assertEquals(expectedCOSMICCountList, resultCOSMICCountList);
    }
}
