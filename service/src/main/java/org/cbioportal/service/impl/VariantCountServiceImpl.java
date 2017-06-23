package org.cbioportal.service.impl;

import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.VariantCount;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.VariantCountService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VariantCountServiceImpl implements VariantCountService {
    
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
        List<MutationSampleCountByGene> mutationSampleCountByGeneList = 
            mutationService.getSampleCountByEntrezGeneIdsAndSampleIds(geneticProfileId, null, entrezGeneIds);
        List<MutationSampleCountByKeyword> mutationSampleCountByKeywordList = mutationService.getSampleCountByKeywords(
            geneticProfileId, keywords);
        
        List<VariantCount> variantCounts = new ArrayList<>();
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            Integer entrezGeneId = entrezGeneIds.get(i);

            VariantCount variantCount = new VariantCount();
            variantCount.setGeneticProfileId(geneticProfileId);
            variantCount.setEntrezGeneId(entrezGeneId);
            variantCount.setKeyword(keyword);
            variantCount.setNumberOfSamples(numberOfSamplesInGeneticProfile);
            
            Optional<MutationSampleCountByGene> mutationSampleCountByGene = mutationSampleCountByGeneList.stream()
                .filter(p -> p.getEntrezGeneId().equals(entrezGeneId)).findFirst();
            mutationSampleCountByGene.ifPresent(m -> variantCount.setNumberOfSamplesWithMutationInGene(m
                .getSampleCount()));
            
            if (keyword != null) {
                Optional<MutationSampleCountByKeyword> mutationSampleCountByKeyword = mutationSampleCountByKeywordList
                    .stream().filter(p -> p.getKeyword().equals(keyword)).findFirst();
                mutationSampleCountByKeyword.ifPresent(m -> variantCount.setNumberOfSamplesWithKeyword(m
                    .getSampleCount()));
            }
            variantCounts.add(variantCount);
        }
        
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
