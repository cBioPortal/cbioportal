package org.cbioportal.domain.clinical_event.usecase;

import java.util.List;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.springframework.stereotype.Service;

@Service
public class GetPatientClinicalEventsUseCase {

  private final ClinicalEventRepository clinicalEventRepository;

  public GetPatientClinicalEventsUseCase(ClinicalEventRepository clinicalEventRepository) {
    this.clinicalEventRepository = clinicalEventRepository;
  }

  public List<ClinicalEvent> execute(String studyId, String patientId) {
    return clinicalEventRepository.getPatientClinicalEvents(studyId, patientId);
  }
}
