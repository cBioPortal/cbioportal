package org.cbioportal.domain.sample.usecase;

import org.springframework.stereotype.Service;

@Service
public record SampleUseCases(
    FetchMetaSamplesUseCase fetchMetaSamplesUseCase,
    FetchSamplesUseCase fetchSamplesUseCase,
    GetAllSamplesInStudyUseCase getAllSamplesInStudyUseCase,
    GetAllSamplesOfPatientInStudyUseCase getAllSamplesOfPatientInStudyUseCase,
    GetAllSamplesUseCase getAllSamplesUseCase,
    GetFilteredSamplesCountUseCase getFilteredSamplesCountUseCase,
    GetFilteredSamplesUseCase getFilteredSamplesUseCase,
    GetMetaSamplesInStudyUseCase getMetaSamplesInStudyUseCase,
    GetMetaSamplesOfPatientInStudyUseCase getMetaSamplesOfPatientInStudyUseCase,
    GetMetaSamplesUseCase getMetaSamplesUseCase,
    GetSampleInStudyUseCase getSampleInStudyUseCase) {}
