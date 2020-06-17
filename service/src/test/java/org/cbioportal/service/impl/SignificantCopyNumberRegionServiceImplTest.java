package org.cbioportal.service.impl;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantCopyNumberRegionRepository;
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
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SignificantCopyNumberRegionServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private SignificantCopyNumberRegionServiceImpl significantCopyNumberRegionService;
    
    @Mock
    private SignificantCopyNumberRegionRepository significantCopyNumberRegionRepository;
    @Mock
    private StudyService studyService;
    
    @Test
    public void getSignificantCopyNumberRegions() throws Exception {

        List<Gistic> expectedGisticList = new ArrayList<>();
        Gistic gistic = new Gistic();
        gistic.setGisticRoiId(GISTIC_ROI_ID);
        expectedGisticList.add(gistic);

        Mockito.when(significantCopyNumberRegionRepository.getSignificantCopyNumberRegions(STUDY_ID, PROJECTION, 
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION)).thenReturn(expectedGisticList);
        
        List<GisticToGene> expectedGisticToGeneList = new ArrayList<>();
        GisticToGene gisticToGene = new GisticToGene();
        gisticToGene.setGisticRoiId(GISTIC_ROI_ID);
        expectedGisticToGeneList.add(gisticToGene);
        
        Mockito.when(significantCopyNumberRegionRepository.getGenesOfRegions(Arrays.asList(GISTIC_ROI_ID)))
            .thenReturn(expectedGisticToGeneList);
        
        List<Gistic> result = significantCopyNumberRegionService.getSignificantCopyNumberRegions(STUDY_ID, PROJECTION,
            PAGE_SIZE, PAGE_NUMBER, SORT, DIRECTION);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(gistic, result.get(0));
        Assert.assertEquals(1, result.get(0).getGenes().size());
        Assert.assertEquals(gisticToGene, result.get(0).getGenes().get(0));
    }

    @Test(expected = StudyNotFoundException.class)
    public void getSignificantCopyNumberRegionsStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        significantCopyNumberRegionService.getSignificantCopyNumberRegions(STUDY_ID, PROJECTION, PAGE_SIZE, PAGE_NUMBER, 
            SORT, DIRECTION);
    }

    @Test
    public void getMetaSignificantCopyNumberRegions() throws Exception {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(significantCopyNumberRegionRepository.getMetaSignificantCopyNumberRegions(STUDY_ID))
            .thenReturn(expectedBaseMeta);
        BaseMeta result = significantCopyNumberRegionService.getMetaSignificantCopyNumberRegions(STUDY_ID);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test(expected = StudyNotFoundException.class)
    public void getMetaSignificantCopyNumberRegionsStudyNotFound() throws Exception {
        
        Mockito.when(studyService.getStudy(STUDY_ID)).thenThrow(new StudyNotFoundException(STUDY_ID));
        significantCopyNumberRegionService.getMetaSignificantCopyNumberRegions(STUDY_ID);
    }
}