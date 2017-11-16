package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.VariantCount;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.VariantCountRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
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
public class VariantCountServiceImplTest extends BaseServiceImplTest {
    
    @InjectMocks
    private VariantCountServiceImpl variantCountService;
    
    @Mock
    private VariantCountRepository variantCountRepository;
    @Mock
    private MutationService mutationService;
    @Mock
    private MolecularProfileService molecularProfileService;
    
    @Test
    public void fetchVariantCounts() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        MutationMeta mutationMeta = new MutationMeta();
        mutationMeta.setSampleCount(5);
        Mockito.when(mutationService.fetchMetaMutationsInMolecularProfile(MOLECULAR_PROFILE_ID, null, null))
            .thenReturn(mutationMeta);
        
        List<VariantCount> expectedVariantCounts = new ArrayList<>();
        VariantCount variantCount = new VariantCount();
        expectedVariantCounts.add(variantCount);
        
        Mockito.when(variantCountRepository.fetchVariantCounts(MOLECULAR_PROFILE_ID, Arrays.asList(ENTREZ_GENE_ID), 
            Arrays.asList(KEYWORD))).thenReturn(expectedVariantCounts);
        
        List<VariantCount> result = variantCountService.fetchVariantCounts(MOLECULAR_PROFILE_ID, 
            Arrays.asList(ENTREZ_GENE_ID), Arrays.asList(KEYWORD));

        Assert.assertEquals(expectedVariantCounts, result);
        Assert.assertEquals((Integer) 5, result.get(0).getNumberOfSamples());
    }
}
