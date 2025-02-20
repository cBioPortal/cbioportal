package org.cbioportal.domain.treatment.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public record TreatmentCountReportUseCases(
        GetPatientTreatmentReportUseCase getPatientTreatmentReportUseCase,
        GetSampleTreatmentReportUseCase getSampleTreatmentReportUseCase) {

}
