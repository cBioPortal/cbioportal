package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.MutationSpectrumService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MutationSpectrumServiceImpl implements MutationSpectrumService {
    
    @Autowired
    private MutationService mutationService;
    
    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSpectrum> getMutationSpectrums(String geneticProfileId, String sampleListId) 
        throws GeneticProfileNotFoundException {

        List<Mutation> mutations = mutationService.getMutationsInGeneticProfileBySampleListId(geneticProfileId, 
            sampleListId, null, true, "SUMMARY", null, null, null, null);
        
        return createMutationSpectrums(geneticProfileId, mutations);
    }

    @Override
    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")
    public List<MutationSpectrum> fetchMutationSpectrums(String geneticProfileId, List<String> sampleIds) 
        throws GeneticProfileNotFoundException {
        
        List<Mutation> mutations = mutationService.fetchMutationsInGeneticProfile(geneticProfileId, sampleIds, null, 
            true, "SUMMARY", null, null, null, null);
        
        return createMutationSpectrums(geneticProfileId, mutations);
    }

    private List<MutationSpectrum> createMutationSpectrums(String geneticProfileId, List<Mutation> mutations) {
        
        Map<String, List<Mutation>> mutationMap = mutations.stream().collect(Collectors.groupingBy(
            Mutation::getSampleId));

        List<MutationSpectrum> mutationSpectrums = new ArrayList<>();
        for (String sampleId : mutationMap.keySet()) {

            List<Mutation> mutationsInSample = mutationMap.get(sampleId);
            MutationSpectrum mutationSpectrum = new MutationSpectrum();
            mutationSpectrum.setGeneticProfileId(geneticProfileId);
            mutationSpectrum.setSampleId(sampleId);
            mutationSpectrum.setCtoA(Math.toIntExact(mutationsInSample.stream().filter(m -> checkSpectrum(m, "C", "A") 
                || checkSpectrum(m, "G", "T")).count()));
            mutationSpectrum.setCtoG(Math.toIntExact(mutationsInSample.stream().filter(m -> checkSpectrum(m, "C", "G") 
                || checkSpectrum(m, "G", "C")).count()));
            mutationSpectrum.setCtoT(Math.toIntExact(mutationsInSample.stream().filter(m -> checkSpectrum(m, "C", "T") 
                || checkSpectrum(m, "G", "A")).count()));
            mutationSpectrum.setTtoA(Math.toIntExact(mutationsInSample.stream().filter(m -> checkSpectrum(m, "T", "A") 
                || checkSpectrum(m, "A", "T")).count()));
            mutationSpectrum.setTtoC(Math.toIntExact(mutationsInSample.stream().filter(m -> checkSpectrum(m, "T", "C") 
                || checkSpectrum(m, "A", "G")).count()));
            mutationSpectrum.setTtoG(Math.toIntExact(mutationsInSample.stream().filter(m -> checkSpectrum(m, "T", "G") 
                || checkSpectrum(m, "A", "C")).count()));
            mutationSpectrums.add(mutationSpectrum);
        }

        return mutationSpectrums;
    }
    
    private boolean checkSpectrum(Mutation mutation, String referenceAllele, String tumorSeqAllele) {
        
        return mutation.getReferenceAllele().equals(referenceAllele) && 
            mutation.getTumorSeqAllele().equals(tumorSeqAllele);
    }
}
