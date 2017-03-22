package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.util.ChromosomeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private ChromosomeCalculator chromosomeCalculator;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<Mutation> getMutationsInGeneticProfile(String geneticProfileId, String sampleId, String projection, 
                                                       Integer pageSize, Integer pageNumber, String sortBy, 
                                                       String direction) {

        List<Mutation> mutationList = mutationRepository.getMutationsInGeneticProfile(geneticProfileId, sampleId, 
            projection, pageSize, pageNumber, sortBy, direction);
        
        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public MutationMeta getMetaMutationsInGeneticProfile(String geneticProfileId, String sampleId) {
        
        return mutationRepository.getMetaMutationsInGeneticProfile(geneticProfileId, sampleId);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction) {

        List<Mutation> mutationList = mutationRepository.fetchMutationsInGeneticProfile(geneticProfileId, sampleIds, 
            projection, pageSize, pageNumber, sortBy, direction);

        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds) {

        return mutationRepository.fetchMetaMutationsInGeneticProfile(geneticProfileId, sampleIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSampleCountByGene> getSampleCountByEntrezGeneIds(String geneticProfileId, 
                                                                         List<Integer> entrezGeneIds) {
        
        return mutationRepository.getSampleCountByEntrezGeneIds(geneticProfileId, entrezGeneIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSampleCountByKeyword> getSampleCountByKeywords(String geneticProfileId, List<String> keywords) {
        
        return mutationRepository.getSampleCountByKeywords(geneticProfileId, keywords);
    }
}
