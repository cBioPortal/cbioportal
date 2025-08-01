package org.cbioportal.domain.mutation.usecase;

import org.cbioportal.domain.mutation.repository.MutationRepository;

public class FetchMetaMutationsBySampleListUseCase {
    private final MutationRepository mutationRepository;

    public FetchMetaMutationsBySampleListUseCase(MutationRepository mutationRepository) {
        this.mutationRepository = mutationRepository;
    }
}
