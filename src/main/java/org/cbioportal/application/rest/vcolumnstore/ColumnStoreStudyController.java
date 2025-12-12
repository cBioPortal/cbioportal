package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.cbioportal.application.rest.mapper.CancerStudyMetadataMapper;
import org.cbioportal.application.rest.response.CancerStudyMetadataDTO;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing and retrieving cancer study metadata from a column-store data
 * source.
 *
 * <p>This controller provides an endpoint to fetch cancer study metadata with support for
 * filtering, sorting, and controlling the level of detail in the response. It is designed to work
 * with a column-store database, which is optimized for querying large datasets efficiently.
 *
 * @see GetCancerStudyMetadataUseCase
 * @see CancerStudyMetadataDTO
 * @see ProjectionType
 * @see StudySortBy
 * @see Direction
 */
@RestController
@RequestMapping("/api/column-store")
@Profile("clickhouse")
public class ColumnStoreStudyController {

  private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

  private final GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;

  /**
   * Constructs a new {@link ColumnStoreStudyController} with the specified use case.
   *
   * @param getCancerStudyMetadataUseCase the use case responsible for retrieving cancer study
   *     metadata.
   */
  public ColumnStoreStudyController(GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase) {
    this.getCancerStudyMetadataUseCase = getCancerStudyMetadataUseCase;
  }

  /**
   * Retrieves a list of cancer study metadata based on the specified criteria.
   *
   * <p>This endpoint supports filtering by keyword, controlling the level of detail in the response
   * through the projection parameter, and sorting the results by a specified property and
   * direction.
   *
   * <p><b>Note:</b> This endpoint is marked as {@link Hidden} and will not be exposed in the API
   * documentation.
   *
   * @param keyword the search keyword that applies to the name and cancer type of the studies. This
   *     parameter is optional.
   * @param projection the level of detail of the response. Defaults to {@link
   *     ProjectionType#SUMMARY}.
   * @param sortBy the name of the property that the result list is sorted by. This parameter is
   *     optional.
   * @param direction the direction of the sort. Defaults to {@link Direction#ASC}.
   * @return a {@link ResponseEntity} containing a list of {@link CancerStudyMetadataDTO} objects
   *     and an HTTP status code {@link HttpStatus#OK}.
   * @see ProjectionType
   * @see StudySortBy
   * @see Direction
   */
  @Hidden
  @GetMapping(value = "/studies", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<CancerStudyMetadataDTO>> getAllStudies(
      @Parameter(description = "Search keyword that applies to name and cancer type of the studies")
          @RequestParam(required = false)
          String keyword,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          ProjectionType projection,
      @Parameter(description = "Name of the property that the result list is sorted by")
          @RequestParam(required = false)
          StudySortBy sortBy,
      @Parameter(description = "Page size of the result list")
          @Max(PagingConstants.MAX_PAGE_SIZE)
          @Min(PagingConstants.MIN_PAGE_SIZE)
          @RequestParam(required = false)
          Integer pageSize,
      @Parameter(description = "Page number of the result list")
          @Min(PagingConstants.MIN_PAGE_NUMBER)
          @RequestParam(required = false)
          Integer pageNumber,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction) {

    var sortAndSearchCriteria =
        new SortAndSearchCriteria(
            keyword,
            (sortBy != null ? sortBy.getOriginalValue() : ""),
            direction.toString(),
            pageSize,
            pageNumber);

    var studies = getCancerStudyMetadataUseCase.execute(projection, sortAndSearchCriteria);

    // Pagination should be handled at the DB layer, but currently our query is not
    // setup to handle this with authorization
    if (pageSize != null) {
      studies = studies.stream().limit(pageSize).toList();
    }
    var headers = new HttpHeaders();
    if (projection == ProjectionType.META) {
      headers.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(studies.size()));
      headers.add(TOTAL_COUNT_HEADER, String.valueOf(studies.size()));
    }

    List<CancerStudyMetadataDTO> responseBody =
        (projection == ProjectionType.META)
            ? List.of()
            : CancerStudyMetadataMapper.INSTANCE.toDtos(studies);

    return ResponseEntity.ok().headers(headers).body(responseBody);
  }

  /**
   * Retrieves metadata information for cancer studies, specifically the total number of studies
   * matching the given filter and sort criteria.
   *
   * <p>This endpoint is intended for metadata retrieval only and does not return a response body.
   * The total count of matching studies is provided in the {@code X-Total-Count} HTTP response
   * header.
   *
   * <p>Clients can use this endpoint to determine the number of studies that would be returned by
   * {@link #getAllStudies(String, ProjectionType, StudySortBy, Direction)} without fetching the
   * full list.
   *
   * @param keyword optional search keyword that filters studies by name or cancer type
   * @param sortBy optional property name used to sort the results
   * @param direction sort direction; defaults to {@link Direction#ASC}
   * @return an empty response body with an {@code X-Total-Count} header indicating the total number
   *     of matching studies
   * @see ProjectionType#META
   * @see Direction
   * @see StudySortBy
   */
  @GetMapping("/studies/meta")
  public ResponseEntity<Void> getAllStudiesMeta(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) StudySortBy sortBy,
      @RequestParam(defaultValue = "ASC") Direction direction) {

    var sortAndSearchCriteria =
        new SortAndSearchCriteria(
            keyword,
            (sortBy != null ? sortBy.getOriginalValue() : ""),
            direction.toString(),
            null,
            null);

    var studies = getCancerStudyMetadataUseCase.execute(ProjectionType.META, sortAndSearchCriteria);

    var headers = new HttpHeaders();
    headers.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(studies.size()));
    headers.add(TOTAL_COUNT_HEADER, String.valueOf(studies.size()));

    return ResponseEntity.ok().headers(headers).build();
  }
}
