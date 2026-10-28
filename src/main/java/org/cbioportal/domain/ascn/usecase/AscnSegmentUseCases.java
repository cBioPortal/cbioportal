package org.cbioportal.domain.ascn.usecase;

import org.springframework.stereotype.Service;

@Service
public record AscnSegmentUseCases(
    FetchAscnSegmentsUseCase fetchAscnSegmentsUseCase,
    FetchMetaAscnSegmentsUseCase fetchMetaAscnSegmentsUseCase,
    GetAscnSegmentsInStudyUseCase getAscnSegmentsInStudyUseCase,
    GetMetaAscnSegmentsInStudyUseCase getMetaAscnSegmentsInStudyUseCase) {}
