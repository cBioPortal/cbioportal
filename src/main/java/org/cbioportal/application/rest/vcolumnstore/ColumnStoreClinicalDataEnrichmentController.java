package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.cbioportal.application.rest.mapper.ClinicalDataEnrichmentMapper;
import org.cbioportal.application.rest.response.ClinicalDataEnrichmentDTO;
import org.cbioportal.domain.clinical_data_enrichment.usecase.FetchClinicalDataEnrichmentsUseCase;
import org.cbioportal.legacy.web.parameter.GroupFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for clinical data enrichment analysis endpoints using ClickHouse column store.
 *
 * <p>This controller provides API endpoints for performing statistical enrichment analysis on
 * clinical data across multiple sample groups. It uses the ClickHouse column store backend for
 * efficient data retrieval and supports both numerical (Kruskal-Wallis) and categorical
 * (Chi-squared) statistical tests.
 *
 * <p>Only available when the "clickhouse" profile is active.
 */
@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnStoreClinicalDataEnrichmentController {

  private final FetchClinicalDataEnrichmentsUseCase fetchClinicalDataEnrichmentsUseCase;

  /**
   * Constructor for dependency injection.
   *
   * @param fetchClinicalDataEnrichmentsUseCase use case for executing enrichment analysis
   */
  public ColumnStoreClinicalDataEnrichmentController(
      FetchClinicalDataEnrichmentsUseCase fetchClinicalDataEnrichmentsUseCase) {
    this.fetchClinicalDataEnrichmentsUseCase = fetchClinicalDataEnrichmentsUseCase;
  }

  /**
   * Fetches clinical data enrichments for multiple sample groups.
   *
   * <p>This endpoint performs statistical enrichment analysis on clinical attributes across the
   * provided sample groups. For each clinical attribute, it determines if there is a statistically
   * significant difference between the groups using appropriate statistical tests:
   *
   * <ul>
   *   <li>Numerical attributes: Kruskal-Wallis test (or Wilcoxon for 2 groups)
   *   <li>Categorical attributes: Chi-squared test
   * </ul>
   *
   * <p>Access control is enforced via {@code @PreAuthorize} which validates user permissions
   * against the cancer studies referenced in the group filter. Study IDs are automatically
   * extracted by {@link org.cbioportal.application.security.CancerStudyPermissionEvaluator}.
   *
   * @param groupFilter filter containing multiple groups of sample identifiers
   * @return list of clinical data enrichments with p-values and test statistics, sorted by
   *     significance
   */
  @Hidden
  @PreAuthorize(
      "hasPermission(#groupFilter, 'GroupFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
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
                  @ArraySchema(schema = @Schema(implementation = ClinicalDataEnrichmentDTO.class))))
  public ResponseEntity<List<ClinicalDataEnrichmentDTO>> fetchClinicalEnrichments(
      @Parameter(required = true, description = "Group filter with sample identifiers")
          @Valid
          @RequestBody(required = false)
          GroupFilter groupFilter) {

    return new ResponseEntity<>(
        ClinicalDataEnrichmentMapper.INSTANCE.toDTOs(
            fetchClinicalDataEnrichmentsUseCase.execute(groupFilter)),
        HttpStatus.OK);
  }
}
