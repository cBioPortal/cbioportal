package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.VariantCount;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.MutationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class VariantCountServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private VariantCountServiceImpl variantCountService;
    
    @Mock
    private MutationService mutationService;
    @Mock
    private GeneticProfileService geneticProfileService;
    
    @Test
    public void fetchVariantCounts() throws Exception {

        List<MutationSampleCountByGene> mutationSampleCountByGeneList = new ArrayList<>();
        MutationSampleCountByGene mutationSampleCountByGene = new MutationSampleCountByGene();
        mutationSampleCountByGene.setEntrezGeneId(ENTREZ_GENE_ID);
        mutationSampleCountByGene.setSampleCount(3);
        mutationSampleCountByGeneList.add(mutationSampleCountByGene);

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID);

        Mockito.when(mutationService.getSampleCountByEntrezGeneIds(GENETIC_PROFILE_ID, entrezGeneIds))
            .thenReturn(mutationSampleCountByGeneList);
        
        List<MutationSampleCountByKeyword> mutationSampleCountByKeywordList = new ArrayList<>();
        MutationSampleCountByKeyword mutationSampleCountByKeyword = new MutationSampleCountByKeyword();
        mutationSampleCountByKeyword.setKeyword(KEYWORD);
        mutationSampleCountByKeyword.setSampleCount(2);
        mutationSampleCountByKeywordList.add(mutationSampleCountByKeyword);
        
        List<String> keywords = new ArrayList<>();
        keywords.add(KEYWORD);
        
        Mockito.when(mutationService.getSampleCountByKeywords(GENETIC_PROFILE_ID, keywords))
            .thenReturn(mutationSampleCountByKeywordList);

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setSampleCount(5);
        Mockito.when(mutationService.fetchMetaMutationsInGeneticProfile(GENETIC_PROFILE_ID, null))
            .thenReturn(mutationMeta);
        
        List<VariantCount> result = variantCountService.fetchVariantCounts(GENETIC_PROFILE_ID, entrezGeneIds, keywords);

        Assert.assertEquals(1, result.size());
        VariantCount variantCount = result.get(0);
        Assert.assertEquals(GENETIC_PROFILE_ID, variantCount.getGeneticProfileId());
        Assert.assertEquals(ENTREZ_GENE_ID, variantCount.getEntrezGeneId());
        Assert.assertEquals(KEYWORD, variantCount.getKeyword());
        Assert.assertEquals((Integer) 5, variantCount.getNumberOfSamples());
        Assert.assertEquals((Integer) 3, variantCount.getNumberOfSamplesWithMutationInGene());
        Assert.assertEquals((Integer) 2, variantCount.getNumberOfSamplesWithKeyword());
    }
}