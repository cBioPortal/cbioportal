package org.cbioportal.domain.mutation.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * A record representing a collection of use cases related to Mutation data operations. This record
 * encapsulates instances of various use case classes, providing a centralized way to access and utilize 
 * the use cases 
 * @param  fetchAllMetaMutationsInProfileUseCase
 * @param fetchAllMutationsInProfileUseCase
 */
public record GetMutationUseCases(
    FetchMetaMutationsUseCase fetchMetaMutationsUseCase,
    FetchAllMutationsInProfileUseCase fetchAllMutationsInProfileUseCase
    ) {
}
