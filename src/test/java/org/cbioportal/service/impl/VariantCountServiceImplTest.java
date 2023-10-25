package org.cbioportal.service.impl;

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.cbioportal.model.VariantCount;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.VariantCountRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleListService;
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

@RunWith(MockitoJUnitRunner.Silent.class)
public class VariantCountServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private VariantCountServiceImpl variantCountService;
    
    @Mock
    private VariantCountRepository variantCountRepository;
    @Mock
    private MutationService mutationService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private SampleListService sampleListService;
    
    @Test
    public void fetchVariantCounts() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setSampleCount(5);
        Mockito.when(mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, null, null))
            .thenReturn(mutationMeta);

        SampleList sampleList = new SampleList();
        sampleList.setSampleCount(5);
        Mockito.when(sampleListService.getSampleList(STUDY_ID + "_sequenced")).thenReturn(sampleList);
        
        List<VariantCount> expectedVariantCounts = new ArrayList<>();
        VariantCount variantCount = new VariantCount();
        expectedVariantCounts.add(variantCount);
        
        Mockito.when(variantCountRepository.fetchVariantCounts(MOLECULAR_PROFILE_ID, Arrays.asList(ENTREZ_GENE_ID_1), 
            Arrays.asList(KEYWORD))).thenReturn(expectedVariantCounts);
        
        List<VariantCount> result = variantCountService.fetchVariantCounts(MOLECULAR_PROFILE_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), Arrays.asList(KEYWORD));

        Assert.assertEquals(expectedVariantCounts, result);
        Assert.assertEquals((Integer) 5, result.get(0).getNumberOfSamples());
    }
}
