package org.cbioportal.domain.clinical_data.usecase;

import org.springframework.stereotype.Service;

/**
 * A record representing a collection of use cases related to clinical data operations. This record
 * encapsulates instances of various use case classes, providing a centralized way to access and
 * manage clinical data-related operations.
 *
 * @param getClinicalDataCountsUseCase the use case for retrieving and processing clinical data
 *     counts
 * @param getClinicalDataForXyPlotUseCase the use case for retrieving clinical data for XY plots
 * @param getPatientClinicalDataUseCase the use case for retrieving clinical data for patients
 * @param getSampleClinicalDataUseCase the use case for retrieving clinical data for samples
 */
@Service
public record ClinicalDataUseCases(
    GetClinicalDataCountsUseCase getClinicalDataCountsUseCase,
    GetClinicalDataForXyPlotUseCase getClinicalDataForXyPlotUseCase,
    GetPatientClinicalDataUseCase getPatientClinicalDataUseCase,
    GetSampleClinicalDataUseCase getSampleClinicalDataUseCase,
    FetchClinicalDataUseCase fetchClinicalDataUseCase,
    FetchClinicalDataMetaUseCase fetchClinicalDataMetaUseCase) {}
