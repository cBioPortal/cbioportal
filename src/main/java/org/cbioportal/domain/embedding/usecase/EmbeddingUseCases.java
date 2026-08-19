package org.cbioportal.domain.embedding.usecase;

import org.springframework.stereotype.Service;

@Service
/**
 * A record representing a collection of use cases related to embedding data operations. This record
 * encapsulates instances of various use case classes, providing a centralized way to access and
 * utilize the use cases.
 *
 * @param fetchEmbeddingInStudyUseCase the use case for retrieving embedding data for one or more
 *     studies
 */
public record EmbeddingUseCases(FetchEmbeddingInStudyUseCase fetchEmbeddingInStudyUseCase) {}
