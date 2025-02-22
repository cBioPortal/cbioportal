package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.service.AlterationEnrichmentService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Alteration Enrichments", description = " ")
public class AlterationEnrichmentController {

  @Autowired private AlterationEnrichmentService alterationEnrichmentService;

  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
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
  public ResponseEntity<List<AlterationEnrichment>> fetchAlterationEnrichments(
      @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
          @RequestAttribute(required = false, value = "involvedCancerStudies")
          Collection<String> involvedCancerStudies,
      @Parameter(hidden = true)
          // prevent reference to this attribute in the swagger-ui interface. this attribute is
          // needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(
              required = false,
              value = "interceptedMolecularProfileCasesGroupFilters")
          List<MolecularProfileCasesGroupFilter> interceptedMolecularProfileCasesGroupFilters,
      @Parameter(hidden = true)
          @Valid
          @RequestAttribute(required = false, value = "alterationEventTypes")
          AlterationFilter alterationEventTypes,
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
        interceptedMolecularProfileCasesGroupFilters.stream()
            .collect(
                Collectors.toMap(
                    MolecularProfileCasesGroupFilter::getName,
                    MolecularProfileCasesGroupFilter::getMolecularProfileCaseIdentifiers));

    List<AlterationEnrichment> alterationEnrichments =
        alterationEnrichmentService.getAlterationEnrichments(
            groupCaseIdentifierSet, enrichmentType, alterationEventTypes);

    return new ResponseEntity<>(alterationEnrichments, HttpStatus.OK);
  }
}
