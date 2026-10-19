package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.cbioportal.application.rest.mapper.StructuralVariantMapper;
import org.cbioportal.application.rest.response.StructuralVariantDTO;
import org.cbioportal.domain.fusion.FusionFilter;
import org.cbioportal.domain.fusion.usecase.FetchFusionsUseCase;
import org.cbioportal.legacy.web.config.InternalApiTags;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller serving fusions from a column-store data source.
 *
 * <p>Fusions are currently derived from the structural variant table; see {@code
 * ClickhouseFusionMapper.xml} for the criterion that selects them.
 */
@InternalApi
@Tag(name = InternalApiTags.STRUCTURAL_VARIANTS, description = " ")
@RestController
@RequestMapping("/api")
public class ColumnStoreFusionController {
  private final FetchFusionsUseCase fetchFusionsUseCase;

  public ColumnStoreFusionController(FetchFusionsUseCase fetchFusionsUseCase) {
    this.fetchFusionsUseCase = fetchFusionsUseCase;
  }

  @Hidden
  // Authorizes off the request body: InvolvedCancerStudyExtractorInterceptor skips the column-store
  // controller package, so the "involvedCancerStudies" attribute is never populated here. Safe
  // navigation keeps a missing body a denial rather than a 500.
  @PreAuthorize(
      "hasPermission(#filter?.studyIds(), 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/not-ready-yet/fusions/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = StructuralVariantDTO.class))))
  public ResponseEntity<List<StructuralVariantDTO>> fetchFusions(
      @Valid @RequestBody(required = false) FusionFilter filter) {
    return ResponseEntity.ok(
        StructuralVariantMapper.INSTANCE.toDtos(fetchFusionsUseCase.execute(filter)));
  }
}
