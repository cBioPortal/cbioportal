package org.cbioportal.application.rest.vcolumnstore;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.domain.generic_assay.usecase.GetGenericAssayMetaUseCase;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
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
@PublicApi
@Tag(name = PublicApiTags.GENERIC_ASSAYS, description = " ")
@RestController
@RequestMapping("/api")
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
    return streamMeta(
        genericAssayMetaFilter.getGenericAssayStableIds(),
        genericAssayMetaFilter.getMolecularProfileIds(),
        projection.name());
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
    return streamMeta(null, Arrays.asList(molecularProfileId), projection.name());
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
    return streamMeta(Arrays.asList(genericAssayStableId), null, projection.name());
  }

  /**
   * Streams the matching {@link GenericAssayMeta} as a JSON array, writing each element to the
   * response as it is read from the database. The result set is never collected into a list, so
   * peak heap stays bounded regardless of how many entities the request resolves to.
   */
  private ResponseEntity<StreamingResponseBody> streamMeta(
      List<String> stableIds, List<String> molecularProfileIds, String projection) {
    StreamingResponseBody body =
        outputStream -> {
          try (JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream)) {
            generator.writeStartArray();
            getGenericAssayMetaUseCase.execute(
                stableIds,
                molecularProfileIds,
                projection,
                meta -> {
                  try {
                    generator.writeObject(meta);
                  } catch (IOException e) {
                    // ResultHandler/Consumer cannot throw checked exceptions; unwrapped below
                    throw new UncheckedIOException(e);
                  }
                });
            generator.writeEndArray();
          } catch (UncheckedIOException e) {
            throw e.getCause();
          }
        };
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
  }
}
