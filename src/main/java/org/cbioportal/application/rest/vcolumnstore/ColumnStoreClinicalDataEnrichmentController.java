package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import org.cbioportal.domain.clinical_data_enrichment.ClinicalDataEnrichment;
import org.cbioportal.domain.clinical_data_enrichment.usecase.FetchClinicalDataEnrichmentsUseCase;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

public class ColumnStoreClinicalDataEnrichmentController {

  private final FetchClinicalDataEnrichmentsUseCase fetchClinicalDataEnrichmentsUseCase;

  public ColumnStoreClinicalDataEnrichmentController(
      FetchClinicalDataEnrichmentsUseCase fetchClinicalDataEnrichmentsUseCase) {
    this.fetchClinicalDataEnrichmentsUseCase = fetchClinicalDataEnrichmentsUseCase;
  }

  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @PostMapping(
      value = "/clinical-data-enrichments/fetch",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch clinical data enrichments for the sample groups")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = ClinicalDataEnrichment.class))))
  public ResponseEntity<List<ClinicalDataEnrichment>> fetchClinicalEnrichments(
      @Parameter(required = true, description = "List of altered and unaltered Sample/Patient IDs")
          @Valid
          @RequestBody(required = false)
          GroupFilter groupFilter,
      @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
          @RequestAttribute(required = false, value = "involvedCancerStudies")
          Collection<String> involvedCancerStudies,
      @Parameter(
              hidden =
                  true) // prevent reference to this attribute in the swagger-ui interface. this
          // attribute is needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(required = false, value = "interceptedGroupFilter")
          GroupFilter interceptedGroupFilter) {
    return new ResponseEntity<>(
        fetchClinicalDataEnrichmentsUseCase.execute(interceptedGroupFilter), HttpStatus.OK);
  }
}
