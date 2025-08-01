package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;

public class FetchMutationsBySampleListUseCase {
    private final MutationRepository mutationRepository;

    public FetchMutationsBySampleListUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }
    
}
