package org.cbioportal.service.impl;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.VariantCount;
import org.cbioportal.persistence.VariantCountRepository;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.VariantCountService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VariantCountServiceImpl implements VariantCountService {
    
    @Autowired
    private VariantCountRepository variantCountRepository;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private GeneticProfileService geneticProfileService;
    
    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<VariantCount> fetchVariantCounts(String geneticProfileId, List<Integer> entrezGeneIds, 
                                                 List<String> keywords) throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);
        
        Integer numberOfSamplesInGeneticProfile = mutationService.fetchMetaMutationsInGeneticProfile(geneticProfileId,
            null, null).getSampleCount();
        
        List<VariantCount> variantCounts = variantCountRepository.fetchVariantCounts(geneticProfileId, entrezGeneIds, 
            keywords);
        variantCounts.forEach(v -> v.setNumberOfSamples(numberOfSamplesInGeneticProfile));
        return variantCounts;
    }

    private void validateGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException {

        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);

        if (!geneticProfile.getGeneticAlterationType()
            .equals(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED)) {

            throw new GeneticProfileNotFoundException(geneticProfileId);
        }
    }
}
