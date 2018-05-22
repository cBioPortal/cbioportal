package org.cbioportal.service.impl;

import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.persistence.FractionGenomeAlteredRepository;
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
public class FractionGenomeAlteredServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private FractionGenomeAlteredServiceImpl fractionGenomeAlteredService;
    
    @Mock
    private FractionGenomeAlteredRepository fractionGenomeAlteredRepository;
    
    @Test
    public void getFractionGenomeAltered() throws Exception {

        List<FractionGenomeAltered> expectedFractionGenomeAlteredList = new ArrayList<>();
        FractionGenomeAltered fractionGenomeAltered = new FractionGenomeAltered();
        expectedFractionGenomeAlteredList.add(fractionGenomeAltered);
        
        Mockito.when(fractionGenomeAlteredRepository.getFractionGenomeAltered(STUDY_ID, SAMPLE_LIST_ID))
            .thenReturn(expectedFractionGenomeAlteredList);
        
        List<FractionGenomeAltered> result = fractionGenomeAlteredService.getFractionGenomeAltered(STUDY_ID, 
            SAMPLE_LIST_ID);

        Assert.assertEquals(expectedFractionGenomeAlteredList, result);
    }

    @Test
    public void fetchFractionGenomeAltered() throws Exception {

        List<FractionGenomeAltered> expectedFractionGenomeAlteredList = new ArrayList<>();
        FractionGenomeAltered fractionGenomeAltered = new FractionGenomeAltered();
        expectedFractionGenomeAlteredList.add(fractionGenomeAltered);

        Mockito.when(fractionGenomeAlteredRepository.fetchFractionGenomeAltered(STUDY_ID, 
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2))).thenReturn(expectedFractionGenomeAlteredList);

        List<FractionGenomeAltered> result = fractionGenomeAlteredService.fetchFractionGenomeAltered(STUDY_ID,
            Arrays.asList(SAMPLE_ID1, SAMPLE_ID2));

        Assert.assertEquals(expectedFractionGenomeAlteredList, result);
    }
}
