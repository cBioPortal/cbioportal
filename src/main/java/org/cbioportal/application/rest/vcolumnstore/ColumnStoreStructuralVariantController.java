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
import org.cbioportal.application.rest.mapper.StructuralVariantMapper;
import org.cbioportal.application.rest.response.StructuralVariantDTO;
import org.cbioportal.domain.structural_variant.usecase.FetchStructuralVariantsUseCase;
import org.cbioportal.legacy.web.config.InternalApiTags;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.STRUCTURAL_VARIANTS, description = " ")
public class ColumnStoreStructuralVariantController {

  private final FetchStructuralVariantsUseCase fetchStructuralVariantsUseCase;

  public ColumnStoreStructuralVariantController(
      FetchStructuralVariantsUseCase fetchStructuralVariantsUseCase) {
    this.fetchStructuralVariantsUseCase = fetchStructuralVariantsUseCase;
  }

  // Not subject to InvolvedCancerStudyExtractorInterceptor (column-store controllers do their own
  // authorization), so the study check is done by the permission evaluator on the request body.
  @PreAuthorize(
      "hasPermission(#structuralVariantFilter, 'StructuralVariantFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/structural-variant/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description =
          "Fetch structural variants for entrezGeneIds and molecularProfileIds or sampleMolecularIdentifiers")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array = @ArraySchema(schema = @Schema(implementation = StructuralVariantDTO.class))))
  public ResponseEntity<List<StructuralVariantDTO>> fetchStructuralVariants(
      @Parameter(
              required = true,
              description =
                  "List of entrezGeneIds, structural variant queries and molecularProfileIds or sampleMolecularIdentifiers")
          @Valid
          @RequestBody
          StructuralVariantFilter structuralVariantFilter) {

    return new ResponseEntity<>(
        StructuralVariantMapper.INSTANCE.toDtos(
            fetchStructuralVariantsUseCase.execute(structuralVariantFilter)),
        HttpStatus.OK);
  }
}
