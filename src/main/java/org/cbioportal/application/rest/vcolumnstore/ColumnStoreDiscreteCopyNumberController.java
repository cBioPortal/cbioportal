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
import org.cbioportal.application.rest.mapper.DiscreteCopyNumberDataMapper;
import org.cbioportal.application.rest.response.DiscreteCopyNumberDataDTO;
import org.cbioportal.domain.discrete_copy_number.usecase.FetchDiscreteCopyNumbersUseCase;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.DiscreteCopyNumberEventType;
import org.cbioportal.legacy.web.parameter.DiscreteCopyNumberFilter;
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
@Tag(name = PublicApiTags.DISCRETE_COPY_NUMBER_ALTERATIONS, description = " ")
public class ColumnStoreDiscreteCopyNumberController {

  private final FetchDiscreteCopyNumbersUseCase fetchDiscreteCopyNumbersUseCase;

  public ColumnStoreDiscreteCopyNumberController(
      FetchDiscreteCopyNumbersUseCase fetchDiscreteCopyNumbersUseCase) {
    this.fetchDiscreteCopyNumbersUseCase = fetchDiscreteCopyNumbersUseCase;
  }

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/discrete-copy-number/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "Fetch discrete copy number alterations in a molecular profile by sample ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = DiscreteCopyNumberDataDTO.class))))
  public ResponseEntity<List<DiscreteCopyNumberDataDTO>> fetchDiscreteCopyNumbersInMolecularProfile(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_gistic")
          @PathVariable
          String molecularProfileId,
      @Parameter(description = "Type of the copy number event")
          @RequestParam(defaultValue = "HOMDEL_AND_AMP")
          DiscreteCopyNumberEventType discreteCopyNumberEventType,
      @Parameter(
              required = true,
              description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
          @Valid
          @RequestBody
          DiscreteCopyNumberFilter discreteCopyNumberFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection)
      throws MolecularProfileNotFoundException {

    List<Integer> alterationTypes = discreteCopyNumberEventType.getAlterationTypes();
    if (projection == ProjectionType.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      int totalCount =
          fetchDiscreteCopyNumbersUseCase.count(
              molecularProfileId,
              discreteCopyNumberFilter.getSampleListId(),
              discreteCopyNumberFilter.getSampleIds(),
              discreteCopyNumberFilter.getEntrezGeneIds(),
              alterationTypes);
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(totalCount));
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<>(
        DiscreteCopyNumberDataMapper.INSTANCE.toDtos(
            fetchDiscreteCopyNumbersUseCase.execute(
                molecularProfileId,
                discreteCopyNumberFilter.getSampleListId(),
                discreteCopyNumberFilter.getSampleIds(),
                discreteCopyNumberFilter.getEntrezGeneIds(),
                alterationTypes,
                projection == ProjectionType.DETAILED)),
        HttpStatus.OK);
  }
}
