package org.cbioportal.application.rest.vcolumnstore;

import static org.cbioportal.legacy.utils.Encoder.calculateBase64;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.cbioportal.domain.clinical_event.usecase.GetPatientClinicalEventsUseCase;
import org.cbioportal.legacy.model.ClinicalEvent;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.sort.ClinicalEventSortBy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
      @Parameter(required = true, description = "Patient ID") @PathVariable String patientId,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection,
      @Parameter(description = "Page size of the result list")
          @Max(PagingConstants.MAX_PAGE_SIZE)
          @Min(PagingConstants.MIN_PAGE_SIZE)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE)
          Integer pageSize,
      @Parameter(description = "Page number of the result list")
          @Min(PagingConstants.MIN_PAGE_NUMBER)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
          Integer pageNumber,
      @Parameter(description = "Name of the property that the result list is sorted by")
          @RequestParam(required = false)
          ClinicalEventSortBy sortBy,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction)
      throws PatientNotFoundException, StudyNotFoundException {

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(
          HeaderKeyConstants.TOTAL_COUNT,
          getPatientClinicalEventsUseCase
              .executeMeta(studyId, patientId)
              .getTotalCount()
              .toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    } else {
      List<ClinicalEvent> clinicalEvents =
          getPatientClinicalEventsUseCase.execute(
              studyId,
              patientId,
              projection.name(),
              pageSize,
              pageNumber,
              sortBy == null ? null : sortBy.getOriginalValue(),
              direction.name());

      clinicalEvents.forEach(
          e -> e.setUniquePatientKey(calculateBase64(e.getPatientId(), e.getStudyId())));

      return new ResponseEntity<>(clinicalEvents, HttpStatus.OK);
    }
  }
}
