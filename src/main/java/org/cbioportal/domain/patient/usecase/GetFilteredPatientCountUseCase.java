package org.cbioportal.domain.patient.usecase;

import org.cbioportal.domain.patient.repository.PatientRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving the count of filtered patients. This class interacts with the {@link
 * PatientRepository} to fetch the number of patients that match the given filter criteria specified
 * in the study view filter context.
 */
public class GetFilteredPatientCountUseCase {

  private final PatientRepository patientRepository;

  /**
   * Constructs a {@code GetFilteredPatientCountUseCase} with the provided {@link
   * PatientRepository}.
   *
   * @param patientRepository the repository to be used for retrieving the filtered patient count
   */
  public GetFilteredPatientCountUseCase(PatientRepository patientRepository) {
    this.patientRepository = patientRepository;
  }

  /**
   * Executes the use case to retrieve the count of filtered patients based on the given study view
   * filter context.
   *
   * @param studyViewFilterContext the context of the study view filter to apply
   * @return the count of filtered patients
   */
  public int execute(StudyViewFilterContext studyViewFilterContext) {
    return patientRepository.getFilteredPatientCount(studyViewFilterContext);
  }
}
