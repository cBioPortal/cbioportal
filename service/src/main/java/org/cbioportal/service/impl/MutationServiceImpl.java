package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.util.ChromosomeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MutationServiceImpl implements MutationService {

    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private ChromosomeCalculator chromosomeCalculator;

    @Override
    public List<Mutation> getMutationsInGeneticProfile(String geneticProfileId, String sampleId, String projection, 
                                                       Integer pageSize, Integer pageNumber, String sortBy, 
                                                       String direction) {

        List<Mutation> mutationList = mutationRepository.getMutationsInGeneticProfile(geneticProfileId, sampleId, projection, pageSize,
            pageNumber, sortBy, direction);
        
        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    public BaseMeta getMetaMutationsInGeneticProfile(String geneticProfileId, String sampleId) {
        
        return mutationRepository.getMetaMutationsInGeneticProfile(geneticProfileId, sampleId);
    }

    @Override
    public List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                         String projection, Integer pageSize, Integer pageNumber,
                                                         String sortBy, String direction) {

        List<Mutation> mutationList = mutationRepository.fetchMutationsInGeneticProfile(geneticProfileId, sampleIds, projection, pageSize,
            pageNumber, sortBy, direction);

        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    public BaseMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds) {

        return mutationRepository.fetchMetaMutationsInGeneticProfile(geneticProfileId, sampleIds);
    }
}
