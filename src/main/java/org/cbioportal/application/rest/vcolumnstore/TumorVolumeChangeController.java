package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.cbioportal.application.rest.response.TumorVolumeChangeDTO;
import org.cbioportal.domain.clinical_event.usecase.GetTumorVolumeChange;
import org.cbioportal.legacy.web.config.InternalApiTags;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@RequestMapping("/api")
@Tag(name = InternalApiTags.CLINICAL_EVENTS)
public class TumorVolumeChangeController {

  private final GetTumorVolumeChange getTumorVolumeChange;

  public TumorVolumeChangeController(GetTumorVolumeChange getTumorVolumeChange) {
    this.getTumorVolumeChange = getTumorVolumeChange;
  }

  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/studies/{studyId}/tumor-volume-changes",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Get tumor-volume percentage changes for patients in a study",
      description =
          "Compares TUMOR_VOLUME attributes at two clinical-event start dates, expressed as days"
              + " since diagnosis. Patients with missing or non-finite measurements, a zero baseline,"
              + " or a non-finite calculated change are excluded.")
  @ApiResponse(
      responseCode = "200",
      description = "Per-patient percentage changes",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = TumorVolumeChangeDTO.class))))
  public List<TumorVolumeChangeDTO> getTumorVolumeChanges(
      @Parameter(description = "Study ID", required = true) @PathVariable String studyId,
      @Parameter(description = "Baseline day since diagnosis") @RequestParam(defaultValue = "0")
          Integer baselineTimepoint,
      @Parameter(description = "Measurement day since diagnosis", required = true) @RequestParam
          Integer measurementTimepoint) {
    return getTumorVolumeChange
        .calculateTumorVolumePercentageChange(studyId, baselineTimepoint, measurementTimepoint)
        .stream()
        .map(
            result ->
                new TumorVolumeChangeDTO(
                    result.studyId(), result.patientId(), result.percentageChange()))
        .toList();
  }
}
