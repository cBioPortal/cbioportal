package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.MutationSpectrumService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<MutationSpectrum> getMutationSpectrums(String molecularProfileId, String sampleListId) 
        throws MolecularProfileNotFoundException {

        List<Mutation> mutations = mutationService.getMutationsInMolecularProfileBySampleListId(molecularProfileId, 
            sampleListId, null, true, "SUMMARY", null, null, null, null);
        
        return createMutationSpectrums(molecularProfileId, mutations);
    }

    @Override
    public List<MutationSpectrum> fetchMutationSpectrums(String molecularProfileId, List<String> sampleIds) 
        throws MolecularProfileNotFoundException {
        
        List<Mutation> mutations = mutationService.fetchMutationsInMolecularProfile(molecularProfileId, sampleIds, null, 
            true, "SUMMARY", null, null, null, null);
        
        return createMutationSpectrums(molecularProfileId, mutations);
    }

    private List<MutationSpectrum> createMutationSpectrums(String molecularProfileId, List<Mutation> mutations) {
        
        Map<String, List<Mutation>> mutationMap = mutations.stream().collect(Collectors.groupingBy(
            Mutation::getSampleId));

        List<MutationSpectrum> mutationSpectrums = new ArrayList<>();
        for (String sampleId : mutationMap.keySet()) {

            List<Mutation> mutationsInSample = mutationMap.get(sampleId);
            MutationSpectrum mutationSpectrum = new MutationSpectrum();
            mutationSpectrum.setMolecularProfileId(molecularProfileId);
            mutationSpectrum.setSampleId(sampleId);
            mutationSpectrum.setPatientId(mutationsInSample.get(0).getPatientId());
            mutationSpectrum.setStudyId(mutationsInSample.get(0).getStudyId());
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
