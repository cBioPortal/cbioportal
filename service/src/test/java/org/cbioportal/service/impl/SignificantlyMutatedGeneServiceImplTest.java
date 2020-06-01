package org.cbioportal.service.impl;

import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SignificantlyMutatedGeneServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private SignificantlyMutatedGeneServiceImpl significantlyMutatedGeneService;

    @Mock
    private SignificantlyMutatedGeneRepository significantlyMutatedGeneRepository;
    @Mock
    private StudyService studyService;
    
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

    @Test(expected = StudyNotFoundException.class)
    public void getSignificantlyMutatedGenesStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        significantlyMutatedGeneService.getSignificantlyMutatedGenes(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, SORT, 
            DIRECTION);
    }

    @Test
    public void getMetaSignificantlyMutatedGenes() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(significantlyMutatedGeneRepository.getMetaSignificantlyMutatedGenes(STUDY_ID))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = significantlyMutatedGeneService.getMetaSignificantlyMutatedGenes(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaSignificantlyMutatedGenesStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        significantlyMutatedGeneService.getMetaSignificantlyMutatedGenes(STUDY_ID);
    }
}