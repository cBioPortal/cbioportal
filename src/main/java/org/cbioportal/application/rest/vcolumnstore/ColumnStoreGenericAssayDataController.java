package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.cbioportal.application.rest.mapper.GenericAssayDataMapper;
import org.cbioportal.application.rest.response.GenericAssayDataDTO;
import org.cbioportal.domain.generic_assay.usecase.FetchGenericAssayDataUseCase;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.GenericAssayFilter;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.GENERIC_ASSAY_DATA, description = " ")
public class ColumnStoreGenericAssayDataController {

  private final FetchGenericAssayDataUseCase fetchGenericAssayDataUseCase;

  public ColumnStoreGenericAssayDataController(
      FetchGenericAssayDataUseCase fetchGenericAssayDataUseCase) {
    this.fetchGenericAssayDataUseCase = fetchGenericAssayDataUseCase;
  }

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/generic_assay_data/{molecularProfileId}/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "fetch generic_assay_data in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = GenericAssayDataDTO.class))))
  public ResponseEntity<List<GenericAssayDataDTO>> fetchGenericAssayDataInMolecularProfile(
      @Parameter(required = true, description = "Molecular Profile ID") @PathVariable
          String molecularProfileId,
      @Parameter(
              required = true,
              description = "List of Sample IDs/Sample List ID and Generic Assay IDs")
          @Valid
          @RequestBody
          GenericAssayFilter genericAssayDataFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection)
      throws MolecularProfileNotFoundException {

    List<GenericAssayDataDTO> result =
        GenericAssayDataMapper.INSTANCE.toDtos(
            fetchGenericAssayDataUseCase.execute(
                molecularProfileId,
                genericAssayDataFilter.getSampleListId(),
                genericAssayDataFilter.getSampleIds(),
                genericAssayDataFilter.getGenericAssayStableIds()));

    if (projection == ProjectionType.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}
