package org.cbioportal.clinical_data.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public record ClinicalDataUseCases(
        GetClinicalDataCountsUseCase getClinicalDataCountsUseCase,
        GetClinicalDataForXyPlotUseCase getClinicalDataForXyPlotUseCase,
        GetPatientClinicalDataUseCase getPatientClinicalDataUseCase,
        GetSampleClinicalDataUseCase getSampleClinicalDataUseCase
) {
}
