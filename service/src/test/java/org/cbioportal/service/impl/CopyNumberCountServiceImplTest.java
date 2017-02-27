package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberSampleCountByGene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
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
public class CopyNumberCountServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private CopyNumberCountServiceImpl copyNumberCountService;

    @Mock
    private GeneticDataService geneticDataService;
    @Mock
    private DiscreteCopyNumberService discreteCopyNumberService;
    @Mock
    private GeneticProfileService geneticProfileService;
    
    @Test
    public void fetchCopyNumberCounts() throws Exception {

        List<CopyNumberSampleCountByGene> copyNumberSampleCountByGeneList = new ArrayList<>();
        CopyNumberSampleCountByGene copyNumberSampleCountByGene = new CopyNumberSampleCountByGene();
        copyNumberSampleCountByGene.setEntrezGeneId(ENTREZ_GENE_ID);
        copyNumberSampleCountByGene.setAlteration(-2);
        copyNumberSampleCountByGene.setSampleCount(1);
        copyNumberSampleCountByGeneList.add(copyNumberSampleCountByGene);

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
        
        Mockito.when(geneticDataService.getNumberOfSamplesInGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(2);
        
        Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlteration(GENETIC_PROFILE_ID, 
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2))).thenReturn(copyNumberSampleCountByGeneList);
        
        List<CopyNumberCount> result = copyNumberCountService.fetchCopyNumberCounts(GENETIC_PROFILE_ID, 
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(-2));

        Assert.assertEquals(1, result.size());
        CopyNumberCount copyNumberCount = result.get(0);
        Assert.assertEquals(GENETIC_PROFILE_ID, copyNumberCount.getGeneticProfileId());
        Assert.assertEquals(ENTREZ_GENE_ID, copyNumberCount.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), copyNumberCount.getAlteration());
        Assert.assertEquals((Integer) 2, copyNumberCount.getNumberOfSamples());
        Assert.assertEquals((Integer) 1, copyNumberCount.getNumberOfSamplesWithAlterationInGene());
    }
}