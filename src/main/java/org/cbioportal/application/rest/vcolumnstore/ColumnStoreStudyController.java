package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.cbioportal.application.rest.mapper.CancerStudyMetadataMapper;
import org.cbioportal.application.rest.response.CancerStudyMetadataDTO;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.sort.StudySortBy;
import org.cbioportal.shared.SortAndSearchCriteria;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@PublicApi
@Tag(name = PublicApiTags.STUDIES, description = " ")
@RestController
@RequestMapping("/api")
public class ColumnStoreStudyController {

  private static final String TOTAL_COUNT_HEADER = "X-Total-Count";

  private final GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;
  private final PermissionEvaluator permissionEvaluator;

  /**
   * Constructs a new {@link ColumnStoreStudyController}, with the specified use case and an
   * optional permission evaluator.
   *
   * @param getCancerStudyMetadataUseCase the use case responsible for retrieving cancer study
   *     metadata.
   * @param permissionEvaluator defines the permission of the cancer study.
   */
  public ColumnStoreStudyController(
      GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase,
      @Autowired(required = false) PermissionEvaluator permissionEvaluator) {
    this.getCancerStudyMetadataUseCase = getCancerStudyMetadataUseCase;
    this.permissionEvaluator = permissionEvaluator;
  }

  /**
   * Retrieves a list of cancer study metadata based on the specified criteria.
   *
   * <p>This endpoint supports filtering by keyword, controlling the level of detail in the response
   * through the projection parameter, and sorting the results by a specified property and
   * direction.
   *
   * @param keyword the search keyword that applies to the name and cancer type of the studies. This
   *     parameter is optional.
   * @param projection the level of detail of the response. Defaults to {@link
   *     ProjectionType#SUMMARY}.
   * @param sortBy the name of the property that the result list is sorted by. This parameter is
   *     optional.
   * @param pageSize the maximum number of items to return per page. When {@code null}, all results
   *     are returned. Must be between {@code 1} and {@link PagingConstants#MAX_PAGE_SIZE}.
   * @param pageNumber the 1-based page number to return. {@code null} and {@code 0} are both
   *     treated as page 1 for backward compatibility. Must be {@code >= 0}.
   * @param direction the direction of the sort. Defaults to {@link Direction#ASC}.
   * @return a {@link ResponseEntity} containing a list of {@link CancerStudyMetadataDTO} objects
   *     and an HTTP status code {@link HttpStatus#OK}.
   * @see ProjectionType
   * @see StudySortBy
   * @see Direction
   */
  @RequestMapping(
      method = RequestMethod.GET,
      value = "/studies",
      produces = MediaType.APPLICATION_JSON_VALUE)
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

    // Pagination is applied in-memory after authorization filtering (@PostFilter on the use case
    // removes studies the caller cannot read). True DB-level pagination is not yet possible here
    // because the authorized study set is only known after @PostFilter runs.
    //
    // pageNumber is 1-based. Values <= 0 and null are treated as page 1 (offset 0) for backward
    // compatibility. Long arithmetic prevents overflow when pageSize * pageNumber is large.
    // Capture total count before pagination so META headers report the full
    // authorized study count, not the number of items on the current page.
    int totalCount = studies.size();
    if (pageSize != null) {
      long offset =
          (pageNumber != null && pageNumber > 0) ? (long) pageSize * (pageNumber - 1) : 0L;
      int fromIndex = (int) Math.min(offset, studies.size());
      int toIndex = (int) Math.min(offset + pageSize, studies.size());
      studies = studies.subList(fromIndex, toIndex);
    }
    var headers = new HttpHeaders();
    if (projection == ProjectionType.META) {
      headers.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(totalCount));
      headers.add(TOTAL_COUNT_HEADER, String.valueOf(totalCount));
    }

    List<CancerStudyMetadataDTO> responseBody;
    if (projection == ProjectionType.META) {
      responseBody = List.of();
    } else {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      responseBody =
          studies.stream()
              .map(
                  study -> {
                    // Default to true if security is disabled (permissionEvaluator is null)
                    boolean hasReadPermission = true;
                    if (permissionEvaluator != null && authentication != null) {
                      hasReadPermission =
                          permissionEvaluator.hasPermission(
                              authentication,
                              study.cancerStudyIdentifier(),
                              "CancerStudyId",
                              org.cbioportal.legacy.utils.security.AccessLevel.READ);
                    }
                    return CancerStudyMetadataMapper.INSTANCE.toDto(study, hasReadPermission);
                  })
              .toList();
    }

    return ResponseEntity.ok().headers(headers).body(responseBody);
  }

  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      method = RequestMethod.GET,
      value = "/studies/{studyId}",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<CancerStudyMetadataDTO> getStudy(@PathVariable String studyId)
      throws StudyNotFoundException {
    var study = getCancerStudyMetadataUseCase.getStudy(studyId);
    var dto = CancerStudyMetadataMapper.INSTANCE.toDto(study, true);
    return ResponseEntity.ok(dto);
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
  @RequestMapping(method = RequestMethod.GET, value = "/studies/meta")
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
