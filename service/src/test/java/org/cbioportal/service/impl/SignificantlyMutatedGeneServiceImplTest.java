package org.cbioportal.service.impl;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SignificantlyMutatedGeneServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private SignificantlyMutatedGeneServiceImpl significantlyMutatedGeneService;

    @Mock
    private SignificantlyMutatedGeneRepository significantlyMutatedGeneRepository;
    
    @Test
    public void getSignificantlyMutatedGenes() throws Exception {

        List<MutSig> expectedMutSigList = new ArrayList<>();
        MutSig mutSig = new MutSig();
        expectedMutSigList.add(mutSig);

        Mockito.when(significantlyMutatedGeneRepository.getSignificantlyMutatedGenes(STUDY_ID, PROJECTION, PAGE_SIZE, 
            PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedMutSigList);
        
        List<MutSig> result = significantlyMutatedGeneService.getSignificantlyMutatedGenes(STUDY_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(expectedMutSigList, result);
    }

    @Test
    public void getMetaSignificantlyMutatedGenes() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(significantlyMutatedGeneRepository.getMetaSignificantlyMutatedGenes(GENETIC_PROFILE_ID))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = significantlyMutatedGeneService.getMetaSignificantlyMutatedGenes(GENETIC_PROFILE_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }
}