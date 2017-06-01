package org.cbioportal.service.impl;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
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
    private GeneticProfileService geneticProfileService;
    @Autowired
    private ChromosomeCalculator chromosomeCalculator;

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<Mutation> getMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId,
                                                                     List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                     String projection, Integer pageSize, 
                                                                     Integer pageNumber, String sortBy, 
                                                                     String direction)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        List<Mutation> mutationList = mutationRepository.getMutationsInGeneticProfileBySampleListId(geneticProfileId,
            sampleListId, entrezGeneIds, snpOnly, projection, pageSize, pageNumber, sortBy, direction);

        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public MutationMeta getMetaMutationsInGeneticProfileBySampleListId(String geneticProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.getMetaMutationsInGeneticProfileBySampleListId(geneticProfileId, sampleListId,
            entrezGeneIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileIds, 'List<GeneticProfileId>', 'read')")
    public List<Mutation> getMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, List<String> sampleIds, 
                                                                List<Integer> entrezGeneIds, String projection, 
                                                                Integer pageSize, Integer pageNumber, String sortBy, 
                                                                String direction) {

        List<Mutation> mutationList = mutationRepository.getMutationsInMultipleGeneticProfiles(geneticProfileIds, 
            sampleIds, entrezGeneIds, projection, pageSize, pageNumber, sortBy, direction);

        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileIds, 'List<GeneticProfileId>', 'read')")
    public MutationMeta getMetaMutationsInMultipleGeneticProfiles(List<String> geneticProfileIds, 
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds) {
        
        return mutationRepository.getMetaMutationsInMultipleGeneticProfiles(geneticProfileIds, sampleIds, 
            entrezGeneIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<Mutation> fetchMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                         List<Integer> entrezGeneIds, Boolean snpOnly, 
                                                         String projection, Integer pageSize, Integer pageNumber, 
                                                         String sortBy, String direction)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        List<Mutation> mutationList = mutationRepository.fetchMutationsInGeneticProfile(geneticProfileId, sampleIds,
            entrezGeneIds, snpOnly, projection, pageSize, pageNumber, sortBy, direction);

        mutationList.forEach(mutation -> chromosomeCalculator.setChromosome(mutation.getGene()));
        return mutationList;
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public MutationMeta fetchMetaMutationsInGeneticProfile(String geneticProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.fetchMetaMutationsInGeneticProfile(geneticProfileId, sampleIds, entrezGeneIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSampleCountByGene> getSampleCountByEntrezGeneIdsAndSampleListId(String geneticProfileId,
                                                                                        String sampleListId,
                                                                                        List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.getSampleCountByEntrezGeneIdsAndSampleListId(geneticProfileId, sampleListId,
            entrezGeneIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSampleCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String geneticProfileId,
                                                                                     List<String> sampleIds,
                                                                                     List<Integer> entrezGeneIds)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.getSampleCountByEntrezGeneIdsAndSampleIds(geneticProfileId, sampleIds, entrezGeneIds);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSampleCountByKeyword> getSampleCountByKeywords(String geneticProfileId, List<String> keywords)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.getSampleCountByKeywords(geneticProfileId, keywords);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationCount> getMutationCountsInGeneticProfileBySampleListId(String geneticProfileId,
                                                                               String sampleListId)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.getMutationCountsInGeneticProfileBySampleListId(geneticProfileId, sampleListId);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationCount> fetchMutationCountsInGeneticProfile(String geneticProfileId, List<String> sampleIds)
        throws GeneticProfileNotFoundException {

        validateGeneticProfile(geneticProfileId);

        return mutationRepository.fetchMutationCountsInGeneticProfile(geneticProfileId, sampleIds);
    }

    private void validateGeneticProfile(String geneticProfileId) throws GeneticProfileNotFoundException {

        GeneticProfile geneticProfile = geneticProfileService.getGeneticProfile(geneticProfileId);

        if (!geneticProfile.getGeneticAlterationType()
            .equals(GeneticProfile.GeneticAlterationType.MUTATION_EXTENDED)) {

            throw new GeneticProfileNotFoundException(geneticProfileId);
        }
    }
}
