package org.cbioportal.service.impl;

import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.VariantCount;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.VariantCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class VariantCountServiceImpl implements VariantCountService {
    
    @Autowired
    private MutationService mutationService;
    
    @Override
    public List<VariantCount> fetchVariantCounts(String geneticProfileId, List<Integer> entrezGeneIds, 
                                                 List<String> keywords) {

        Integer numberOfSamplesInGeneticProfile = mutationService.fetchMetaMutationsInGeneticProfile(geneticProfileId, 
            null).getSampleCount();
        List<MutationSampleCountByGene> mutationSampleCountByGeneList = mutationService.getSampleCountByEntrezGeneIds(
            geneticProfileId, entrezGeneIds);
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
            variantCount.setNumberOfSamplesWithMutationInGene(mutationSampleCountByGeneList.stream()
                .filter(p -> p.getEntrezGeneId().equals(entrezGeneId)).findFirst().get().getSampleCount());
            variantCount.setNumberOfSamplesWithKeyword(mutationSampleCountByKeywordList.stream()
                .filter(p -> p.getKeyword().equals(keyword)).findFirst().get().getSampleCount());
            
            variantCounts.add(variantCount);
        }
        
        return variantCounts;
    }
}
