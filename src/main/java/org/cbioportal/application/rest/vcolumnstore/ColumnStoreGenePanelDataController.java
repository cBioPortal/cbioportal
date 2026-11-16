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
import org.cbioportal.application.rest.mapper.GenePanelDataMapper;
import org.cbioportal.application.rest.response.GenePanelDataDTO;
import org.cbioportal.domain.gene_panel_data.usecase.FetchGenePanelDataUseCase;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.GenePanelDataMultipleStudyFilter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.GENE_PANEL_DATA, description = " ")
public class ColumnStoreGenePanelDataController {

  private final FetchGenePanelDataUseCase fetchGenePanelDataUseCase;

  public ColumnStoreGenePanelDataController(FetchGenePanelDataUseCase fetchGenePanelDataUseCase) {
    this.fetchGenePanelDataUseCase = fetchGenePanelDataUseCase;
  }

  // Not subject to InvolvedCancerStudyExtractorInterceptor (column-store controllers do their own
  // authorization), so the study check is done by the permission evaluator on the request body.
  @PreAuthorize(
      "hasPermission(#genePanelDataMultipleStudyFilter, 'GenePanelDataMultipleStudyFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/gene-panel-data/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch gene panel data")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = GenePanelDataDTO.class))))
  public ResponseEntity<List<GenePanelDataDTO>> fetchGenePanelDataInMultipleMolecularProfiles(
      @Parameter(required = true, description = "Gene panel data filter object") @Valid @RequestBody
          GenePanelDataMultipleStudyFilter genePanelDataMultipleStudyFilter) {

    return ResponseEntity.ok(
        GenePanelDataMapper.INSTANCE.toDtos(
            fetchGenePanelDataUseCase.execute(genePanelDataMultipleStudyFilter)));
  }
}
