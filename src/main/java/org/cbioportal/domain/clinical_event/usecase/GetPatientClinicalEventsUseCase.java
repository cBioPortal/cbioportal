package org.cbioportal.domain.clinical_event.usecase;

import java.util.List;
import org.cbioportal.domain.clinical_event.repository.ClinicalEventRepository;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving patient clinical events from a column-store backend.
 *
 * <p>Note: patient existence validation is delegated to {@link PatientService} from the legacy
 * layer. This is intentional: the legacy service already encapsulates the study-not-found /
 * patient-not-found exception semantics and JPA-backed lookups that we do not want to duplicate
 * here. A domain-level patient port can be introduced once the broader migration to the new domain
 * layer is further along.
 */
@Service
public class GetPatientClinicalEventsUseCase {

  private final ClinicalEventRepository clinicalEventRepository;
  private final PatientService patientService;

  public GetPatientClinicalEventsUseCase(
      ClinicalEventRepository clinicalEventRepository, PatientService patientService) {
    this.clinicalEventRepository = clinicalEventRepository;
    this.patientService = patientService;
  }

  public List<ClinicalEvent> execute(
      String studyId,
      String patientId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws PatientNotFoundException, StudyNotFoundException {
    patientService.getPatientInStudy(studyId, patientId);
    return clinicalEventRepository.getPatientClinicalEvents(
        studyId, patientId, projection, pageSize, pageNumber, sortBy, direction);
  }

  public BaseMeta executeMeta(String studyId, String patientId)
      throws PatientNotFoundException, StudyNotFoundException {
    patientService.getPatientInStudy(studyId, patientId);
    return clinicalEventRepository.getMetaPatientClinicalEvents(studyId, patientId);
  }
}
