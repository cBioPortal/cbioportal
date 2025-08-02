package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;
import org.cbioportal.legacy.model.meta.MutationMeta;

import java.util.List;

public class FetchAllMetaMutationsInProfileUseCase {
    private final MutationRepository mutationRepository;
    
    
    public FetchAllMetaMutationsInProfileUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }
    public MutationMeta execute(List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds){
        return mutationRepository.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds);
    }
}
