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
import org.cbioportal.application.rest.mapper.ClinicalDataMapper;
import org.cbioportal.application.rest.response.ClinicalDataDTO;
import org.cbioportal.domain.clinical_data.ClinicalData;
import org.cbioportal.domain.clinical_data.ClinicalDataType;
import org.cbioportal.domain.clinical_data.usecase.FetchClinicalDataMetaUseCase;
import org.cbioportal.domain.clinical_data.usecase.FetchClinicalDataUseCase;
import org.cbioportal.legacy.web.parameter.ClinicalDataMultiStudyFilter;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving clinical data from ClickHouse column-store database.
 *
 * <p>This controller provides endpoints to fetch clinical data with support for filtering by
 * patient/sample identifiers and attribute IDs. It leverages ClickHouse's columnar storage
 * architecture for efficient querying of large clinical datasets.
 *
 * <p>Key features:
 *
 * <ul>
 *   <li>Supports both patient and sample-level clinical data retrieval
 *   <li>Configurable projection levels (ID, SUMMARY, DETAILED) for response optimization
 *   <li>Multi-study filtering capabilities
 *   <li>Metadata queries for count operations
 * </ul>
 *
 * <p>This controller is only active when the "clickhouse" profile is enabled and requires
 * appropriate read permissions for the requested cancer studies.
 *
 * @see FetchClinicalDataUseCase
 * @see FetchClinicalDataMetaUseCase
 * @see ClinicalDataDTO
 */
@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnStoreClinicalDataController {

  private final FetchClinicalDataMetaUseCase fetchClinicalDataMetaUseCase;
  private final FetchClinicalDataUseCase fetchClinicalDataUseCase;

  /**
   * Constructs a new ColumnarStoreClinicalDataController with the required use cases.
   *
   * @param fetchClinicalDataMetaUseCase use case for retrieving clinical data metadata/counts
   * @param fetchClinicalDataUseCase use case for retrieving clinical data
   */
  public ColumnStoreClinicalDataController(
      FetchClinicalDataMetaUseCase fetchClinicalDataMetaUseCase,
      FetchClinicalDataUseCase fetchClinicalDataUseCase) {
    this.fetchClinicalDataMetaUseCase = fetchClinicalDataMetaUseCase;
    this.fetchClinicalDataUseCase = fetchClinicalDataUseCase;
  }

  /**
   * Fetches clinical data by patient IDs or sample IDs across multiple studies.
   *
   * <p>This endpoint retrieves clinical data based on the provided filter criteria and returns the
   * data in the specified projection format. For metadata queries (projection = META), only the
   * total count is returned in the response headers.
   *
   * <p>Projection types:
   *
   * <ul>
   *   <li><strong>ID</strong> - Returns only basic identifiers (internal ID, sample/patient ID,
   *       study ID, attribute ID)
   *   <li><strong>SUMMARY</strong> - Includes basic fields plus attribute values
   *   <li><strong>DETAILED</strong> - Complete data including nested clinical attribute metadata
   *   <li><strong>META</strong> - Returns only count information in response headers
   * </ul>
   *
   * @param interceptedClinicalDataMultiStudyFilter security-intercepted filter for permission
   *     validation
   * @param clinicalDataType type of clinical data to retrieve (SAMPLE or PATIENT)
   * @param clinicalDataMultiStudyFilter filter containing patient/sample identifiers and attribute
   *     IDs
   * @param projection level of detail for the response data
   * @return ResponseEntity containing list of clinical data DTOs, or empty body with count header
   *     for META projection
   */
  @Hidden
  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @PostMapping(
      value = "/clinical-data/fetch",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch clinical data by patient IDs or sample IDs (all studies)")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = ClinicalData.class))))
  public ResponseEntity<List<ClinicalDataDTO>> fetchClinicalData(
      @Parameter(
              hidden =
                  true) // prevent reference to this attribute in the swagger-ui interface. this
          // attribute is needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(required = false, value = "interceptedClinicalDataMultiStudyFilter")
          ClinicalDataMultiStudyFilter interceptedClinicalDataMultiStudyFilter,
      @Parameter(description = "Type of the clinical data") @RequestParam(defaultValue = "SAMPLE")
          ClinicalDataType clinicalDataType,
      @Parameter(
              required = true,
              description = "List of patient or sample identifiers and attribute IDs")
          @Valid
          @RequestBody(required = false)
          ClinicalDataMultiStudyFilter clinicalDataMultiStudyFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection) {

    if (projection == ProjectionType.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(
          HeaderKeyConstants.TOTAL_COUNT,
          fetchClinicalDataMetaUseCase
              .execute(interceptedClinicalDataMultiStudyFilter, clinicalDataType)
              .toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return ResponseEntity.ok(
        ClinicalDataMapper.INSTANCE.toDTOs(
            fetchClinicalDataUseCase.execute(
                interceptedClinicalDataMultiStudyFilter, clinicalDataType, projection)));
  }
}
