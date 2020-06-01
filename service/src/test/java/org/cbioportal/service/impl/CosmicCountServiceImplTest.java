package org.cbioportal.service.impl;

import org.cbioportal.model.CosmicMutation;
import org.cbioportal.persistence.CosmicCountRepository;
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
public class CosmicCountServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private CosmicCountServiceImpl cosmicCountService;
    
    @Mock
    private CosmicCountRepository cosmicCountRepository;
    
    @Test
    public void getCosmicCountsByKeywords() throws Exception {

        List<CosmicMutation> expectedCosmicMutationList = new ArrayList<>();
        CosmicMutation cosmicMutation = new CosmicMutation();
        expectedCosmicMutationList.add(cosmicMutation);

        Mockito.when(cosmicCountRepository.fetchCosmicCountsByKeywords(Arrays.asList(KEYWORD)))
            .thenReturn(expectedCosmicMutationList);

        List<CosmicMutation> result = cosmicCountService.fetchCosmicCountsByKeywords(Arrays.asList(KEYWORD));

        Assert.assertEquals(expectedCosmicMutationList, result);
    }
}