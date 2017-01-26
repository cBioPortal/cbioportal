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
import org.cbioportal.model.CosmicCount;
import org.cbioportal.persistence.CosmicCountRepository;

@RunWith(MockitoJUnitRunner.class)
public class COSMICCountServiceImplTest {

    @InjectMocks
    private CosmicCountServiceImpl cosmicCountService;

    @Mock
    private CosmicCountRepository cosmicCountRepository;
    
    @Test
    public void getCOSMICCountsByKeywords() throws Exception {

	    
        ArrayList<String> testKeywords = new ArrayList<>();
        testKeywords.add("test_keyword");

        List<CosmicCount> expectedCOSMICCountList = new ArrayList<>();
	CosmicCount cosmicCount = new CosmicCount();
	expectedCOSMICCountList.add(cosmicCount);

        Mockito.when(cosmicCountRepository.getCOSMICCountsByKeywords(testKeywords)).thenReturn(expectedCOSMICCountList);

	List<CosmicCount> resultCOSMICCountList = cosmicCountService.getCOSMICCountsByKeywords(testKeywords);
	Assert.assertEquals(expectedCOSMICCountList, resultCOSMICCountList);
    }
}
