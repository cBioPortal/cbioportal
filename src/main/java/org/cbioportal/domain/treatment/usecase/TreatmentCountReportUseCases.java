package org.cbioportal.domain.treatment.usecase;

import org.springframework.stereotype.Service;

@Service
public record TreatmentCountReportUseCases(
    GetPatientTreatmentReportUseCase getPatientTreatmentReportUseCase,
    GetSampleTreatmentReportUseCase getSampleTreatmentReportUseCase) {}
