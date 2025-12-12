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
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.cbioportal.domain.alteration.usecase.GetAlterationEnrichmentsUseCase;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/column-store")
public class ColumnarStoreAlterationEnrichmentController {

  private final GetAlterationEnrichmentsUseCase getAlterationEnrichmentsUseCase;
  private final Logger logger =
      Logger.getLogger(ColumnarStoreAlterationEnrichmentController.class.getName());

  public ColumnarStoreAlterationEnrichmentController(
      GetAlterationEnrichmentsUseCase getAlterationEnrichmentsUseCase) {
    this.getAlterationEnrichmentsUseCase = getAlterationEnrichmentsUseCase;
  }

  @PreAuthorize(
      "hasPermission(#groupsAndAlterationTypes, 'MolecularProfileCasesGroupAndAlterationTypeFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/alteration-enrichments/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Fetch alteration enrichments in molecular profiles")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = AlterationEnrichment.class))))
  public ResponseEntity<Collection<AlterationEnrichment>> fetchAlterationEnrichments(
      @Parameter(description = "Type of the enrichment e.g. SAMPLE or PATIENT")
          @RequestParam(defaultValue = "SAMPLE")
          EnrichmentType enrichmentType,
      @Parameter(
              required = true,
              description =
                  "List of groups containing sample identifiers and list of Alteration Types")
          @Valid
          @RequestBody(required = false)
          MolecularProfileCasesGroupAndAlterationTypeFilter groupsAndAlterationTypes)
      throws MolecularProfileNotFoundException {
    Map<String, List<MolecularProfileCaseIdentifier>> groupCaseIdentifierSet =
        groupsAndAlterationTypes.getMolecularProfileCasesGroupFilter().stream()
            .collect(
                Collectors.toMap(
                    MolecularProfileCasesGroupFilter::getName,
                    MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

    return ResponseEntity.ok(
        getAlterationEnrichmentsUseCase.execute(
            groupCaseIdentifierSet,
            enrichmentType,
            groupsAndAlterationTypes.getAlterationEventTypes()));
  }
}
