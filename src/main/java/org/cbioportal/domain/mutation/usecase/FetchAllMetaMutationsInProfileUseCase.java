package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;

public class FetchAllMetaMutationsInProfileUseCase {
    private final MutationRepository mutationRepository;
    
    
    public FetchAllMetaMutationsInProfileUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }
    
}
