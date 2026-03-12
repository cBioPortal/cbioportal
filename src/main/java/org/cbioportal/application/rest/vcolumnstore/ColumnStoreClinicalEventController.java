package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import org.cbioportal.domain.clinical_event.usecase.GetPatientClinicalEventsUseCase;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/column-store")
@Validated
public class ColumnStoreClinicalEventController {

  private final GetPatientClinicalEventsUseCase getPatientClinicalEventsUseCase;

  public ColumnStoreClinicalEventController(
      GetPatientClinicalEventsUseCase getPatientClinicalEventsUseCase) {
    this.getPatientClinicalEventsUseCase = getPatientClinicalEventsUseCase;
  }

  @Hidden
  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @GetMapping(
      value = "/studies/{studyId}/patients/{patientId}/clinical-events",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ClinicalEvent>> getAllClinicalEventsOfPatientInStudy(
      @Parameter(required = true, description = "Study ID") @PathVariable String studyId,
      @Parameter(required = true, description = "Patient ID") @PathVariable String patientId) {

    List<ClinicalEvent> clinicalEvents =
        getPatientClinicalEventsUseCase.execute(studyId, patientId);

    return new ResponseEntity<>(clinicalEvents, HttpStatus.OK);
  }
}
