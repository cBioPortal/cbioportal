package org.cbioportal.domain.embedding.usecase;

import org.springframework.stereotype.Service;

@Service
/**
 * A record representing a collection of use cases related to Mutation data operations. This record
 * encapsulates instances of various use case classes, providing a centralized way to access and
 * utilize the use cases
 *
 * @param fetchEmbeddingInStudyUseCase
 */
public record EmbeddingUseCases(FetchEmbeddingInStudyUseCase fetchEmbeddingInStudyUseCase) {}
