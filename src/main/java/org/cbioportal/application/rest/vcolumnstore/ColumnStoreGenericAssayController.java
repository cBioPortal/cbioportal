package org.cbioportal.application.rest.vcolumnstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.domain.generic_assay.usecase.GetGenericAssayMetaUseCase;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.parameter.GenericAssayMetaFilter;
import org.cbioportal.legacy.web.parameter.Projection;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * REST controller for retrieving generic assay meta data from ClickHouse column-store database.
 *
 * <p>Provides endpoints to fetch generic assay meta data by molecular profile ID or stable ID,
 * leveraging the {@link GetGenericAssayMetaUseCase} for the core logic.
 */
@Hidden
@RestController
@RequestMapping("/api/column-store")
@Validated
public class ColumnStoreGenericAssayController {

  private final GetGenericAssayMetaUseCase getGenericAssayMetaUseCase;
  private final ObjectMapper objectMapper;

  public ColumnStoreGenericAssayController(
      GetGenericAssayMetaUseCase getGenericAssayMetaUseCase, ObjectMapper objectMapper) {
    this.getGenericAssayMetaUseCase = getGenericAssayMetaUseCase;
    this.objectMapper = objectMapper;
  }

  // PreAuthorize is removed for performance reason
  @RequestMapping(
      value = "/generic-assay-meta/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch meta data for generic-assay by ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayMeta.class))))
  public ResponseEntity<StreamingResponseBody> fetchGenericAssayMeta(
      @Parameter(required = true, description = "List of Molecular Profile ID or List of Stable ID")
          @Valid
          @RequestBody
          GenericAssayMetaFilter genericAssayMetaFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection) {
    List<GenericAssayMeta> result =
        getGenericAssayMetaUseCase.execute(
            genericAssayMetaFilter.getGenericAssayStableIds(),
            genericAssayMetaFilter.getMolecularProfileIds(),
            projection.name());
    return streamJson(result);
  }

  // PreAuthorize is removed for performance reason
  @RequestMapping(
      value = "/generic-assay-meta/{molecularProfileId}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch meta data for generic-assay by ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayMeta.class))))
  public ResponseEntity<StreamingResponseBody> getGenericAssayMeta(
      @Parameter(required = true, description = "Molecular Profile ID") @PathVariable
          String molecularProfileId,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection) {
    return streamJson(
        getGenericAssayMetaUseCase.execute(
            null, Arrays.asList(molecularProfileId), projection.name()));
  }

  @RequestMapping(
      value = "/generic-assay-meta/generic-assay/{genericAssayStableId}",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch meta data for generic-assay by ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = GenericAssayMeta.class))))
  public ResponseEntity<StreamingResponseBody> getGenericAssayMetaByStableId(
      @Parameter(required = false, description = "Generic Assay stable ID") @PathVariable
          String genericAssayStableId,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection) {
    return streamJson(
        getGenericAssayMetaUseCase.execute(
            Arrays.asList(genericAssayStableId), null, projection.name()));
  }

  private ResponseEntity<StreamingResponseBody> streamJson(List<GenericAssayMeta> data) {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(outputStream -> objectMapper.writeValue(outputStream, data));
  }
}
