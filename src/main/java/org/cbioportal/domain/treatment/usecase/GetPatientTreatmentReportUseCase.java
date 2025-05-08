package org.cbioportal.domain.treatment.usecase;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.treatment.repository.TreatmentRepository;
import org.cbioportal.legacy.model.PatientTreatmentReport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class GetPatientTreatmentReportUseCase {
  private final TreatmentRepository treatmentRepository;

  /**
   * Constructs a new use case with the given treatment repository.
   *
   * @param treatmentRepository the repository used to fetch treatment data
   */
  public GetPatientTreatmentReportUseCase(TreatmentRepository treatmentRepository) {
    this.treatmentRepository = treatmentRepository;
  }

  /**
   * Executes the use case to retrieve a patient treatment report based on the given filter context.
   *
   * @param studyViewFilterContext the filtering criteria for retrieving patient treatments
   * @return a {@link PatientTreatmentReport} containing treatment data for patients
   */
  public PatientTreatmentReport execute(StudyViewFilterContext studyViewFilterContext) {
    var patientTreatments = treatmentRepository.getPatientTreatments(studyViewFilterContext);
    var totalPatientTreatmentCount =
        treatmentRepository.getTotalPatientTreatmentCount(studyViewFilterContext);
    return new PatientTreatmentReport(totalPatientTreatmentCount, 0, patientTreatments);
  }
}
