package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.cbioportal.application.rest.mapper.MutationMapper;
import org.cbioportal.application.rest.response.MutationDTO;
import org.cbioportal.domain.mutation.usecase.MutationUseCases;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.*;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;
import org.cbioportal.shared.MutationQueryOptions;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing and retrieving Mutation data from a column-store data source.
 *
 * <p>This controller provides an endpoint to fetch Mutation data with support for filtering,
 * sorting, and controlling the level of detail in the response. It is designed to work with a
 * column-store database, which is optimized for querying large datasets efficiently.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Configurable projection levels (ID, SUMMARY, DETAILED) for response optimization
 *   <li>Multi-study filtering capabilities
 *   <li>Metadata queries for count operations
 * </ul>
 *
 * <p>This controller is only active when the "clickhouse" profile is enabled and requires
 * appropriate read permissions for the requested cancer studies.
 *
 * @see MutationDTO
 */
@PublicApi
@Tag(name = PublicApiTags.MUTATIONS, description = " ")
@RestController
@RequestMapping("/api")
@Validated
public class ColumnStoreMutationController {
  private final MutationUseCases mutationUseCases;

  /**
   * Constructs a new {@link ColumnStoreMutationController} with the specified use case.
   *
   * @param mutationUseCases the use case responsible for retrieving Mutation metadata or Mutation
   */
  public ColumnStoreMutationController(MutationUseCases mutationUseCases) {
    this.mutationUseCases = mutationUseCases;
  }

  /**
   * Fetch Mutation by exactly one sampleUniqueIdentifier or molecularProfileId must or
   * entrezGeneIds
   *
   * @param interceptedMutationMultipleStudyFilter security-intercepted filter for permission
   *     validation
   * @param projection level of detail for the response data
   * @return ResponseEntity containing list of Mutation data DTOs, or empty body with count header
   *     for META projection
   */
  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/mutations/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch mutations in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = MutationDTO.class))))
  public ResponseEntity<List<MutationDTO>> fetchMutationsInMolecularProfile(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
          @PathVariable
          String molecularProfileId,
      @Parameter(
              required = true,
              description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
          @Valid
          @RequestBody
          MutationFilter mutationFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection,
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
          MutationSortBy sortBy,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction)
      throws MolecularProfileNotFoundException {

    var useCase = mutationUseCases.fetchMutationsInSingleProfileUseCase();
    if (projection == ProjectionType.META) {
      MutationMeta mutationMeta =
          useCase.executeMeta(
              molecularProfileId,
              mutationFilter.getSampleListId(),
              mutationFilter.getSampleIds(),
              mutationFilter.getEntrezGeneIds());
      return new ResponseEntity<>(metaHeaders(mutationMeta), HttpStatus.OK);
    }
    MutationQueryOptions mutationQueryOptions =
        new MutationQueryOptions(
            projection,
            pageSize,
            pageNumber,
            sortBy == null ? null : sortBy.getOriginalValue(),
            direction);
    return ResponseEntity.ok(
        MutationMapper.INSTANCE.toDTOs(
            useCase.execute(
                molecularProfileId,
                mutationFilter.getSampleListId(),
                mutationFilter.getSampleIds(),
                mutationFilter.getEntrezGeneIds(),
                mutationQueryOptions)));
  }

  // Not subject to InvolvedCancerStudyExtractorInterceptor (column-store controllers do their own
  // authorization), so the study check is done by the permission evaluator on the request body.
  @PreAuthorize(
      "hasPermission(#mutationMultipleStudyFilter, 'MutationMultipleStudyFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/mutations/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch mutations in multiple molecular profiles by sample IDs")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = MutationDTO.class))))
  public ResponseEntity<List<MutationDTO>> fetchMutationsInMultipleMolecularProfiles(
      @Parameter(
              required = true,
              description =
                  "List of Molecular Profile IDs or List of Molecular Profile ID / Sample ID pairs,"
                      + " and List of Entrez Gene IDs")
          @Valid
          @RequestBody
          MutationMultipleStudyFilter mutationMultipleStudyFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection,
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
          MutationSortBy sortBy,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction) {

    if (projection == ProjectionType.META) {
      MutationMeta mutationMeta =
          mutationUseCases.fetchMetaMutationsUseCase().execute(mutationMultipleStudyFilter);
      return new ResponseEntity<>(metaHeaders(mutationMeta), HttpStatus.OK);
    }
    MutationQueryOptions mutationQueryOptions =
        new MutationQueryOptions(
            projection,
            pageSize,
            pageNumber,
            sortBy == null ? null : sortBy.getOriginalValue(),
            direction);
    return ResponseEntity.ok(
        MutationMapper.INSTANCE.toDTOs(
            mutationUseCases
                .fetchAllMutationsInProfileUseCase()
                .execute(mutationMultipleStudyFilter, mutationQueryOptions)));
  }

  private HttpHeaders metaHeaders(MutationMeta mutationMeta) {
    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
    responseHeaders.add(HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
    return responseHeaders;
  }
}
