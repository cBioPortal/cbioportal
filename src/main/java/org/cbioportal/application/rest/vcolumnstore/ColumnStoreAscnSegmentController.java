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
import org.cbioportal.application.rest.mapper.AscnSegmentMapper;
import org.cbioportal.application.rest.request.AscnSegmentFilter;
import org.cbioportal.application.rest.response.AscnSegmentDTO;
import org.cbioportal.domain.ascn.usecase.AscnSegmentUseCases;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.PagingConstants;
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
@Tag(name = PublicApiTags.ASCN_SEGMENTS, description = " ")
public class ColumnStoreAscnSegmentController {

  private final AscnSegmentUseCases ascnSegmentUseCases;

  public ColumnStoreAscnSegmentController(AscnSegmentUseCases ascnSegmentUseCases) {
    this.ascnSegmentUseCases = ascnSegmentUseCases;
  }

  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      method = RequestMethod.GET,
      value = "/studies/{studyId}/ascn-segments",
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get allele-specific copy number segments in a study")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = AscnSegmentDTO.class))))
  public ResponseEntity<List<AscnSegmentDTO>> getAscnSegmentsInStudy(
      @Parameter(required = true, description = "Study ID e.g. acc_tcga") @PathVariable
          String studyId,
      @Parameter(description = "Sample ID e.g. TCGA-OR-A5J2-01") @RequestParam(required = false)
          String sampleId,
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
          Integer pageNumber)
      throws StudyNotFoundException {

    if (projection == ProjectionType.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(
          HeaderKeyConstants.TOTAL_COUNT,
          ascnSegmentUseCases
              .getMetaAscnSegmentsInStudyUseCase()
              .execute(studyId, sampleId)
              .getTotalCount()
              .toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<>(
        AscnSegmentMapper.INSTANCE.toDtos(
            ascnSegmentUseCases
                .getAscnSegmentsInStudyUseCase()
                .execute(studyId, sampleId, pageSize, pageNumber)),
        HttpStatus.OK);
  }

  @PreAuthorize(
      "hasPermission(#filter?.studyIds(), 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/ascn-segments/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch allele-specific copy number segments by sample ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = AscnSegmentDTO.class))))
  public ResponseEntity<List<AscnSegmentDTO>> fetchAscnSegments(
      @Parameter(required = true, description = "List of sample identifiers") @Valid @RequestBody
          AscnSegmentFilter filter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection) {

    if (projection == ProjectionType.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(
          HeaderKeyConstants.TOTAL_COUNT,
          ascnSegmentUseCases
              .fetchMetaAscnSegmentsUseCase()
              .execute(filter.sampleIdentifiers())
              .getTotalCount()
              .toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<>(
        AscnSegmentMapper.INSTANCE.toDtos(
            ascnSegmentUseCases.fetchAscnSegmentsUseCase().execute(filter.sampleIdentifiers())),
        HttpStatus.OK);
  }
}
